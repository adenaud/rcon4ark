package com.anthonydenaud.arkrcon.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Anthony on 21/10/2015.
 */
public class Player implements Parcelable {

    private String name;
    private String steamId;
    private float connectTime;

    public Player(String name, String steamId, float connectTime) {
        this.name = name;
        this.steamId = steamId;
        this.connectTime = connectTime;
    }

    public Player(Parcel source) {
        name = source.readString();
        steamId = source.readString();
        connectTime = source.readFloat();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSteamId() {
        return steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

    public float getConnectTime() {
        return connectTime;
    }

    public void setConnectTime(float connectTime) {
        this.connectTime = connectTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(steamId);
        dest.writeFloat(connectTime);
    }

    public static final Creator<Player> CREATOR = new Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel source) {
            return new Player(source);
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };
}
