package com.skritter.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SkritterDatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "skritterDatabase";

    private static final SkritterDatabaseTable[] tables = new SkritterDatabaseTable[] {
            new StudyItemTable(),
            new VocabTable(),
            new ReviewTable(),
            new StrokeDataTable(),
            new SentenceTable()
    };

    public SkritterDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (int i = 0; i < tables.length; i++) {
            db.execSQL(tables[i].getCreateStatement());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = 0; i < tables.length; i++) {
            db.execSQL("DROP TABLE IF EXISTS " + tables[i].getTableName());
        }

        onCreate(db);
    }

    public static String convertArrayToCSV(Object[] array) {
        if (array == null) {
            return "";
        }
        String separator = "";
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            result.append(separator).append(array[i]);
            separator = ",";
        }

        return result.toString();
    }

    public static String[] convertCSVToArray(String csv) {
        if (csv == null || "".equals(csv)) {
            return null;
        }

        return csv.split(",");
    }

    public void closeDB() {
        SQLiteDatabase db = getReadableDatabase();

        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}
