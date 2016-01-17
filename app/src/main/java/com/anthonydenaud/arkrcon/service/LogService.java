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
            String filename = String.format("server_%s_%d.log", server.getHostname().replaceAll("\\.", "-"), server.getPort());
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

    public String read(Context context, Server server) {
        String log = "";

        File path = new File(context.getExternalFilesDir(null), "logs");
        String filename = String.format("server_%s_%d.log", server.getHostname().replaceAll("\\.", "-"), server.getPort());

        File logFile = new File(path, filename);

        if (isExternalStorageReadable() && logFile.exists()) {

            try {
                log = IOUtils.toString(new FileInputStream(logFile));
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
