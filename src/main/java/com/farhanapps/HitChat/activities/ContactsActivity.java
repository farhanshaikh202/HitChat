package com.farhanapps.HitChat.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.farhanapps.HitChat.R;
import com.farhanapps.HitChat.adapters.ContactListViewAdapter;
import com.farhanapps.HitChat.interfaces.OnContactSyncListener;
import com.farhanapps.HitChat.services.MessageGateway;
import com.farhanapps.HitChat.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactsActivity extends AppCompatActivity {

    private MessageGateway messageGateway;
    private Intent serviceIntent = null;
    //binding
    private boolean serviceBound = false;
    ContactListViewAdapter ad;
    ListView lv;
    SharedPreferences sp;

    ProgressBar pb;

    //connect to the service
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MessageGateway.ServiceBinder binder = (MessageGateway.ServiceBinder) service;
            //get service
            messageGateway = binder.getService();
            //Toast.makeText(getApplicationContext(), "binded", Toast.LENGTH_LONG).show();
            serviceBound = true;
            ArrayList<HashMap<String, String>> clist = messageGateway.db.getContacts();
            if (clist != null && clist.size() > 0) {
                ad = new ContactListViewAdapter(ContactsActivity.this, clist);
                lv.setAdapter(ad);
            }
            messageGateway.setOnContactSync(new OnContactSyncListener() {
                @Override
                public void syncStart() {
                    pb.setVisibility(View.VISIBLE);
                }

                @Override
                public void syncStop() {
                    pb.setVisibility(View.GONE);
                    ad = new ContactListViewAdapter(ContactsActivity.this, messageGateway.db.getContacts());
                    lv.setAdapter(ad);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            finish();
        }
    };

    //start and bind the service when the activity starts
    @Override
    protected void onStart() {
        super.onStart();
        if (serviceIntent == null) {
            serviceIntent = new Intent(this, MessageGateway.class);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(serviceIntent);
            //Toast.makeText(getApplicationContext(), "svrs start", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        lv = (ListView) findViewById(R.id.contact_list);
        pb = (ProgressBar) findViewById(R.id.progressBar);


        /*Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        //String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.Contacts._ID}, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        if(sp.getInt("allcontacts", 0)!=cursor.getCount()){

        }
        cursor.close();*/

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int arg2,
                                    long arg3) {
                finish();
                Intent i = new Intent(getApplicationContext(), SingleChatActivity.class);
                i.putExtra(Constants.TAG_INTENT_USER_NAME, ((TextView) v.findViewById(R.id.user_name_tv)).getText().toString());
                i.putExtra(Constants.TAG_INTENT_USER_NUMBER, ((TextView) v.findViewById(R.id.last_msg_tv)).getText().toString());
                startActivity(i);
            }

        });


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.refreshcontact) {
            messageGateway.SyncContacts();
        }
        return super.onOptionsItemSelected(item);
    }


}
