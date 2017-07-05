package com.farhanapps.HitChat.activities;

import android.app.ProgressDialog;
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
import android.support.design.widget.Snackbar;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.farhanapps.HitChat.Models.ContactModel;
import com.farhanapps.HitChat.R;
import com.farhanapps.HitChat.adapters.MainChatListViewAdapter;
import com.farhanapps.HitChat.interfaces.OnMessageReceiveListener;
import com.farhanapps.HitChat.net.ConnectionDetactor;
import com.farhanapps.HitChat.services.MessageGateway;
import com.farhanapps.HitChat.utils.Constants;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.security.spec.ECField;

public class ProfileActivity extends AppCompatActivity {

    RoundedImageView user_pic;
    boolean self;
    ImageView cover_pic;
    TextView name_tv,number_tv,status_tv;
    String num;
    ContactModel contactModel;
    private MessageGateway messageGateway;
    private Intent serviceIntent = null;
    //binding
    private boolean serviceBound = false;

    //connect to the service
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MessageGateway.ServiceBinder binder = (MessageGateway.ServiceBinder) service;
            //get service
            messageGateway = binder.getService();
            //Toast.makeText(getApplicationContext(), "binded", Toast.LENGTH_LONG).show();
            serviceBound = true;
            setUpData();

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
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(serviceIntent);
            //Toast.makeText(getApplicationContext(), "svrs start", Toast.LENGTH_LONG).show();
        }
        Log.i("pointer", "start");
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        try {
            unbindService(serviceConnection);
        } catch (Exception e) {
        }
        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        user_pic=(RoundedImageView)findViewById(R.id.user_pic);
        cover_pic=(ImageView)findViewById(R.id.user_cover);

        num=getIntent().getStringExtra(Constants.TAG_INTENT_USER_NUMBER);
        final SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);
        self=num.equals(pref.getString(Constants.TAG_MY_NUMBER, ""));


        user_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProfileActivity.this,ProfileImageViewer.class);

                if (self) {
                    intent.putExtra(Constants.TAG_IMAGE_TYPE,Constants.TAG_IMAGE_TYPE_PIC);
                    intent.putExtra(Constants.TAG_INTENT_USER_IMAGE_URL,pref.getString(Constants.TAG_PREF_IMAGE_URL,""));
                    intent.putExtra(Constants.TAG_INTENT_USER_NAME,pref.getString(Constants.TAG_PREF_NAME,""));
                    intent.putExtra(Constants.TAG_INTENT_USER_NUMBER,pref.getString(Constants.TAG_MY_NUMBER,""));
                } else {
                    intent.putExtra(Constants.TAG_IMAGE_TYPE,Constants.TAG_IMAGE_TYPE_PIC);
                    intent.putExtra(Constants.TAG_INTENT_USER_IMAGE_URL,contactModel.getContact_pic());
                    intent.putExtra(Constants.TAG_INTENT_USER_NAME,contactModel.getContact_name());
                    intent.putExtra(Constants.TAG_INTENT_USER_NUMBER,contactModel.getContact_number());
                }
                startActivity(intent);

            }
        });

        cover_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProfileActivity.this,ProfileImageViewer.class);
                if (self) {
                    intent.putExtra(Constants.TAG_IMAGE_TYPE,Constants.TAG_IMAGE_TYPE_COVER);
                    intent.putExtra(Constants.TAG_INTENT_USER_IMAGE_URL,pref.getString(Constants.TAG_PREF_COVER_URL,""));
                    intent.putExtra(Constants.TAG_INTENT_USER_NAME,pref.getString(Constants.TAG_PREF_NAME,""));
                    intent.putExtra(Constants.TAG_INTENT_USER_NUMBER,pref.getString(Constants.TAG_MY_NUMBER,""));
                } else {
                    intent.putExtra(Constants.TAG_IMAGE_TYPE,Constants.TAG_IMAGE_TYPE_COVER);
                    intent.putExtra(Constants.TAG_INTENT_USER_IMAGE_URL,contactModel.getContact_cover());
                    intent.putExtra(Constants.TAG_INTENT_USER_NAME,contactModel.getContact_name());
                    intent.putExtra(Constants.TAG_INTENT_USER_NUMBER,contactModel.getContact_number());
                }
                startActivity(intent);
            }
        });

        name_tv=(TextView)findViewById(R.id.user_name_tv);
        number_tv=(TextView)findViewById(R.id.user_phone_tv);
        status_tv=(TextView)findViewById(R.id.user_status_tv);



        uil = ImageLoader.getInstance();
        if (!uil.isInited()){
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                    .memoryCacheSize(41943040)
                    .diskCacheSize(104857600)
                    .threadPoolSize(10)
                    .build();
            uil.init(config);
        }
    }

    ImageLoader uil;
    DisplayImageOptions dip = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();


    void setUpData(){


        if (!self) {
            contactModel=messageGateway.db.getContactDetail(num);
            //todo : here need to check whether user avail or not
            if(contactModel!=null) {
                uil.displayImage(contactModel.getContact_pic_thumb(), (ImageView) user_pic, dip);
                uil.displayImage(contactModel.getContact_cover_thumb(), (ImageView) cover_pic, dip);
                uil.displayImage(contactModel.getContact_pic(), (ImageView) user_pic, dip);
                uil.displayImage(contactModel.getContact_cover(), (ImageView) cover_pic, dip);
                number_tv.setText("" + num);

                if (!contactModel.getContact_name().isEmpty()) {
                    name_tv.setText(contactModel.getContact_name());
                    if (contactModel.getContact_status().equals("null")||contactModel.getContact_status().isEmpty()) {
                        status_tv.setText("No Status");
                    } else {
                        status_tv.setText(contactModel.getContact_status());
                    }
                } else {
                    name_tv.setVisibility(View.GONE);

                }
            }
        } else {

            SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


            uil.displayImage(preferences.getString(Constants.TAG_PREF_IMAGE_THUMB,""), (ImageView) user_pic, dip);
            uil.displayImage(preferences.getString(Constants.TAG_PREF_COVER_THUMB,""), (ImageView) cover_pic, dip);
            uil.displayImage(preferences.getString(Constants.TAG_PREF_IMAGE_URL,""), (ImageView) user_pic, dip);
            uil.displayImage(preferences.getString(Constants.TAG_PREF_COVER_URL, ""), (ImageView) cover_pic, dip);

            name_tv.setText(preferences.getString(Constants.TAG_PREF_NAME, "NaN"));
            number_tv.setText("" + num);

            if (preferences.getString(Constants.TAG_PREF_STATUS, "").equals("null")||preferences.getString("mystatus", "").isEmpty()) {
                status_tv.setText("No Status");
            } else {
                status_tv.setText(preferences.getString("mystatus",""));
            }


            name_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View view1=LayoutInflater.from(getApplicationContext()).inflate(R.layout.editbox,null);
                    final EditText et= (EditText)view1.findViewById(R.id.edit_box);
                    et.setText(name_tv.getText());
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setTitle("Name:")
                            .setView(view1)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    newstatus = et.getText().toString();
                                    AsyncTaskCompat.executeParallel(new updateName(), messageGateway.MyNumber + "", et.getText().toString());

                                }
                            })
                            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create()
                            .show();
                }
            });
                status_tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View view1=LayoutInflater.from(getApplicationContext()).inflate(R.layout.editbox,null);
                        final EditText et= (EditText)view1.findViewById(R.id.edit_box);
                        et.setText(status_tv.getText());
                        AlertDialog.Builder builder=new AlertDialog.Builder(ProfileActivity.this);
                        builder.setTitle("New Status")
                                .setView(view1)
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        newstatus = et.getText().toString();
                                        AsyncTaskCompat.executeParallel(new updateStatus(),messageGateway.MyNumber+"",et.getText().toString());

                                    }
                                })
                                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .create()
                                .show();
                    }
                });
            }


    }

    String newstatus;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if(self){
                SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                uil.displayImage(preferences.getString(Constants.TAG_PREF_IMAGE_THUMB,""), (ImageView) user_pic, dip);
                uil.displayImage(preferences.getString(Constants.TAG_PREF_COVER_THUMB,""), (ImageView) cover_pic, dip);
                uil.displayImage(preferences.getString(Constants.TAG_PREF_IMAGE_URL,""), (ImageView) user_pic, dip);
                uil.displayImage(preferences.getString(Constants.TAG_PREF_COVER_URL, ""), (ImageView) cover_pic, dip);
            }else {
                contactModel = messageGateway.db.getContactDetail(num);
                uil.displayImage(contactModel.getContact_pic_thumb(), (ImageView) user_pic, dip);
                uil.displayImage(contactModel.getContact_cover_thumb(), (ImageView) cover_pic, dip);
                uil.displayImage(contactModel.getContact_pic(), (ImageView) user_pic, dip);
                uil.displayImage(contactModel.getContact_cover(), (ImageView) cover_pic, dip);
            }
        }catch (Exception e){}
    }

    public class updateStatus extends AsyncTask<String, String, Boolean> {

        ProgressDialog pd=new ProgressDialog(ProfileActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setIndeterminate(true);
            pd.setMessage("Updating...");
            pd.show();

        }
        @Override
        protected Boolean doInBackground(String... params) {
            if (!ConnectionDetactor.isConnecting(getApplicationContext())) {
                return false;
            }
            boolean ismsg=messageGateway.communicate.updateStatus(params[0], params[1]);
            return ismsg;
        }
        protected void onProgressUpdate(String... p) {

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            try {
                pd.dismiss();
                if (success) {
                    status_tv.setText(newstatus);
                    if(self){
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("mystatus",newstatus).apply();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error updating status", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){

            }

        }
    }

    public class updateName extends AsyncTask<String, String, Boolean> {

        ProgressDialog pd=new ProgressDialog(ProfileActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setIndeterminate(true);
            pd.setMessage("Updating...");
            pd.show();

        }
        @Override
        protected Boolean doInBackground(String... params) {
            if (!ConnectionDetactor.isConnecting(getApplicationContext())) {
                return false;
            }
            boolean ismsg=messageGateway.communicate.updateName(params[0], params[1]);
            return ismsg;
        }
        protected void onProgressUpdate(String... p) {

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            try {
                pd.dismiss();
                if (success) {
                    name_tv.setText(newstatus);
                    if(self){
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString(Constants.TAG_PREF_NAME,newstatus).apply();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error updating status", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){

            }

        }
    }
}
