package com.anthonydenaud.arkrcon.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.anthonydenaud.arkrcon.event.ConnectionListener;
import com.anthonydenaud.arkrcon.network.SteamQuery;
import com.anthonydenaud.arkrcon.network.SteamQueryException;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.dao.ServerDAO;
import com.anthonydenaud.arkrcon.model.Server;
import com.anthonydenaud.arkrcon.service.ArkService;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;


import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import toothpick.Scope;
import toothpick.Toothpick;

public class ServerConnectionActivity extends ThemeActivity implements View.OnClickListener {

    @Inject
    ServerDAO dao;

    @Inject
    ArkService arkService;

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

    @BindView(R.id.test_button)
    Button testConnectionButton;

    @BindView(R.id.test_connection_progress)
    ProgressBar progressBar;

    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_connection);
        ButterKnife.bind(this);
        Scope s = Toothpick.openScopes(getApplication(), this);
        Toothpick.inject(this, s);
        Toolbar toolbar = findViewById(R.id.toolbar_server_connection);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        testConnectionButton.setOnClickListener(this);
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

            if(StringUtils.isNotEmpty(queryPortEditText.getText().toString())){
                try{
                    int queryPort = Integer.parseInt(queryPortEditText.getText().toString());
                    server.setQueryPort(queryPort);
                } catch (NumberFormatException e){
                    Toast.makeText(this, R.string.invalid_query_port, Toast.LENGTH_SHORT).show();
                    return true;
                }
            }

            try {

                String host = hostnameEditText.getText().toString();
                int rconPort = Integer.parseInt(rconPortEditText.getText().toString());

                String adminName = adminNameEditText.getText().toString();
                if (StringUtils.isEmpty(adminName)) {
                    adminName = getString(R.string.default_admin_name);
                }

                if (rconPort > 0 && rconPort < 65535) {
                    if (StringUtils.isNotEmpty(host)) {
                        server.setName(nameEditText.getText().toString());
                        server.setHostname(host);
                        server.setPort(rconPort);
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
                Toast.makeText(this, R.string.invalid_rcon_port, Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view == fetchNameButton) {
            Thread thread = new Thread(() -> {
                try {

                    String host = hostnameEditText.getText().toString();
                    int queryPort = Integer.parseInt(queryPortEditText.getText().toString());
                    steamQuery.connect(host, queryPort);
                    final String serverName = steamQuery.getServerName();

                    runOnUiThread(() -> nameEditText.setText(serverName));
                }catch (SteamQueryException e) {
                    runOnUiThread(() -> Toast.makeText(ServerConnectionActivity.this, "Unable to fetch server name", Toast.LENGTH_SHORT).show());
                }catch (NumberFormatException e){
                    runOnUiThread(() -> {
                        String message = getResources().getString(R.string.invalid_query_port);
                        Toast.makeText(ServerConnectionActivity.this, message, Toast.LENGTH_SHORT).show();
                    });
                }
            });
            thread.start();
        }

        if (view == testConnectionButton) {
            testConnection();
        }
    }

    private void testConnection(){

        progressBar.setVisibility(View.VISIBLE);

        arkService.addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnect(boolean reconnect) {
                runOnUiThread(() -> hideProgressBar());
            }
            @Override
            public void onConnectionFail() {
                runOnUiThread(() -> hideProgressBar());
            }

            @Override
            public void onDisconnect() {}
            @Override
            public void onConnectionDrop() {}
        });
        arkService.connect(server);
        arkService.removeAllConnectionListener();

    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}
