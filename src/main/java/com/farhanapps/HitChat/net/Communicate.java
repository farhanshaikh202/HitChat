package com.farhanapps.HitChat.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.farhanapps.HitChat.Models.ContactModel;
import com.farhanapps.HitChat.database.DatabaseHandler;
import com.farhanapps.HitChat.utils.Constants;
import com.farhanapps.HitChat.utils.MySql;
import com.farhanapps.HitChat.utils.Server;


public class Communicate {

	boolean success=false;




	Context cc;
	
	public Communicate(Context c) {
		cc=c;
	}
	
	public boolean register(String name,String no,String password){
		success=false;

	    JSONParser jParser = new JSONParser();
	   	 List<NameValuePair> params = new ArrayList<NameValuePair>();
	   	 params.add(new BasicNameValuePair("no", no));
	   	params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("pwd", password));
	        // getting JSON string from URL
	   	JSONObject json = null;
		try {
			json = jParser.makeHttpRequest(Server.SERVER_URL+Server.REGISTER_URL, "GET", params);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(cc, "Something wrong !!", Toast.LENGTH_LONG).show();
			return false;
		}
	   	
	    if(json != null){
        	//status.getCode();
                //successful ajax call, show status code and json content
        	try {
				if(json.getString("success").equals("yes")){
					success=true;
				}else
				success=false;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        Log.i("json",json.toString());
        
        }else{
                
                //ajax error, show error code
        	success=false;
        }
		
		return success;
	}
	
