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
public class SettingsFragment extends Fragment{

    private RaftConnection mRaftConnection;
    private TextView mFMVersionText;

    public SettingsFragment(RaftConnection connection)  {
        super();
        mRaftConnection = connection;
    }

    public SettingsFragment(){
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.settings_fragment, container, false);
        mFMVersionText = (TextView) view.findViewById(R.id.fm_version_text);
        mFMVersionText.setText(mRaftConnection.getVersion());
        return view;
    }

}