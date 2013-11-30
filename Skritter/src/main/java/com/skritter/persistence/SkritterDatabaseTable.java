package com.skritter.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public abstract class SkritterDatabaseTable<T> {
    public static class Column {
        public static String NULL_TYPE = "NULL";
        public static String INTEGER_TYPE = "INTEGER";
        public static String REAL_TYPE = "REAL";
        public static String TEXT_TYPE = "TEXT";
        public static String BLOB_TYPE = "BLOB";

        private boolean isPrimaryKey;
        private String name;
        private String columnType;

        public Column() {

        }

        public Column(boolean isPrimaryKey, String name, String columnType) {
            this.isPrimaryKey = isPrimaryKey;
            this.name = name;
            this.columnType = columnType;
        }

        public String getColumnType() {
            return columnType;
        }

        public void setColumnType(String columnType) {
            this.columnType = columnType;
        }

        public boolean isPrimaryKey() {
            return isPrimaryKey;
        }

        public void setPrimaryKey(boolean primaryKey) {
            isPrimaryKey = primaryKey;
        }

        public String getName() {
            return name;
        }
    }

    protected static final String ROW_ID = "rowid";
    public abstract String getTableName();
    public abstract Column[] getColumns();
    public abstract T populateItem(Cursor cursor);
    public abstract ContentValues populateContentValues(T item);

    public abstract long create(SkritterDatabaseHelper db, T item);
    public abstract T read(SkritterDatabaseHelper db, long id);
    public abstract long update(SkritterDatabaseHelper db, T item);
    public abstract void delete(SkritterDatabaseHelper db, T item);

    public List<T> getAllItems(SkritterDatabaseHelper db) {
        List<T> items = new ArrayList<T>();
        String selectQuery = "SELECT rowid, * FROM " + getTableName();

        SQLiteDatabase sqlDB = db.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                T item = populateItem(cursor);
                items.add(item);
            } while (cursor.moveToNext());
        }

        return items;
    }

    public void deleteAllItems(SkritterDatabaseHelper db) {
        String deleteQuery = "DELETE FROM " + getTableName() + ";";
        SQLiteDatabase sqlDB = db.getWritableDatabase();

        sqlDB.execSQL(deleteQuery);
    }

    public String getCreateStatement() {
        StringBuilder sb = new StringBuilder();

        sb.append("CREATE TABLE ").append(getTableName()).append("(");

        String separator = "";

        Column[] columns = getColumns();
        for (int i = 0; i < columns.length; i++) {
            Column col = columns[i];
            sb.append(separator).append(col.getName()).append(" ").append(col.getColumnType());

            if (col.isPrimaryKey()) {
                sb.append(" PRIMARY KEY");
            }

            separator = ",";
        }

        sb.append(");");

        return sb.toString();
    }
}
