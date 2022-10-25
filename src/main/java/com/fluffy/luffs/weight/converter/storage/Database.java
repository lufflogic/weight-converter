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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Database */
@SuppressWarnings("java:S2115")
public class Database {

  private static final String JDBC_USER = "SA";
  private static final String JDBC_PWD = "";

  private Database() {}

  /** Creates the database if not present */
  public static void createDatabse() throws FileNotFoundException {

    String schema = "CREATE SCHEMA IF NOT EXISTS WEIGHTCONVERTER";
    String weightTable =
        """
          CREATE TABLE IF NOT EXISTS WEIGHTCONVERTER.WEIGHT
          (
           ID IDENTITY NOT NULL PRIMARY KEY,
           WEIGHT VARCHAR NOT NULL,
           DT_TM TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
          );
          """;

    String settingsTable =
        """
          CREATE TABLE IF NOT EXISTS WEIGHTCONVERTER.SETTINGS
          (
           ID IDENTITY NOT NULL PRIMARY KEY,
           SETTING_TYPE VARCHAR NOT NULL,
           SETTING VARCHAR NOT NULL,
           DT_TM TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
          );
        """;

    String index =
        "CREATE INDEX IF NOT EXISTS WEIGHTCONVERTER.DT_TM_IDX ON WEIGHTCONVERTER.WEIGHT(DT_TM);";

    try (Connection connection =
            DriverManager.getConnection(getConnectionString(), JDBC_USER, JDBC_PWD);
        Statement statement = connection.createStatement()) {

      statement.addBatch(schema);
      statement.addBatch(weightTable);
      statement.addBatch(settingsTable);
      statement.addBatch(index);
      statement.executeBatch();

    } catch (SQLException | FileNotFoundException ex) {
      throw new WeightConverterException(ex.getLocalizedMessage(), ex);
    }
  }

  private static String getConnectionString() throws FileNotFoundException {
    return getStorageType(StorageService::getPrivateStorage)
        .map(path -> "jdbc:h2:" + path + File.separatorChar + "weightconverter")
        .orElseThrow(() -> new FileNotFoundException("Could not access private storage."));
  }

  /**
   * Get past weights
   *
   * @return Collection of {@link PastWeight}
   */
  public static Collection<PastWeight> getPastWeights() {

    String retrieveWeights =
        """
        SELECT W.ID, W.WEIGHT, W.DT_TM
        FROM WEIGHTCONVERTER.WEIGHT W
        ORDER BY W.DT_TM DESC;
        """;

    try (Connection connection =
            DriverManager.getConnection(getConnectionString(), JDBC_USER, JDBC_PWD);
        Statement statement =
            connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

      boolean execution = statement.execute(retrieveWeights);

      if (execution) {
        ResultSet resultSet = statement.getResultSet();
        if (resultSet.last()) {
          List<PastWeight> results = new ArrayList<>(resultSet.getRow());
          resultSet.beforeFirst();

          while (resultSet.next()) {
            results.add(
                new PastWeight(
                    resultSet.getLong(1),
                    resultSet.getString(2),
                    resultSet.getTimestamp(3).toLocalDateTime()));
          }

          return results;
        }
      }
    } catch (SQLException | FileNotFoundException ex) {
      Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
    }

    return Collections.emptyList();
  }

  /**
   * Insert a new weight
   *
   * @param weight weight
   */
  public static void setWeight(String weight) {

    String insertWeight =
        """
        INSERT INTO WEIGHTCONVERTER.WEIGHT
        (WEIGHT)
        VALUES (?)
        """;

    try (Connection connection =
            DriverManager.getConnection(getConnectionString(), JDBC_USER, JDBC_PWD);
        PreparedStatement prepareStatement = connection.prepareStatement(insertWeight)) {

      prepareStatement.setString(1, weight);
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
  public static void deletePastWeight(long id) {
    String deleteWeight =
        """
        DELETE FROM WEIGHTCONVERTER.WEIGHT W
        WHERE W.ID = ?
        """;

    try (Connection connection =
            DriverManager.getConnection(getConnectionString(), JDBC_USER, JDBC_PWD);
        PreparedStatement prepareStatement = connection.prepareStatement(deleteWeight)) {

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
