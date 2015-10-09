package net.nexusrcon.nexusrconark.network;

/**
 * Created by Anthony on 09/10/2015.
 */
public class SRPConnection {

    private int sequenceNumber;

    public synchronized int getSequenceNumber(){
        return ++sequenceNumber;
    }

}
