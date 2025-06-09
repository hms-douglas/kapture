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

    private static final int DB_VERSION = 4;

    private static final String TABLE_KAPTURE = "kapture",
                                KAPTURE_COL_ID = "k_id",
                                KAPTURE_COL_LOCATION = "k_location",
                                KAPTURE_COL_PROFILE_ID = "k_profile_id",
                                KAPTURE_COL_FROM = "k_from",

                                TABLE_EXTRAS = "extras",
                                EXTRAS_COL_ID = "e_id",
                                EXTRAS_COL_ID_KAPTURE = "e_id_kapture",
                                EXTRAS_COL_LOCATION = "e_location",
                                EXTRAS_COL_TYPE = "e_type",

                                TABLE_SCREENSHOTS = "screenshots",
                                SCREENSHOTS_COL_ID = "s_id",
                                SCREENSHOTS_COL_ID_KAPTURE = "s_id_kapture",
                                SCREENSHOTS_COL_LOCATION = "s_location";

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

        final String q1 = "CREATE TABLE "
                          + TABLE_EXTRAS + " ("
                          + EXTRAS_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                          + EXTRAS_COL_ID_KAPTURE + " INTEGER, "
                          + EXTRAS_COL_LOCATION + " TEXT, "
                          + EXTRAS_COL_TYPE + " INTEGER);";

        db.execSQL(q0);
        db.execSQL(q1);

        Update.createScreenshotsTableHelper(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch(oldVersion) {
            case 1:
                Update.createScreenshotsTableHelper(db);

            case 2:
                Update.updateKaptureTableAddProfileCol(db);

            case 3:
                Update.updateKaptureTableAddFromCol(db);
                break;
        }
    }

    private static class Update {
        public static void createScreenshotsTableHelper(SQLiteDatabase db) {
            final String q = "CREATE TABLE "
                + TABLE_SCREENSHOTS + " ("
                + SCREENSHOTS_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SCREENSHOTS_COL_ID_KAPTURE + " INTEGER, "
                + SCREENSHOTS_COL_LOCATION + " TEXT);";

            db.execSQL(q);
        }

        public static void updateKaptureTableAddProfileCol(SQLiteDatabase db) {
            final String q = "ALTER TABLE "
                + TABLE_KAPTURE
                + " ADD COLUMN "
                + KAPTURE_COL_PROFILE_ID
                + " TEXT DEFAULT "
                + "\"" + Constants.NO_PROFILE + "\"";

            db.execSQL(q);
        }

        public static void updateKaptureTableAddFromCol(SQLiteDatabase db) {
            final String q = "ALTER TABLE "
                    + TABLE_KAPTURE
                    + " ADD COLUMN "
                    + KAPTURE_COL_FROM
                    + " TEXT DEFAULT "
                    + "\"" + Kapture.FROM_PHONE + "\"";

            db.execSQL(q);
        }
    }

    public void insertKapture(Kapture kapture) {
        final SQLiteDatabase db = this.getReadableDatabase();

        final ContentValues valuesKapture = new ContentValues();

        valuesKapture.put(KAPTURE_COL_LOCATION, kapture.getLocation());
        valuesKapture.put(KAPTURE_COL_PROFILE_ID, kapture.getProfileId());
        valuesKapture.put(KAPTURE_COL_FROM, kapture.getFrom());

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

        for(Kapture.Screenshot screenshot : kapture.getScreenshots()) {
            final ContentValues valuesScreenshot = new ContentValues();

            valuesScreenshot.put(SCREENSHOTS_COL_ID_KAPTURE, idKapture);
            valuesScreenshot.put(SCREENSHOTS_COL_LOCATION, screenshot.getLocation());

            final long idScreenshot = db.insert(TABLE_SCREENSHOTS, null, valuesScreenshot);

            screenshot.setId(idScreenshot);
        }
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
            kapture.setExtras(selectExtras(kapture));
            kapture.setScreenshots(selectScreenshots(kapture));

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
            kapture.setProfileId(cursor.getString(cursor.getColumnIndexOrThrow(KAPTURE_COL_PROFILE_ID)));
            kapture.setFrom(cursor.getString(cursor.getColumnIndexOrThrow(KAPTURE_COL_FROM)));
            kapture.setExtras(selectExtras(kapture));
            kapture.setScreenshots(selectScreenshots(kapture));

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

    public ArrayList<Kapture.Screenshot> selectScreenshots(Kapture kapture) {
        final ArrayList<Kapture.Screenshot> screenshots = new ArrayList<>();

        final SQLiteDatabase db = this.getReadableDatabase();

        final Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SCREENSHOTS + " WHERE " + SCREENSHOTS_COL_ID_KAPTURE + " = " + kapture.getId(), null);

        while(true) {
            assert cursor != null;
            if(!cursor.moveToNext()) {
                break;
            }

            final Kapture.Screenshot screenshot = new Kapture.Screenshot();

            screenshot.setId(cursor.getLong(cursor.getColumnIndexOrThrow(SCREENSHOTS_COL_ID)));
            screenshot.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(SCREENSHOTS_COL_LOCATION)));
            screenshot.setIdKapture(cursor.getLong(cursor.getColumnIndexOrThrow(SCREENSHOTS_COL_ID_KAPTURE)));

            screenshots.add(screenshot);
        }

        cursor.close();

        return screenshots;
    }

    public void deleteKapture(Kapture kapture) {
        final SQLiteDatabase db = this.getReadableDatabase();

        db.beginTransaction();

        db.execSQL("DELETE FROM " + TABLE_KAPTURE + " WHERE " + KAPTURE_COL_ID + " = " +  kapture.getId());
        db.execSQL("DELETE FROM " + TABLE_EXTRAS + " WHERE " + EXTRAS_COL_ID_KAPTURE + " = " + kapture.getId());
        db.execSQL("DELETE FROM " + TABLE_SCREENSHOTS + " WHERE " + SCREENSHOTS_COL_ID_KAPTURE + " = " + kapture.getId());

        db.setTransactionSuccessful();

        db.endTransaction();

        try {
            ((NotificationManager) CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE)).cancel((int) kapture.getId());
        } catch (Exception ignore) {}
    }

    public void deleteExtra(Kapture.Extra extra) {
        final SQLiteDatabase db = this.getReadableDatabase();

        db.execSQL("DELETE FROM " + TABLE_EXTRAS + " WHERE " + EXTRAS_COL_ID + " = " + extra.getId());
    }

    public void deleteExtras(Kapture kapture) {
        final SQLiteDatabase db = this.getReadableDatabase();

        db.execSQL("DELETE FROM " + TABLE_EXTRAS + " WHERE " + EXTRAS_COL_ID_KAPTURE + " = " + kapture.getId());
    }

    public void deleteScreenshot(Kapture.Screenshot screenshot) {
        final SQLiteDatabase db = this.getReadableDatabase();

        db.execSQL("DELETE FROM " + TABLE_SCREENSHOTS + " WHERE " + SCREENSHOTS_COL_ID + " = " + screenshot.getId());
    }

    public void deleteScreenshots(Kapture kapture) {
        final SQLiteDatabase db = this.getReadableDatabase();

        db.execSQL("DELETE FROM " + TABLE_SCREENSHOTS + " WHERE " + SCREENSHOTS_COL_ID_KAPTURE + " = " + kapture.getId());
    }

    public void updateKapture(Kapture kapture) {
        final SQLiteDatabase db = this.getReadableDatabase();

        ContentValues valuesKapture = new ContentValues();

        valuesKapture.put(KAPTURE_COL_LOCATION, kapture.getLocation());
        valuesKapture.put(KAPTURE_COL_PROFILE_ID, kapture.getProfileId());
        valuesKapture.put(KAPTURE_COL_FROM, kapture.getFrom());

        for(Kapture.Extra extra : kapture.getExtras()) {
            updateExtra(extra);
        }

        for(Kapture.Screenshot screenshot : kapture.getScreenshots()) {
            updateScreenshot(screenshot);
        }

        db.update(TABLE_KAPTURE, valuesKapture, KAPTURE_COL_ID + " = " + kapture.getId(), null);
    }

    public void updateExtra(Kapture.Extra extra) {
        final SQLiteDatabase db = this.getReadableDatabase();

        final ContentValues valuesExtra = new ContentValues();

        valuesExtra.put(EXTRAS_COL_LOCATION, extra.getLocation());

        db.update(TABLE_EXTRAS, valuesExtra, EXTRAS_COL_ID + " = " + extra.getId(), null);
    }

    public void updateScreenshot(Kapture.Screenshot screenshot) {
        final SQLiteDatabase db = this.getReadableDatabase();

        final ContentValues valuesScreenshot = new ContentValues();

        valuesScreenshot.put(SCREENSHOTS_COL_LOCATION, screenshot.getLocation());

        db.update(TABLE_SCREENSHOTS, valuesScreenshot, SCREENSHOTS_COL_ID + " = " + screenshot.getId(), null);
    }
}
