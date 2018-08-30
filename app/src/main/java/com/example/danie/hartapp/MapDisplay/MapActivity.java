package com.example.danie.hartapp.MapDisplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.danie.hartapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.Objects;

import PacketGenerator.PacketList;

/**
 * Used for display methods and User interaction
 *
 * <P>This Activity is the View in the MVP design pattern. No logic should be
 * performed in this class. Information should be passed back to Presenter class
 * for any other actions. This class is responsible for displaying map fragment
 * to screen and initializing API interaction.</P>
 *
 * @see MapContract
 */
public class MapActivity extends AppCompatActivity implements MapContract.MapView{

    private static final String TAG = "MapActivity";

    //Allows the MapView to update the presenter
    /**
     * Used as a handle to allow interaction with Presenter from View
     *
     * <P>Allows for the View to send data </P>
     */
    private MapPresenter mapPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapPresenter = new MapPresenter(this);
        Intent getList = new Intent("com.example.danie.hartapp.MapActvivty.MapPresenter.GET_LIST");
        sendBroadcast(getList);
        MapReceiver receiver = new MapReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("PacketGenerator.PacketService.NEW_LIST");
        registerReceiver(receiver, filter);
        beginMap();
    }

    /**
     * Starts the build of the map
     */
    private void beginMap(){
        if (mapPresenter.getLocationPermission()){
            initMap();

            //Gathers the current location of the phone and adds it to the list
            mapPresenter.getDeviceLocation();

            //Moves the camera to the current location of phone
//            mapPresenter.moveCamera();
        }
    }

    /**
     * Initializes the Map fragment
     *
     * <P>Pulls the fragment from the XML sets it to the screen. Then calls
     * {@link MapPresenter#onMapReady(GoogleMap)} using the "getMapAsync()"
     * function. The "getMapAsync()" function is executed through the API and
     * only if map is ready. Function must remain in the Activity as it is
     * tied to the XML</P>
     *
     * @see MapPresenter#onMapReady(GoogleMap)
     */
    public void initMap() {
        Log.d(TAG, "initMap: initializing map");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(mapPresenter);
    }

    /**************************************************
     Braden's aprs stuff
     **************************************************/
    public class MapReceiver extends BroadcastReceiver {

        public MapReceiver()
        {}

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("PACKET_SERVICE", "map received intent: " + intent.toString());

                String a = intent.getAction();
                switch (a) {
                    case "PacketGenerator.PacketService.NEW_LIST":
                        for(String x : intent.getExtras().keySet()) {
                            Log.d("PACKET_SERVICE", "Received list intent: " + x);
                        }
                        Bundle bundle = intent.getExtras();
                        PacketList list = (PacketList) bundle.getSerializable("packetList");
                        mapPresenter.setList(list);
                        Log.d("PACKET_SERVICE", "map called setList");
                        mapPresenter.updateMap();
                        Log.d("PACKET_SERVICE", "map called updateMap");
                        break;

            }
        }
    }
}