package com.skritter.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.skritter.models.StudyItem;

public class StudyItemTable extends SkritterDatabaseTable<StudyItem> {
    private static final String STUDY_ITEM_ID = "study_item_id";
    private static final String PART = "part";
    private static final String VOCAB_IDS = "vocab_ids";
    private static final String STYLE = "style";
    private static final String TIME_STUDIED = "time_studied";
    private static final String NEXT = "next";
    private static final String LAST = "last";
    private static final String INTERVAL = "interval";
    private static final String VOCAB_LIST_IDS = "vocab_list_ids";
    private static final String SECTION_IDS = "section_ids";
    private static final String REVIEWS = "reviews";
    private static final String SUCCESSES = "successes";
    private static final String CREATED = "created";
    private static final String CHANGED = "changed";
    private static final String PREVIOUS_SUCCESS = "previous_success";
    private static final String PREVIOUS_INTERVAL = "previous_interval";

    private static final StudyItemTable studyItemTable = new StudyItemTable();

    public static StudyItemTable getInstance() {
        return studyItemTable;
    }

    @Override
    public String getTableName() {
        return "study_item";
    }

    @Override
    public Column[] getColumns() {
        return new Column[] {
                new Column(false, STUDY_ITEM_ID, Column.TEXT_TYPE),
                new Column(false, PART, Column.TEXT_TYPE),
                new Column(false, VOCAB_IDS, Column.TEXT_TYPE),
                new Column(false, STYLE, Column.TEXT_TYPE),
                new Column(false, TIME_STUDIED, Column.INTEGER_TYPE),
                new Column(false, NEXT, Column.INTEGER_TYPE),
                new Column(false, LAST, Column.INTEGER_TYPE),
                new Column(false, INTERVAL, Column.INTEGER_TYPE),
                new Column(false, VOCAB_LIST_IDS, Column.TEXT_TYPE),
                new Column(false, SECTION_IDS, Column.TEXT_TYPE),
                new Column(false, REVIEWS, Column.INTEGER_TYPE),
                new Column(false, SUCCESSES, Column.INTEGER_TYPE),
                new Column(false, CREATED, Column.INTEGER_TYPE),
                new Column(false, CHANGED, Column.INTEGER_TYPE),
                new Column(false, PREVIOUS_SUCCESS, Column.INTEGER_TYPE),
                new Column(false, PREVIOUS_INTERVAL, Column.INTEGER_TYPE)
        };
    }

    @Override
    public long create(SkritterDatabaseHelper db, StudyItem studyItem) {
        ContentValues values = populateContentValues(studyItem);

        SQLiteDatabase sqlDB = db.getWritableDatabase();
        long studyItemID = sqlDB.insert(getTableName(), null, values);

        return studyItemID;
    }

    @Override
    public StudyItem read(SkritterDatabaseHelper db, long id) {
        String selectQuery = "SELECT rowid, * FROM " + getTableName() + " WHERE rowid = " + id;

        SQLiteDatabase sqlDB = db.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(selectQuery, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        StudyItem studyItem = populateItem(cursor);

        return studyItem;
    }

    @Override
    public long update(SkritterDatabaseHelper db, StudyItem studyItem) {
        ContentValues values = populateContentValues(studyItem);

        SQLiteDatabase sqlDB = db.getWritableDatabase();
        return sqlDB.update(getTableName(), values, "rowid = ?", new String[] { String.valueOf(studyItem.getOid()) });
    }

    @Override
    public void delete(SkritterDatabaseHelper db, StudyItem item) {
        SQLiteDatabase sqlDB = db.getWritableDatabase();
        sqlDB.delete(getTableName(), "rowid = ?", new String[] { String.valueOf( item.getOid()) });
    }

    @Override
    public StudyItem populateItem(Cursor cursor) {
        StudyItem studyItem = new StudyItem();

        studyItem.setOid(cursor.getLong(cursor.getColumnIndex(ROW_ID)));
        studyItem.setId(cursor.getString(cursor.getColumnIndex(STUDY_ITEM_ID)));
        studyItem.setVocabIDs(SkritterDatabaseHelper.convertCSVToArray(cursor.getString(cursor.getColumnIndex(VOCAB_IDS))));
        studyItem.setPart(cursor.getString(cursor.getColumnIndex(PART)));
        studyItem.setStyle(cursor.getString(cursor.getColumnIndex(STYLE)));
        studyItem.setTimeStudied(cursor.getLong(cursor.getColumnIndex(TIME_STUDIED)));
        studyItem.setNext(cursor.getLong(cursor.getColumnIndex(NEXT)));
        studyItem.setLast(cursor.getLong(cursor.getColumnIndex(LAST)));
        studyItem.setInterval(cursor.getLong(cursor.getColumnIndex(INTERVAL)));
        studyItem.setVocabListIDs(SkritterDatabaseHelper.convertCSVToArray(cursor.getString(cursor.getColumnIndex(VOCAB_LIST_IDS))));
        studyItem.setSectionIDs(SkritterDatabaseHelper.convertCSVToArray(cursor.getString(cursor.getColumnIndex(SECTION_IDS))));
        studyItem.setReviews(cursor.getInt(cursor.getColumnIndex(REVIEWS)));
        studyItem.setSuccesses(cursor.getInt(cursor.getColumnIndex(SUCCESSES)));
        studyItem.setCreated(cursor.getLong(cursor.getColumnIndex(CREATED)));
        studyItem.setChanged(cursor.getLong(cursor.getColumnIndex(CHANGED)));
        studyItem.setPreviousSuccess(cursor.getInt(cursor.getColumnIndex(PREVIOUS_SUCCESS)) == 1);
        studyItem.setPreviousInterval(cursor.getLong(cursor.getColumnIndex(PREVIOUS_INTERVAL)));

        return studyItem;
    }

    @Override
    public ContentValues populateContentValues(StudyItem studyItem) {
        ContentValues values = new ContentValues();

        values.put(STUDY_ITEM_ID, studyItem.getId());
        values.put(VOCAB_IDS, SkritterDatabaseHelper.convertArrayToCSV(studyItem.getVocabIDs()));
        values.put(PART, studyItem.getPart());
        values.put(STYLE, studyItem.getStyle());
        values.put(TIME_STUDIED, studyItem.getTimeStudied());
        values.put(NEXT, studyItem.getNext());
        values.put(LAST, studyItem.getLast());
        values.put(INTERVAL, studyItem.getInterval());
        values.put(VOCAB_LIST_IDS, SkritterDatabaseHelper.convertArrayToCSV(studyItem.getVocabListIDs()));
        values.put(SECTION_IDS, SkritterDatabaseHelper.convertArrayToCSV(studyItem.getSectionIDs()));
        values.put(REVIEWS, studyItem.getReviews());
        values.put(SUCCESSES, studyItem.getSuccesses());
        values.put(CREATED, studyItem.getCreated());
        values.put(CHANGED, studyItem.getChanged());
        values.put(PREVIOUS_SUCCESS, studyItem.isPreviousSuccess());
        values.put(PREVIOUS_INTERVAL, studyItem.getPreviousInterval());

        return values;
    }

}
