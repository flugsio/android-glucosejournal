package se.huffyreek.glucosejournal.database;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import se.huffyreek.glucosejournal.JournalEntry;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class JournalEntryTable {

    public static final String TABLE_JOURNALENTRY = "journal_entries";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_AT = "at";
    public static final String COLUMN_GLUCOSE = "glucose";
    public static final String COLUMN_CARBOHYDRATES = "carbohydrates";
    public static final String COLUMN_DOSE = "dose";

    private static final String TABLE_CREATE = "create table "
        + TABLE_JOURNALENTRY
        + "("
        + COLUMN_ID + " integer primary key autoincrement, "
        + COLUMN_AT + " integer not null, "
        + COLUMN_GLUCOSE + " real not null,"
        + COLUMN_CARBOHYDRATES + " real not null,"
        + COLUMN_DOSE + " real not null"
        + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
            int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            // nothing to do
        } else {
            Log.w(JournalEntryTable.class.getName(), "Upgrading database from version "
                    + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            database.execSQL("DROP TABLE IF EXISTS " + TABLE_JOURNALENTRY);
            onCreate(database);
        }
    }

    public static ContentValues contentValues(JournalEntry journalEntry) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_AT, journalEntry.at.toMillis(false));
        values.put(COLUMN_GLUCOSE, journalEntry.glucose);
        values.put(COLUMN_CARBOHYDRATES, journalEntry.carbohydrates);
        values.put(COLUMN_DOSE, journalEntry.dose);

        return values;
    }
}
