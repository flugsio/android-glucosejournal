package se.huffyreek.glucosejournal.database;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

import se.huffyreek.glucosejournal.JournalEntry;
import se.huffyreek.glucosejournal.InfusionChange;

public class GlucoseJournalDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "glucosejournal.db";
    private static final int DATABASE_VERSION = 2;

    public GlucoseJournalDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        JournalEntryTable.onCreate(database);
        InfusionChangeTable.onCreate(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
            int newVersion) {
        JournalEntryTable.onUpgrade(database, oldVersion, newVersion);
        InfusionChangeTable.onUpgrade(database, oldVersion, newVersion);
    }

    // Adding new journal entry
    public void addJournalEntry(JournalEntry journalEntry) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Inserting Row
        db.insert(JournalEntryTable.TABLE_JOURNALENTRY, null, JournalEntryTable.contentValues(journalEntry));
        db.close(); // Closing database connection
    }

    // Deleting journal entry
    public void deleteJournalEntry(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(JournalEntryTable.TABLE_JOURNALENTRY, JournalEntryTable.COLUMN_ID + " = ?", new String [] { Integer.toString(id) } );
        db.close(); // Closing database connection
    }

    public JournalEntry findJournalEntry(int id) {
        JournalEntry journalEntry = null;
        String selectQuery = "SELECT  * FROM " + JournalEntryTable.TABLE_JOURNALENTRY + " WHERE " + JournalEntryTable.COLUMN_ID + " = " + id;

        // TODO: writable really needed?
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            android.text.format.Time time = new android.text.format.Time();
            journalEntry = new JournalEntry();
            journalEntry.id = Integer.parseInt(cursor.getString(0));
            time.set(cursor.getLong(1));
            journalEntry.at = time;
            journalEntry.glucose = cursor.getString(2);
            journalEntry.carbohydrates = cursor.getString(3);
            journalEntry.dose = cursor.getString(4);
        }

        db.close(); // needed? was not included in example
        // return contact list
        return journalEntry;
    }

    public void updateJournalEntry(JournalEntry journalEntry) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(JournalEntryTable.TABLE_JOURNALENTRY, JournalEntryTable.contentValues(journalEntry), JournalEntryTable.COLUMN_ID + " = ?", new String [] { Integer.toString(journalEntry.id) } );
        db.close(); // Closing database connection
    }

    // Getting all journal entries
    public List<JournalEntry> getAllJournalEntries(long after_at) {
        List<JournalEntry> journalEntries = new ArrayList<JournalEntry>();
        // Select All Query
        String selectQuery =
            "SELECT * FROM " + JournalEntryTable.TABLE_JOURNALENTRY +
            " WHERE " + JournalEntryTable.COLUMN_AT + " > " + after_at +
            " ORDER BY " + JournalEntryTable.COLUMN_AT + " DESC ";

        // TODO: writable really needed?
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                JournalEntry journalEntry = new JournalEntry();
                journalEntry.id = Integer.parseInt(cursor.getString(0));
                journalEntry.at = timeFromLong(cursor.getLong(1));
                journalEntry.glucose = cursor.getString(2);
                journalEntry.carbohydrates = cursor.getString(3);
                journalEntry.dose = cursor.getString(4);

                journalEntries.add(journalEntry);
            } while (cursor.moveToNext());
        }

        db.close(); // needed? was not included in example
        // return contact list
        return journalEntries;
    }


    // Adding new infusion change
    public void addInfusionChange(InfusionChange infusionChange) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(InfusionChangeTable.TABLE_INFUSIONCHANGE, null, InfusionChangeTable.contentValues(infusionChange));
        db.close();
    }

    public InfusionChange findLatestInfusionChange() {
        InfusionChange infusionChange = null;
        String selectQuery = "SELECT * FROM " + InfusionChangeTable.TABLE_INFUSIONCHANGE + " ORDER BY " + InfusionChangeTable.COLUMN_AT + " DESC LIMIT 1";

        // TODO: writable really needed?
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            infusionChange = new InfusionChange();
            infusionChange.id = Integer.parseInt(cursor.getString(0));
            infusionChange.at = timeFromLong(cursor.getLong(1));
            infusionChange.units = cursor.getString(2);
        }

        db.close(); // needed? was not included in example
        // return contact list
        return infusionChange;
    }

    private android.text.format.Time timeFromLong(long longTime) {
        android.text.format.Time time = new android.text.format.Time();
        time.set(longTime);
        return time;
    }
}
