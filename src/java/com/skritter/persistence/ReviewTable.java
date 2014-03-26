package com.skritter.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.skritter.models.Review;

public class ReviewTable extends SkritterDatabaseTable<Review> {

    private static final String ITEM_ID = "item_id";
    private static final String SCORE = "score";
    private static final String BEAR_TIME = "bear_time";
    private static final String SUBMIT_TIME = "submit_time";
    private static final String REVIEW_TIME = "review_time";
    private static final String THINKING_TIME = "thinking_time";
    private static final String CURRENT_INTERVAL = "current_interval";
    private static final String ACTUAL_INTERVAL = "actual_interval";
    private static final String NEW_INTERVAL = "new_interval";
    private static final String WORD_GROUP = "word_group";
    private static final String PREVIOUS_INTERVAL = "previous_interval";
    private static final String PREVIOUS_SUCCESS = "previous_success";

    private static final ReviewTable reviewTable = new ReviewTable();

    public static ReviewTable getInstance() {
        return reviewTable;
    }

    @Override
    public String getTableName() {
        return "review";
    }

    @Override
    public Column[] getColumns() {
        return new Column[] {
                new Column(false, ITEM_ID, Column.TEXT_TYPE),
                new Column(false, SCORE, Column.INTEGER_TYPE),
                new Column(false, BEAR_TIME, Column.INTEGER_TYPE),
                new Column(false, SUBMIT_TIME, Column.INTEGER_TYPE),
                new Column(false, REVIEW_TIME, Column.REAL_TYPE),
                new Column(false, THINKING_TIME, Column.REAL_TYPE),
                new Column(false, CURRENT_INTERVAL, Column.INTEGER_TYPE),
                new Column(false, ACTUAL_INTERVAL, Column.INTEGER_TYPE),
                new Column(false, NEW_INTERVAL, Column.INTEGER_TYPE),
                new Column(false, WORD_GROUP, Column.TEXT_TYPE),
                new Column(false, PREVIOUS_INTERVAL, Column.INTEGER_TYPE),
                new Column(false, PREVIOUS_SUCCESS, Column.INTEGER_TYPE)
        };
    }

    @Override
    public long create(SkritterDatabaseHelper db, Review review) {
        ContentValues values = populateContentValues(review);

        SQLiteDatabase sqlDB = db.getWritableDatabase();
        long reviewID = sqlDB.insert(getTableName(), null, values);

        return reviewID;
    }

    @Override
    public Review read(SkritterDatabaseHelper db, long id) {
        String selectQuery = "SELECT rowid, * FROM " + getTableName() + " WHERE rowid = " + id;

        SQLiteDatabase sqlDB = db.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(selectQuery, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        Review review = populateItem(cursor);

        return review;
    }

    @Override
    public long update(SkritterDatabaseHelper db, Review review) {
        ContentValues values = populateContentValues(review);

        SQLiteDatabase sqlDB = db.getWritableDatabase();
        return sqlDB.update(getTableName(), values, "rowid = ?", new String[] { String.valueOf(review.getOid()) });
    }

    @Override
    public void delete(SkritterDatabaseHelper db, Review review) {
        SQLiteDatabase sqlDB = db.getWritableDatabase();
        sqlDB.delete(getTableName(), "rowid = ?", new String[] { String.valueOf(review.getOid()) });
    }

    @Override
    public Review populateItem(Cursor cursor) {
        Review review = new Review();

        review.setOid(cursor.getLong(cursor.getColumnIndex(ROW_ID)));
        review.setItemID(cursor.getString(cursor.getColumnIndex(ITEM_ID)));
        review.setScore(cursor.getInt(cursor.getColumnIndex(SCORE)));
        review.setBearTime(cursor.getInt(cursor.getColumnIndex(BEAR_TIME)) == 1);
        review.setSubmitTime(cursor.getLong(cursor.getColumnIndex(SUBMIT_TIME)));
        review.setReviewTime(cursor.getFloat(cursor.getColumnIndex(REVIEW_TIME)));
        review.setThinkingTime(cursor.getFloat(cursor.getColumnIndex(THINKING_TIME)));
        review.setCurrentInterval(cursor.getLong(cursor.getColumnIndex(CURRENT_INTERVAL)));
        review.setActualInterval(cursor.getLong(cursor.getColumnIndex(ACTUAL_INTERVAL)));
        review.setNewInterval(cursor.getLong(cursor.getColumnIndex(NEW_INTERVAL)));
        review.setWordGroup(cursor.getString(cursor.getColumnIndex(WORD_GROUP)));
        review.setPreviousInterval(cursor.getLong(cursor.getColumnIndex(PREVIOUS_INTERVAL)));
        review.setPreviousSuccess(cursor.getInt(cursor.getColumnIndex(PREVIOUS_SUCCESS)) == 1);

        return review;
    }

    @Override
    public ContentValues populateContentValues(Review review) {
        ContentValues values = new ContentValues();

        values.put(ITEM_ID, review.getItemID());
        values.put(SCORE, review.getScore());
        values.put(BEAR_TIME, review.isBearTime());
        values.put(SUBMIT_TIME, review.getSubmitTime());
        values.put(REVIEW_TIME, review.getReviewTime());
        values.put(THINKING_TIME, review.getThinkingTime());
        values.put(CURRENT_INTERVAL, review.getCurrentInterval());
        values.put(ACTUAL_INTERVAL, review.getActualInterval());
        values.put(NEW_INTERVAL, review.getNewInterval());
        values.put(WORD_GROUP, review.getWordGroup());
        values.put(PREVIOUS_INTERVAL, review.getPreviousInterval());
        values.put(PREVIOUS_SUCCESS, review.isPreviousSuccess());

        return values;
    }

}
