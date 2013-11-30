package com.skritter.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.skritter.models.Stroke;
import com.skritter.models.StrokeData;

public class StrokeDataTable extends SkritterDatabaseTable<StrokeData> {
    private static final String RUNE = "rune";
    private static final String LANGUAGE = "language";
    private static final String STROKES = "strokes";

    // The numbers for a particular Stroke are stored as text in the database
    // These are just arbitrary separators to separate numbers, and groups of these numbers
    private static final String numberSeparator = "\t";
    private static final String strokeSeparator = ";";

    private static final StrokeDataTable strokeDataTable = new StrokeDataTable();

    public static StrokeDataTable getInstance() {
        return strokeDataTable;
    }

    @Override
    public String getTableName() {
        return "stroke_data";
    }

    @Override
    public Column[] getColumns() {
        return new Column[] {
                new Column(false, RUNE, Column.TEXT_TYPE),
                new Column(false, LANGUAGE, Column.TEXT_TYPE),
                new Column(false, STROKES, Column.TEXT_TYPE)
        };
    }

    @Override
    public long create(SkritterDatabaseHelper db, StrokeData strokeData) {
        ContentValues values = populateContentValues(strokeData);

        SQLiteDatabase sqlDB = db.getWritableDatabase();
        long strokeID = sqlDB.insert(getTableName(), null, values);

        return strokeID;
    }

    @Override
    public StrokeData read(SkritterDatabaseHelper db, long id) {
        String selectQuery = "SELECT rowid, * FROM " + getTableName() + " WHERE rowid = " + id;

        SQLiteDatabase sqlDB = db.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(selectQuery, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        StrokeData strokeData = populateItem(cursor);

        return strokeData;
    }

    public StrokeData getByRuneAndLanguage(SkritterDatabaseHelper db, String rune, String language) {
        String selectQuery = "SELECT rowid, * FROM " + getTableName() + " WHERE " + RUNE + " = '" + rune + "' AND " + LANGUAGE + " = '" + language + "'";

        SQLiteDatabase sqlDB = db.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(selectQuery, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        StrokeData strokeData = populateItem(cursor);

        return strokeData;
    }

    @Override
    public long update(SkritterDatabaseHelper db, StrokeData strokeData) {
        ContentValues values = populateContentValues(strokeData);

        SQLiteDatabase sqlDB = db.getWritableDatabase();
        return sqlDB.update(getTableName(), values, "rowid = ?", new String[] { String.valueOf(strokeData.getOid()) });
    }

    @Override
    public void delete(SkritterDatabaseHelper db, StrokeData strokeData) {
        SQLiteDatabase sqlDB = db.getWritableDatabase();
        sqlDB.delete(getTableName(), "rowid = ?", new String[] { String.valueOf(strokeData.getOid()) });
    }

    @Override
    public StrokeData populateItem(Cursor cursor) {
        StrokeData strokeData = new StrokeData();

        strokeData.setOid(cursor.getLong(cursor.getColumnIndex(ROW_ID)));
        strokeData.setRune(cursor.getString(cursor.getColumnIndex(RUNE)));
        strokeData.setLanguage(cursor.getString(cursor.getColumnIndex(LANGUAGE)));

        String strokesString = cursor.getString(cursor.getColumnIndex(STROKES));
        String[] strokeArray = strokesString.split(strokeSeparator);

        Stroke[] strokes = new Stroke[strokeArray.length];
        for (int i = 0; i < strokeArray.length; i++) {
            String[] numbers = strokeArray[i].split(numberSeparator);

            Stroke newStroke = new Stroke();
            newStroke.strokeID = Integer.parseInt(numbers[0]);
            newStroke.x = Float.parseFloat(numbers[1]);
            newStroke.y = Float.parseFloat(numbers[2]);
            newStroke.width = Float.parseFloat(numbers[3]);
            newStroke.height = Float.parseFloat(numbers[4]);
            newStroke.rotation = Float.parseFloat(numbers[5]);

            strokes[i] = newStroke;
        }

        strokeData.setStrokes(strokes);

        return strokeData;
    }

    @Override
    public ContentValues populateContentValues(StrokeData strokeData) {
        ContentValues values = new ContentValues();

        values.put(RUNE, strokeData.getRune());
        values.put(LANGUAGE, strokeData.getLanguage());
        String strokes = "";
        String arraySeparator = "";

        for (Stroke stroke : strokeData.getStrokes()) {
            strokes += arraySeparator + stroke.strokeID + numberSeparator +
                    stroke.x + numberSeparator +
                    stroke.y + numberSeparator +
                    stroke.width + numberSeparator +
                    stroke.height + numberSeparator +
                    stroke.rotation;

            arraySeparator = strokeSeparator;
        }

        values.put(STROKES, strokes);

        return values;
    }

}
