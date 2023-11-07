package com.anthonydenaud.arkrcon.view;

import android.os.Bundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.anthonydenaud.arkrcon.event.AuthenticationListener;
import com.anthonydenaud.arkrcon.event.ConnectionListener;
import com.anthonydenaud.arkrcon.event.ReceiveEvent;
import com.anthonydenaud.arkrcon.network.Packet;
import com.anthonydenaud.arkrcon.network.PacketType;
import com.anthonydenaud.arkrcon.network.SteamQuery;
import com.anthonydenaud.arkrcon.network.SteamQueryException;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.dao.ServerDAO;
import com.anthonydenaud.arkrcon.model.Server;
import com.anthonydenaud.arkrcon.service.ConnectionTestService;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import javax.inject.Inject;

import toothpick.Scope;
import toothpick.Toothpick;

public class ServerConnectionActivity extends ThemeActivity implements View.OnClickListener {

    @Inject
    ServerDAO dao;

    @Inject
    ConnectionTestService connectionTestService;

    SteamQuery steamQuery;

    LinearLayout step1Layout;
    LinearLayout step2Layout;
    LinearLayout step3Layout;
    EditText nameEditText;
    EditText hostnameEditText;
    EditText rconPortEditText;
    EditText queryPortEditText;
    EditText passwordEditText;
    EditText adminNameEditText;
    ImageButton fetchNameButton;
    Button testConnectionButton;
    ProgressBar progressBar;
    TextView testResultTextView;
    Button backButton;
    Button nextButton;
    Button finishButton;

    private Server server;

    private int currentStep;
    private List<LinearLayout> layouts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_connection);


        Scope s = Toothpick.openScopes(getApplication(), this);
        Toothpick.inject(this, s);
        Toolbar toolbar = findViewById(R.id.toolbar_server_connection);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        step1Layout = findViewById(R.id.step1_layout);
        step2Layout = findViewById(R.id.step2_layout);
        step3Layout = findViewById(R.id.step3_layout);

        nameEditText = findViewById(R.id.name_edittext);
        hostnameEditText = findViewById(R.id.hostname_edittext);
        rconPortEditText = findViewById(R.id.rcon_port_edittext);
        queryPortEditText = findViewById(R.id.query_port_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        adminNameEditText = findViewById(R.id.admin_name_edittext);
        fetchNameButton = findViewById(R.id.fetch_name_button);
        testConnectionButton = findViewById(R.id.test_button);
        progressBar = findViewById(R.id.test_connection_progress);
        testResultTextView = findViewById(R.id.test_result_text);
        backButton = findViewById(R.id.back_button);
        nextButton = findViewById(R.id.next_button);
        finishButton = findViewById(R.id.finish_button);

        this.steamQuery = new SteamQuery();
        this.currentStep = 1;
        this.layouts = new ArrayList<>();
        layouts.add(step1Layout);
        layouts.add(step2Layout);
        layouts.add(step3Layout);

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
        testConnectionButton.setOnClickListener(v -> this.testConnection());
        backButton.setOnClickListener(v -> this.back());
        nextButton.setOnClickListener(v -> this.next());
        finishButton.setOnClickListener(v -> this.save());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_server_conneciton, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (connectionTestService.isTestInProgress()) {
                connectionTestService.close();
            }
        }
        else if (id == R.id.save) {
            return save();
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
    }

    private void testConnection(){



        progressBar.setVisibility(View.VISIBLE);
        testResultTextView.setVisibility(View.GONE);
        testConnectionButton.setVisibility(View.GONE);



        String host = hostnameEditText.getText().toString();
        int port = Integer.parseInt(rconPortEditText.getText().toString());
        String password = passwordEditText.getText().toString();

        Server testServer = new Server();
        testServer.setHostname(host);
        testServer.setPort(port);
        testServer.setPassword(password);

        if(!connectionTestService.isTestInProgress()) {
            connectionTestService.test(testServer, new ConnectionListener() {
                @Override
                public void onConnectionFail() {
                    finishConnectionTest(false, false);
                }
                @Override
                public void onConnect(boolean reconnect) {
                }
                @Override
                public void onDisconnect() {
                }
                @Override
                public void onConnectionDrop() {
                }
            }, new AuthenticationListener() {
                @Override
                public void onAuthenticationSuccess() {
                    finishConnectionTest(true, true);
                }
                @Override
                public void onAuthenticationFail() {
                    finishConnectionTest(true, false);
                }
            });
        }
    }

    private void finishConnectionTest(boolean isConnected, boolean isAuthenticated) {
        runOnUiThread(() -> {
            int message;
            int color;
            if (isConnected && isAuthenticated) {
                message = R.string.test_connection_success;
                color = R.color.green;
            }
            else if (isConnected) {
                message = R.string.test_authentication_fail;
                color = R.color.red;
            }
            else {
                message = R.string.test_connection_fail;
                color = R.color.red;
            }
            testResultTextView.setText(message);
            testResultTextView.setTextColor(ContextCompat.getColor(ServerConnectionActivity.this, color));
            testResultTextView.setVisibility(View.VISIBLE);
            testConnectionButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        });
        connectionTestService.close();
    }

    private void back(){
        LinearLayout currentLayout = this.layouts.get(this.currentStep - 1);
        LinearLayout previousLayout = this.layouts.get((--this.currentStep) - 1);

        currentLayout.setVisibility(View.GONE);
        previousLayout.setVisibility(View.VISIBLE);

        if(currentStep == 1) {
            backButton.setVisibility(View.GONE);
            return;
        }
        backButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
        finishButton.setVisibility(View.GONE);

    }

    private void next(){

        if(currentStep == 1) {
            connectionTestService.close();
        }

        LinearLayout currentLayout = this.layouts.get(this.currentStep - 1);
        LinearLayout nextLayout = this.layouts.get((++this.currentStep) - 1);

        currentLayout.setVisibility(View.GONE);
        nextLayout.setVisibility(View.VISIBLE);

        if (this.currentStep == layouts.size()){
            nextButton.setVisibility(View.GONE);
            finishButton.setVisibility(View.VISIBLE);
            return;
        }
        backButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
    }

    private boolean save() {
        if (connectionTestService.isTestInProgress()) {
            connectionTestService.close();
        }

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
}
