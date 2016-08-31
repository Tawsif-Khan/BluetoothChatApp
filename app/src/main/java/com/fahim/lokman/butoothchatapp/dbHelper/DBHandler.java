package com.fahim.lokman.bluetoothchatapp.dbHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.fahim.lokman.bluetoothchatapp.contents.MessageContents;
import com.fahim.lokman.bluetoothchatapp.logger.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by tawsifkhan on 7/29/16.
 */
public class DBHandler extends SQLiteOpenHelper {

    Context context;

    private static final String DATABASE_NAME = "learningAndroid";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESSAGES = "TABLE_MESSAGES";

    private static final String KEY_ID = "KEY_ID";
    private static final String KEY_DEVICE_NAME = "KEY_DEVICE_NAME";
    private static final String KEY_MSG = "KEY_MSG";
    private static final String KEY_TIME = "KEY_TIME";
    private static final String KEY_TYPE = "KEY_TYPE";
    private static final String KEY_SENDER = "KEY_SENDER";
    private static final String KEY_RECEIVER = "KEY_RECEIVER";

    private static final String CREATE_TABLE_MESSAGES = "CREATE TABLE IF NOT EXISTS " + TABLE_MESSAGES
            + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_DEVICE_NAME + " VARCHAR," +
            KEY_MSG + " VARCHAR,"+
            KEY_TIME + " VARCHAR,"+
            KEY_TYPE + " VARCHAR," +
            KEY_SENDER + " VARCHAR," +
            KEY_RECEIVER + " VARCHAR)";

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION
        );
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ArrayList<MessageContents> getAllMessages(String sender, String receiver){
        ArrayList<MessageContents> messageContentses = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_MESSAGES+" WHERE ("+KEY_SENDER+"='"+sender+"' "  +
                "AND "+KEY_RECEIVER+"='"+receiver+"') OR ("+KEY_SENDER+"='"+receiver+"' " +
                "AND "+KEY_RECEIVER+"='"+sender+"');",null);

        /* */

        if(cursor != null && cursor.getCount()>0){
            cursor.moveToFirst();
            do{
                MessageContents messageContents = new MessageContents();
                messageContents.id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                messageContents.message = cursor.getString(cursor.getColumnIndex(KEY_MSG));
                messageContents.time = cursor.getString(cursor.getColumnIndex(KEY_TIME));
                messageContents.type = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
                messageContents.sender = cursor.getString(cursor.getColumnIndex(KEY_SENDER));
                messageContents.receiver = cursor.getString(cursor.getColumnIndex(KEY_RECEIVER));
                messageContentses.add(messageContents);
                Log.e("My_MSG",messageContents.message);
            }while (cursor.moveToNext());
        }

        return messageContentses;
    }

    public long saveMessage(String msg, String sender,String receiver,String type){
        SQLiteDatabase db = getWritableDatabase();

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String time = format.format(date);

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_MSG,msg);
        contentValues.put(KEY_SENDER,sender);
        contentValues.put(KEY_RECEIVER,receiver);
        contentValues.put(KEY_TIME,time);
        contentValues.put(KEY_TYPE,type);

        long id = db.insert(TABLE_MESSAGES,null,contentValues);
        return id;
    }

}
