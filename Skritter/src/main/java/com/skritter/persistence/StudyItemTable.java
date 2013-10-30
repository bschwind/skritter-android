package com.skritter.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.skritter.models.StudyItem;

/**
 * Created by bschwind on 10/29/13.
 */
public class StudyItemTable extends SkritterDatabaseTable<StudyItem> {
    private static final String ID = "id";
    private static final String STUDY_ITEM_ID = "study_item_id";
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

    @Override
    public String getTableName() {
        return "study_item";
    }

    @Override
    public Column[] getColumns() {
        return new Column[] {
                new Column(true, ID, Column.INTEGER_TYPE),
                new Column(false, STUDY_ITEM_ID, Column.TEXT_TYPE),
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

    public long create(SQLiteDatabase db, StudyItem studyItem) {
        ContentValues values = new ContentValues();

        values.put(STUDY_ITEM_ID, studyItem.getId());
        values.put(VOCAB_IDS, SkritterDatabaseHelper.convertArrayToCSV(studyItem.getVocabIDs()));
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

        long studyItemID = db.insert(getTableName(), null, values);

        return studyItemID;
    }

    public StudyItem read(SQLiteDatabase db, long id) {
        String selectQuery = "SELECT  * FROM " + getTableName() + " WHERE " + ID + " = " + id;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        StudyItem studyItem = populateItem(cursor);

        return studyItem;
    }

    public StudyItem populateItem(Cursor cursor) {
        StudyItem studyItem = new StudyItem();

        studyItem.setId(cursor.getString(cursor.getColumnIndex(STUDY_ITEM_ID)));
        studyItem.setVocabIDs(SkritterDatabaseHelper.convertCSVToArray(cursor.getString(cursor.getColumnIndex(VOCAB_IDS))));
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

    public long update(SQLiteDatabase db, StudyItem studyItem) {
        ContentValues values = new ContentValues();

        values.put(STUDY_ITEM_ID, studyItem.getId());
        values.put(VOCAB_IDS, SkritterDatabaseHelper.convertArrayToCSV(studyItem.getVocabIDs()));
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

        // updating row
        return db.update(getTableName(), values, ID + " = ?", new String[] { String.valueOf(studyItem.getDatabaseID()) });
    }

    public void delete(SQLiteDatabase db, StudyItem item) {
        db.delete(getTableName(), ID + " = ?", new String[] { String.valueOf( item.getDatabaseID()) });
    }

}
