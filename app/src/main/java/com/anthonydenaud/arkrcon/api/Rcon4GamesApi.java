package com.anthonydenaud.arkrcon.api;

import android.os.AsyncTask;

import com.anthonydenaud.arkrcon.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import roboguice.util.Ln;

/**
 * Created by Anthony Denaud on 08/12/2016.
 */

public class Rcon4GamesApi {

    private static final String API_URL = "http://rcon4games.com/ark/api/";

    private ApiCallback apiCallback;


    public void getLastVersion(ApiCallback apiCallback){
        this.apiCallback = apiCallback;
        new CheckUpdateAsyncTask().execute(API_URL + "last_version");
    }

    public void saveUser(String uuid){
        try {
            JSONObject body = new JSONObject();
            body.put("uuid", uuid);
            new PostAsyncTask().execute(API_URL + "user", body.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveServer(String uuid, String name, String hostname, int rcon_port, int query_port, String user_uuid){
        try {
            JSONObject body = new JSONObject();
            body.put("uuid", uuid);
            body.put("name", name);
            body.put("hostname", hostname);
            body.put("rcon_port", rcon_port);
            body.put("query_port", query_port);
            body.put("user_uuid", user_uuid);
            new PostAsyncTask().execute(API_URL + "server",body.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class  PostAsyncTask extends AsyncTask<String, Void, Void> {

        private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        @Override
        protected Void doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(JSON, params[1]);
                Request request = new Request.Builder().url(params[0]).post(body).build();
                Response response = client.newCall(request).execute();
                JSONObject json = new JSONObject(response.body().string());

                if(!"OK".equals(json.getString("status"))){
                    throw new ApiException(response.body().string());
                }

            }catch (IOException | JSONException e){
                Ln.e("Unable to POST data : " + e.getMessage());
            }
            return null;
        }
    }

    private class CheckUpdateAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            int versionCode = 0;
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(params[0]).build();
                Response response = client.newCall(request).execute();

                JSONObject json = new JSONObject(response.body().string());
                versionCode = json.getInt("last_version");

            } catch (IOException | JSONException e) {
                Ln.e("Unable to get last version : " + e.getMessage());
            }

            if (versionCode > BuildConfig.VERSION_CODE && apiCallback != null) {
                apiCallback.response(versionCode);
            }
            return null;
        }
    }
}
