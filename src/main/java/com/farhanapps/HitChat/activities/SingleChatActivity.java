package com.farhanapps.HitChat.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.farhanapps.HitChat.HtmlEditor.HtmlEditor;
import com.farhanapps.HitChat.Models.ContactModel;
import com.farhanapps.HitChat.R;
import com.farhanapps.HitChat.adapters.MsgListViewAdapter;
import com.farhanapps.HitChat.interfaces.OnMessageReceiveListener;
import com.farhanapps.HitChat.net.ConnectionDetactor;
import com.farhanapps.HitChat.services.MessageGateway;
import com.farhanapps.HitChat.utils.Constants;
import com.farhanapps.HitChat.utils.NumberUtils;
import com.farhanapps.HitChat.utils.StringUtils;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class SingleChatActivity extends AppCompatActivity {

    private MessageGateway messageGateway;
    ContactModel contactModel;
    RoundedImageView pro_pic;
    TextView title;
    TextView number;

    private Intent serviceIntent=null;
    //binding
    private boolean serviceBound=false;
    EmojiconEditText text;
    ImageButton send;
    ImageView emojiButton;
    ListView lv;
    String otherPersonNO,otherPersonName,myNumber;
    SharedPreferences sp;
    SimpleDateFormat timeFormat;
    MsgListViewAdapter ad;
    Activity act;
    HtmlEditor htmlEditor;
    //connect to the service
    private ServiceConnection serviceConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MessageGateway.ServiceBinder binder = (MessageGateway.ServiceBinder)service;
            //get service
            messageGateway = binder.getService();
            //Toast.makeText(getApplicationContext(), "binded", Toast.LENGTH_LONG).show();
            serviceBound = true;
            myNumber=messageGateway.MyNumber;
            ad=new MsgListViewAdapter(SingleChatActivity.this,messageGateway.db.getmsgs(myNumber, otherPersonNO),myNumber);
            lv.setAdapter(ad);
            messageGateway.isLiveChat=true;
            messageGateway.setOnMessageReceiveListener(new OnMessageReceiveListener() {
                @Override
                public void onMessageReceived() {
                    sp.edit().putInt(otherPersonNO, 0).apply();
                    ad=new MsgListViewAdapter(SingleChatActivity.this,messageGateway.db.getmsgs(myNumber, otherPersonNO),myNumber);
                    lv.setAdapter(ad);
                    playSound();
                }
            });

            String num=getIntent().getStringExtra(Constants.TAG_INTENT_USER_NUMBER);
            contactModel=messageGateway.db.getContactDetail(num);
            //todo : here need to check whether user avail or not

            uil.displayImage(contactModel.getContact_pic_thumb(), (ImageView) pro_pic, dip);
            uil.displayImage(contactModel.getContact_pic(), (ImageView) pro_pic, dip);

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
        if(serviceIntent==null){
            serviceIntent = new Intent(this, MessageGateway.class);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(serviceIntent);
            //Toast.makeText(getApplicationContext(), "svrs start", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onDestroy() {
        if(htmlEditor!=null)htmlEditor.stop();
        messageGateway.setOnMessageReceiveListener(null);
        messageGateway.isLiveChat=false;
        unbindService(serviceConnection);
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
        setContentView(R.layout.activity_single_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        lv=(ListView)findViewById(R.id.single_chat_lv);
        text=(EmojiconEditText)findViewById(R.id.editText);
        send=(ImageButton)findViewById(R.id.send_btn);
        emojiButton=(ImageView)findViewById(R.id.emojiBtn);
        RelativeLayout rootview=(RelativeLayout)findViewById(R.id.chat_rootview);
        Intent i=getIntent();
        otherPersonNO=i.getStringExtra(Constants.TAG_INTENT_USER_NUMBER);
        otherPersonName=i.getStringExtra(Constants.TAG_INTENT_USER_NAME);
        act=this;

        htmlEditor=new HtmlEditor(this,text);

        sp= PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putInt(otherPersonNO, 0).apply();

        View custom_view = LayoutInflater.from(this).inflate(R.layout.user_profile_app_bar,null);


        getSupportActionBar().setCustomView(custom_view);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        pro_pic=(RoundedImageView)custom_view.findViewById(R.id.user_image);
        title=(TextView)custom_view.findViewById(R.id.user_name_tv);
        number=(TextView)custom_view.findViewById(R.id.user_status_tv);


        custom_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SingleChatActivity.this, ProfileActivity.class);
                intent.putExtra(Constants.TAG_INTENT_USER_NUMBER,otherPersonNO);
                startActivity(intent);
            }
        });
        title.setText(otherPersonName);
        number.setText(otherPersonNO);

        uil = ImageLoader.getInstance();
        if (!uil.isInited()){
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                    .memoryCacheSize(41943040)
                    .diskCacheSize(104857600)
                    .threadPoolSize(10)
                    .build();
            uil.init(config);
        }


        timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (text.getText().length() < 1) return;
                else {
                    AsyncTaskCompat.executeParallel(new SendMsg());
                }
            }
        });


        EmojIconActions emojIcon=new EmojIconActions(this,rootview,text,emojiButton);
        emojIcon.ShowEmojIcon();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }else if(item.getItemId()==R.id.editing){
            if(htmlEditor==null){
                htmlEditor=new HtmlEditor(this,text);
            }else {
                htmlEditor.stop();
                htmlEditor=null;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();
        try {
            String num=getIntent().getStringExtra(Constants.TAG_INTENT_USER_NUMBER);
            contactModel=messageGateway.db.getContactDetail(num);
            uil.displayImage(contactModel.getContact_pic_thumb(), (ImageView) pro_pic, dip);
            uil.displayImage(contactModel.getContact_pic(), (ImageView) pro_pic, dip);

        }catch (Exception e){}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    public class SendMsg extends AsyncTask<String, String, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!ConnectionDetactor.isConnecting(getApplicationContext())){
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                return;
            }
        }
        @Override
        protected Boolean doInBackground(String... params) {

            if (!ConnectionDetactor.isConnecting(getApplicationContext())) {
                return false;
            }

            String textt="";
            if(htmlEditor!=null)textt=htmlEditor.getTextWithTag();
            else textt=text.getText().toString();
                if(messageGateway.sendMessage(myNumber, NumberUtils.getNumber(otherPersonNO), StringUtils.filterPHP(textt))){
                    String t=" ";
                    t= timeFormat.format(new Date()).toString();
                    messageGateway.db.insertmsg(myNumber,NumberUtils.getNumber(otherPersonNO),textt,t,myNumber);
                    return true;
                }else{
                    return false;
                }


        }
        protected void onProgressUpdate(String... p) {



        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success ){
                ad=new MsgListViewAdapter(act,messageGateway.db.getmsgs(myNumber, otherPersonNO),myNumber);
                lv.setAdapter(ad);
                text.setText("");
                //Toast.makeText(getApplicationContext(), "Message Sent.", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getApplicationContext(), "Message not sent! Retry.", Toast.LENGTH_LONG).show();
            }

        }
    }


    public class loadmsgs extends AsyncTask<String, String, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected Boolean doInBackground(String... params) {


            return true;
        }
        protected void onProgressUpdate(String... p) {
            ad=new MsgListViewAdapter(act,messageGateway.db.getmsgs(myNumber, otherPersonNO),myNumber);


        }

        @Override
        protected void onPostExecute(final Boolean success) {


                lv.setAdapter(ad);
                playSound();
                sp.edit().putInt(otherPersonNO, 0).commit();

        }
    }

    public void playSound(){
        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            AssetFileDescriptor afd = getAssets().openFd("msgcome.mp3");
            mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
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
}
