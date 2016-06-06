package com.anthonydenaud.arkrcon.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.inject.Inject;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.dao.ServerDAO;
import com.anthonydenaud.arkrcon.model.Server;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class ServerConnectionActivity extends RoboActionBarActivity {

    @Inject
    private ServerDAO dao;

    @InjectView(R.id.name_edittext)
    private EditText nameEditText;

    @InjectView(R.id.hostname_edittext)
    private EditText hostnameEditText;

    @InjectView(R.id.rcon_port_edittext)
    private EditText rconPortEditText;

    @InjectView(R.id.query_port_edittext)
    private EditText queryPortEditText;

    @InjectView(R.id.password_edittext)
    private EditText passwordEditText;

    @InjectView(R.id.admin_name_edittext)
    private EditText adminNameEditText;

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
        if(server == null){
            server = new Server();
            server.setUuid(UUID.randomUUID().toString());
        }
        nameEditText.setText(server.getName());
        hostnameEditText.setText(server.getHostname());
        rconPortEditText.setText(String.valueOf(server.getPort()));
        passwordEditText.setText(server.getPassword());
        adminNameEditText.setText(server.getAdminName());
        if(server.getQueryPort() != 0){
            queryPortEditText.setText(String.valueOf(server.getQueryPort()));
        }
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
                int rconPort = Integer.parseInt(rconPortEditText.getText().toString());
                int queryPort = Integer.parseInt(queryPortEditText.getText().toString());

                String adminName = adminNameEditText.getText().toString();
                if(StringUtils.isEmpty(adminName)){
                    adminName = getString(R.string.default_admin_name);
                }


                if(rconPort > 0 && rconPort < 65535){
                    if(StringUtils.isNotEmpty(host)){
                        server.setName(nameEditText.getText().toString());
                        server.setHostname(host);
                        server.setPort(rconPort);
                        server.setQueryPort(queryPort);
                        server.setPassword(passwordEditText.getText().toString());
                        server.setAdminName(adminName);

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
