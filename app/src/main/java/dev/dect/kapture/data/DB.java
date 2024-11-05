package dev.dect.kapture.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import dev.dect.kapture.model.Kapture;

public class DB extends SQLiteOpenHelper {
    private static final String DB_NAME = "kapture.db";

    private static final int DB_VERSION = 1;

    private static final String TABLE_KAPTURE = "kapture",
                                KAPTURE_COL_ID = "k_id",
                                KAPTURE_COL_LOCATION = "k_location",

                                TABLE_EXTRAS = "extras",
                                EXTRAS_COL_ID = "e_id",
                                EXTRAS_COL_ID_KAPTURE = "e_id_kapture",
                                EXTRAS_COL_LOCATION = "e_location",
                                EXTRAS_COL_TYPE = "e_type";

    private Context CONTEXT;

    public DB(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);

        this.CONTEXT = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String q0 = "CREATE TABLE "
                           + TABLE_KAPTURE + " ("
                           + KAPTURE_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                           + KAPTURE_COL_LOCATION + " TEXT);";

        final String q1 = "CREATE TABLE "
                          + TABLE_EXTRAS + " ("
                          + EXTRAS_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                          + EXTRAS_COL_ID_KAPTURE + " INTEGER, "
                          + EXTRAS_COL_LOCATION + " TEXT, "
                          + EXTRAS_COL_TYPE + " INTEGER);";

        db.execSQL(q0);
        db.execSQL(q1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public Kapture insertKapture(Kapture kapture) {
        final SQLiteDatabase db = this.getReadableDatabase();

        final ContentValues valuesKapture = new ContentValues();

        valuesKapture.put(KAPTURE_COL_LOCATION, kapture.getLocation());

        final long idKapture = db.insert(TABLE_KAPTURE, null, valuesKapture);

        kapture.setId(idKapture);

        for(Kapture.Extra extra : kapture.getExtras()) {
            final ContentValues valuesExtra = new ContentValues();

            valuesExtra.put(EXTRAS_COL_ID_KAPTURE, idKapture);
            valuesExtra.put(EXTRAS_COL_LOCATION, extra.getLocation());
            valuesExtra.put(EXTRAS_COL_TYPE, extra.getType());

            final long idExtra = db.insert(TABLE_EXTRAS, null, valuesExtra);

            extra.setId(idExtra);
        }

        return kapture;
    }

    public ArrayList<Kapture> selectAllKaptures(boolean desc) {
        final ArrayList<Kapture> kaptures = new ArrayList<>();

        final SQLiteDatabase db = this.getReadableDatabase();

        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_KAPTURE + " ORDER BY " + KAPTURE_COL_ID + (desc ? " DESC" : " ASC"), null);

        while(true) {
            assert cursor != null;
            if(!cursor.moveToNext()) {
                break;
            }

            final Kapture kapture = new Kapture(CONTEXT);

            kapture.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KAPTURE_COL_ID)));
            kapture.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(KAPTURE_COL_LOCATION)));
            kapture.setExtras(selectExtras(kapture));

            kaptures.add(kapture);
        }

        cursor.close();

        return kaptures;
    }

    public Kapture selectKapture(long id) {
        return selectKapture((int) id);
    }

    public Kapture selectKapture(int id) {
        final SQLiteDatabase db = this.getReadableDatabase();

        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_KAPTURE + " WHERE " + KAPTURE_COL_ID + " = " + id, null);

        if(cursor.moveToFirst()) {
            final Kapture kapture = new Kapture(CONTEXT);

            kapture.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KAPTURE_COL_ID)));
            kapture.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(KAPTURE_COL_LOCATION)));
            kapture.setExtras(selectExtras(kapture));

            cursor.close();

            return kapture;
        }

        cursor.close();

        return null;
    }

    public ArrayList<Kapture.Extra> selectExtras(Kapture kapture) {
        final ArrayList<Kapture.Extra> extras = new ArrayList<>();

        final SQLiteDatabase db = this.getReadableDatabase();

        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EXTRAS + " WHERE " + EXTRAS_COL_ID_KAPTURE + " = " + kapture.getId(), null);

        while(true) {
            assert cursor != null;
            if(!cursor.moveToNext()) {
                break;
            }

            final Kapture.Extra extra = new Kapture.Extra();

            extra.setId(cursor.getLong(cursor.getColumnIndexOrThrow(EXTRAS_COL_ID)));
            extra.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(EXTRAS_COL_LOCATION)));
            extra.setType(cursor.getInt(cursor.getColumnIndexOrThrow(EXTRAS_COL_TYPE)));
            extra.setIdKapture(cursor.getLong(cursor.getColumnIndexOrThrow(EXTRAS_COL_ID_KAPTURE)));

            extras.add(extra);
        }

        cursor.close();

        return extras;
    }

    public void deleteKapture(Kapture kapture) {
        final SQLiteDatabase db = this.getReadableDatabase();

        db.beginTransaction();

        db.execSQL("DELETE FROM " + TABLE_KAPTURE + " WHERE " + KAPTURE_COL_ID + " = " +  kapture.getId());
        db.execSQL("DELETE FROM " + TABLE_EXTRAS + " WHERE " + EXTRAS_COL_ID_KAPTURE + " = " + kapture.getId());

        db.setTransactionSuccessful();

        db.endTransaction();
    }

    public void deleteExtra(Kapture.Extra extra) {
        final SQLiteDatabase db = this.getReadableDatabase();

        db.execSQL("DELETE FROM " + TABLE_EXTRAS + " WHERE " + EXTRAS_COL_ID + " = " + extra.getId());
    }

    public void deleteExtras(Kapture kapture) {
        final SQLiteDatabase db = this.getReadableDatabase();

        db.execSQL("DELETE FROM " + TABLE_EXTRAS + " WHERE " + EXTRAS_COL_ID_KAPTURE + " = " + kapture.getId());
    }

    public void updateKapture(Kapture kapture) {
        final SQLiteDatabase db = this.getReadableDatabase();

        ContentValues valuesKapture = new ContentValues();

        valuesKapture.put(KAPTURE_COL_ID, kapture.getId());
        valuesKapture.put(KAPTURE_COL_LOCATION, kapture.getLocation());

        for(Kapture.Extra extra : kapture.getExtras()) {
            updateExtra(extra);
        }

        db.update(TABLE_KAPTURE, valuesKapture, KAPTURE_COL_ID + " = " + kapture.getId(), null);
    }

    public void updateExtra(Kapture.Extra extra) {
        final SQLiteDatabase db = this.getReadableDatabase();

        final ContentValues valuesExtra = new ContentValues();

        valuesExtra.put(EXTRAS_COL_ID_KAPTURE, extra.getIdKapture());
        valuesExtra.put(EXTRAS_COL_LOCATION, extra.getLocation());
        valuesExtra.put(EXTRAS_COL_TYPE, extra.getType());

        db.update(TABLE_EXTRAS, valuesExtra, EXTRAS_COL_ID + " = " + extra.getId(), null);
    }
}
