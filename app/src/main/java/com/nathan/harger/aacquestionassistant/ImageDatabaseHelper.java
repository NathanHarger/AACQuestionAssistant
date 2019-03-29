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

    private static final int DATABASE_VERSION = 14;
    private static final String TABLE_IMAGES = "images";
    private static final String KEY_IMAGE_ID = "image_id";
    private static final String KEY_IMAGE_FILE_LOC = "fileID";
    private static final String KEY_IMAGE_LOCATION = "imageLoc";
    private static final String KEY_IMAGE_SYMBOL = "symbol";
    private static final String KEY_VOCAB_PRONUNCIATION = "pronunciation";


    private static final String TABLE_VOCAB_SETS = "sets";
    private static final String KEY_VOCAB_SET_ID = "vocab_set_id";
    private static final String VOCAB_SET_NULL_HACK = "nullColumnHack";

    private static final String TABLE_VOCAB_GROUP = "vocab_group";
    private static final String KEY_VOCAB_GROUP_ID = "vocab_group_id";
    private static final String KEY_VOCAB_FOREIGN_KEY = "foreign_key";
    private static final String IMAGE_FOREIGN_KEY = "image_foreign_key";






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


    void searchImages(String searchQuery, List<Card> result) {
        String query;
        if (searchQuery.length() == 0) {
            query = "SELECT * FROM images";
        } else if (!searchQuery.matches("^[A-Za-z0-9]*$")) {

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

        String CREATE_VOCAB_SET_TABLE = "CREATE TABLE " + TABLE_VOCAB_SETS +
                "(" +
                KEY_VOCAB_SET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // Define a primary key
                VOCAB_SET_NULL_HACK + " TEXT" +
                ")";

        String CREATE_VOCAB_GROUP_TABLE = "CREATE TABLE " + TABLE_VOCAB_GROUP +
                "(" +
                KEY_VOCAB_GROUP_ID + " INTEGER PRIMARY KEY, " + // Define a primary key
                KEY_VOCAB_FOREIGN_KEY + " INTEGER, " + IMAGE_FOREIGN_KEY + " INTEGER, " +
                "FOREIGN KEY (" + KEY_VOCAB_FOREIGN_KEY + ") REFERENCES " + TABLE_VOCAB_SETS + "(" + KEY_VOCAB_SET_ID + "), " +
                "FOREIGN KEY (" + IMAGE_FOREIGN_KEY + ") REFERENCES " + TABLE_IMAGES + "(" + KEY_IMAGE_ID + "))";
        //foreign key to create vocab set
        // foreign key to image table;

        db.execSQL(CREATE_IMAGE_TABLE);
        db.execSQL(CREATE_VOCAB_SET_TABLE);
        db.execSQL(CREATE_VOCAB_GROUP_TABLE);


    }

    long addImage(Card i) {
        size++;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        long key = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_IMAGE_FILE_LOC, i.photoId);
            values.put(KEY_IMAGE_SYMBOL, i.label);
            values.put(KEY_IMAGE_LOCATION, i.resourceLocation);
            values.put(KEY_VOCAB_PRONUNCIATION, i.pronunciation);
            key = db.insertOrThrow(TABLE_IMAGES, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error adding image to database");

        } finally {
            db.endTransaction();

        }

        return key;

    }

    long addNewCardGroup(List<Card> cardGroup) {
        // add new vocab set
        ContentValues values = new ContentValues();
        long key = db.insertOrThrow(TABLE_VOCAB_SETS, "nullColumnHack", values);

        int size = 0;
        for (Card c : cardGroup) {
            if (c.id == 0) {
                continue;
            }
            size++;
            ContentValues v = new ContentValues();
            v.put(KEY_VOCAB_FOREIGN_KEY, key);
            v.put(IMAGE_FOREIGN_KEY, c.id);
            db.insertOrThrow(TABLE_VOCAB_GROUP, null, v);

        }

        if (size == 0) {
            db.delete(TABLE_VOCAB_SETS, "? = ?", new String[]{KEY_VOCAB_SET_ID, String.valueOf(key)});
            return -1;
        }
        return key;
    }



    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOCAB_SETS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOCAB_GROUP);

            onCreate(db);
        }
    }


    public void deleteCustomVocab(long id) {
        String q = "DELETE FROM images WHERE " + KEY_IMAGE_ID + " = " + id;
        db.execSQL(q);

    }

    List<Card> getCardGroup(long key) {
        String query = "SELECT * FROM " + TABLE_VOCAB_GROUP + " a JOIN " + TABLE_IMAGES + " b ON a.image_foreign_key = b.image_id WHERE " + KEY_VOCAB_FOREIGN_KEY + " = " + key;

        List<Card> result = new LinkedList<>();
        try (Cursor c = db.rawQuery(query, null)) {
            db.beginTransaction();
            if (c.moveToFirst()) {
                do {
                    int id = c.getInt(2);
                    String filename = c.getString(5);
                    int imageLoc = c.getInt(6);
                    String pronunciation = c.getString(7);
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

        return result;
    }

    public List<Long> getCardSets() {
        String query = "SELECT * FROM " + TABLE_VOCAB_SETS;
        List<Long> result = new LinkedList<>();
        try (Cursor c = db.rawQuery(query, null)) {
            db.beginTransaction();
            if (c.moveToFirst()) {
                do {
                    long id = c.getLong(0);
                    result.add(id);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get images from database");

        } finally {
            db.endTransaction();
        }

        return result;
    }


    /*
    *  private static final String TABLE_VOCAB_SETS = "sets";
    private static final String KEY_VOCAB_SET_ID = "vocab_set_id";
    private static final String VOCAB_SET_NULL_HACK = "nullColumnHack";

    private static final String TABLE_VOCAB_GROUP = "vocab_group";
    private static final String KEY_VOCAB_GROUP_ID = "vocab_group_id";
    private static final String KEY_VOCAB_FOREIGN_KEY = "foreign_key";
    private static final String IMAGE_FOREIGN_KEY = "image_foreign_key";

    *
    *
    *
    *
    *
    * */
    public void addCardGroup(long vocabKey, String[] tokens) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_VOCAB_SET_ID, vocabKey);
        db.insert(TABLE_VOCAB_SETS, null, cv);

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("")) {
                continue;
            }
            long currKey = Long.parseLong(tokens[i]);
            ContentValues currCv = new ContentValues();
            currCv.put(KEY_VOCAB_FOREIGN_KEY, vocabKey);
            currCv.put(IMAGE_FOREIGN_KEY, currKey);

            db.insert(TABLE_VOCAB_GROUP, null, currCv);
        }
    }
}


