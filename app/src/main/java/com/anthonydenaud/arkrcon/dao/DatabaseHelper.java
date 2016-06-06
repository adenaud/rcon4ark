package com.anthonydenaud.arkrcon.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import com.anthonydenaud.arkrcon.model.Server;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Created by Anthony on 09/10/2015.
 */
@Singleton
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "nexusrcon-ark.db";
    private static final int DATABASE_VERSION = 3;

    @Inject
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Server.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            getRuntimeExceptionDao(Server.class).executeRaw("ALTER TABLE `server` ADD COLUMN adminName VARCHAR(255);");
        }
        if (oldVersion < 3) {
            getRuntimeExceptionDao(Server.class).executeRaw("ALTER TABLE `server` ADD COLUMN queryPort INTEGER DEFAULT 0;");
            getRuntimeExceptionDao(Server.class).executeRaw("ALTER TABLE `server` ADD COLUMN uuid AFTER `id` VARCHAR(36);");

            List<Server> servers = getRuntimeExceptionDao(Server.class).queryForAll();
            for (Server server :servers) {
                server.setUuid(UUID.randomUUID().toString());
                getRuntimeExceptionDao(Server.class).update(server);
            }
        }
    }
}
