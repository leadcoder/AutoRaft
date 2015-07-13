package com.example.johan.autoraft;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;


import java.util.ArrayList;


/**
 * Created by johan on 2014-08-13.
 */
public class RouteFragment extends Fragment implements OnClickListener {

    private RaftConnection mRaftConnection;
    private Button mConnectButton;
    private TextView mRouteText;
    private Boolean mRoutStarted;
    private static final String START_ROUTE_MESSAGE = "Click button to select and start new route";
    private static final String START_ROUTE_BUTTON_TEXT = "Start Route...";
    private static final String STOP_ROUTE_BUTTON_TEXT = "Stop Route";

    public RouteFragment(RaftConnection connection)  {
        super();
        mRaftConnection = connection;
        mRoutStarted = false;
    }

    public RouteFragment(){
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.route_fragment, container, false);
        mConnectButton = (Button) view.findViewById(R.id.route_button);
        mRouteText = (TextView) view.findViewById(R.id.route_text);
        mRouteText.setText(START_ROUTE_MESSAGE);
        mConnectButton.setOnClickListener(this);
        mConnectButton.setText(START_ROUTE_BUTTON_TEXT);
        return view;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.route_button:

                if(mRoutStarted){

                    Boolean response = mRaftConnection.stopRout();
                    //Toggle button text and function
                    mConnectButton.setText(START_ROUTE_BUTTON_TEXT);
                    mRouteText.setText(START_ROUTE_MESSAGE);
                    mRoutStarted = false;
                }
                else {
                    ArrayList<String> list = mRaftConnection.getRoutList();
                    if (list.size() > 0) {
                        final String[] items = new String[list.size()];
                        for (int i = 0; i < list.size(); i++) {
                            items[i] = list.get(i);
                        }

                        //Create BT selection dialog template
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setTitle("Select route");
                        dialogBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String selected = items[which];
                                Boolean response = mRaftConnection.startRout(selected);
                                if (response){
                                    Toast.makeText(getActivity().getBaseContext(), "Route Started!", Toast.LENGTH_SHORT).show();

                                    //Toggle button text and function
                                    mConnectButton.setText(STOP_ROUTE_BUTTON_TEXT);
                                    mRoutStarted = true;
                                    //Change text
                                    mRouteText.setText("Current Route: " + selected);

                                }
                                else
                                    Toast.makeText(getActivity().getBaseContext(), "Failed to start route", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                        AlertDialog routeDialog = dialogBuilder.create();
                        routeDialog.show();
                    } else {
                        Toast.makeText(
                                getActivity().getBaseContext(),
                                "No routes available",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
 }