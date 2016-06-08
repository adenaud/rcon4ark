package com.anthonydenaud.arkrcon.service;

import android.content.Context;
import android.os.AsyncTask;

import com.anthonydenaud.arkrcon.BuildConfig;
import com.anthonydenaud.arkrcon.R;
import com.google.inject.Singleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import roboguice.util.Ln;


@Singleton
public class AppUpdateService {

    public interface AppUpdateListener {
        void onUpdateAvailable(int versionCode);
    }

    public class CheckUpdateAsyncTask extends AsyncTask<String, Void, Void> {

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

            if (versionCode > BuildConfig.VERSION_CODE && appUpdateListener != null) {
                appUpdateListener.onUpdateAvailable(versionCode);
            }
            return null;
        }
    }

    private AppUpdateListener appUpdateListener;

    public void checkAppUpdateAvailable(Context context) {
        new CheckUpdateAsyncTask().execute(context.getString(R.string.update_url));
    }

    public void setAppUpdateListener(AppUpdateListener appUpdateListener) {
        this.appUpdateListener = appUpdateListener;
    }
}
