package net.nexusrcon.nexusrconark.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import net.nexusrcon.nexusrconark.R;
import net.nexusrcon.nexusrconark.model.Server;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class ServerConnectionActivity extends RoboActionBarActivity {

    @InjectView(R.id.hostname_edittext)
    private EditText hostnameEditText;

    @InjectView(R.id.port_edittext)
    private EditText portEditText;

    @InjectView(R.id.password_edittext)
    private EditText passwordEditText;

    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_connection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        server = getIntent().getParcelableExtra("server");

        hostnameEditText.setText(server.getHostname());
        portEditText.setText(String.valueOf(server.getPort()));
        passwordEditText.setText(server.getPassword());
    }

}
