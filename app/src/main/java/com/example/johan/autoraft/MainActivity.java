package com.example.johan.autoraft;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.lang.Thread;


public class MainActivity extends Activity implements ActionBar.TabListener{

    private static final String TAG = "MainActivity";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter BA;
    private RaftConnection mRaftConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(savedInstanceState == null) {
            BA = BluetoothAdapter.getDefaultAdapter();
            if (!BA.isEnabled()) {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn, 0);
                try {
                    Thread.sleep(4000); //sleep to let bt detect devices
                } catch (Exception e) {

                }
            }

            if (!BA.isEnabled())
                showMessageAndExit("Failed Enable Bluetooth Adapter");

            //showMessage("Failed to connect!");

            //TODO: Add check that we have BT adapter

            BluetoothDevice autoraft_bt = getDeviceByName("AutoRaft");
            if (autoraft_bt != null) {
                connect(autoraft_bt);
                setupGUI();
            } else { //AutoRaft not found, show bt device connection dialog

                Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();

                //need to joggle with some lists.....probably better way
                ArrayList<String> list = new ArrayList<String>();
                for (BluetoothDevice bt : pairedDevices) {
                    list.add(bt.getName());
                }
                if(list.size() == 0)
                    showMessageAndExit("No paired devices found!");
                else {

                    final String[] items = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        items[i] = list.get(i);
                    }

                    AlertDialog.Builder connectionDialogBuilder = new AlertDialog.Builder(this);
                    //Create BT selection dialog template
                    connectionDialogBuilder.setTitle("Bluetooth device selection");
                    //mDialogBuilder.setMessage("No bluetooth device called AutoRaft found, please select other device!");
                    connectionDialogBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String selected = items[which];
                            dialog.dismiss();
                            connect(selected);
                            setupGUI();

                        }
                    });

                    //Create and show
                    AlertDialog connectionDialog = connectionDialogBuilder.create();
                    connectionDialog.show();
                }
            }
        }
    }

    private void showMessageAndExit(String message) {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(message);
        builder1.setCancelable(false);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                }
        );
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }


    public void setupGUI(){
        if(mRaftConnection == null){
            //failed to connect, exit gracefully...in someway
            return;
        }

        ActionBar actionbar = getActionBar();
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // create new tabs and and set up the titles of the tabs
        ActionBar.Tab mRouteTab = actionbar.newTab().setText("Route");
        ActionBar.Tab mSettingsTab = actionbar.newTab().setText("Settings");
        mRouteTab.setTabListener(this);
        mSettingsTab .setTabListener(this);

        Fragment mRouteFragment = new RouteFragment(mRaftConnection);
        Fragment mSettingsFragment = new SettingsFragment(mRaftConnection);

        mRouteTab.setTabListener(new AutoRaftTabsListener(mRouteFragment,
                getApplicationContext()));
        mSettingsTab .setTabListener(new AutoRaftTabsListener(mSettingsFragment,
                getApplicationContext()));

        actionbar.addTab(mRouteTab);
        actionbar.addTab(mSettingsTab);
        actionbar.setSelectedNavigationItem(0);
    }

    public boolean connect(String device_name) {
        BluetoothDevice bt = getDeviceByName(device_name);
        return connect(bt);
    }

    public boolean connect(BluetoothDevice bt) {
        if (bt != null) {
            //Toast.makeText(getApplicationContext(), "Start connection..",
            //        Toast.LENGTH_SHORT).show();
            // Start the thread to connect with the given device
            //mConnectThread = new ConnectThread(bt);
            //mConnectThread.start();
            mRaftConnection = new RaftConnection(MY_UUID);
            if(!mRaftConnection.connect(bt)){
                showMessageAndExit("Failed open connection to " + bt.getName() + " Please restart");
                return false;
            }
            else{
                Toast.makeText(getApplicationContext(), "Connected!",
                        Toast.LENGTH_SHORT).show();
            }

            return true;
        }
        return false;
    }

    public BluetoothDevice getDeviceByName(String dev_name) {
        Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();
        for (BluetoothDevice bt : pairedDevices) {
            if (bt.getName().equals(dev_name)) {
                return bt;
            }
        }
        return null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {

    }
}

class AutoRaftTabsListener implements ActionBar.TabListener {
    public Fragment fragment;
    public Context context;

    public AutoRaftTabsListener(Fragment fragment, Context context) {
        this.fragment = fragment;
        this.context = context;

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        Toast.makeText(context, "Reselected!", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        Toast.makeText(context, "Selected!", Toast.LENGTH_SHORT).show();
        ft.replace(R.id.fragment_container, fragment);
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        Toast.makeText(context, "Unselected!", Toast.LENGTH_SHORT).show();
        ft.remove(fragment);
    }
}

