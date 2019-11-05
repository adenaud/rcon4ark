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
import okhttp3.ResponseBody;
import timber.log.Timber;

/**
 * Created by Anthony Denaud on 08/12/2016.
 */

public class Rcon4GamesApi {

    private static final String API_URL = "http://rcon4games.com/ark/api/";


    public void getLastVersion(ApiCallback apiCallback){
        new CheckUpdateAsyncTask(apiCallback).execute(API_URL + "last_version");
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

    private static class PostAsyncTask extends AsyncTask<String, Void, Void> {

        private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        @Override
        protected Void doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(JSON, params[1]);
                Request request = new Request.Builder().url(params[0]).post(body).build();
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                JSONObject json = responseBody != null ? new JSONObject(responseBody.string()) : new JSONObject();

                if(!"OK".equals(json.getString("status"))){
                    throw new ApiException(json.toString());
                }

            }catch (IOException | JSONException e){
                Timber.e(e, "Unable to POST data : %s", e.getMessage());
            }
            return null;
        }
    }

    private static class CheckUpdateAsyncTask extends AsyncTask<String, Void, Void> {

        private ApiCallback apiCallback;

        CheckUpdateAsyncTask(ApiCallback apiCallback) {
         this.apiCallback = apiCallback;
        }

        @Override
        protected Void doInBackground(String... params) {
            int versionCode = 0;
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(params[0]).build();
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                JSONObject json = responseBody != null ? new JSONObject(responseBody.string()) : new JSONObject();
                versionCode = json.getInt("last_version");

            } catch (IOException | JSONException e) {
                Timber.e(e, "Unable to get last version : %s", e.getMessage());
            }

            if (versionCode > BuildConfig.VERSION_CODE && apiCallback != null) {
                apiCallback.response(versionCode);
            }
            return null;
        }
    }
}
