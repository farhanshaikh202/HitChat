package com.farhanapps.HitChat.HtmlEditor;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.PopupWindowCompat;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.farhanapps.HitChat.R;


/**
 * Created by farhan on 27-04-2016.
 */
public class HtmlEditor implements View.OnClickListener{
    Context context;
    EditText editText;
    
    PopupWindow popupWindow;
    View functionView;
    String textWithTag,partText,textWithoutTag;
    int startIndex,endIndex,occurranceIndex;
    Spanned spanned;


    public HtmlEditor(Context context, final EditText editText) {
        this.context = context;
        this.editText = editText;

        this.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void afterTextChanged(Editable editable) {
                spanned=new SpannedString(editText.getText());
            }
        });

        this.functionView= LayoutInflater.from(context).inflate(R.layout.html_editor_popup_view,null);

        popupWindow = new PopupWindow(
                functionView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        setUpFunctions();
        rn=new Runnable() {
            @Override
            public void run() {
                if(editText.getSelectionStart()==editText.getSelectionEnd()){
                    hidePopup();
                    Log.i("popup","hide");
                }else {
                    showPopup();
                    Log.i("popup", "show");
                }
                handler.postDelayed(this, 400);
            }
        };
        rn.run();


    }
    Runnable rn;
    Handler handler=new Handler();
    public void stop(){
        handler.removeCallbacks(rn);
    }

    void showPopup(){

        if(!popupWindow.isShowing())
            popupWindow.showAsDropDown(editText,0,-200);
    }

    void hidePopup(){
        popupWindow.dismiss();
    }

    String encapsulateWithList(String text,String tag){
        return "<ul><li>"+text+"</li></ul>";
    }
    String encapsulateWithCenter(String text){
        return "<center>"+text+"</center>";
    }

    String encapsulateWithFont(String text,String tag){
        return "<"+tag+" color=blue >"+text+"</"+tag+">";
    }

    String encapsulateWithBg(String text,String tag){
        return "<"+tag+" background-color=\"yellow\" >"+text+"</"+tag+">";
    }
    String encapsulateWithTag(String text,String tag){
        return "<"+tag+">"+text+"</"+tag+">";
    }

    String unEncapsulateWithTag(String text,String tag){
        text="</"+tag+">"+text+"<"+tag+">";

        return text;
    }

    void setUpFunctions(){
        functionView.findViewById(R.id.popup_btn_size_h1).setOnClickListener(this);
        functionView.findViewById(R.id.popup_btn_bold).setOnClickListener(this);
        functionView.findViewById(R.id.popup_btn_italic).setOnClickListener(this);
        functionView.findViewById(R.id.popup_btn_underline).setOnClickListener(this);
        functionView.findViewById(R.id.popup_btn_color).setOnClickListener(this);
        functionView.findViewById(R.id.popup_btn_background).setOnClickListener(this);
        functionView.findViewById(R.id.popup_btn_left).setOnClickListener(this);
        functionView.findViewById(R.id.popup_btn_center).setOnClickListener(this);
        functionView.findViewById(R.id.popup_btn_right).setOnClickListener(this);
        functionView.findViewById(R.id.popup_btn_list_bullet).setOnClickListener(this);
        functionView.findViewById(R.id.popup_btn_list_num).setOnClickListener(this);
    }


    public static int ordinalIndexOf(String str, String c, int n) {
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos+1);
        if(pos==-1)return 0;
        return pos;
    }

    void setEditedText(){
        editText.setText(Html.fromHtml(textWithTag.replace("<p>", "").replace("</p>", "").trim()));
        spanned=new SpannedString(editText.getText());
    }

    public String getTextWithTag(){
        //return spanned.toString();
        return Html.toHtml(spanned).replace("<p>","").replace("</p>","").trim();
    }

    @Override
    public void onClick(View view) {
        textWithoutTag=editText.getText().toString();
        textWithTag=Html.toHtml(spanned);
        startIndex=editText.getSelectionStart();
        endIndex=editText.getSelectionEnd();
        partText=textWithoutTag.substring(startIndex,endIndex);
        int i=0;
        int last=0;

        while (last!=-1&&i<textWithoutTag.length()){
            last=textWithoutTag.indexOf(partText,last);
            if(startIndex==last){
               occurranceIndex=i;
                break;
            }else {
                i++;
            }
            Log.i("WHile loop"," i="+i+"  last="+last);
            last++;
        }
        
        int id=view.getId();
        switch(id){
            case R.id.popup_btn_size_h1:
                h1();
                break;
            case R.id.popup_btn_bold:
                bold();
                break;
            case R.id.popup_btn_italic:
                italic();
                break;
            case R.id.popup_btn_underline:
                underline();
                break;
            case R.id.popup_btn_color:
                textcolor();
                break;
            case R.id.popup_btn_background:
                highlight();
                break;
            case R.id.popup_btn_left:
                left();
                break;
            case R.id.popup_btn_center:
                center();
                break;
            case R.id.popup_btn_right:
                right();
                break;
            case R.id.popup_btn_list_bullet:
                listB();
                break;
            case R.id.popup_btn_list_num:
                listN();
                break;
        }

        setEditedText();
    }


    void h1(){
        int i=ordinalIndexOf(textWithTag, partText, occurranceIndex);
        textWithTag=new StringBuilder(textWithTag).replace(i, i+partText.length(), encapsulateWithTag(partText, "h1")).toString();

    }

    void bold(){
        int i=ordinalIndexOf(textWithTag, partText, occurranceIndex);
        textWithTag=new StringBuilder(textWithTag).replace(i, i+partText.length(), encapsulateWithTag(partText, "b")).toString();
    }

    void italic(){
        int i=ordinalIndexOf(textWithTag, partText, occurranceIndex);
        textWithTag=new StringBuilder(textWithTag).replace(i, i+partText.length(), encapsulateWithTag(partText, "i")).toString();
    }
    void underline(){
        int i=ordinalIndexOf(textWithTag, partText, occurranceIndex);
        textWithTag=new StringBuilder(textWithTag).replace(i, i+partText.length(), encapsulateWithTag(partText, "u")).toString();
    }

    void highlight(){
        int i=ordinalIndexOf(textWithTag, partText, occurranceIndex);
        textWithTag=new StringBuilder(textWithTag).replace(i, i+partText.length(), encapsulateWithBg(partText, "span")).toString();
    }

    void textcolor(){
        int i=ordinalIndexOf(textWithTag, partText, occurranceIndex);
        textWithTag=new StringBuilder(textWithTag).replace(i, i+partText.length(), encapsulateWithFont(partText, "font")).toString();

    }

    void left(){
        int i=ordinalIndexOf(textWithTag, partText, occurranceIndex);
        textWithTag=new StringBuilder(textWithTag).replace(i, i+partText.length(), encapsulateWithCenter(partText)).toString();

    }
    void center(){
        int i=ordinalIndexOf(textWithTag, partText, occurranceIndex);
        textWithTag=new StringBuilder(textWithTag).replace(i, i+partText.length(), encapsulateWithCenter(partText)).toString();

    }
    void right(){
        int i=ordinalIndexOf(textWithTag, partText, occurranceIndex);
        textWithTag=new StringBuilder(textWithTag).replace(i, i+partText.length(), encapsulateWithCenter(partText)).toString();

    }

    void listB(){
        int i=ordinalIndexOf(textWithTag, partText, occurranceIndex);
        textWithTag=new StringBuilder(textWithTag).replace(i, i+partText.length(), encapsulateWithList(partText, "span")).toString();

    }
    void listN(){
        int i=ordinalIndexOf(textWithTag, partText, occurranceIndex);
        textWithTag=new StringBuilder(textWithTag).replace(i, i+partText.length(), encapsulateWithList(partText, "span")).toString();

    }
}
