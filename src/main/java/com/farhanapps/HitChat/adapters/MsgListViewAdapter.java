package com.farhanapps.HitChat.adapters;



import java.util.ArrayList;
import java.util.HashMap;


import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.farhanapps.HitChat.R;
import com.farhanapps.HitChat.database.DatabaseHandler;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;


public class MsgListViewAdapter extends BaseAdapter {
 
    private Activity activity;
    
    private static LayoutInflater inflater;
    String myNo;
    ArrayList<HashMap<String, String>> all = new ArrayList<HashMap<String, String>>();
    
    
    public MsgListViewAdapter(Activity a,ArrayList<HashMap<String, String>> alll,String no) {
        activity = a;
       all=alll;
       myNo=no;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       
    }
 
    public int getCount() {
        return all.size();
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }

  ViewHolder viewHolder;
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi=convertView;
       
        if(convertView==null){
            viewHolder=new ViewHolder();
            vi = inflater.inflate(R.layout.msg_item, null);
            viewHolder.left=(LinearLayout)vi.findViewById(R.id.msgviewleft);
            viewHolder.right=(LinearLayout)vi.findViewById(R.id.msgviewright);
            viewHolder.msgl=(EmojiconTextView)viewHolder.left.findViewById(R.id.msgMain);
            viewHolder.timel=(TextView)viewHolder.left.findViewById(R.id.msgTime);
            viewHolder.msgr=(EmojiconTextView)viewHolder.right.findViewById(R.id.msgMain);
            viewHolder.timer=(TextView)viewHolder.right.findViewById(R.id.msgTime);
            vi.setTag(viewHolder);
        }else viewHolder=(ViewHolder)vi.getTag();


        HashMap<String,String> item=all.get(position);
        if(item.get(DatabaseHandler.TAG_SENDER).contains(myNo)){

            viewHolder.timer.setText(item.get(DatabaseHandler.TAG_TIME));
        	viewHolder.right.setVisibility(View.VISIBLE);
        	viewHolder.left.setVisibility(View.GONE);
            viewHolder.msgr.setText(Html.fromHtml(item.get(DatabaseHandler.TAG_MESSAGE)));
        }else{
            viewHolder.timel.setText(item.get(DatabaseHandler.TAG_TIME));
        	viewHolder.left.setVisibility(View.VISIBLE);
        	viewHolder.right.setVisibility(View.GONE);
            viewHolder.msgl.setText(Html.fromHtml(item.get(DatabaseHandler.TAG_MESSAGE)));
        }

       return vi;
    }

 class ViewHolder{
     EmojiconTextView msgl,msgr;
     TextView timel,timer;
     LinearLayout left,right;
 }


}