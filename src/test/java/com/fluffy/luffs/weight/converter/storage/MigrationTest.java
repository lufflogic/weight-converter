package com.fluffy.luffs.weight.converter.storage;

import com.fluffy.luffs.weight.converter.controllers.model.PastWeight;
import com.fluffy.luffs.weight.converter.controllers.model.WeightConverterException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tmatesoft.sqljet.core.table.SqlJetDb;
import static org.junit.jupiter.api.Assertions.*;

public class MigrationTest {
    @TempDir Path temporary;

    private static final LocalDateTime DATE = LocalDateTime.of(2021, 7, 15, 23, 30);

    @Test
    public void importsLegacyWeightsAndDatesWithoutChangingSource() throws Exception {
        File source = legacyDatabase("10st 2lb 8oz", "11st 0lbs 0oz");
        byte[] original = Files.readAllBytes(source.toPath());
        try (Connection connection = database()) {
            Database.importLegacyWeights(connection, () -> Migration.getLegacyWeights(source));
            assertEquals(2, count(connection, "WEIGHT"));
            assertEquals(1, count(connection, "MIGRATION"));
            try (Statement statement = connection.createStatement();
                    ResultSet rows = statement.executeQuery("SELECT * FROM WEIGHTCONVERTER.WEIGHT ORDER BY ID")) {
                assertTrue(rows.next());
                assertEquals("10st 2lb 8oz", rows.getString("WEIGHT"));
                assertEquals(142.5 * 0.45359237, rows.getDouble("KILOS"), 0.000001);
                assertEquals(DATE, rows.getTimestamp("DT_TM").toLocalDateTime());
            }
        }
        assertArrayEquals(original, Files.readAllBytes(source.toPath()));
    }

    @Test
    public void completedMigrationDoesNotImportAgainOrReadSource() throws Exception {
        File source = legacyDatabase("10st 0lb 0oz");
        try (Connection connection = database()) {
            Database.importLegacyWeights(connection, () -> Migration.getLegacyWeights(source));
            Database.importLegacyWeights(connection, () -> { throw new AssertionError("Source read twice"); });
            assertEquals(1, count(connection, "WEIGHT"));
        }
    }

    @Test
    public void failedInsertRollsBackAllRowsAndCanBeRetried() throws Exception {
        PastWeight valid = new PastWeight(1, "10st 0lb 0oz", 63.5, DATE);
        PastWeight invalid = new PastWeight(2, null, 63.5, DATE);
        try (Connection connection = database()) {
            assertThrows(SQLException.class, () ->
                    Database.importLegacyWeights(connection, () -> Arrays.asList(valid, invalid)));
            assertEquals(0, count(connection, "WEIGHT"));
            assertEquals(0, count(connection, "MIGRATION"));
            Database.importLegacyWeights(connection, () -> List.of(valid));
            assertEquals(1, count(connection, "WEIGHT"));
            assertEquals(1, count(connection, "MIGRATION"));
        }
    }

    @Test
    public void malformedLegacyWeightDoesNotMarkMigrationComplete() throws Exception {
        File source = legacyDatabase("10st 0lb 0oz", "not a weight");
        try (Connection connection = database()) {
            assertThrows(WeightConverterException.class, () ->
                    Database.importLegacyWeights(connection, () -> Migration.getLegacyWeights(source)));
            assertEquals(0, count(connection, "WEIGHT"));
            assertEquals(0, count(connection, "MIGRATION"));
        }
    }

    @Test
    public void invalidLegacyDateDoesNotMarkMigrationComplete() throws Exception {
        File source = legacyDatabase("10st 0lb 0oz");
        SqlJetDb db = SqlJetDb.open(source, true);
        try {
            db.runWriteTransaction(connection -> {
                org.tmatesoft.sqljet.core.table.ISqlJetCursor cursor = connection.getTable("weight").open();
                try {
                    cursor.update(1, "10st 0lb 0oz", Long.MAX_VALUE);
                } finally {
                    cursor.close();
                }
                return null;
            });
        } finally {
            db.close();
        }
        try (Connection connection = database()) {
            assertThrows(WeightConverterException.class, () ->
                    Database.importLegacyWeights(connection, () -> Migration.getLegacyWeights(source)));
            assertEquals(0, count(connection, "WEIGHT"));
            assertEquals(0, count(connection, "MIGRATION"));
        }
    }

