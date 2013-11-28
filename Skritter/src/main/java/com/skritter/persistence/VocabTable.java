package com.skritter.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.skritter.models.Vocab;

public class VocabTable extends SkritterDatabaseTable<Vocab> {
    private static final String ID = "id";
    private static final String VOCAB_ID = "vocab_id";
    private static final String CONTAINED_VOCAB_IDS = "contained_vocab_ids";
    private static final String LANGUAGE = "language";
    private static final String IS_RARE_KANJI = "is_rare_kanji";
    private static final String AUDIO_FILE = "audio_file";
    private static final String TOUGHNESS = "toughness";
    private static final String CHANGED = "changed";
    private static final String WRITING = "writing";
    private static final String TOUGHNESS_STRING = "toughness_string";
    private static final String DEFINITIONS = "definitions";
    private static final String STARRED = "starred";
    private static final String READING = "reading";

    private static final VocabTable vocabTable = new VocabTable();

    public static VocabTable getInstance() {
        return vocabTable;
    }

    @Override
    public String getTableName() {
        return "vocab";
    }

    @Override
    public Column[] getColumns() {
        return new Column[] {
                new Column(true, ID, Column.INTEGER_TYPE),
                new Column(false, VOCAB_ID, Column.TEXT_TYPE),
                new Column(false, CONTAINED_VOCAB_IDS, Column.TEXT_TYPE),
                new Column(false, LANGUAGE, Column.TEXT_TYPE),
                new Column(false, IS_RARE_KANJI, Column.TEXT_TYPE),
                new Column(false, AUDIO_FILE, Column.TEXT_TYPE),
                new Column(false, TOUGHNESS, Column.TEXT_TYPE),
                new Column(false, CHANGED, Column.TEXT_TYPE),
                new Column(false, WRITING, Column.TEXT_TYPE),
                new Column(false, TOUGHNESS_STRING, Column.TEXT_TYPE),
                new Column(false, DEFINITIONS, Column.TEXT_TYPE),
                new Column(false, STARRED, Column.TEXT_TYPE),
                new Column(false, READING, Column.TEXT_TYPE)
        };
    }

    @Override
    public long create(SkritterDatabaseHelper db, Vocab vocab) {
        ContentValues values = populateContentValues(vocab);

        SQLiteDatabase sqlDB = db.getWritableDatabase();
        long vocabID = sqlDB.insert(getTableName(), null, values);

        return vocabID;
    }

    @Override
    public Vocab read(SkritterDatabaseHelper db, long id) {
        String selectQuery = "SELECT  * FROM " + getTableName() + " WHERE " + ID + " = " + id;

        SQLiteDatabase sqlDB = db.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(selectQuery, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        Vocab vocab = populateItem(cursor);

        return vocab;
    }

    public Vocab getByStringID(SkritterDatabaseHelper db, String id) {
        String selectQuery = "SELECT  * FROM " + getTableName() + " WHERE " + VOCAB_ID + " = '" + id + "'";

        SQLiteDatabase sqlDB = db.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(selectQuery, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        Vocab vocab = populateItem(cursor);

        return vocab;
    }

    @Override
    public long update(SkritterDatabaseHelper db, Vocab vocab) {
        ContentValues values = populateContentValues(vocab);

        SQLiteDatabase sqlDB = db.getWritableDatabase();
        return sqlDB.update(getTableName(), values, ID + " = ?", new String[] { String.valueOf(vocab.getDatabaseID()) });
    }

    @Override
    public void delete(SkritterDatabaseHelper db, Vocab vocab) {
        SQLiteDatabase sqlDB = db.getWritableDatabase();
        sqlDB.delete(getTableName(), ID + " = ?", new String[] { String.valueOf(vocab.getDatabaseID()) });
    }

    @Override
    public Vocab populateItem(Cursor cursor) {
        Vocab vocab = new Vocab();

        vocab.setId(cursor.getString(cursor.getColumnIndex(VOCAB_ID)));
        vocab.setContainedVocabIDs(SkritterDatabaseHelper.convertCSVToArray(cursor.getString(cursor.getColumnIndex(CONTAINED_VOCAB_IDS))));
        vocab.setLanguage(cursor.getString(cursor.getColumnIndex(LANGUAGE)));
        vocab.setRareKanji(cursor.getInt(cursor.getColumnIndex(IS_RARE_KANJI)) == 1);
        vocab.setAudioFile(cursor.getString(cursor.getColumnIndex(AUDIO_FILE)));
        vocab.setToughness(cursor.getInt(cursor.getColumnIndex(TOUGHNESS)));
        vocab.setChanged(cursor.getLong(cursor.getColumnIndex(CHANGED)));
        vocab.setWriting(cursor.getString(cursor.getColumnIndex(WRITING)));
        vocab.setToughnessString(cursor.getString(cursor.getColumnIndex(TOUGHNESS_STRING)));
        vocab.setDefinitions(cursor.getString(cursor.getColumnIndex(DEFINITIONS)));
        vocab.setStarred(cursor.getInt(cursor.getColumnIndex(STARRED)) == 1);
        vocab.setReading(cursor.getString(cursor.getColumnIndex(READING)));

        return vocab;
    }

    @Override
    public ContentValues populateContentValues(Vocab vocab) {
        ContentValues values = new ContentValues();

        values.put(VOCAB_ID, vocab.getId());
        values.put(CONTAINED_VOCAB_IDS, SkritterDatabaseHelper.convertArrayToCSV(vocab.getContainedVocabIDs()));
        values.put(LANGUAGE, vocab.getLanguage());
        values.put(IS_RARE_KANJI, vocab.isRareKanji());
        values.put(AUDIO_FILE, vocab.getAudioFile());
        values.put(TOUGHNESS, vocab.getToughness());
        values.put(CHANGED, vocab.getChanged());
        values.put(WRITING, vocab.getWriting());
        values.put(TOUGHNESS_STRING, vocab.getToughnessString());
        values.put(DEFINITIONS, vocab.getDefinitions());
        values.put(STARRED, vocab.isStarred());
        values.put(READING, vocab.getReading());

        return values;
    }

}
