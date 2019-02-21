package com.anthonydenaud.arkrcon.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.anthonydenaud.arkrcon.dao.DatabaseHelper;
import com.anthonydenaud.arkrcon.network.SteamQuery;
import com.anthonydenaud.arkrcon.network.SteamQueryException;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.dao.ServerDAO;
import com.anthonydenaud.arkrcon.model.Server;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;


import butterknife.BindView;
import butterknife.ButterKnife;

public class ServerConnectionActivity extends AppCompatActivity implements View.OnClickListener {

    ServerDAO dao;

    SteamQuery steamQuery;

    @BindView(R.id.name_edittext)
    EditText nameEditText;

    @BindView(R.id.hostname_edittext)
    EditText hostnameEditText;

    @BindView(R.id.rcon_port_edittext)
    EditText rconPortEditText;

    @BindView(R.id.query_port_edittext)
    EditText queryPortEditText;

    @BindView(R.id.password_edittext)
    EditText passwordEditText;

    @BindView(R.id.admin_name_edittext)
    EditText adminNameEditText;

    @BindView(R.id.fetch_name_button)
    ImageButton fetchNameButton;

    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_connection);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar_server_connection);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.dao = new ServerDAO(new DatabaseHelper(this)); //TODO Fix that crap too
        this.steamQuery = new SteamQuery();

        server = getIntent().getParcelableExtra("server");
        setTitle(getIntent().getIntExtra("titleId", R.string.edit_server));

        if (server == null) {
            server = new Server();
            server.setUuid(UUID.randomUUID().toString());
        }
        nameEditText.setText(server.getName());
        hostnameEditText.setText(server.getHostname());
        rconPortEditText.setText(String.valueOf(server.getPort()));
        passwordEditText.setText(server.getPassword());
        adminNameEditText.setText(server.getAdminName());
        if (server.getQueryPort() != 0) {
            queryPortEditText.setText(String.valueOf(server.getQueryPort()));
        }

        fetchNameButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_server_conneciton, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save) {

            try {

                String host = hostnameEditText.getText().toString();
                int rconPort = Integer.parseInt(rconPortEditText.getText().toString());
                int queryPort = Integer.parseInt(queryPortEditText.getText().toString());

                String adminName = adminNameEditText.getText().toString();
                if (StringUtils.isEmpty(adminName)) {
                    adminName = getString(R.string.default_admin_name);
                }


                if (rconPort > 0 && rconPort < 65535) {
                    if (StringUtils.isNotEmpty(host)) {
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
                    } else {
                        Toast.makeText(this, R.string.hostname_not_valid, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, R.string.invalid_rcon_port, Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.port_not_valid, Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view == fetchNameButton) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        String host = hostnameEditText.getText().toString();
                        int queryPort = Integer.parseInt(queryPortEditText.getText().toString());
                        steamQuery.connect(host, queryPort);
                        final String serverName = steamQuery.getServerName();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                nameEditText.setText(serverName);
                            }
                        });
                    }catch (SteamQueryException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ServerConnectionActivity.this, "Unable to fetch server name", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }catch (NumberFormatException e){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String message = getResources().getString(R.string.invalid_query_port);
                                Toast.makeText(ServerConnectionActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            thread.start();
        }
    }
}
