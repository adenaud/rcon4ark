package net.nexusrcon.nexusrconark.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Anthony on 21/10/2015.
 */
public class Player implements Parcelable {

    private int ue4Id;

    private String name;

    private String steamId;

    public Player(int ue4Id, String name, String steamId) {
        this.ue4Id = ue4Id;
        this.name = name;
        this.steamId = steamId;
    }

    public Player(Parcel source) {
        ue4Id = source.readInt();
        name = source.readString();
        steamId = source.readString();
    }

    public int getUe4Id() {
        return ue4Id;
    }

    public void setUe4Id(int ue4Id) {
        this.ue4Id = ue4Id;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ue4Id);
        dest.writeString(name);
        dest.writeString(steamId);
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
