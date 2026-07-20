package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class V6__lead_notes_editable_global_tags extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        execute(connection, """
                CREATE TABLE lead_tag_definitions (
                    id UUID PRIMARY KEY,
                    name VARCHAR(80) NOT NULL,
                    type VARCHAR(40) NOT NULL,
                    active BOOLEAN NOT NULL,
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    CONSTRAINT uk_lead_tag_definitions_name UNIQUE (name)
                )
                """);
        execute(connection, "CREATE INDEX idx_lead_tag_definitions_active ON lead_tag_definitions(active)");
        execute(connection, "CREATE INDEX idx_lead_tag_definitions_type ON lead_tag_definitions(type)");

        execute(connection, "ALTER TABLE lead_notes ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE");
        execute(connection, "UPDATE lead_notes SET updated_at = created_at WHERE updated_at IS NULL");
        execute(connection, "ALTER TABLE lead_notes ALTER COLUMN updated_at SET NOT NULL");

        execute(connection, "ALTER TABLE lead_tags ADD COLUMN tag_id UUID");
        execute(connection, "ALTER TABLE lead_tags ADD COLUMN type VARCHAR(40)");
        migrateExistingTags(connection);
        execute(connection, "ALTER TABLE lead_tags ALTER COLUMN tag_id SET NOT NULL");
        execute(connection, "ALTER TABLE lead_tags ALTER COLUMN type SET NOT NULL");
        execute(connection, "ALTER TABLE lead_tags ADD CONSTRAINT fk_lead_tags_tag_definition FOREIGN KEY (tag_id) REFERENCES lead_tag_definitions(id)");
        execute(connection, "ALTER TABLE lead_tags ADD CONSTRAINT uk_lead_tags_lead_tag UNIQUE (lead_id, tag_id)");
        execute(connection, "ALTER TABLE lead_tags ADD CONSTRAINT uk_lead_tags_lead_type UNIQUE (lead_id, type)");
        execute(connection, "CREATE INDEX idx_lead_tags_tag_id ON lead_tags(tag_id)");
    }

    private void migrateExistingTags(Connection connection) throws Exception {
        Map<String, UUID> tagIdsByName = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT DISTINCT name FROM lead_tags ORDER BY name")) {
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                tagIdsByName.put(name, UUID.randomUUID());
            }
        }
        Instant now = Instant.now();
        for (Map.Entry<String, UUID> entry : tagIdsByName.entrySet()) {
            String type = defaultType(entry.getKey());
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO lead_tag_definitions (id, name, type, active, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """)) {
                statement.setObject(1, entry.getValue());
                statement.setString(2, entry.getKey());
                statement.setString(3, type);
                statement.setBoolean(4, true);
                statement.setObject(5, now);
                statement.setObject(6, now);
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("UPDATE lead_tags SET tag_id = ?, type = ? WHERE name = ?")) {
                statement.setObject(1, entry.getValue());
                statement.setString(2, type);
                statement.setString(3, entry.getKey());
                statement.executeUpdate();
            }
        }
    }

    private String defaultType(String name) {
        String normalizedName = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        return switch (normalizedName) {
            case "quente" -> "TEMPERATURE";
            case "troca" -> "NEGOTIATION";
            case "financiamento" -> "FINANCING";
            case "perdido" -> "OUTCOME";
            case "duplicado" -> "STATUS";
            default -> "GENERAL";
        };
    }

    private void execute(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
