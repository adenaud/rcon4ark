package net.nexusrcon.nexusrconark.model;

/**
 * Created by Anthony on 06/10/2015.
 */
public class Server {

    private String hostname;
    private int port;
    private String password;

    public Server(){
    }

    public Server(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
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

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
