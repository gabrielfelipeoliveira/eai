package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class V3__expand_lead_status_checks extends BaseJavaMigration {

    private static final String STATUS_VALUES = "'NEW', 'AVAILABLE', 'ASSIGNED', 'FIRST_CONTACT', "
            + "'IN_NEGOTIATION', 'VISIT_SCHEDULED', 'SIMULATING', 'PROPOSAL_APPROVED', "
            + "'PROPOSAL_SENT', 'SOLD', 'LOST', 'DUPLICATED'";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        String product = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);

        replaceCheck(connection, product, "leads", "status", "chk_leads_status",
                "status IN (" + STATUS_VALUES + ")");
        replaceCheck(connection, product, "lead_history", "previous_status", "chk_lead_history_previous_status",
                "previous_status IS NULL OR previous_status IN (" + STATUS_VALUES + ")");
        replaceCheck(connection, product, "lead_history", "new_status", "chk_lead_history_new_status",
                "new_status IN (" + STATUS_VALUES + ")");
    }

    private void replaceCheck(
            Connection connection,
            String product,
            String tableName,
            String columnName,
            String constraintName,
            String checkExpression
    ) throws SQLException {
        for (String existingConstraint : findCheckConstraints(connection, product, tableName, columnName)) {
            execute(connection, "ALTER TABLE " + tableName + " DROP CONSTRAINT " + constraintIdentifier(product, existingConstraint));
        }
        execute(connection, "ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName + " CHECK (" + checkExpression + ")");
    }

    private List<String> findCheckConstraints(Connection connection, String product, String tableName, String columnName) throws SQLException {
        if (product.contains("postgresql")) {
            return findPostgresCheckConstraints(connection, tableName, columnName);
        }
        if (product.contains("h2")) {
            return findH2CheckConstraints(connection, tableName, columnName);
        }
        throw new SQLException("Unsupported database for lead status check migration: " + product);
    }

    private List<String> findPostgresCheckConstraints(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = """
                SELECT con.conname
                FROM pg_constraint con
                JOIN pg_class rel ON rel.oid = con.conrelid
                JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
                JOIN pg_attribute att ON att.attrelid = rel.oid AND att.attnum = ANY(con.conkey)
                WHERE con.contype = 'c'
                  AND nsp.nspname = current_schema()
                  AND rel.relname = ?
                  AND att.attname = ?
                """;
        return queryConstraintNames(connection, sql, tableName, columnName);
    }

    private List<String> findH2CheckConstraints(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = """
                SELECT tc.CONSTRAINT_NAME
                FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
                JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu
                  ON ccu.CONSTRAINT_SCHEMA = tc.CONSTRAINT_SCHEMA
                 AND ccu.CONSTRAINT_NAME = tc.CONSTRAINT_NAME
                WHERE tc.CONSTRAINT_TYPE = 'CHECK'
                  AND LOWER(tc.TABLE_NAME) = ?
                  AND LOWER(ccu.COLUMN_NAME) = ?
                """;
        return queryConstraintNames(connection, sql, tableName, columnName);
    }

    private List<String> queryConstraintNames(Connection connection, String sql, String tableName, String columnName) throws SQLException {
        List<String> constraints = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName.toLowerCase(Locale.ROOT));
            statement.setString(2, columnName.toLowerCase(Locale.ROOT));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    constraints.add(resultSet.getString(1));
                }
            }
        }
        return constraints;
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private String constraintIdentifier(String product, String constraintName) {
        if (product.contains("h2")) {
            return "\"" + constraintName.replace("\"", "\"\"") + "\"";
        }
        return constraintName;
    }
}
