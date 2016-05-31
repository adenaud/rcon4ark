package com.anthonydenaud.arkrcon.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.widget.Toast;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.dao.ServerDAO;
import com.anthonydenaud.arkrcon.model.Server;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import roboguice.util.Ln;

@Singleton
public class LogService {

    private static final String MIGRATION_21_KEY = "LogMigration21";

    @Inject
    private ServerDAO serverDAO;

    public boolean write(Context context, Server server, String log) {
        boolean success = false;
        if (isExternalStorageWritable() && StringUtils.isNotEmpty(log)) {
            File path = new File(context.getExternalFilesDir(null), "logs/" + server.getUuid());
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    Ln.e("Unable to create log directory");
                }
            }
            String filename = "server.log";
            File logFile = new File(path, filename);

            if (logFile.exists() && logFile.length() >= R.integer.log_max_size) {
                archiveLog(path, logFile);
            }

            try {
                FileWriter writer = new FileWriter(logFile, true);
                writer.write(log);
                writer.close();
                success = true;
            } catch (IOException e) {
                Ln.e("Unable to create log file : %s", e.getMessage());
            }
        }
        return success;
    }

    public List<String> listArchives(Context context, Server server) {
        List<String> filenames = new ArrayList<>();
        File[] files = new File(context.getExternalFilesDir(null), "logs/" + server.getUuid()).listFiles();
        for (File file : files) {
            filenames.add(file.getName());
        }
        return filenames;
    }

    private void archiveLog(File path, File logFile) {
        String destination = String.format(Locale.ENGLISH, "server_%s.log", new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss", Locale.ENGLISH).format(new Date()));
        try {
            FileUtils.moveFile(logFile, new File(path, destination));
        } catch (IOException e) {
            Ln.e("Unable to archive log file : %s", e.getMessage());
        }
    }

    /**
     * Read the most recent log file.
     *
     * @param context The Android context
     * @param server  The corresponding server
     * @return The log as plain text
     */
    public String readLatest(Context context, Server server) {
        String filename = "server.log";
        return read(context, filename, server.getUuid());
    }

    /**
     * Read an archived log file.
     *
     * @param context  The Android context
     * @param server   The corresponding server
     * @param filename The name of the file to read
     * @return The log as plain text
     */
    public String readArchive(Context context, Server server, String filename) {
        return read(context, filename, server.getUuid());
    }

    public void migrate(Context context) {


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (!preferences.contains(MIGRATION_21_KEY)) {
            List<Server> servers = serverDAO.findAll();
            if (servers.size() > 0) {
                Toast.makeText(context, context.getString(R.string.migrate_log), Toast.LENGTH_LONG).show();
                for (Server server : servers) {
                    File path = new File(context.getExternalFilesDir(null), "logs");
                    String filename = String.format(Locale.ENGLISH, "server_%s_%d.log", server.getHostname().replaceAll("\\.", "-"), server.getPort());
                    File file = new File(path, filename);
                    String log = "";
                    if (isExternalStorageReadable() && file.exists()) {
                        try {
                            log = IOUtils.toString(new FileInputStream(file));
                        } catch (IOException e) {
                            Ln.e("Error reading log file : %s", e.getMessage());
                        }
                    }
                    write(context, server, log);
                }
            }
            preferences.edit().putBoolean(MIGRATION_21_KEY, true).apply();
        }
    }

    private String read(Context context, String filename, String serverUuid) {
        File path = new File(context.getExternalFilesDir(null), "logs/" + serverUuid);
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
