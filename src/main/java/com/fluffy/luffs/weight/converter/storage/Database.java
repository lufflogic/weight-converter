/*
MIT License

Copyright (c) 2022  Fluffy Luffs

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.fluffy.luffs.weight.converter.storage;

import com.fluffy.luffs.weight.converter.controllers.model.PastWeight;
import com.fluffy.luffs.weight.converter.controllers.model.WeightConverterException;
import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Services;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Database
 */
@SuppressWarnings("java:S2115")
public class Database {

    private static final String JDBC_USER = "SA";
    private static final String JDBC_PWD = "";

    /**
     * Creates the database if not present
     * @throws java.io.FileNotFoundException
     */
    public void createDatabse() throws FileNotFoundException {

        String schema = "CREATE SCHEMA IF NOT EXISTS WEIGHTCONVERTER";
        String weightTable
                = """
          CREATE TABLE IF NOT EXISTS WEIGHTCONVERTER.WEIGHT
          (
           ID IDENTITY NOT NULL PRIMARY KEY,
           WEIGHT VARCHAR NOT NULL,
           KILOS NUMBER NOT NULL,
           DT_TM TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
          );      
          """;

        String index
                = "CREATE INDEX IF NOT EXISTS WEIGHTCONVERTER.DT_TM_IDX ON WEIGHTCONVERTER.WEIGHT(DT_TM);";

        try (Connection connection
                = DriverManager.getConnection(getConnectionString(), JDBC_USER, JDBC_PWD); Statement statement = connection.createStatement()) {

            statement.addBatch(schema);
            statement.addBatch(weightTable);
            statement.addBatch(index);
            statement.executeBatch();

        } catch (SQLException | FileNotFoundException ex) {
            throw new WeightConverterException(ex.getLocalizedMessage(), ex);
        }
    }

    private String getConnectionString() throws FileNotFoundException {

        return getStorageType(StorageService::getPrivateStorage)
                .map(path -> "jdbc:h2:" + path + File.separatorChar + "weightconverter")
                .orElseThrow(() -> new FileNotFoundException("Could not access private storage."));
    }

    /**
     * Get past weights
     *
     * @return Collection of {@link PastWeight}
     */
    public ObservableList<PastWeight> getPastWeights() {

        ObservableList<PastWeight> results = FXCollections.observableArrayList();

        String retrieveWeights
                = """
        SELECT W.ID, W.WEIGHT, W.KILOS, W.DT_TM
        FROM WEIGHTCONVERTER.WEIGHT W
        ORDER BY W.DT_TM DESC;
        """;

        try (Connection connection
                = DriverManager.getConnection(getConnectionString(), JDBC_USER, JDBC_PWD); Statement statement
                = connection.createStatement(
                        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            boolean execution = statement.execute(retrieveWeights);

            if (execution) {
                ResultSet resultSet = statement.getResultSet();
                if (resultSet.last()) {
                    resultSet.beforeFirst();

                    while (resultSet.next()) {
                        results.add(
                                new PastWeight(
                                        resultSet.getLong(1),
                                        resultSet.getString(2),
                                        resultSet.getDouble(3),
                                        resultSet.getTimestamp(4).toLocalDateTime()));
                    }

                    return results;
                }
            }
        } catch (SQLException | FileNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }

        return results;
    }

    /**
     * Insert a new weight
     *
     * @param weight weight in stones, pounds and ounces
     * @param kilos weight in kilos
     */
    public void setWeight(String weight, double kilos) {

        setWeight(weight, kilos, LocalDateTime.now());
    }

    /**
     * Modify a date and time
     *
     * @param dtTm date and time
     * @param id id
     */
    public void setDate(LocalDateTime dtTm, long id) {

        String updateDtTm = """
                          UPDATE WEIGHTCONVERTER.WEIGHT
                          SET DT_TM = ?
                          WHERE ID = ?
                          """;
        try (Connection connection
                = DriverManager.getConnection(getConnectionString(), JDBC_USER, JDBC_PWD); PreparedStatement prepareStatement = connection.prepareStatement(updateDtTm)) {

            prepareStatement.setTimestamp(1, Timestamp.valueOf(dtTm));
            prepareStatement.setLong(2, id);
            prepareStatement.executeUpdate();
        } catch (SQLException | FileNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setWeight(String weight, double kilos, LocalDateTime dtTm) {

        String insertWeight
                = """
        INSERT INTO WEIGHTCONVERTER.WEIGHT
        (WEIGHT, KILOS, DT_TM)
        VALUES (?, ?, ?)
        """;

        try (Connection connection
                = DriverManager.getConnection(getConnectionString(), JDBC_USER, JDBC_PWD); PreparedStatement prepareStatement = connection.prepareStatement(insertWeight)) {

            prepareStatement.setString(1, weight);
            prepareStatement.setDouble(2, kilos);
            prepareStatement.setTimestamp(3, Timestamp.valueOf(dtTm));
            prepareStatement.executeUpdate();
        } catch (SQLException | FileNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Attempts to delete a past weight from the connection supplier.
     *
     * @param id weight unique identifier
     */
    public void deletePastWeight(long id) {
        String deleteWeight
                = """
        DELETE FROM WEIGHTCONVERTER.WEIGHT W
        WHERE W.ID = ?
        """;

        try (Connection connection
                = DriverManager.getConnection(getConnectionString(), JDBC_USER, JDBC_PWD); PreparedStatement prepareStatement = connection.prepareStatement(deleteWeight)) {

            prepareStatement.setLong(1, id);
            prepareStatement.executeUpdate();
        } catch (SQLException | FileNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static <T> T getStorageType(Function<StorageService, T> storageService) {
        return Services.get(StorageService.class).map(storageService::apply).orElse(null);
    }
}