	public String login(String no,String pass){
		 String success="no";
		JSONParser jParser = new JSONParser();
	   	 List<NameValuePair> params = new ArrayList<NameValuePair>();
	   	 params.add(new BasicNameValuePair("no", no));
		params.add(new BasicNameValuePair("pwd", pass));
	        // getting JSON string from URL
	   	JSONObject json = null;
		try {
			json = jParser.makeHttpRequest(Server.SERVER_URL+Server.LOGIN_URL, "GET", params);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			//Toast.makeText(cc, "Something wrong !!", Toast.LENGTH_LONG).show();
			return "Error connection server";
		}

	    if(json != null){
       	//status.getCode();
               //successful ajax call, show status code and json content
       	try {
				if(json.getString("success").equals("yes")){
					success="yes";
				}else
				success="Incorrect Phone Number or Password";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
       Log.i("json",json.toString());
       
       }else{

       }
		return success;
	}
	
	public boolean getmsgs(Context c,String no,DatabaseHandler dbdb){
		success=false;

	    
	   	 List<NameValuePair> params = new ArrayList<NameValuePair>();
	   	 params.add(new BasicNameValuePair("mynum", no));
	   	ServiceHandler sh = new ServiceHandler();
	    
        // Making a request to url and getting response
        String json = sh.makeServiceCall(Server.SERVER_URL+Server.MESSAGE_URL, ServiceHandler.GET,params);
	        // getting JSON string from URL
        
        
	    if(json != null && !json.equals("nullnullnull")){
	    	Log.i("msgjson",json.toString());
	    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cc);

	    	try {
	    		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("hh:mm a", Locale.US);
	    		dateFormatGmt.setTimeZone(TimeZone.getDefault());

	    		//Local time zone   
	    		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

		   		int i=0;
				JSONArray jar=new JSONArray(json.toString());
				
				for(i=0;i<jar.length();i++){
					
				JSONObject jo=jar.getJSONObject(i);
				String id,time,me,you,msg;
				id=jo.getString(DatabaseHandler.TAG_ID);
				time=jo.getString(DatabaseHandler.TAG_TIME);
				you=jo.getString(DatabaseHandler.TAG_SENDER);
				me=jo.getString(DatabaseHandler.TAG_RECEIVER);
				msg=jo.getString(DatabaseHandler.TAG_MESSAGE);
				
				sp.edit().putInt(you, sp.getInt(you, 0)+1).commit();
				time= dateFormatGmt.format(dateFormatLocal.parse(time)).toString();
				dbdb.insertmsg(you, me, msg,time,no);
				}
				success=true;
			} catch (JSONException e1) {
				e1.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        
        
        }else{
            
        	success=false;
        }
	    
	    return success;
	}
	
	public boolean scanner(String s){
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
	   	 params.add(new BasicNameValuePair("list", s));
	   	ServiceHandler sh = new ServiceHandler();
	    
       // Making a request to url and getting response
       String json = sh.makeServiceCall(Server.SERVER_URL+Server.SCAN_URL, ServiceHandler.GET,params);
	        // getting JSON string from URL
	   	
       
	    if(json != null){
	    	try {
	    		Log.i("json",json.toString());
		   		
				JSONObject jo=new JSONObject(json);
				if(jo.getString("success").equals("yes")){
					return true;
				}else return false;
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
       	
       }else{
           
       	return false;
       }
		return false;
		
	}
public boolean sendMsg(String sender,String receiver,String msg){
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
	   	 params.add(new BasicNameValuePair("mynum", sender));
	   	params.add(new BasicNameValuePair("tonum", receiver));
	   	params.add(new BasicNameValuePair("message", msg));
	   	ServiceHandler sh = new ServiceHandler();
	    
       // Making a request to url and getting response
       String json = sh.makeServiceCall(Server.SERVER_URL+Server.SEND_MSG_URL, ServiceHandler.GET,params);
	        // getting JSON string from URL
	   	
       
	    if(json != null){
	    	try {
	    		Log.i("json",json.toString());
		   		
				JSONObject jo=new JSONObject(json);
				if(jo.getString("success").equals("yes")){
					return true;
				}else return false;
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
       	//status.getCode();
               //successful ajax call, show status code and json content
       	
       
       
       }else{
           
       	return false;
       }
		return false;
		
	}

    public boolean updateName(String contactNumber,String name){

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("contact", contactNumber));
        params.add(new BasicNameValuePair("name", name));
        ServiceHandler sh = new ServiceHandler();

        // Making a request to url and getting response
        String json = sh.makeServiceCall(Server.SERVER_URL+Server.UPDATE_NAME_URL, ServiceHandler.GET,params);
        // getting JSON string from URL


        if(json != null){
            try {
                Log.i("json",json.toString());

                JSONObject jo=new JSONObject(json);
                if(jo.getString("success").equals("yes")){
                    return true;
                }else return false;
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }else{

            return false;
        }
        return false;

    }
    public boolean updateStatus(String contactNumber,String status){

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("contact", contactNumber));
        params.add(new BasicNameValuePair("status", status));
        ServiceHandler sh = new ServiceHandler();

        // Making a request to url and getting response
        String json = sh.makeServiceCall(Server.SERVER_URL+Server.UPDATE_STATUS_URL, ServiceHandler.GET,params);
        // getting JSON string from URL


        if(json != null){
            try {
                Log.i("json",json.toString());

                JSONObject jo=new JSONObject(json);
                if(jo.getString("success").equals("yes")){
                    return true;
                }else return false;
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }else{

            return false;
        }
        return false;

    }

	public ContactModel getContactInfo(String contactNumber){

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("contact", contactNumber));

		ServiceHandler sh = new ServiceHandler();

		// Making a request to url and getting response
		String json = sh.makeServiceCall(Server.SERVER_URL+Server.GET_CONTACT_INFO_URL, ServiceHandler.GET,params);
		// getting JSON string from URL

        ContactModel contactModel=null;

		if(json != null){
			try {
                Log.i("json",json.toString());
                JSONArray jsonArray=new JSONArray(json);
                JSONObject jsonObject=jsonArray.getJSONObject(0);
                    contactModel=new ContactModel();
                    contactModel.setContact_pic_thumb(jsonObject.getString(MySql.TAG_PHP_CONTACT_PIC_THUMB));
                contactModel.setContact_pic(jsonObject.getString(MySql.TAG_PHP_CONTACT_PIC));
                contactModel.setContact_cover_thumb(jsonObject.getString(MySql.TAG_PHP_CONTACT_COVER_THUMB));
                contactModel.setContact_cover(jsonObject.getString(MySql.TAG_PHP_CONTACT_COVER));
                contactModel.setContact_status(jsonObject.getString(MySql.TAG_PHP_CONTACT_STATUS));
                contactModel.setContact_number(jsonObject.getString(MySql.TAG_PHP_CONTACT_NUMBER));
                contactModel.setContact_name(jsonObject.getString(MySql.TAG_PHP_CONTACT_NAME));


			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//status.getCode();
			//successful ajax call, show status code and json content



		}
		return contactModel;

	}
	public static String uploadFile(String sourceFileUri) {


		String fileName = sourceFileUri;
		String serverResponseMessage="";
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		File sourceFile = new File(sourceFileUri);

		if (!sourceFile.isFile()) {


			Log.e("uploadFile", "Source File not exist :");


			return null;

		}
		else
		{
			int serverResponseCode = 0;
            FileInputStream fileInputStream=null;
            try {

				// open a URL connection to the Servlet
				 fileInputStream = new FileInputStream(sourceFile);
				URL url = new URL(Server.SERVER_URL+Server.SCAN_URL);

				// Open a HTTP  connection to  the URL
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("text",fileName);

				dos = new DataOutputStream(conn.getOutputStream());

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"text\";filename=\""
						+fileName + "\"" + lineEnd);

				dos.writeBytes(lineEnd);

				// create a buffer of  maximum size
				bytesAvailable = fileInputStream.available();

				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0) {

					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				}

				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				serverResponseCode = conn.getResponseCode();
				//serverResponseMessage = conn.getContent().toString();



				Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

				if(serverResponseCode == 200){
					InputStreamReader in = new InputStreamReader((InputStream) conn.getContent());
					BufferedReader buff = new BufferedReader(in);

					String line = null;

					do {
						line = buff.readLine();
						if(line!=null)serverResponseMessage=serverResponseMessage+line;
					}while (line != null);

				}

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

			} catch (MalformedURLException ex) {
				ex.printStackTrace();
				Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("Upload file to server Exception", "Exception : "
						+ e.getMessage(), e);
			}

			return serverResponseMessage;

		} // End else block
	}

}
