package com.example.johan.autoraft;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Class used to communicate with raft software.
 */
public class RaftConnection {
    private static final String TAG = "RaftConnection";

    private BluetoothSocket mSocket = null;
    private BluetoothDevice mDevice = null;
    private InputStream mInStream = null;
    private OutputStream mOutStream = null;
    private java.util.UUID mID;
    public RaftConnection(UUID connection_id) {
        mID = connection_id;
    }

    public Boolean connect(BluetoothDevice device)
    {
        BluetoothSocket tmp = null;
        mDevice = device;
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(mID);
        } catch (IOException e) {
            Log.e(TAG, "createRfcommSocketToServiceRecord failed", e);
        }
        mSocket = tmp;

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        ba.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
          mSocket.connect();
        } catch (IOException connectException) {
            Log.e(TAG, "connection failed", connectException);
            // Unable to connect; close the socket and get out
            try {
                mSocket.close();
            } catch (IOException closeException) {
                return false;
            }
            return false;
        }
        try {
            mInStream = mSocket.getInputStream();
            mOutStream = mSocket.getOutputStream();
        } catch (IOException e) {
            Log.e("MyActivity", "temp sockets not created", e);
            return false;
        }
        return true;
    }


    private void sendCommand(String command) {

        byte[] send_b = command.getBytes();

        try {
            mOutStream.write(send_b);

        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

    public String getVersion()
    {
        sendCommand("version");
        //wait for response
        String response = getResponse();
        return response;
    }

    public String getCourseInfo()
    {
        sendCommand("course_info");
        //wait for response
        String response = getResponse();
        return response;
    }

    public ArrayList getRoutList()
    {
        ArrayList list = new ArrayList();
        sendCommand("route_list");
        Boolean done = false;
        String all_responses ="";
        while(!done) {
            String response = getResponse();
            if(response.equals("R Empty"))
                return list;

            //Add response to complete response string
            all_responses = all_responses + response;

            //if response hold "R " we have all routes and
            // we have to stop reading from socket
            if(response.indexOf("R ") > -1) {
                done = true;
            }
        }

        //parse out routs from complete response
        done = false;
        while(!done)
        {
            //remove "R-" or "R " at start of string
            all_responses = all_responses.substring(2,all_responses.length());

            //try find next route, delimiter is "R-" or "R "  (if last route)
            int i = all_responses.indexOf("R-");
            if(i == -1) {
                i = all_responses.indexOf("R ");
            }
            if(i > -1) {
                String element = all_responses.substring(0,i);
                all_responses = all_responses.substring(i,all_responses.length());
                list.add(element);
            }
            else //last element
            {
                list.add(all_responses);
                done= true;
            }
        }
        return list;
    }

    public Boolean startRout(String route_name){
        sendCommand("route_load " + route_name);

        //wait for response
        String response  = getResponse();
        if(response.indexOf("OK") < 0)
            return false;

        sendCommand("route_start");
        response  = getResponse();
        //if(response.indexOf("OK") < 0)
        //    return false;

        return true;
    }

    public Boolean stopRout(){
        sendCommand("route_stop");
        //wait for response
        String response  = getResponse();
        //if(response.indexOf("OK") < 0)
         //   return false;
        return true;
    }






    private String getResponse()
    {
        byte[] buffer = new byte[1024];
        int bytes;
        String response_message = "";
        Boolean has_start_r = false;
        while(true) {
            try {
                // Read from the InputStream
                bytes = mInStream.read(buffer);
                String readMessage = new String(buffer, 0, bytes);
                if(has_start_r || readMessage.startsWith("R ") || readMessage.startsWith("R-")){
                    has_start_r = true;
                    response_message = response_message + readMessage;
                    //check if we got new line
                    if(buffer[bytes] == 0)
                        return response_message;
                }
            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
                //connectionLost();
                return "";
            }
        }
    }

    /**
     * Will cancel an in-progress connection, and close the socket
     */
    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "failed to close connection", e);
        }
    }

}