    @Test
    public void unreadableSourceDoesNotMarkMigrationComplete() throws Exception {
        File source = Files.createTempFile(temporary, "corrupt-", ".sqlite").toFile();
        Files.writeString(source.toPath(), "not a SQLite database");
        try (Connection connection = database()) {
            assertThrows(WeightConverterException.class, () ->
                    Database.importLegacyWeights(connection, () -> Migration.getLegacyWeights(source)));
            assertEquals(0, count(connection, "MIGRATION"));
            assertEquals(0, count(connection, "WEIGHT"));
        }
    }

    @Test
    public void resumesOldPartialImportWithoutDuplicatingExistingRows() throws Exception {
        File source = legacyDatabase("10st 0lb 0oz", "11st 0lb 0oz");
        try (Connection connection = database()) {
            try (java.sql.PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO WEIGHTCONVERTER.WEIGHT (WEIGHT, KILOS, DT_TM) VALUES (?, 0, ?)")) {
                insert.setString(1, "10st 0lb 0oz");
                insert.setTimestamp(2, java.sql.Timestamp.valueOf(DATE));
                insert.executeUpdate();
            }
            Database.importLegacyWeights(connection, () -> Migration.getLegacyWeights(source));
            assertEquals(2, count(connection, "WEIGHT"));
            try (Statement statement = connection.createStatement();
                    ResultSet result = statement.executeQuery("SELECT MIN(KILOS) FROM WEIGHTCONVERTER.WEIGHT")) {
                result.next();
                assertEquals(140 * 0.45359237, result.getDouble(1), 0.000001);
            }
        }
    }

    @Test
    public void preservesRepeatedEntriesInLegacyDatabase() throws Exception {
        File source = legacyDatabase("10st 0lb 0oz", "10st 0lb 0oz");
        try (Connection connection = database()) {
            Database.importLegacyWeights(connection, () -> Migration.getLegacyWeights(source));
            assertEquals(2, count(connection, "WEIGHT"));
            Database.importLegacyWeights(connection, () -> Migration.getLegacyWeights(source));
            assertEquals(2, count(connection, "WEIGHT"));
        }
    }

    @Test
    public void emptyLegacyDatabaseIsCompleted() throws Exception {
        File source = legacyDatabase();
        try (Connection connection = database()) {
            Database.importLegacyWeights(connection, () -> Migration.getLegacyWeights(source));
            assertEquals(0, count(connection, "WEIGHT"));
            assertEquals(1, count(connection, "MIGRATION"));
        }
    }

    private Connection database() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID(), "SA", "");
        Database.createSchema(connection);
        return connection;
    }

    private File legacyDatabase(String... weights) throws Exception {
        File file = temporary.resolve("weightconverter.sqlite").toFile();
        SqlJetDb db = SqlJetDb.open(file, true);
        try {
            db.runWriteTransaction(connection -> {
                connection.getOptions().setUserVersion(1);
                connection.createTable("CREATE TABLE weight (id INTEGER PRIMARY KEY, weight TEXT NOT NULL, dt_tm INTEGER NOT NULL)");
                for (int index = 0; index < weights.length; index++) {
                    connection.getTable("weight").insert(index + 1, weights[index], DATE.toEpochSecond(ZoneOffset.UTC));
                }
                return null;
            });
        } finally {
            db.close();
        }
        return file;
    }

    private int count(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM WEIGHTCONVERTER." + table)) {
            result.next();
            return result.getInt(1);
        }
    }
}
