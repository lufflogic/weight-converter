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
import com.gluonhq.attach.settings.SettingsService;
import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Services;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

/**
 * Migration
 *
 * @author Chris
 */
public class Migration {

  public void doMigration() {
    try {
      File databaseFile = getDatabaseFile();
      System.out.println(MessageFormat.format("Reading from {0}", databaseFile.toString()));
      getLegacyWeights(databaseFile)
          .forEach(weight -> new Database().setWeight(weight.getWeight(), weight.getDate()));
      SettingsService.create().ifPresent(service -> service.store("migration", "1"));
      databaseFile.deleteOnExit();
    } catch (FileNotFoundException ex) {
      Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private File getDatabaseFile() throws FileNotFoundException {

    Optional<File> storageLocation =
        Services.get(StorageService.class).map(StorageService::getPrivateStorage).orElse(null);

    return new File(
        storageLocation
                .orElseThrow(() -> new FileNotFoundException("Could not access private storage."))
                .getAbsolutePath()
            + "/weightconverter.sqlite");
  }

  private List<PastWeight> getLegacyWeights(File dbFile) {
    List<PastWeight> results = new ArrayList<>();

    try {
      SqlJetDb connection = SqlJetDb.open(dbFile, true);
      connection.runTransaction(
          (SqlJetDb db) -> {
            db.getOptions().setUserVersion(1);
            return true;
          },
          SqlJetTransactionMode.WRITE);

      connection.beginTransaction(SqlJetTransactionMode.READ_ONLY);
      ISqlJetTable weightISqlJetTable = connection.getTable("weight");
      ISqlJetCursor wieghtTablJetCursor = weightISqlJetTable.order("dt_tm_idx");
      if (!wieghtTablJetCursor.eof()) {
        do {
          results.add(
              new PastWeight(
                  wieghtTablJetCursor.getInteger("id"),
                  wieghtTablJetCursor.getString("weight"),
                  LocalDateTime.ofInstant(
                      Instant.ofEpochSecond(wieghtTablJetCursor.getInteger("dt_tm")),
                      ZoneId.systemDefault())));
        } while (wieghtTablJetCursor.next());
      }
      wieghtTablJetCursor.close();
      connection.close();
    } catch (SqlJetException ex) {
      Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
    }
    return results;
  }
}
