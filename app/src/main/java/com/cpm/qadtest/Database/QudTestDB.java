package com.cpm.qadtest.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cpm.qadtest.Constant.CommonString;
import com.cpm.qadtest.GetterSetter.UserDatum;
import com.cpm.qadtest.GetterSetter.UserDatumGetterSetter;

import java.util.ArrayList;
import java.util.List;

public class QudTestDB extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Qud_Test_DB";
    public static final int DATABASE_VERSION = 1;
    private SQLiteDatabase db;
    Context context;
    private ArrayList<UserDatum> userData;

    public QudTestDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public void open() {
        try {
            db = this.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CommonString.CREATE_TABLE_USER_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean insertUserData(UserDatumGetterSetter userData) {
        db.delete(CommonString.TABLE_USER_DATA, null, null);
        ContentValues values = new ContentValues();
        List<UserDatum> data = userData.getUserData();
         try {
            if (data.size() == 0) {
                return false;
            }

            for (int i = 0; i < data.size(); i++) {

                values.put(CommonString.KEY_USER_ID, data.get(i).getUserId());
                values.put(CommonString.KEY_USER_NAME, data.get(i).getEmployee());

                long id = db.insert(CommonString.TABLE_USER_DATA, null, values);
                if (id == -1) {
                    throw new Exception();
                }
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("Database Exception  ", ex.toString());
            return false;
        }
    }

    public ArrayList<UserDatum> getUserData() {
        Log.d("FetchingStoredata-----","------------------");
        ArrayList<UserDatum> list = new ArrayList<UserDatum>();
        Cursor dbcursor = null;

        try {
            dbcursor = db.rawQuery("SELECT * from User_Data ", null);

            if (dbcursor != null) {
                dbcursor.moveToFirst();
                while (!dbcursor.isAfterLast()) {
                    UserDatum sb = new UserDatum();
                    sb.setEmployee(dbcursor.getString(dbcursor.getColumnIndexOrThrow(CommonString.KEY_USER_NAME)));
                    sb.setUserId(dbcursor.getString(dbcursor.getColumnIndexOrThrow(CommonString.KEY_USER_ID)));

                    list.add(sb);
                    dbcursor.moveToNext();
                }
                dbcursor.close();
                return list;
            }

        } catch (Exception e) {
            Log.d("Exception when fetching",
                    e.toString());
            return list;
        }
        return list;
    }
}
