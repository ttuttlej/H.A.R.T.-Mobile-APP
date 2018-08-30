package com.example.danie.hartapp.Main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.danie.hartapp.MapDisplay.MapActivity;
import com.example.danie.hartapp.MapDisplay.MapPresenter;


// grabs the reference to the main view
import com.example.danie.hartapp.R;

import net.ab0oo.aprs.parser.APRSPacket;
import net.ab0oo.aprs.parser.APRSTypes;
import net.ab0oo.aprs.parser.Position;
import net.ab0oo.aprs.parser.PositionPacket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import PacketGenerator.PacketList;

/**
 * This Presenter determines how the rest of the Program will execute when a
 * button is pressed.
 *
 * <P>This class holds the methods that are called when a button is pressed in
 * the {@link MainActivity}. Methods are responsible for Activity transitions
 * and logic responsible for setting up modules.</P>
 *
 * @see MainActivity
 */
public class MainPresenter implements MainContract.Presenter {

    //    private MainContract.MainView mView;
    private boolean receiving = false;


    /**
     * The handle the {@link MainPresenter} uses to access the {@link MainActivity}.
     * <p>
     * This variable is used for accessing any sort of show methods the
     * {@link MainActivity} may contain. And for use of CONTEXT.
     */
    private MainActivity mView;

    /**
     * Initializes {@link #mView} upon construction of {@link MainPresenter} object
     * to retain access to {@link MainActivity} object.
     *
     * @param view The Activity that created the {@link MainPresenter}
     */
    MainPresenter(MainContract.MainView view) {
        mView = (MainActivity) view;
    }

    /**
     * This method is called when the "Map" button is pressed.
     * <p>
     * Is responsible for handling the logic behind the transition from the
     * {@link MainActivity} to the {@link MapActivity}. Any logic that must be
     * executed before the Map is built should be written here. All other logic
     * regarding the Map should be written in the {@link MapPresenter}.
     *
     * @param view The Activity that created the {@link MainPresenter}.
     *             Should only ever be the {@link MainActivity}.
     * @see MainActivity#mPresenter
     */
    @Override
    public void handleButtonClick(View view) {
        Intent intent = new Intent(mView, MapActivity.class);
        mView.startActivity(intent);
    }

    public void requestList(View view) {
        Toast.makeText(mView, "writing .csv", Toast.LENGTH_SHORT).show();
        mView.sendRequest();
    }

    public void writeList(PacketList list) {
        if(list.size() > 0) {
            BufferedWriter writer = null;
            PositionPacket lastPos = (PositionPacket) list.getLast().getAprsInformation();
            String fileName = android.os.Environment.getDataDirectory() + "/" + lastPos.getPosition().getTimestamp() + "_Data.csv";
            try {
                writer = new BufferedWriter(new FileWriter(fileName, true));
                writer.write("Call Sign,Latitude,Longitude,Altitude,Time Stamp\n");
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    APRSPacket p = (APRSPacket) it.next();
                    PositionPacket posPac = (PositionPacket) p.getAprsInformation();
                    Position pos = posPac.getPosition();
                    writer.write(p.getSourceCall() + ","
                            + pos.getLatitude() + ","
                            + pos.getLongitude() + ","
                            + pos.getAltitude() + ","
                            + pos.getTimestamp() +
                            "\n");
                }
//            If altitude = -1, no altitude or whatever. Let the user know altitude is not included

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**************************************************
     * Author: Jesse Hillman
     *
     *  uses: import com.example.danie.hartapp.R;
     *        found at top of file.
     *
     *  parameters: The main activity view
     *
     *  Function: Grabs the text from the
     *            edit text that has the call sign.
     *
     *            Puts this text into a string and parses it.
     *
     *            changes the text on the button to say stop service.
     *
     *            passes the call sign to startPacketService
     *
     *            if the button says stop service
     *             1.) stops the service
     *             2.) changes the button to say stopping service
     *             3.) toasts the message stopping service
     *
     *
     * ********************************************************/
    public void startServiceButton(View view) {


        if (receiving == false) {
            // holds the callsign in a string
            String callSign = "";

            // reference to the edit text for retrieving the text.
            EditText enteredCallSign;
            enteredCallSign = this.mView.findViewById(R.id.enterCallsign);

            // reference to the button for changing text on it.
            Button button;
            button = this.mView.findViewById(R.id.startService);


            // get the callSign and make it upperCase
            callSign = enteredCallSign.getText().toString();
            callSign = callSign.toUpperCase();
            callSign = callSign.trim();

            Log.d("PACKET_SERVICE", "Button call sign: " + callSign);


            // testing what we got
            Toast.makeText(mView, "Starting service for " + callSign, Toast.LENGTH_SHORT).show();

            // change the button to say stop service.
            button.setText("Stop Service");


            //TODO get the call sign and start tracking
            mView.startPacketService(callSign);

            receiving = true;
        } else {
            // reference to the button for changing text on it.
            Button button;
            button = this.mView.findViewById(R.id.startService);

            // change the button to say start service.
            button.setText("Start Service");


            mView.stopPacketService();

            receiving = false;

            // testing what we got
            Toast.makeText(mView, "Stopping service", Toast.LENGTH_SHORT).show();
        }
    }
}
