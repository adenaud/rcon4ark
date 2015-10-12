package net.nexusrcon.nexusrconark;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.nexusrcon.nexusrconark.event.OnReceiveListener;
import net.nexusrcon.nexusrconark.event.ReceiveEvent;
import net.nexusrcon.nexusrconark.model.Server;
import net.nexusrcon.nexusrconark.network.Packet;
import net.nexusrcon.nexusrconark.network.SRPConnection;

import java.io.IOException;
import java.sql.Time;

import roboguice.util.Ln;

/**
 * Created by Anthony on 12/10/2015.
 */
@Singleton
public class ArkService implements OnReceiveListener {

    @Inject
    private SRPConnection connection;

    public void connect(Server server){
        connection.open(server.getHostname(),server.getPort());

        connection.setOnReceiveListener(this);
    }

    public void login(String password){
        Packet packet = new Packet(3,password);
        try {
            connection.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listPlayers(){

    }

    public void broadcast(String message){

    }

    public void destroyWildDinos(){

    }
    public void setTimeofDay(int hour, int minute){

    }

    public void saveWorld(){

    }

    @Override
    public void onReceive(ReceiveEvent event) {
        Packet packet = event.getPacket();
        if(packet.getType() == 2){
            if(packet.getId() == -1){
                Ln.d("Auth Fail");
            }
            else{
                Ln.d("Auth success");
            }
        }
    }
}
