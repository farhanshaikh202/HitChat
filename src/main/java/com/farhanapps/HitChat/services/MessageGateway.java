package com.farhanapps.HitChat.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;
import android.widget.Toast;

import com.farhanapps.HitChat.database.DatabaseHandler;
import com.farhanapps.HitChat.interfaces.OnContactSyncListener;
import com.farhanapps.HitChat.interfaces.OnMessageReceiveListener;
import com.farhanapps.HitChat.interfaces.OnMessageSentListener;
import com.farhanapps.HitChat.net.Communicate;
import com.farhanapps.HitChat.net.ConnectionDetactor;
import com.farhanapps.HitChat.utils.Constants;
import com.farhanapps.HitChat.utils.NumberUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by farhan on 16-04-2016.
 * message gateway service
 */
public class MessageGateway extends Service {

    public Communicate communicate ;
    Context context;
    private IBinder serviceBinder = new ServiceBinder();
    public String MyNumber;
    public boolean isLiveChat=false;
    OnMessageReceiveListener messageReceiveListener;
    OnMessageSentListener messageSentListener;
    OnContactSyncListener contactSyncListener;

    public DatabaseHandler db;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    public class ServiceBinder extends Binder {
        public MessageGateway getService() {
            return MessageGateway.this;
        }
    }

    public void setOnMessageReceiveListener(OnMessageReceiveListener messageReceiveListener){
        this.messageReceiveListener=messageReceiveListener;
    }
    public void setMessageSentListener(OnMessageSentListener messageSentListener){
        this.messageSentListener=messageSentListener;
    }
    @Override
    public void onCreate() {
        super.onCreate();
       /* IntentFilter notificationFilter = new IntentFilter("android.intent.action.PHONE_STATE");
        registerReceiver(onPhoneStateChange, notificationFilter);*/

        MyContentObserver observer = new MyContentObserver();
        getApplicationContext().getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, observer);
    }
    private class MyContentObserver extends ContentObserver {

        public MyContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.i("CHATTS", "CONTACT CHANGE RECEIVED");
            SyncContacts();
        }

    }

    /*BroadcastReceiver onPhoneStateChange = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intenta) {
            Log.i("CHATTS","CONTACT CHANGE RECEIVED");
            SyncContacts();
        }
    };*/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        context = getApplicationContext();
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.TAG_IS_LOGGED_IN, false)) {

            MyNumber=PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.TAG_MY_NUMBER, "");

            if(db==null) {
                db = new DatabaseHandler(getApplicationContext());
                try {
                    db.createDataBase();
                } catch (IOException ioe) {
                    throw new Error("Unable to create database");
                }
                try {
                    db.openDataBase();
                } catch (SQLException sqle) {
                    throw sqle;
                }
            }
            if (communicate==null) {
                communicate = new Communicate(getApplicationContext());
                AsyncTaskCompat.executeParallel(new getMessages());
            }
        }else stopSelf();
        return super.onStartCommand(intent, flags, startId);

    }

    public boolean sendMessage(String sender,String receiver,String message){
        return communicate.sendMsg(sender,receiver,message);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(db!=null)db.close();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void playSound(){
        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            Uri defaultRingtoneUri =RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mediaPlayer.setDataSource(context, defaultRingtoneUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp)
                {
                    mp.release();
                }
            });
            mediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public class getMessages extends AsyncTask<String, String, Boolean> {

        boolean isConnect=true;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.i("MessageGateway","getting msgs");
        }
        @Override
        protected Boolean doInBackground(String... params) {
            if (!ConnectionDetactor.isConnecting(getApplicationContext())) {
                isConnect=false;
                stopSelf();
                return false;
            }
            boolean ismsg=communicate.getmsgs(context,MyNumber,db);
            return ismsg;
        }
        protected void onProgressUpdate(String... p) {

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){
                    if(!isLiveChat)playSound();
                    if(messageReceiveListener!=null)messageReceiveListener.onMessageReceived();
            }

            if(isConnect)
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AsyncTaskCompat.executeParallel(new getMessages());
                    }
                },500);


        }
    }


    public class sendMessage extends AsyncTask<String, String, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!ConnectionDetactor.isConnecting(getApplicationContext())) {
                stopSelf();

                return;
            }
        }
        @Override
        protected Boolean doInBackground(String... params) {

            boolean ismsg=communicate.sendMsg(params[0], params[1], params[2]);
            return ismsg;
        }
        protected void onProgressUpdate(String... p) {

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){
                if(messageSentListener!=null)messageSentListener.onMessageSent();
            }else {
            }

        }
    }
    public void SyncContacts(){
        AsyncTaskCompat.executeParallel(new SyncContacts());
    }
    public void setOnContactSync(OnContactSyncListener onContactSync){
        this.contactSyncListener=onContactSync;
    }
    public class SyncContacts extends AsyncTask<String, String, Boolean> {


        HashMap<String, String> hm;
        @Override
        protected void onPreExecute() {

            if(contactSyncListener!=null)contactSyncListener.syncStart();
            if(!ConnectionDetactor.isConnecting(getApplicationContext())){
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                if(contactSyncListener!=null)contactSyncListener.syncStop();
                return;
            }else super.onPreExecute();

        }
        @Override
        protected Boolean doInBackground(String... params) {
            if (!ConnectionDetactor.isConnecting(getApplicationContext())) {
                return false;
            }
            ArrayList<HashMap<String ,String >> arr=new ArrayList<>();
            String list="0000000000";
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            //String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER;
            Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,   ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.Contacts._ID}, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            //sp.edit().putInt("allcontacts",cursor.getCount()).commit();
            if(cursor.getCount()>0){
                cursor.moveToFirst();
                hm=new HashMap<String, String>();
                while (!cursor.isAfterLast()) {

                    String contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    //int phoneContactID = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                    //int contactID = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    //Log.d("con ", "name " + contactName + " " + " PhoeContactID " + phoneContactID + "  ContactID " + contactID)

                    list=list+","+ NumberUtils.getNumber(contactNumber);

                    hm.put(NumberUtils.getNumber(contactNumber), contactName);

                    cursor.moveToNext();
                }
                cursor.close();
                cursor = null;
                publishProgress("writing...");
                write(list, MyNumber + ".txt");
                publishProgress("writing...done");
                String storage= Environment.getExternalStorageDirectory().getPath();
                publishProgress("uploading...");
                String json= Communicate.uploadFile(storage + "/" + MyNumber + ".txt");
                publishProgress("uploading...done");
                if(json!=null && !json.isEmpty())
                db.loadcontacts(json,hm);

                new File(storage + "/" + MyNumber + ".txt").delete();


            }
            else return false;

            //------------------------------------



            return true;
        }
        protected void onProgressUpdate(String... p) {
            Log.i("MessGateway",p[0]);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(contactSyncListener!=null)contactSyncListener.syncStop();
        }
    }

    public void write( String data,String fileName) {
        File root = Environment.getExternalStorageDirectory();
        File outDir = new File(root.getAbsolutePath());
        if (!outDir.isDirectory()) {
            outDir.mkdir();
        }
        try {
            if (!outDir.isDirectory()) {
                throw new IOException(
                        "Unable to create directory EZ_time_tracker. Maybe the SD card is mounted?");
            }
            File outputFile = new File(outDir, fileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(data);

            writer.close();
        } catch (IOException e) {
            Log.w("eztt", e.getMessage(), e);

        }

    }
}
