package com.farhanapps.HitChat.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.farhanapps.HitChat.Models.ContactModel;
import com.farhanapps.HitChat.utils.DatabaseUtils;
import com.farhanapps.HitChat.utils.MySql;
import com.farhanapps.HitChat.utils.NumberUtils;
import com.farhanapps.HitChat.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class DatabaseHandler extends SQLiteOpenHelper {

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.farhanapps.HitChat/databases/";

    private static String DB_NAME = "chatts.db";

    private SQLiteDatabase myDataBase;

    private final Context myContext;

    public static final String TAG_TABLE_MESSAGE = "messages";
    public static final String TAG_TABLE_HOMECHATS = "homechats";
    public static final String TAG_TABLE_CONTACTS = "contacts";

    public static final String TAG_CONTACT_ID = "contactId";
    public static final String TAG_CONTACT_NAME = "name";
    public static final String TAG_CONTACT_NUMBER = "number";
    public static final String TAG_CONTACT_PIC_THUMB = "image_thumb";
    public static final String TAG_CONTACT_COVER_THUMB = "cover_thumb";
    public static final String TAG_CONTACT_PIC_URL = "imageUrl";
    public static final String TAG_CONTACT_COVER_URL = "coverUrl";
    public static final String TAG_CONTACT_STATUS = "status";

    public static final String TAG_HOME_MESSAGE = "lastmsg";

    public static final String TAG_SENDER = "sender";
    public static final String TAG_ID = "id";
    public static final String TAG_RECEIVER = "receiver";
    public static final String TAG_MESSAGE = "message";
    public static final String TAG_ISRECEIVED = "isReceived";
    public static final String TAG_TIME = "time";
    public static final String TAG_DATE = "date";
    public static final String TAG_IS_SENT = "isSent";
    public static final String TAG_MESSAGE_TYPE = "type";
    public static final String TAG_FILE_THUMB = "thumbnail";
    public static final String TAG_FILE_URL = "url";
    public static final String TAG_IS_SEEN = "isSeen";
    public static final String TAG_MESSAGE_ID = "msgId";



    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     *
     * @param context
     */
    public DatabaseHandler(Context context) {

        super(context, DB_NAME, null, 2);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.myContext);

        if (dbExist) {
            //do nothing - database already exist
            if (sp.getInt("dbver", 1) < 2)// check for old database
            {
                this.getWritableDatabase();

                try {

                    copyDataBase();
                    Editor e = sp.edit();
                    e.putInt("dbver", 2);//update version
                    e.commit();
                } catch (IOException e) {

                    throw new Error("Error copying database");

                }
            }
        } else {

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getWritableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {

            //database does't exist yet.

        }

        if (checkDB != null) {

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException {

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);

    }

    @Override
    public synchronized void close() {

        if (myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.


    public ArrayList<HashMap<String, String>> getmsgs(String sender, String receiver) {
        ArrayList<HashMap<String, String>> main = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> hm;
        Cursor c = null;
        String x = TAG_ID + "," + TAG_SENDER + "," + TAG_RECEIVER + "," + TAG_MESSAGE + "," + TAG_TIME;
        String sql = "SELECT " + x + " FROM " + TAG_TABLE_MESSAGE + " WHERE (" + TAG_SENDER + "=" + sender + " AND " + TAG_RECEIVER + "=" + receiver + ") OR (" + TAG_SENDER + "=" + receiver + " AND " + TAG_RECEIVER + "=" + sender + ") ORDER BY " + TAG_ID + "";
        c = myDataBase.rawQuery(sql, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            hm = new HashMap<String, String>();
            hm.put(TAG_ID, c.getString(c.getColumnIndex(TAG_ID)));
            hm.put(TAG_TIME, c.getString(c.getColumnIndex(TAG_TIME)));
            hm.put(TAG_SENDER, c.getString(c.getColumnIndex(TAG_SENDER)));
            hm.put(TAG_RECEIVER, c.getString(c.getColumnIndex(TAG_RECEIVER)));
            hm.put(TAG_MESSAGE, c.getString(c.getColumnIndex(TAG_MESSAGE)));


            main.add(hm);


            c.moveToNext();
        }
        c.close();

        return main;
    }


    public void deleteAllChat(String he, String me) {
        String del = "DELETE FROM '" + TAG_TABLE_MESSAGE + "' WHERE (" + TAG_SENDER + "=" + he + " AND " + TAG_RECEIVER + "=" + me + ") OR (" + TAG_SENDER + "=" + me + " AND " + TAG_RECEIVER + "=" + he + ")";
        String del2 = "DELETE FROM '" + TAG_TABLE_HOMECHATS + "' WHERE " + TAG_SENDER + "='" + he + "'";
        try {
            myDataBase.execSQL(del);
            myDataBase.execSQL(del2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ContactModel getContactDetail(String number){
        ContactModel contactModel=null;
        Cursor c=myDataBase.rawQuery("SELECT DISTINCT * FROM " + TAG_TABLE_CONTACTS + " WHERE " + TAG_CONTACT_NUMBER + "='" + number+"';", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {

            contactModel = new ContactModel(
                    c.getInt(c.getColumnIndex(TAG_CONTACT_ID)),
                    c.getString(c.getColumnIndex(TAG_CONTACT_NAME)),
                    c.getString(c.getColumnIndex(TAG_CONTACT_NUMBER)),
                    c.getString(c.getColumnIndex(TAG_CONTACT_PIC_THUMB)),
                    c.getString(c.getColumnIndex(TAG_CONTACT_PIC_URL)),
                    c.getString(c.getColumnIndex(TAG_CONTACT_COVER_URL)),
                    c.getString(c.getColumnIndex(TAG_CONTACT_COVER_THUMB)),
                    c.getString(c.getColumnIndex(TAG_CONTACT_STATUS))
            );
            c.moveToNext();
        }
        return contactModel;
    }

    public ArrayList<HashMap<String, String>> getHomeList() {
        ArrayList<HashMap<String, String>> main = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> hm;
        Cursor c = null;
        String sql = "SELECT DISTINCT * FROM " + TAG_TABLE_HOMECHATS + " LEFT JOIN "+TAG_TABLE_CONTACTS+" ON "+TAG_TABLE_HOMECHATS+"."+TAG_SENDER+"="+TAG_TABLE_CONTACTS+"."+TAG_CONTACT_NUMBER+" ORDER BY "+TAG_TABLE_HOMECHATS+".id DESC";

        c = myDataBase.rawQuery(sql, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {

            hm = new HashMap<String, String>();
            if (!c.getString(c.getColumnIndex(TAG_CONTACT_NAME)).isEmpty()) {
                hm.put(TAG_CONTACT_NAME, c.getString(c.getColumnIndex(TAG_CONTACT_NAME)));
            } else {
                hm.put(TAG_CONTACT_NAME, c.getString(c.getColumnIndex(TAG_SENDER)));
            }
            hm.put(TAG_ID, c.getString(c.getColumnIndex(TAG_ID)));
            hm.put(TAG_TIME, c.getString(c.getColumnIndex(TAG_TIME)));
            hm.put(TAG_HOME_MESSAGE, c.getString(c.getColumnIndex(TAG_HOME_MESSAGE)));
            hm.put(TAG_CONTACT_NUMBER, c.getString(c.getColumnIndex(TAG_SENDER)));
            hm.put(TAG_CONTACT_PIC_THUMB, c.getString(c.getColumnIndex(TAG_CONTACT_PIC_THUMB)));
            main.add(hm);
            c.moveToNext();
        }
        c.close();
        return main;
    }

    public void insertmsg(String sender, String receiver, String msg, String time, String myNo) {
        msg = new StringUtils().filterAndi(msg);
        String x = "('" + TAG_SENDER + "','" + TAG_RECEIVER + "','" + TAG_MESSAGE + "','" + TAG_TIME + "')";
        String sql = "INSERT INTO '" + TAG_TABLE_MESSAGE + "' " + x + " VALUES('" + sender + "','" + receiver + "','" + msg + "','" + time + "')";
        if (sender.contains(myNo)) sender = receiver;


        String x2 = "('" + TAG_SENDER + "','" + TAG_HOME_MESSAGE + "','" + TAG_TIME + "')";
        String sql2 = "INSERT INTO '" + TAG_TABLE_HOMECHATS + "' " + x2 + " VALUES('" + sender + "','" + msg + "','" + time + "')";
        String del = "DELETE FROM '" + TAG_TABLE_HOMECHATS + "' WHERE " + TAG_SENDER + "='" + sender + "'";
        try {
            myDataBase.execSQL(sql);
            myDataBase.execSQL(del);
            myDataBase.execSQL(sql2);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateContact(ContactModel contactModel) {

            try {
                ContentValues values=new ContentValues();
                values.put(TAG_CONTACT_COVER_THUMB,contactModel.getContact_cover_thumb());
                values.put(TAG_CONTACT_COVER_URL,contactModel.getContact_cover());
                values.put(TAG_CONTACT_PIC_THUMB,contactModel.getContact_pic_thumb());
                values.put(TAG_CONTACT_PIC_URL,contactModel.getContact_pic());
                values.put(TAG_CONTACT_STATUS,contactModel.getContact_status());
                myDataBase.update(TAG_TABLE_CONTACTS,values,TAG_CONTACT_NUMBER+"="+contactModel.getContact_number(),null);
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    public void loadcontacts(String json, HashMap<String, String> all) {
        NumberUtils nu = new NumberUtils();
        try {
            String del2 = "DELETE FROM '" + TAG_TABLE_CONTACTS + "';";
            //myDataBase.execSQL(del2);
            JSONArray jar = new JSONArray(json);
            for (int i = 0; i < jar.length(); i++) {
                JSONObject jobj=jar.getJSONObject(i);
                int id=jobj.getInt(MySql.TAG_PHP_CONTACT_ID);
                String number= jobj.getString(MySql.TAG_PHP_CONTACT_NUMBER);
                String dp=DatabaseUtils.sqlDecodeString(jobj.getString(MySql.TAG_PHP_CONTACT_PIC));
                String dpth=DatabaseUtils.sqlDecodeString(jobj.getString(MySql.TAG_PHP_CONTACT_PIC_THUMB));
                String cover=DatabaseUtils.sqlDecodeString(jobj.getString(MySql.TAG_PHP_CONTACT_COVER));
                String coverth=DatabaseUtils.sqlDecodeString(jobj.getString(MySql.TAG_PHP_CONTACT_COVER_THUMB));
                String status=DatabaseUtils.sqlDecodeString(jobj.getString(MySql.TAG_PHP_CONTACT_STATUS));

                ContentValues values=new ContentValues();
                values.put(TAG_CONTACT_ID,id);
                values.put(TAG_CONTACT_NAME,all.get(number));
                values.put(TAG_CONTACT_NUMBER,number);
                values.put(TAG_CONTACT_PIC_URL,dp);
                values.put(TAG_CONTACT_PIC_THUMB,dpth);
                values.put(TAG_CONTACT_COVER_URL,cover);
                values.put(TAG_CONTACT_COVER_THUMB,coverth);
                values.put(TAG_CONTACT_STATUS,status);

                try {
                    myDataBase.insert(TAG_TABLE_CONTACTS, null, values);
                }catch (Exception e){
                    e.printStackTrace();
                    try {
                        myDataBase.update(TAG_TABLE_CONTACTS, values, TAG_CONTACT_NUMBER+"="+number, null);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
                //String x = "(" + TAG_CONTACT_NAME + "," + TAG_CONTACT_NUMBER + "," + TAG_CONTACT_PIC_URL+ "," + TAG_CONTACT_PIC_THUMB+ "," + TAG_CONTACT_COVER_URL+ "," + TAG_CONTACT_COVER_THUMB+ "," + TAG_CONTACT_STATUS+ "," + TAG_CONTACT_ID+ ")";

                //String sql = "INSERT INTO " + TAG_TABLE_CONTACTS + " " + x + " VALUES('" + all.get(number) + "','" + number + "','" +dp + "','" + dpth + "','" + cover + "','" + coverth + "','" + status + "'," + id +")";
                //myDataBase.execSQL(sql);

            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public ArrayList<HashMap<String, String>> getContacts() {
        ArrayList<HashMap<String, String>> main = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> hm;
        Cursor c = null;
        String x = TAG_CONTACT_NAME + "," + TAG_CONTACT_NUMBER;
        String sql = "SELECT DISTINCT * FROM " + TAG_TABLE_CONTACTS + " ORDER BY " + TAG_CONTACT_NAME + ";";
        c = myDataBase.rawQuery(sql, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            hm = new HashMap<String, String>();

            hm.put(TAG_CONTACT_PIC_THUMB,c.getString(c.getColumnIndex(TAG_CONTACT_PIC_THUMB)));
            hm.put(TAG_CONTACT_NAME, c.getString(c.getColumnIndex(TAG_CONTACT_NAME)));
            hm.put(TAG_CONTACT_NUMBER, c.getString(c.getColumnIndex(TAG_CONTACT_NUMBER)));

            main.add(hm);
            c.moveToNext();
        }
        c.close();
        return main;
    }
}