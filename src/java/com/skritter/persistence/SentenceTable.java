package com.skritter.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.skritter.models.Sentence;

public class SentenceTable extends SkritterDatabaseTable<Sentence> {
    private static final String SENTENCE_ID = "sentence_id";
    private static final String CONTAINED_VOCAB_IDS = "contained_vocab_ids";
    private static final String DEFINITIONS = "definitions";
    private static final String LANGUAGE = "language";
    private static final String IS_RARE_KANJI = "is_rare_kanji";
    private static final String READING = "reading";
    private static final String STARRED = "starred";
    private static final String TOUGHNESS = "toughness";
    private static final String TOUGHNESS_STRING = "toughness_string";
    private static final String WRITING = "writing";

    private static final SentenceTable sentenceTable = new SentenceTable();

    public static SentenceTable getInstance() {
        return sentenceTable;
    }

    @Override
    public String getTableName() {
        return "sentence";
    }

    @Override
    public Column[] getColumns() {
        return new Column[] {
                new Column(false, SENTENCE_ID, Column.TEXT_TYPE),
                new Column(false, CONTAINED_VOCAB_IDS, Column.TEXT_TYPE),
                new Column(false, LANGUAGE, Column.TEXT_TYPE),
                new Column(false, IS_RARE_KANJI, Column.TEXT_TYPE),
                new Column(false, TOUGHNESS, Column.TEXT_TYPE),
                new Column(false, WRITING, Column.TEXT_TYPE),
                new Column(false, TOUGHNESS_STRING, Column.TEXT_TYPE),
                new Column(false, DEFINITIONS, Column.TEXT_TYPE),
                new Column(false, STARRED, Column.TEXT_TYPE),
                new Column(false, READING, Column.TEXT_TYPE)
        };
    }

    @Override
    public long create(SkritterDatabaseHelper db, Sentence sentence) {
        ContentValues values = populateContentValues(sentence);

        SQLiteDatabase sqlDB = db.getWritableDatabase();
        long vocabID = sqlDB.insert(getTableName(), null, values);

        return vocabID;
    }

    @Override
    public Sentence read(SkritterDatabaseHelper db, long id) {
        String selectQuery = "SELECT rowid, * FROM " + getTableName() + " WHERE rowid = " + id;

        SQLiteDatabase sqlDB = db.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(selectQuery, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        Sentence sentence = populateItem(cursor);

        return sentence;
    }

    public Sentence getByStringID(SkritterDatabaseHelper db, String id) {
        String selectQuery = "SELECT rowid, * FROM " + getTableName() + " WHERE " + SENTENCE_ID + " = '" + id + "'";

        SQLiteDatabase sqlDB = db.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(selectQuery, null);
        
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }

        cursor.moveToFirst();

        Sentence sentence = populateItem(cursor);

        return sentence;
    }

    @Override
    public long update(SkritterDatabaseHelper db, Sentence sentence) {
        ContentValues values = populateContentValues(sentence);

        SQLiteDatabase sqlDB = db.getWritableDatabase();
        return sqlDB.update(getTableName(), values, "rowid = ?", new String[] { String.valueOf(sentence.getOid()) });
    }

    @Override
    public void delete(SkritterDatabaseHelper db, Sentence sentence) {
        SQLiteDatabase sqlDB = db.getWritableDatabase();
        sqlDB.delete(getTableName(), "rowid = ?", new String[] { String.valueOf(sentence.getOid()) });
    }

    @Override
    public Sentence populateItem(Cursor cursor) {
        Sentence sentence = new Sentence();

        sentence.setOid(cursor.getLong(cursor.getColumnIndex(ROW_ID)));
        sentence.setId(cursor.getString(cursor.getColumnIndex(SENTENCE_ID)));
        sentence.setContainedVocabIDs(SkritterDatabaseHelper.convertCSVToArray(cursor.getString(cursor.getColumnIndex(CONTAINED_VOCAB_IDS))));
        sentence.setLanguage(cursor.getString(cursor.getColumnIndex(LANGUAGE)));
        sentence.setRareKanji(cursor.getInt(cursor.getColumnIndex(IS_RARE_KANJI)) == 1);
        sentence.setToughness(cursor.getInt(cursor.getColumnIndex(TOUGHNESS)));
        sentence.setWriting(cursor.getString(cursor.getColumnIndex(WRITING)));
        sentence.setToughnessString(cursor.getString(cursor.getColumnIndex(TOUGHNESS_STRING)));
        sentence.setDefinitions(cursor.getString(cursor.getColumnIndex(DEFINITIONS)));
        sentence.setStarred(cursor.getInt(cursor.getColumnIndex(STARRED)) == 1);
        sentence.setReading(cursor.getString(cursor.getColumnIndex(READING)));

        return sentence;
    }

    @Override
    public ContentValues populateContentValues(Sentence sentence) {
        ContentValues values = new ContentValues();

        values.put(SENTENCE_ID, sentence.getId());
        values.put(CONTAINED_VOCAB_IDS, SkritterDatabaseHelper.convertArrayToCSV(sentence.getContainedVocabIDs()));
        values.put(LANGUAGE, sentence.getLanguage());
        values.put(IS_RARE_KANJI, sentence.isRareKanji());
        values.put(TOUGHNESS, sentence.getToughness());
        values.put(WRITING, sentence.getWriting());
        values.put(TOUGHNESS_STRING, sentence.getToughnessString());
        values.put(DEFINITIONS, sentence.getDefinitions());
        values.put(STARRED, sentence.isStarred());
        values.put(READING, sentence.getReading());

        return values;
    }

}
