package com.example.natha.aacquestionassistant;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ImageDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "imageDatabase";
    private static final int DATABASE_VERSION = 7;
    private static final String TABLE_IMAGES = "images";
    private static final String KEY_IMAGE_ID = "id";
    private static final String KEY_IMAGE_FILE_LOC_ID = "fileID";
    private static final String KEY_IMAGE_LOCATION = "imageLoc";
    private static final String KEY_IMAGE_SYMBOL = "symbol";
    private static final String KEY_VOCAB_PRONUNCIATION = "pronunciation";
    private static final String KEY_IMAGE_GRAMMAR = "grammar";
    @SuppressLint("SdCardPath")
    private static String DB_FILEPATH ="/data/data/com.example.natha.aacquestionassistant/databases/imageDatabase";
    private static ImageDatabaseHelper sInstance;
    private SQLiteDatabase db;

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

    /**
     * Copies the database file at the specified location over the current
     * internal application database.
     */
    public boolean importDatabase(Context context) throws IOException {

        // Close the SQLiteOpenHelper so it will commit the created empty
        // database to internal storage.
        close();
        InputStream f = context.getAssets().open("imageDatabase.db");
        File oldDb = new File(DB_FILEPATH);
        byte[] buffer = new byte[f.available()];
        f.read(buffer);
        if (oldDb.exists()) {
            OutputStream out = new FileOutputStream(oldDb);
            out.write(buffer);
            // Access the copied database so SQLiteHelper will cache it and mark
            // it as created.
            getWritableDatabase().close();
            return true;
        }
        return false;
    }

    int getSize() {
        //SELECT COUNT(*) FROM IMAGES
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(String.format("SELECT COUNT(*) FROM %s", TABLE_IMAGES), null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        return count;

    }

    public List<String> getAll() {
        //SELECT symbol FROM IMAGES

        List<String> result = new LinkedList<>();

        String query =
                String.format("SELECT %s FROM %s",
                        KEY_IMAGE_SYMBOL, TABLE_IMAGES);

        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();

        try (Cursor c = db.rawQuery(query, null)) {
            if (c.moveToFirst()) {
                do {
                    result.add(c.getString(0));
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
        if(searchQuery.length() == 0){
           return;
        }
        if(searchQuery.length() == 1){
            query = "SELECT * FROM images WHERE symbol = " + "'"+searchQuery+"'" +
                            " OR symbol = "  + "'"+(searchQuery + "_lower_case")+ "'";
        } else{
            query =
            String.format("SELECT * FROM %s WHERE %s LIKE '%%%s%%'",
                     TABLE_IMAGES, KEY_IMAGE_SYMBOL, searchQuery, KEY_IMAGE_GRAMMAR);
        }


        try (Cursor c = db.rawQuery(query, null)) {
            db.beginTransaction();
            //int count = 0;
            if (c.moveToFirst()) {
                do {

                    result.add(new Card(c.getInt(1), c.getString(2), c.getInt(3), c.getString(4)));
                    // count++;
                    //if(count > 15){
                    //     break;
                    //}
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
                KEY_IMAGE_FILE_LOC_ID + " INTEGER," +
                KEY_IMAGE_SYMBOL + " TEXT," +

                KEY_IMAGE_LOCATION + " INTEGER," +
                KEY_VOCAB_PRONUNCIATION + " TEXT " +
                ")";

        db.execSQL(CREATE_IMAGE_TABLE);
    }

    void addImage(Card i) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_IMAGE_FILE_LOC_ID, i.id);
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
}


