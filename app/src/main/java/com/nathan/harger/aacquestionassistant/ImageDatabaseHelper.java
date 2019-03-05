package com.nathan.harger.aacquestionassistant;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import static android.content.ContentValues.TAG;

class ImageDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 9;
    private static final String TABLE_IMAGES = "images";
    private static final String KEY_IMAGE_ID = "id";
    private static final String KEY_IMAGE_FILE_LOC = "fileID";
    private static final String KEY_IMAGE_LOCATION = "imageLoc";
    private static final String KEY_IMAGE_SYMBOL = "symbol";
    private static final String KEY_VOCAB_PRONUNCIATION = "pronunciation";
    @SuppressLint("SdCardPath")
    private static ImageDatabaseHelper sInstance;
    private final SQLiteDatabase db;
    private int size = 0;

    private ImageDatabaseHelper(Context context) {
        super(context, null, null, DATABASE_VERSION);
        db = getReadableDatabase();
    }

    static ImageDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ImageDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    int getSize() {
        return size;
    }

    public List<Card> getAll() {
        //SELECT symbol FROM IMAGES

        List<Card> result = new LinkedList<>();
        String query =
                String.format("SELECT * FROM %s",
                        TABLE_IMAGES);

        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();

        try (Cursor c = db.rawQuery(query, null)) {
            if (c.moveToFirst()) {
                do {
                    result.add(new Card(c.getInt(0),
                            c.getString(2), c.getString(1),
                            c.getInt(3), c.getString(4)));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get images from database");
        } finally {
            db.endTransaction();
        }
        return result;
    }

    void searchImages(String searchQuery, List<Card> result) {
        String query;
        if (searchQuery.length() == 0) {
            query = "SELECT * FROM images";
        } else if (searchQuery.matches("[^A-Za-z0-9]+")) {

            query = "SELECT * FROM images WHERE symbol " + "LIKE '%" + searchQuery + "%'";
            //          " OR symbol = " + "'" + (searchQuery + "_lower_case") + "'";
        } else {
            query =
                    String.format("SELECT * FROM %s WHERE %s LIKE '%%%s%%'" +
                                    "order by " +
                                    "case when " + "symbol" + " like " + "'" + searchQuery.charAt(0) + "%%' "
                                    + "then 1 else 2 end ",
                            TABLE_IMAGES, KEY_IMAGE_SYMBOL, searchQuery);
        }


        try (Cursor c = db.rawQuery(query, null)) {
            db.beginTransaction();
            if (c.moveToFirst()) {
                do {
                    int id = c.getInt(0);
                    String filename = c.getString(2);
                    int imageLoc = c.getInt(3);
                    String pronunciation = c.getString(4);
                    if (imageLoc == 1) {
                        result.add(new Card(id, filename, filename + "-" + pronunciation,
                                imageLoc, pronunciation));
                    } else {
                        result.add(new Card(id, filename, filename,
                                imageLoc, pronunciation));
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get images from database");

        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_IMAGE_TABLE = "CREATE TABLE " + TABLE_IMAGES +
                "(" +
                KEY_IMAGE_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_IMAGE_FILE_LOC + " TEXT," +
                KEY_IMAGE_SYMBOL + " TEXT," +

                KEY_IMAGE_LOCATION + " INTEGER," +
                KEY_VOCAB_PRONUNCIATION + " TEXT " +
                ")";
        db.execSQL(CREATE_IMAGE_TABLE);
    }

    void addImage(Card i) {
        size++;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(KEY_IMAGE_FILE_LOC, i.photoId);
            values.put(KEY_IMAGE_SYMBOL, i.label);
            values.put(KEY_IMAGE_LOCATION, i.resourceLocation);
            values.put(KEY_VOCAB_PRONUNCIATION, i.pronunciation);
            db.insertOrThrow(TABLE_IMAGES, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error adding image to database");

        } finally {
            db.endTransaction();

        }

    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
            onCreate(db);
        }
    }


    public void deleteCustomVocab(int id) {
        String q = "DELETE FROM images WHERE id = " + id;
        db.execSQL(q);

    }
}


