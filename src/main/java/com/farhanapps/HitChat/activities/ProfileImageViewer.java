package com.farhanapps.HitChat.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.farhanapps.HitChat.Models.ContactModel;
import com.farhanapps.HitChat.R;
import com.farhanapps.HitChat.adapters.MainChatListViewAdapter;
import com.farhanapps.HitChat.interfaces.OnMessageReceiveListener;
import com.farhanapps.HitChat.interfaces.UploadListener;
import com.farhanapps.HitChat.net.JSONParser;
import com.farhanapps.HitChat.net.UploadImageToServer;
import com.farhanapps.HitChat.services.MessageGateway;
import com.farhanapps.HitChat.utils.Constants;
import com.farhanapps.HitChat.utils.StringUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.hybridsquad.android.library.CropHandler;
import org.hybridsquad.android.library.CropHelper;
import org.hybridsquad.android.library.CropParams;
import org.json.JSONObject;

import java.util.prefs.Preferences;

public class ProfileImageViewer extends AppCompatActivity implements CropHandler{

    ImageView imageView;
    ProgressBar progressBar;
    CropParams mCropParams;
    String phoneNumber;
    ImageLoader uil;
    int type;
    boolean self;
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
    DisplayImageOptions dip = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_image_viewer);
        imageView=(ImageView)findViewById(R.id.profileImage);
        progressBar=(ProgressBar)findViewById(R.id.profileImageLoading);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra(Constants.TAG_INTENT_USER_NAME));
        type=getIntent().getIntExtra(Constants.TAG_IMAGE_TYPE, 0);
        progressBar.setVisibility(View.GONE);




         uil = ImageLoader.getInstance();
        if (!uil.isInited()){
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                    .memoryCacheSize(41943040)
                    .diskCacheSize(104857600)
                    .threadPoolSize(10)
                    .build();
            uil.init(config);
        }

        phoneNumber=getIntent().getStringExtra(Constants.TAG_INTENT_USER_NUMBER);
        String url=getIntent().getStringExtra(Constants.TAG_INTENT_USER_IMAGE_URL);

        self=phoneNumber.equals(PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.TAG_MY_NUMBER, ""));
        uil.displayImage(url, imageView, dip);
        this.getCropParams();

        mCropParams.outputFormat=Bitmap.CompressFormat.PNG.toString();
        Log.i("URI",mCropParams.uri.toString());
        if(type==Constants.TAG_IMAGE_TYPE_COVER){
            mCropParams.aspectX=3;
            mCropParams.aspectY=2;
            mCropParams.outputX=500;
            mCropParams.outputY=400;
        }else {
            mCropParams.outputX=500;
            mCropParams.outputY=500;
        }




    }

    void setNewImage(){

        // MUST!! Clear Last Cached Image
        CropHelper.clearCachedCropFile(mCropParams.uri);
        AlertDialog.Builder builder=new AlertDialog.Builder(ProfileImageViewer.this);
        builder.setItems(new String[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    Intent intent = CropHelper.buildCaptureIntent(mCropParams.uri);
                    startActivityForResult(intent, CropHelper.REQUEST_CAMERA);
                } else {
                    Intent intent = CropHelper.buildCropFromGalleryIntent(mCropParams);
                    startActivityForResult(intent, CropHelper.REQUEST_CROP);
                }
            }
        });
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        try {
            unbindService(serviceConnection);
        } catch (Exception e) {
        }
        if (this.getCropParams() != null)
            CropHelper.clearCachedCropFile(mCropParams.uri);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CropHelper.handleResult(this, requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(self)
        getMenuInflater().inflate(R.menu.profile_image_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }else if(item.getItemId()==R.id.setNewImage){
            setNewImage();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPhotoCropped(Uri uri) {
        UploadImageToServer uploader=new UploadImageToServer();
        uploader.setUploadListener(new UploadListener() {
            @Override
            public void onUploadStart() {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setMax(100);
            }

            @Override
            public void onUploadProgress(int progress) {
                progressBar.setIndeterminate(false);
                progressBar.setProgress(progress);
            }

            @Override
            public void onUploadFinish(String json) {
                progressBar.setVisibility(View.GONE);
                try {
                    if(!json.isEmpty()) {
                        JSONObject jsonObject = new JSONObject(json);
                        if(jsonObject.getString("error").equals("false")) {
                            String url = jsonObject.getString("url");
                            String thumb = jsonObject.getString("thumb");

                            if (self){

                                SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                if(type==Constants.TAG_IMAGE_TYPE_PIC) {
                                    preferences.edit().putString("picurl",url).putString("picthumb",thumb).apply();
                                }else {
                                    preferences.edit().putString("coverurl",url).putString("coverthumb",thumb).apply();
                                }
                            }else{
                                ContactModel contactModel=messageGateway.db.getContactDetail(phoneNumber);

                                if (contactModel!=null) {

                                    if(type==Constants.TAG_IMAGE_TYPE_PIC) {
                                        contactModel.setContact_pic(StringUtils.filterAndi(url));
                                        contactModel.setContact_pic_thumb(StringUtils.filterAndi(thumb));
                                    }else {
                                        contactModel.setContact_cover(StringUtils.filterAndi(url));
                                        contactModel.setContact_cover_thumb(StringUtils.filterAndi(thumb));
                                    }
                                    messageGateway.db.updateContact(contactModel);
                                } else {
                                    //todo for unknown number
                                }
                            }
                            uil.displayImage(url, imageView, dip, new ImageLoadingListener() {
                                @Override
                                public void onLoadingStarted(String s, View view) {
                                    progressBar.setIndeterminate(true);
                                    progressBar.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onLoadingFailed(String s, View view, FailReason failReason) {
                                    progressBar.setVisibility(View.GONE);
                                }

                                @Override
                                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                                    progressBar.setVisibility(View.GONE);
                                }

                                @Override
                                public void onLoadingCancelled(String s, View view) {
                                    progressBar.setVisibility(View.GONE);
                                }
                            });

                        }else Toast.makeText(getApplicationContext(),"server error",Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onUploadError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        if(type==Constants.TAG_IMAGE_TYPE_PIC){
            AsyncTaskCompat.executeParallel(uploader,uri.getPath(),"pic",phoneNumber);
        }
        else if(type==Constants.TAG_IMAGE_TYPE_COVER){
            AsyncTaskCompat.executeParallel(uploader,uri.getPath(),"cover",phoneNumber);
        }

    }

    @Override
    public void onCropCancel() {

    }

    @Override
    public void onCropFailed(String message) {

    }

    @Override
    public CropParams getCropParams() {
        mCropParams = new CropParams();
        return mCropParams;
    }

    @Override
    public Activity getContext() {
        return this;
    }


}
