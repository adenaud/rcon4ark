package net.nexusrcon.nexusrconark.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.nexusrcon.nexusrconark.R;
import net.nexusrcon.nexusrconark.model.Server;

public class ServerConnectionActivity extends AppCompatActivity {

    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_connection);

    }
}
