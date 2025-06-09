package dev.dect.kapture.data;

import android.app.NotificationManager;
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
                                KAPTURE_COL_PROFILE_ID = "k_profile_id",
                                KAPTURE_COL_FROM = "k_from";

    private final Context CONTEXT;

    public DB(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);

        this.CONTEXT = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String q0 = "CREATE TABLE "
                           + TABLE_KAPTURE + " ("
                           + KAPTURE_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                           + KAPTURE_COL_LOCATION + " TEXT, "
                           + KAPTURE_COL_PROFILE_ID + " TEXT, "
                           + KAPTURE_COL_FROM + " TEXT);";

        db.execSQL(q0);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}

    public void insertKapture(Kapture kapture) {
        final SQLiteDatabase db = this.getReadableDatabase();

        final ContentValues valuesKapture = new ContentValues();

        valuesKapture.put(KAPTURE_COL_LOCATION, kapture.getLocation());
        valuesKapture.put(KAPTURE_COL_PROFILE_ID, kapture.getProfileId());
        valuesKapture.put(KAPTURE_COL_FROM, kapture.getFrom());

        final long idKapture = db.insert(TABLE_KAPTURE, null, valuesKapture);

        kapture.setId(idKapture);
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
            kapture.setProfileId(cursor.getString(cursor.getColumnIndexOrThrow(KAPTURE_COL_PROFILE_ID)));
            kapture.setFrom(cursor.getString(cursor.getColumnIndexOrThrow(KAPTURE_COL_FROM)));

            kaptures.add(kapture);
        }

        cursor.close();

        return kaptures;
    }

    public void deleteKapture(Kapture kapture) {
        final SQLiteDatabase db = this.getReadableDatabase();

        db.beginTransaction();

        db.execSQL("DELETE FROM " + TABLE_KAPTURE + " WHERE " + KAPTURE_COL_ID + " = " +  kapture.getId());

        db.setTransactionSuccessful();

        db.endTransaction();

        try {
            ((NotificationManager) CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE)).cancel((int) kapture.getId());
        } catch (Exception ignore) {}
    }
}
