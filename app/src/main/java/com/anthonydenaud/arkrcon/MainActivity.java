package com.anthonydenaud.arkrcon;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.anthonydenaud.arkrcon.api.ApiCallback;
import com.anthonydenaud.arkrcon.service.Rcon4GamesApiService;
import com.anthonydenaud.arkrcon.service.LogService;
import com.anthonydenaud.arkrcon.view.SettingsActivity;
import com.google.inject.Inject;

import com.anthonydenaud.arkrcon.adapter.ServerAdapter;
import com.anthonydenaud.arkrcon.dao.ServerDAO;
import com.anthonydenaud.arkrcon.event.ConnectionListener;
import com.anthonydenaud.arkrcon.model.Server;
import com.anthonydenaud.arkrcon.service.ArkService;
import com.anthonydenaud.arkrcon.view.RconActivity;
import com.anthonydenaud.arkrcon.view.ServerConnectionActivity;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

public class MainActivity extends RoboActionBarActivity implements AdapterView.OnItemClickListener, ConnectionListener {

    @InjectView(R.id.list_servers)
    private ListView listView;

    @InjectView(R.id.textview_noserver)
    private TextView textViewNoServer;

    @Inject
    private ServerAdapter serverAdapter;

    @Inject
    private ServerDAO serverDAO;

    @Inject
    private ArkService arkService;

    @Inject
    private LogService logService;

    @Inject
    private Rcon4GamesApiService apiService;

    private Server currentServer;
    private ProgressDialog progressDialog;
    private boolean rconActivityStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        registerForContextMenu(listView);

        RavenLogger.getInstance().init(this);

        try {
            Integer.parseInt("ab");
        }catch (NumberFormatException e){
            RavenLogger.getInstance().error(MainActivity.class, "test error", e);
        }

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
        apiService.saveUser();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, ServerConnectionActivity.class);
                    intent.putExtra("titleId", R.string.new_server);
                    startActivityForResult(intent, Codes.REQUEST_NEW_SERVER);
                }
            });
        }

        listView.setOnItemClickListener(this);
        logService.migrate(this);
        refresh();
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
            startActivityForResult(intent, Codes.REQUEST_SETTINGS);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Codes.REQUEST_NEW_SERVER || requestCode == Codes.REQUEST_EDIT_SERVER) {
            refresh();
        }
        if (requestCode == Codes.REQUEST_RCON_CLOSE) {
            arkService.removeAllConnectionListener();
            arkService.disconnect();
            rconActivityStarted = false;
        }

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
        switch (item.getItemId()) {
            case R.id.menu_action_edit:
                Intent intent = new Intent(MainActivity.this, ServerConnectionActivity.class);
                intent.putExtra("server", server);
                intent.putExtra("titleId", R.string.edit_server);
                startActivityForResult(intent, Codes.REQUEST_EDIT_SERVER);
                return true;
            case R.id.menu_action_delete:
                serverDAO.delete(server);
                refresh();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        currentServer = serverAdapter.getItem(position);
        arkService.addConnectionListener(this);
        arkService.connect(currentServer);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.connecting));
        progressDialog.show();
    }

    @Override
    public void onConnect(boolean reconnecting) {
        if (!rconActivityStarted) {
            Intent intent = new Intent(this, RconActivity.class);
            intent.putExtra("server", currentServer);
            startActivityForResult(intent, Codes.REQUEST_RCON_CLOSE);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            });
            rconActivityStarted = true;
            apiService.saveServer(currentServer);
        }
    }

    @Override
    public void onDisconnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Ln.d("onDisconnect");
                Toast.makeText(MainActivity.this, getString(R.string.conneciton_lost), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onConnectionFail(final String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onConnectionDrop() {
    }
}
