package se.huffyreek.glucosejournal.database;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import se.huffyreek.glucosejournal.InfusionChange;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class InfusionChangeTable {

      public static final String TABLE_INFUSIONCHANGE = "infusion_changes";
      public static final String COLUMN_ID = "_id";
      public static final String COLUMN_AT = "at";
      public static final String COLUMN_UNITS = "units"; // fill level

      private static final String TABLE_CREATE = "create table "
          + TABLE_INFUSIONCHANGE
          + "(" 
          + COLUMN_ID + " integer primary key autoincrement, "
          + COLUMN_AT + " integer not null, "
          + COLUMN_UNITS + " real not null"
          + ");";

      public static void onCreate(SQLiteDatabase database) {
        database.execSQL(TABLE_CREATE);
      }

      public static void onUpgrade(SQLiteDatabase database, int oldVersion,
          int newVersion) {
        Log.w(InfusionChangeTable.class.getName(), "Upgrading database from version "
            + oldVersion + " to " + newVersion
            + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_INFUSIONCHANGE);
        onCreate(database);
      }
      
      public static ContentValues contentValues(InfusionChange infusionChange) {
          ContentValues values = new ContentValues();
          values.put(COLUMN_AT, infusionChange.at.toMillis(false));
          values.put(COLUMN_UNITS, infusionChange.units);
          
          return values;
      }
}
