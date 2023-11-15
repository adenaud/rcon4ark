package com.anthonydenaud.arkrcon;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.anthonydenaud.arkrcon.api.ApiCallback;
import com.anthonydenaud.arkrcon.event.AuthenticationListener;
import com.anthonydenaud.arkrcon.service.Rcon4GamesApiService;
import com.anthonydenaud.arkrcon.service.LogService;
import com.anthonydenaud.arkrcon.view.SettingsActivity;

import com.anthonydenaud.arkrcon.adapter.ServerAdapter;
import com.anthonydenaud.arkrcon.dao.ServerDAO;
import com.anthonydenaud.arkrcon.event.ConnectionListener;
import com.anthonydenaud.arkrcon.model.Server;
import com.anthonydenaud.arkrcon.service.ArkService;
import com.anthonydenaud.arkrcon.view.RconActivity;
import com.anthonydenaud.arkrcon.view.ServerConnectionActivity;
import com.anthonydenaud.arkrcon.view.ThemeActivity;

import javax.inject.Inject;

import timber.log.Timber;
import toothpick.Scope;
import toothpick.Toothpick;

public class MainActivity extends ThemeActivity
        implements AdapterView.OnItemClickListener, ConnectionListener, AuthenticationListener {

    ListView listView;

    TextView textViewNoServer;

    private ServerAdapter serverAdapter;

    @Inject ServerDAO serverDAO;

    @Inject ArkService arkService;

    @Inject LogService logService;

    @Inject
    Rcon4GamesApiService apiService;

    private Server currentServer;
    private ProgressDialog progressDialog;
    private boolean rconActivityStarted = false;

    private ActivityResultLauncher<Intent> serverActivityLauncher;
    private ActivityResultLauncher<Intent> rconActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Scope s = Toothpick.openScopes(getApplication(), this);
        Toothpick.inject(this, s);

        listView = findViewById(R.id.list_servers);
        textViewNoServer = findViewById(R.id.textview_noserver);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        this.serverAdapter = new ServerAdapter(this, serverDAO);

        registerForContextMenu(listView);

        apiService.checkAppUpdateAvailable(new ApiCallback() {
            @Override
            public void response(Object response) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage(R.string.update_available);
                        builder.setPositiveButton(R.string.update_now, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_store_url))));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_store_url))));
                                }
                                dialog.cancel();
                            }
                        });
                        builder.setNegativeButton(R.string.update_later, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                });
            }
        });

        listView.setOnItemClickListener(this);
        //logService.migrate(this);
        refresh();

        serverActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> refresh());

        rconActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
            arkService.removeAllConnectionListener();
            arkService.disconnect();
            rconActivityStarted = false;
        });
    }

    @Override
    protected void onResume() {
        refresh();
        super.onResume();
    }

    private void refresh() {
        serverAdapter.refresh();
        listView.setAdapter(serverAdapter);

        if (serverAdapter.getCount() > 0) {
            textViewNoServer.setVisibility(View.GONE);
        } else {
            textViewNoServer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.server_add) {
            Intent intent = new Intent(MainActivity.this, ServerConnectionActivity.class);
            intent.putExtra("titleId", R.string.new_server);
            this.serverActivityLauncher.launch(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Codes.RESULT_CONNECTION_DROP) {
            Toast.makeText(this, getString(R.string.conneciton_lost), Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_servers_floating, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Server server = (Server) listView.getItemAtPosition(info.position);
        if (R.id.menu_action_edit == item.getItemId()) {
            Intent intent = new Intent(MainActivity.this, ServerConnectionActivity.class);
            intent.putExtra("server", server);
            intent.putExtra("titleId", R.string.edit_server);
            this.serverActivityLauncher.launch(intent);
            return true;
        } else if(R.id.menu_action_delete == item.getItemId()){
            serverDAO.delete(server);
            refresh();
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        currentServer = serverAdapter.getItem(position);
        arkService.addConnectionListener(this);
        arkService.addAuthenticationListener(this);
        arkService.connect(currentServer);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.connecting));
        progressDialog.show();
    }



    @Override
    public void onDisconnect() {
        runOnUiThread(() -> {
            Timber.d("onDisconnect");
            if (!MainActivity.this.isDestroyed()) {
                Toast.makeText(MainActivity.this, getString(R.string.conneciton_lost), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onConnectionFail() {
        runOnUiThread(() -> {
            if (!MainActivity.this.isDestroyed()) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(MainActivity.this, getText(R.string.connection_fail), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onAuthenticationSuccess() {
        if (!rconActivityStarted) {
            Intent intent = new Intent(this, RconActivity.class);
            intent.putExtra("server", currentServer);
            rconActivityLauncher.launch(intent);

            runOnUiThread(() -> progressDialog.dismiss());
            rconActivityStarted = true;
        }
    }

    @Override
    public void onAuthenticationFail() {
        runOnUiThread(() -> {
            progressDialog.dismiss();
            Toast.makeText(MainActivity.this, getText(R.string.authentication_fail), Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onConnect(boolean reconnecting) {
    }
    @Override
    public void onConnectionDrop() {
    }
}
