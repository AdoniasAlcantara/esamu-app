package org.myopenproject.esamu.domain;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmergencyGateway extends SQLiteOpenHelper implements AutoCloseable {
    private static final String TAG = "SQL";
    private static final String DB_NAME = "esamu";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase db;

    public EmergencyGateway(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS tb_emergency ( "
                + "id INTEGER PRIMARY KEY, "
                + "date_time INTEGER NOT NULL, "
                + "location TEXT, "
                + "status INTEGER NOT NULL, "
                + "attachment INTEGER NOT NULL)";

        db.execSQL(sql);
        Log.d(TAG, "Database created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void create(EmergencyRecord emergency) {
        db.insert("tb_emergency", null, prepare(emergency));
    }

    public boolean update(EmergencyRecord emergency) {
        String[] idStr = new String[] {String.valueOf(emergency.getId())};
        int count = db.update("tb_emergency", prepare(emergency), "id=?", idStr);

        return count > 0;
    }

    public boolean remove(long id) {
        String[] idStr = new String[] {String.valueOf(id)};
        int count = db.delete("tb_emergency", "id=?", idStr);

        return count > 0;
    }

    public EmergencyRecord find(long id) {
        Cursor c = db.query("tb_emergency", null, "id=" + id, null, null , null , null);
        EmergencyRecord e = null;

        if (c.moveToFirst())
            e = fromRecord(c);

        return e;
    }

    public List<EmergencyRecord> findAll() {
        Cursor c = db.query("tb_emergency", null, null, null, null, null, "id DESC");
        List<EmergencyRecord> emergencies = null;

        if (c.moveToFirst()) {
            emergencies = new ArrayList<>();

            do {
                emergencies.add(fromRecord(c));
            } while (c.moveToNext());
        }

        return emergencies;
    }

    @Override
    public void close() {
        if (db.isOpen())
            db.close();
    }

    private ContentValues prepare(EmergencyRecord emergency) {
        ContentValues values = new ContentValues();
        values.put("id", emergency.getId());
        values.put("date_time", emergency.getDateTime().getTime());
        values.put("status", emergency.getStatus().ordinal());
        values.put("attachment", emergency.getAttachment());

        String location = emergency.getLocation();

        if (location != null)
            values.put("location", location);

        return values;
    }

    private EmergencyRecord fromRecord(Cursor c) {
        EmergencyRecord e = new EmergencyRecord();
        e.setId(c.getInt(c.getColumnIndex("id")));
        e.setDateTime(new Date(c.getLong(c.getColumnIndex("date_time"))));
        e.setLocation(c.getString(c.getColumnIndex("location")));
        e.setAttachment(c.getInt(c.getColumnIndex("attachment")));
        e.setStatus(EmergencyRecord.Status.valueOf(c.getInt(c.getColumnIndex("status"))));

        return e;
    }
}
