package com.eai.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RecordContractTest {

    @DisplayName("Records de API e aplicacao expõem construtor accessors equals hashCode e toString")
    @Test
    void recordsExposeGeneratedContract() throws Exception {
        List<Class<?>> recordClasses = recordClasses("com.eai.api", "com.eai.application");

        assertThat(recordClasses).isNotEmpty();
        for (Class<?> recordClass : recordClasses) {
            Object instance = instantiate(recordClass, new HashSet<>());

            assertThat(instance).isEqualTo(instance);
            assertThat(instance.hashCode()).isEqualTo(instance.hashCode());
            assertThat(instance.toString()).contains(recordClass.getSimpleName());
            for (RecordComponent component : recordClass.getRecordComponents()) {
                assertThat(component.getAccessor().invoke(instance)).isEqualTo(sample(component.getType(), new HashSet<>()));
            }
        }
    }

    private List<Class<?>> recordClasses(String... packages) throws Exception {
        List<Class<?>> records = new ArrayList<>();
        for (String packageName : packages) {
            File directory = Path.of("target", "classes", packageName.replace('.', File.separatorChar)).toFile();
            collectRecords(directory, packageName, records);
        }
        return records;
    }

    private void collectRecords(File directory, String packageName, List<Class<?>> records) throws Exception {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                collectRecords(file, packageName + "." + file.getName(), records);
                continue;
            }
            if (!file.getName().endsWith(".class") || file.getName().contains("$")) {
                continue;
            }
            String className = packageName + "." + file.getName().substring(0, file.getName().length() - ".class".length());
            Class<?> type = Class.forName(className);
            if (type.isRecord() && canInstantiate(type, new HashSet<>())) {
                records.add(type);
            }
        }
    }

    private boolean canInstantiate(Class<?> type, Set<Class<?>> visiting) {
        try {
            instantiate(type, visiting);
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return false;
        }
    }

    private Object instantiate(Class<?> type, Set<Class<?>> visiting) throws ReflectiveOperationException {
        if (!type.isRecord() || !visiting.add(type)) {
            return null;
        }
        try {
            RecordComponent[] components = type.getRecordComponents();
            Class<?>[] parameterTypes = new Class<?>[components.length];
            Object[] values = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                parameterTypes[i] = components[i].getType();
                values[i] = sample(parameterTypes[i], visiting);
            }
            Constructor<?> constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(values);
        } finally {
            visiting.remove(type);
        }
    }

    private Object sample(Class<?> type, Set<Class<?>> visiting) throws ReflectiveOperationException {
        if (type == String.class) {
            return "valor";
        }
        if (type == UUID.class) {
            return UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        if (type == Instant.class) {
            return Instant.parse("2026-07-27T10:00:00Z");
        }
        if (type == BigDecimal.class) {
            return BigDecimal.TEN;
        }
        if (type == byte[].class) {
            return "valor".getBytes();
        }
        if (type == boolean.class || type == Boolean.class) {
            return true;
        }
        if (type == int.class || type == Integer.class) {
            return 1;
        }
        if (type == long.class || type == Long.class) {
            return 1L;
        }
        if (type == double.class || type == Double.class) {
            return 1.0;
        }
        if (type == List.class) {
            return List.of();
        }
        if (type == Set.class) {
            return Set.of();
        }
        if (type.isEnum()) {
            return type.getEnumConstants()[0];
        }
        if (type.isRecord()) {
            return instantiate(type, visiting);
        }
        return null;
    }
}
