/*
MIT License

Copyright (c) 2020 Chris Luff

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
import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Services;
import java.io.File;
import java.io.FileNotFoundException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

/**
 *
 * Database
 */
public class Database {

    /**
     * Attempts to get a connection to SqlJetDb Database and return it for use.
     *
     * @return SqlJetDb
     */
    public static SqlJetDb getConnection() {
        SqlJetDb connection = null;
        try {
            File dbFile = new File(getStorageType(StorageService::getPrivateStorage).orElseThrow(() -> new FileNotFoundException("Could not access private storage."))
                    .getAbsolutePath() + "/weightconverter.sqlite");

            connection = SqlJetDb.open(dbFile, true);
            connection.runTransaction((SqlJetDb db) -> {
                db.getOptions().setUserVersion(1);
                return true;
            }, SqlJetTransactionMode.WRITE);

            connection.beginTransaction(SqlJetTransactionMode.WRITE);
            connection.createTable("CREATE TABLE IF NOT EXISTS weight (id INTEGER PRIMARY KEY, weight TEXT NOT NULL, dt_tm INTEGER NOT NULL)");
            connection.createIndex("CREATE INDEX IF NOT EXISTS id_idx ON weight(id)");
            connection.createIndex("CREATE INDEX IF NOT EXISTS dt_tm_idx ON weight(dt_tm)");
            connection.commit();
        } catch (FileNotFoundException | SqlJetException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            if (connection != null) {
                try {
                    connection.close();
                } catch (SqlJetException sje) {
                    Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return connection;
    }

    /**
     * Attempts to select stored dates from the connection supplier.
     *
     * @param connection
     * @return Optional Pair of Weight and Date
     */
    public static List<PastWeight> getPastWeights(Supplier<SqlJetDb> connection) {
        List<PastWeight> results = new ArrayList<>();
        SqlJetDb sqlJetDb = connection.get();
        try {
            sqlJetDb.beginTransaction(SqlJetTransactionMode.READ_ONLY);
            ISqlJetTable weight_table = sqlJetDb.getTable("weight");
            ISqlJetCursor weight_table_cursor = weight_table.order("dt_tm_idx");
            if (!weight_table_cursor.eof()) {
                do {
                    results.add(new PastWeight(weight_table_cursor.getInteger("id"),
                            weight_table_cursor.getString("weight"),
                            LocalDateTime.ofInstant(Instant.ofEpochSecond(weight_table_cursor.getInteger("dt_tm")), ZoneId.systemDefault())));
                } while (weight_table_cursor.next());
            }
            weight_table_cursor.close();
        } catch (SqlJetException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                sqlJetDb.close();
            } catch (SqlJetException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return results;
    }

    /**
     * Attempts to insert a new weight into the connection supplier.
     *
     * @param connection
     * @param weight
     */
    public static void setPastWeight(Supplier<SqlJetDb> connection, String weight) {
        SqlJetDb sqlJetDb = connection.get();
        try {
            sqlJetDb.beginTransaction(SqlJetTransactionMode.WRITE);
            ISqlJetTable weight_table = sqlJetDb.getTable("weight");
            weight_table.insert(weight, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            sqlJetDb.commit();
        } catch (SqlJetException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                sqlJetDb.close();
            } catch (SqlJetException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    /**
     * Attempts to delete a past weight from the connection supplier.
     *
     * @param connection
     * @param id
     */
    public static void deletePastWeight(Supplier<SqlJetDb> connection, long id) {
        SqlJetDb sqlJetDb = connection.get();
        try {
            sqlJetDb.beginTransaction(SqlJetTransactionMode.WRITE);
            ISqlJetTable weight_table = sqlJetDb.getTable("weight");
            ISqlJetCursor weight_table_cursor = weight_table.lookup("id_idx", id);
            if (!weight_table_cursor.eof()) {
                do {
                    weight_table_cursor.delete();
                } while (weight_table_cursor.next());
            }
            weight_table_cursor.close();
        } catch (SqlJetException ex) {

        } finally {
            try {
                sqlJetDb.commit();
                sqlJetDb.close();
            } catch (SqlJetException ex) {

            }
        }

    }

    private static <T> T getStorageType(Function<StorageService, T> storageService) {
        return Services.get(StorageService.class).map(storageService::apply).orElse(null);
    }

}
