package com.farhanapps.HitChat.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.os.AsyncTaskCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.farhanapps.HitChat.Async.GetContactDetailsToDB;
import com.farhanapps.HitChat.R;
import com.farhanapps.HitChat.activities.ProfileActivity;
import com.farhanapps.HitChat.database.DatabaseHandler;
import com.farhanapps.HitChat.interfaces.ContactListener;
import com.farhanapps.HitChat.utils.Constants;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by farhan on 02-04-2016.
 *
 */
public class MainChatListViewAdapter extends BaseAdapter {
    private Activity activity;
    private static LayoutInflater inflater;
    ArrayList<HashMap<String,String>> arrayList;
    ContactListener listener;

    SharedPreferences sp;
    public MainChatListViewAdapter(Activity context, ArrayList<HashMap<String, String>> arrayList) {
        this.activity = context;
        this.arrayList = arrayList;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sp= PreferenceManager.getDefaultSharedPreferences(context);
        uil = ImageLoader.getInstance();
        if (!uil.isInited()){
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                    .memoryCacheSize(41943040)
                    .diskCacheSize(104857600)
                    .threadPoolSize(10)
                    .build();
            uil.init(config);
        }


    }

    public void setConteactListener(ContactListener conteactListener){
        this.listener=conteactListener;
    }
    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public String getNum(int i){

        return arrayList.get(i).get(DatabaseHandler.TAG_CONTACT_NUMBER);
    }
    ImageLoader uil;
    DisplayImageOptions dip = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();
    holder h;
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if(view==null){
            view=inflater.inflate(R.layout.main_chat_list_item, null);
            h=new holder();
            h.user_pic=(RoundedImageView)view.findViewById(R.id.user_image);
            h.name=(TextView)view.findViewById(R.id.user_name_tv);
            h.message=(TextView)view.findViewById(R.id.last_msg_tv);
            h.time=(TextView)view.findViewById(R.id.msg_time_tv);
            h.count=(TextView)view.findViewById(R.id.msg_count_tv);


            view.setTag(h);
        }else
        {
            h=(holder)view.getTag();
        }
        final HashMap<String,String> hm=arrayList.get(i);
        uil.displayImage(hm.get(DatabaseHandler.TAG_CONTACT_PIC_THUMB), (ImageView) h.user_pic, dip);
        //set listener
        if(listener!=null)listener.getDetail(hm.get(DatabaseHandler.TAG_CONTACT_NUMBER));
        h.user_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra(Constants.TAG_INTENT_USER_NUMBER, hm.get(DatabaseHandler.TAG_CONTACT_NUMBER));
                activity.startActivity(intent);
            }
        });
        h.name.setText(hm.get(DatabaseHandler.TAG_CONTACT_NAME));
        h.message.setText(Html.fromHtml(hm.get(DatabaseHandler.TAG_HOME_MESSAGE)));
        h.time.setText(hm.get(DatabaseHandler.TAG_TIME));

        if(sp.getInt(hm.get(DatabaseHandler.TAG_CONTACT_NUMBER), 0)>0){
            h.count.setVisibility(View.VISIBLE);
            h.count.setText(""+sp.getInt(hm.get(DatabaseHandler.TAG_CONTACT_NUMBER), 0)+"");
        }else{
            h.count.setVisibility(View.GONE);
        }
        return view;
    }

    public void setData(ArrayList<HashMap<String, String>> arrayList){
        this.arrayList=arrayList;

        notifyDataSetChanged();
    }

    class holder {
        RoundedImageView user_pic;
        TextView name,message,time,count;
    }
}
