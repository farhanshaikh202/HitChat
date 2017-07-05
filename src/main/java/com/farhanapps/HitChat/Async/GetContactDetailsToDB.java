package com.farhanapps.HitChat.Async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.farhanapps.HitChat.Models.ContactModel;
import com.farhanapps.HitChat.database.DatabaseHandler;
import com.farhanapps.HitChat.net.Communicate;
import com.farhanapps.HitChat.net.ConnectionDetactor;

/**
 * Created by farhan on 25-04-2016.
 */

public class GetContactDetailsToDB extends AsyncTask<String, String, ContactModel> {
    Communicate communicate;
    Context context;
    DatabaseHandler databaseHandler;

    public GetContactDetailsToDB(Communicate communicate, Context context, DatabaseHandler databaseHandler) {
        this.communicate = communicate;
        this.context = context;
        this.databaseHandler = databaseHandler;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected ContactModel doInBackground(String... params) {
        if (!ConnectionDetactor.isConnecting(context)) {
            return null;
        }
        ContactModel contactModel=null;
        try {
            contactModel = communicate.getContactInfo(params[0]);
            databaseHandler.updateContact(contactModel);
        }catch (Exception e){e.printStackTrace();}
        return contactModel;
    }
    protected void onProgressUpdate(String... p) {

    }

    @Override
    protected void onPostExecute(final ContactModel contactModel) {
        try {
            if (contactModel!=null) {
            } else {
                Log.i("GET_CONTACT_DETAILS", "Error ");
            }
        }catch (Exception e){

        }

    }
}