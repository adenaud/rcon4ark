package net.nexusrcon.nexusrconark.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.inject.Inject;

import net.nexusrcon.nexusrconark.R;
import net.nexusrcon.nexusrconark.dao.ServerDAO;
import net.nexusrcon.nexusrconark.model.Server;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class ServerConnectionActivity extends RoboActionBarActivity {

    @Inject
    private ServerDAO dao;

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

            server.setHostname(hostnameEditText.getText().toString());
            server.setPort(Integer.parseInt(portEditText.getText().toString()));
            server.setPassword(passwordEditText.getText().toString());

            dao.save(server);

            getIntent().putExtra("server",server);
            setResult(RESULT_OK,getIntent());

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
