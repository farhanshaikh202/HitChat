package com.farhanapps.HitChat.net;

/**
 * Created by farhan on 24-04-2016.
 */

import android.app.Service;
import android.os.AsyncTask;
import android.util.Config;
import android.util.Log;
import android.view.View;

import com.farhanapps.HitChat.interfaces.UploadListener;
import com.farhanapps.HitChat.utils.Server;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

/**
 * Uploading the file to server
 * */
public class UploadImageToServer extends AsyncTask<String, Integer, String> {
    UploadListener uploadListener=null;
    String filePath;

    public void setUploadListener(UploadListener uploadListener) {
        this.uploadListener = uploadListener;
    }

    @Override
    protected void onPreExecute() {
        // setting progress bar to zero
        if(uploadListener!=null)uploadListener.onUploadStart();
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        if(uploadListener!=null)uploadListener.onUploadProgress(progress[0]);

    }

    String type;
    @Override
    protected String doInBackground(String... params) {
        filePath=params[0];
        type=params[1];
        phone=params[2];
        return uploadFile();
    }
    long totalSize = 0;
    boolean success=false;
    String phone;

    @SuppressWarnings("deprecation")
    private String uploadFile() {
        String responseString = null;
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(Server.SERVER_URL+Server.UPLOAD_USER_IMAGE_URL);

        try {
            AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                    new AndroidMultiPartEntity.ProgressListener() {

                        @Override
                        public void transferred(long num) {
                            publishProgress((int) ((num / (float) totalSize) * 100));
                        }
                    });

            File sourceFile = new File(filePath);

            // Adding file data to http body
            entity.addPart("image", new FileBody(sourceFile));
            entity.addPart(type, new StringBody("a"));
            entity.addPart("number", new StringBody(phone));

            totalSize = entity.getContentLength();
            httppost.setEntity(entity);

            // Making server call
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity r_entity = response.getEntity();

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                // Server response
                responseString = EntityUtils.toString(r_entity);
                success=true;
            } else {
                responseString = "Error occurred! Http Status Code: "
                        + statusCode;
            }

        } catch (ClientProtocolException e) {
            responseString = e.toString();
        } catch (IOException e) {
            responseString = e.toString();
        }

        return responseString;

    }

    @Override
    protected void onPostExecute(String result) {
        Log.e("IMAGE_UPLOADER", "Response from server: " + result);
        if(success) {
            if (uploadListener != null) uploadListener.onUploadFinish(result);
        }
        else{
            if(uploadListener!=null)uploadListener.onUploadError(result);
        }
        super.onPostExecute(result);
    }

}