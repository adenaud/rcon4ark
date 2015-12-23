package com.anthonydenaud.rconark.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.inject.Inject;

import com.anthonydenaud.rconark.R;
import com.anthonydenaud.rconark.dao.ServerDAO;
import com.anthonydenaud.rconark.model.Server;

import org.apache.commons.lang3.StringUtils;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class ServerConnectionActivity extends RoboActionBarActivity {

    @Inject
    private ServerDAO dao;

    @InjectView(R.id.name_edittext)
    private EditText nameEditText;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_server_connection);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        server = getIntent().getParcelableExtra("server");
        setTitle(getIntent().getIntExtra("titleId",R.string.edit_server));

        nameEditText.setText(server.getName());
        hostnameEditText.setText(server.getHostname());
        portEditText.setText(String.valueOf(server.getPort()));
        passwordEditText.setText(server.getPassword());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_server_conneciton, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.save) {

            try {

                String host = hostnameEditText.getText().toString();
                int port = Integer.parseInt(portEditText.getText().toString());

                if(port > 0 && port < 65535){
                    if(StringUtils.isNotEmpty(host)){
                        server.setName(nameEditText.getText().toString());
                        server.setHostname(host);
                        server.setPort(port);
                        server.setPassword(passwordEditText.getText().toString());

                        dao.save(server);

                        getIntent().putExtra("server", server);
                        setResult(RESULT_OK, getIntent());

                        finish();
                    }else{
                        Toast.makeText(this,R.string.hostname_not_valid,Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(this,R.string.port_not_valid,Toast.LENGTH_SHORT).show();
                }
            }
            catch (NumberFormatException e){
                Toast.makeText(this,R.string.port_not_valid,Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
