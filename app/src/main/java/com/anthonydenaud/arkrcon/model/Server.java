package com.anthonydenaud.arkrcon.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by Anthony on 06/10/2015.
 */
public class Server implements Parcelable {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String name;

    @DatabaseField
    private String hostname;

    @DatabaseField
    private int port;

    @DatabaseField
    private int queryPort;

    @DatabaseField
    private String password;

    @DatabaseField
    private String adminName;

    public Server() {
        name = "";
        hostname = "";
        port = 32330;
        queryPort = 0;
        password = "";
        adminName = "";
    }

    public Server(String name, String hostname, int port) {
        this.name = name;
        this.hostname = hostname;
        this.port = port;
        this.password = "";
        this.adminName = "";
    }

    public Server(Parcel source) {
        this.id = source.readInt();
        this.name = source.readString();
        this.hostname = source.readString();
        this.port = source.readInt();
        this.queryPort = source.readInt();
        this.password = source.readString();
        this.adminName = source.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public int getQueryPort() {
        return queryPort;
    }

    public void setQueryPort(int queryPort) {
        this.queryPort = queryPort;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminname) {
        this.adminName = adminname;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(hostname);
        dest.writeInt(port);
        dest.writeInt(queryPort);
        dest.writeString(password);
        dest.writeString(adminName);
    }

    public static final Creator<Server> CREATOR = new Creator<Server>() {
        @Override
        public Server createFromParcel(Parcel source) {
            return new Server(source);
        }

        @Override
        public Server[] newArray(int size) {
            return new Server[size];
        }
    };


}
