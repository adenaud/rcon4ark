package com.anthonydenaud.arkrcon.service;

import android.content.Context;
import android.os.Environment;

import com.anthonydenaud.arkrcon.model.Server;
import com.google.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import roboguice.util.Ln;

@Singleton
public class LogService {

    public boolean write(Context context, Server server, String log) {
        boolean success = false;
        if (isExternalStorageWritable() && StringUtils.isNotEmpty(log)) {
            File path = new File(context.getExternalFilesDir(null), "logs");
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    Ln.e("Unable to create log directory");
                }
            }
            String filename = String.format(Locale.ENGLISH, "server_%s_%d.log", server.getHostname().replaceAll("\\.", "-"), server.getPort());
            File logFile = new File(path, filename);
            try {
                FileWriter writer = new FileWriter(logFile, true);
                writer.write(log);
                writer.close();
                success= true;
            } catch (IOException e) {
                Ln.e("Unable to create log file : %s", e.getMessage());
            }
        }
        return success;
    }

    /**
     * Read the most recent log file.
     * @param context The Android context
     * @param server The corresponding server
     * @return The log as plain text
     */
    public String readLatest(Context context, Server server) {
        String filename = String.format(Locale.ENGLISH, "server_%s_%d.log", server.getHostname().replaceAll("\\.", "-"), server.getPort());
        return read(context, filename);
    }

    /**
     * Read an archived log file.
     * @param context The Android context
     * @param filename The name of the file to read
     * @return The log as plain text
     */
    public String readArchive(Context context, String filename){
        return read(context, filename);
    }


    private String read(Context context, String filename){
        File path = new File(context.getExternalFilesDir(null), "logs");
        File file = new File(path, filename);
        String log = "";
        if (isExternalStorageReadable() && file.exists()) {
            try {
                log = IOUtils.toString(new FileInputStream(file));
            } catch (IOException e) {
                Ln.e("Error reading log file : %s", e.getMessage());
            }
        }
        return log;
    }



    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
