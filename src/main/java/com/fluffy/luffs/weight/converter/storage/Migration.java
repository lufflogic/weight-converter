/*
MIT License

Copyright (c) 2022  LuffLogic

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
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

/** Imports the legacy SQLite database while preserving the original file. */
public class Migration {

    private static final Logger LOG = Logger.getLogger(Migration.class.getName());
    private static final Pattern LEGACY_WEIGHT = Pattern.compile(
            "(-?\\d+)st\\s+(-?\\d+)lbs?\\s+(-?\\d+)oz");

    public void doMigration() {
        try {
            File directory = StorageService.create()
                    .flatMap(StorageService::getPrivateStorage)
                    .orElseThrow(() -> new WeightConverterException("Could not access private storage for migration."));
            File databaseFile = new File(directory, "weightconverter.sqlite");
            if (!databaseFile.exists()) {
                return;
            }
            if (!databaseFile.isFile()) {
                throw new WeightConverterException("Legacy database is not a file: " + databaseFile);
            }
            Database.Factory.create().migrateLegacyWeights(() -> getLegacyWeights(databaseFile));
        } catch (WeightConverterException ex) {
            LOG.log(Level.SEVERE, "Legacy migration failed; the source is retained for retry.", ex);
        }
    }

    static List<PastWeight> getLegacyWeights(File dbFile) {
        List<PastWeight> results = new ArrayList<>();
        try {
            SqlJetDb connection = SqlJetDb.open(dbFile, false);
            try {
                connection.runReadTransaction(db -> {
                    ISqlJetCursor cursor = db.getTable("weight").open();
                    try {
                        while (!cursor.eof()) {
                            String weight = cursor.getString("weight");
                            results.add(new PastWeight(cursor.getInteger("id"), weight,
                                    kilosFromLegacyWeight(weight),
                                    // The legacy writer encoded local wall time using UTC.
                                    LocalDateTime.ofInstant(Instant.ofEpochSecond(cursor.getInteger("dt_tm")),
                                            ZoneOffset.UTC)));
                            cursor.next();
                        }
                    } finally {
                        cursor.close();
                    }
                    return null;
                });
            } finally {
                connection.close();
            }
        } catch (SqlJetException | RuntimeException ex) {
            throw new WeightConverterException("Could not read legacy database: " + dbFile, ex);
        }
        return results;
    }

    private static double kilosFromLegacyWeight(String weight) {
        Matcher matcher = LEGACY_WEIGHT.matcher(weight);
        if (!matcher.matches()) {
            throw new WeightConverterException("Unrecognized legacy weight: " + weight);
        }
        double pounds = Double.parseDouble(matcher.group(1)) * 14
                + Double.parseDouble(matcher.group(2))
                + Double.parseDouble(matcher.group(3)) / 16;
        if (!Double.isFinite(pounds)) {
            throw new WeightConverterException("Legacy weight is out of range: " + weight);
        }
        // The legacy file only stores rounded stones/pounds/ounces, so this is approximate.
        return pounds * 0.45359237;
    }
}
