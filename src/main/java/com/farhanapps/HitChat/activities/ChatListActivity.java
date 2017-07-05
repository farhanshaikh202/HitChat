package com.farhanapps.HitChat.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.farhanapps.HitChat.Async.GetContactDetailsToDB;
import com.farhanapps.HitChat.Models.ContactModel;
import com.farhanapps.HitChat.R;
import com.farhanapps.HitChat.adapters.MainChatListViewAdapter;
import com.farhanapps.HitChat.interfaces.ContactListener;
import com.farhanapps.HitChat.interfaces.OnMessageReceiveListener;
import com.farhanapps.HitChat.net.ConnectionDetactor;
import com.farhanapps.HitChat.services.MessageGateway;
import com.farhanapps.HitChat.utils.Constants;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class ChatListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    RoundedImageView user_pic;
    ImageView cover_pic;
    TextView name_tv,status_tv;
    String myNumber;
    MainChatListViewAdapter ad;
    SharedPreferences preferences;
    Activity act;
    ListView lv;ContactModel contactModel;
    SharedPreferences sp;
    private MessageGateway messageGateway;
    private Intent serviceIntent = null;
    //binding
    private boolean serviceBound = false;
    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MessageGateway.ServiceBinder binder = (MessageGateway.ServiceBinder) service;
            //get service
            messageGateway = binder.getService();
            //Toast.makeText(getApplicationContext(), "binded", Toast.LENGTH_LONG).show();
            serviceBound = true;

            ad = new MainChatListViewAdapter(act, messageGateway.db.getHomeList());
            ad.setConteactListener(new ContactListener() {
                @Override
                public void getDetail(String contact) {
                    AsyncTaskCompat.executeParallel(new GetContactDetailsToDB(messageGateway.communicate, getApplicationContext(), messageGateway.db), contact);
                }
            });
            lv.setAdapter(ad);
            messageGateway.setOnMessageReceiveListener(new OnMessageReceiveListener() {
                @Override
                public void onMessageReceived() {
                    ad.setData(messageGateway.db.getHomeList());
                }
            });

            AsyncTaskCompat.executeParallel(new LoadMyProfile());
            Log.i("onserviceconned","here");

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

        if (serviceIntent == null) {
            serviceIntent = new Intent(this, MessageGateway.class);
            bindService(serviceIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(serviceIntent);
            //Toast.makeText(getApplicationContext(), "svrs start", Toast.LENGTH_LONG).show();
        }
        Log.i("pointer", "start");
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        try {
            unbindService(musicConnection);
        } catch (Exception e) {
        }
        super.onDestroy();
    }
    ImageLoader uil;
    DisplayImageOptions dip = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        myNumber = sp.getString("accountno", "0");
        if (!sp.contains("isloggedin") || !sp.getBoolean("isloggedin", false)) {
            finish();
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
            return;
        }
        Log.i("pointer", "create");
        setContentView(R.layout.activity_chat_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatListActivity.this, ContactsActivity.class));
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //                      .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerview = navigationView.inflateHeaderView(R.layout.nav_header_chat_list);
        user_pic=(RoundedImageView)headerview.findViewById(R.id.my_pro_pic);
        cover_pic=(ImageView)headerview.findViewById(R.id.my_cover);
        name_tv=(TextView)headerview.findViewById(R.id.profileName);
        status_tv=(TextView)headerview.findViewById(R.id.profileStatus);

        headerview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ChatListActivity.this, ProfileActivity.class);
                intent.putExtra(Constants.TAG_INTENT_USER_NUMBER,myNumber);
                startActivity(intent);
            }
        });
        act = this;
        uil = ImageLoader.getInstance();
        if (!uil.isInited()){
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                    .memoryCacheSize(41943040)
                    .diskCacheSize(104857600)
                    .threadPoolSize(10)
                    .build();
            uil.init(config);
        }

        preferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        updateMyProfile();

        lv = (ListView) findViewById(R.id.main_chat_list);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int arg2,
                                    long arg3) {

                Intent i = new Intent(getApplicationContext(), SingleChatActivity.class);
                i.putExtra(Constants.TAG_INTENT_USER_NAME, ((TextView) v.findViewById(R.id.user_name_tv)).getText().toString());
                i.putExtra(Constants.TAG_INTENT_USER_NUMBER, ad.getNum(arg2));
                startActivity(i);
            }

        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, final View v,
                                           final int arg2, long arg3) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(ChatListActivity.this);

                builder1.setTitle("Delete chat ?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        messageGateway.db.deleteAllChat(ad.getNum(arg2), myNumber);
                                        ad = new MainChatListViewAdapter(act, messageGateway.db.getHomeList());
                                        lv.setAdapter(ad);
                                    }

                                }.run();

                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                builder1.create().show();

                return false;
            }

        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        try {

            if (messageGateway != null) {
                ad .setData(messageGateway.db.getHomeList());
                messageGateway.setOnMessageReceiveListener(new OnMessageReceiveListener() {
                    @Override
                    public void onMessageReceived() {
                        ad.setData(messageGateway.db.getHomeList());
                        Log.i("message come","on recieved");
                    }
                });
                Log.i("onresume","also called");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {

            if (messageGateway != null) {
                AsyncTaskCompat.executeParallel(new LoadMyProfile());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            updateMyProfile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    void updateMyContact(ContactModel contactModel){
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(Constants.TAG_PREF_NAME,contactModel.getContact_name());
        edit.putString(Constants.TAG_PREF_STATUS,contactModel.getContact_status());
        edit.putString(Constants.TAG_PREF_IMAGE_THUMB,contactModel.getContact_pic_thumb());
        edit.putString(Constants.TAG_PREF_IMAGE_URL,contactModel.getContact_pic())
                .putString(Constants.TAG_PREF_COVER_THUMB,contactModel.getContact_cover_thumb())
                .putString(Constants.TAG_PREF_COVER_URL, contactModel.getContact_cover());
        edit.apply();

    }

    void updateMyProfile(){
        uil.displayImage(preferences.getString(Constants.TAG_PREF_IMAGE_THUMB,""), (ImageView) user_pic, dip);
        uil.displayImage(preferences.getString(Constants.TAG_PREF_COVER_THUMB,""), (ImageView) cover_pic, dip);
        uil.displayImage(preferences.getString(Constants.TAG_PREF_IMAGE_URL,""), (ImageView) user_pic, dip);
        uil.displayImage(preferences.getString(Constants.TAG_PREF_COVER_URL,""), (ImageView) cover_pic, dip);

        name_tv.setText(preferences.getString(Constants.TAG_PREF_NAME,""));
        status_tv.setText(preferences.getString(Constants.TAG_PREF_STATUS,""));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            sp.edit().putBoolean("isloggedin", false).commit();
            finish();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_contacts) {
            startActivity(new Intent(ChatListActivity.this, ContactsActivity.class));
        } else if (id == R.id.nav_new_chat) {
            startActivity(new Intent(ChatListActivity.this, ContactsActivity.class));
        }else if(id==R.id.nav_manage){
            Intent intent=new Intent(ChatListActivity.this, ProfileActivity.class);
            intent.putExtra(Constants.TAG_INTENT_USER_NUMBER,myNumber);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class LoadMyProfile extends AsyncTask<Void,Intent,Boolean>{

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean){
                updateMyProfile();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(!ConnectionDetactor.isConnecting(getApplicationContext())){
                return false;
            }
            ContactModel contactModel=messageGateway.communicate.getContactInfo(messageGateway.MyNumber);
            if(contactModel!=null) {
                updateMyContact(contactModel);
                return true;
            }else
            return false;
        }
    }
}
