package com.example.danie.hartapp.MapDisplay;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import net.ab0oo.aprs.parser.APRSPacket;
import net.ab0oo.aprs.parser.Position;
import net.ab0oo.aprs.parser.PositionPacket;

import java.util.Iterator;
import java.util.LinkedList;

import PacketGenerator.PacketList;

/**
 * The Presenter for controlling the logic within the MapDisplay module
 *
 * Only ONE should ever be created. Methods are responsible for any logic that
 * that must be executed in regard to the map.
 *
 * @see com.example.danie.hartapp.Main.MainPresenter
 * @see MapActivity
 */


public class MapPresenter implements MapContract.MPresenter, OnMapReadyCallback {

    private static final String TAG = "MapPresenter";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 13f;
    private static final int ERROR_DIALOG_REQUEST = 9001;

    //widgets
    private ImageView mGps;

    //vars
    private Boolean mLocationPermissionsGranted = false;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProvidedClient;
    /**
     * Creates the Marker that displays the text window
     */
    private Marker mMarker;


    /**
     * Used for accessing the MapView when {@link MapContract} interface methods
     * need to be abide by. Should be used over {@link #mapActivity} in order to
     * reduce coupling and increase cohesion.
     *
     * @see #mapActivity
     */
    private MapContract.MapView mView;
    private PacketList list;

    /**
     * Holds the Activity that created the presenter. Variable is used as a handle
     * on the Activity so that the Presenter may have access to Activity methods.
     * Is primarily used in the instance of when Activity Context is required.
     * As this object is not bound by {@link MapContract}, it can be dangerous if used
     * inappropriately.
     *
     * @see #mView
     * @see MapActivity
     * @see MapPresenter
     */
    private MapActivity mapActivity;

    /**
     * Contains a list of Location points collected during flight.
     *
     * <P>The length of the list is dependent on how long the balloon has been in
     * flight. Most recent location is stored at the end of the List</P>
     */
    private LinkedList<LatLng> balloonPath;

    /**
     * Initializes all variables to be used by the presenter.
     *
     * @see #mView
     * @param view The activity that created the presenter
     */
    public MapPresenter(MapContract.MapView view) {
        mView = view;
        mapActivity = (MapActivity) mView;
        balloonPath = new LinkedList<>();
    }

    /**********************************************************
     * @author: Jesse Hillman
     *
     * This function is responsible for getting the bearing or angle
     * between north and the balloon.
     *
     * @param userXcoordinate is the x coordinate for user
     * @param DestinationY is the y coordinate for user,
     * @pax coordinate for balloon, y coordinate for balloon.
     * returns: bearing / angle between two points as a float.
     *
     * *********************************************************/
    public float getBearing(float userXcoordinate, float playerYcoordinate, float DestinationX, float DestinationY)
    {

        // find true north
        float trueNorthX = userXcoordinate;
        float trueNorthY = DestinationY + 20;

        // coordinates
        // where I am , where I want to go
        // (33,120) , (34,118)
        // y = y2 - y1
        float y = DestinationY - trueNorthY;

        // x = x2 - x1
        float x = DestinationX - trueNorthY;

        // takes y, and x and turns it into degrees.
        double degrees = Math.atan2(y,x);

        // takes the x and y and turns it into a angle in degrees.
        float angle = (float) Math.toDegrees(Math.atan2(y, x));

        if(angle < 0) {
            angle += 360;
        }

        // holds the bearing
        float bearing = angle;

        // rounds it.
        bearing = Math.round(bearing);

        // gives the user the bearing.
        return bearing;
    }

    /**
     * Asks the user for permission to get their location
     *
     * A method required by Android when completing certain actions (like accessing
     * current location). Permissions must be granted for API to function properly.
     *
     * @return Whether or not the permission has been granted.
     */
    public boolean getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {FINE_LOCATION, COURSE_LOCATION};

        if (ContextCompat.checkSelfPermission(mapActivity, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(mapActivity, COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
            } else {
                ActivityCompat.requestPermissions(mapActivity, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(mapActivity, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return mLocationPermissionsGranted;
    }

    /**
     * Builds the map and draws the markers on top of it
     *
     * <P>Method is called by the Google maps API when the map is considered "Ready." This
     * occurs after {@link MapActivity#initMap()} has finished executing. Initializes {@link #mMap}
     * to store the map created and calls all drawing methods to draw balloon path. Method returns without
     * execution if permissions are denied.
     * </P>
     *
     * @param googleMap is the map built by the API to be displayed
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(mapActivity, "map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {

            if (ActivityCompat.checkSelfPermission(mapActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mapActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);

            //Disables the auto center button
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

//            updateBalloonPath(new LatLng(23,34));
            if (balloonPath.size() > 0) {
                drawBalloonPath();
                moveCamera();
            }
        }
    }

    /**
     * draws the balloon path on the map according to the points in {@link #balloonPath}
     */
    public void drawBalloonPath(){
        Polyline ballonPathDraw = mMap.addPolyline(new PolylineOptions());
        ballonPathDraw.setPoints(balloonPath);
    }

    /**
     * Method develops interaction with the API.
     *
     * <P>Method is used for setting up {@link #mFusedLocationProvidedClient} to properly
     * interact with API. Method is intended for pulling the current location of the phone in the
     * {@link OnCompleteListener}.</P>
     */
    public void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the deveices current location");

        mFusedLocationProvidedClient = LocationServices.getFusedLocationProviderClient(mapActivity);

        try {

            if (true){
                final Task location = mFusedLocationProvidedClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");

                            Location currentLocation = (Location) task.getResult();

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(mapActivity, "unable to get current location ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: Security Exception: " + e.getMessage());
        }
    }

    /**
     * Creates a String of information to be displayed
     *
     * <P>Method places string of information next to marker on map. Text is
     * is displayed in the form of a box, containing a linear string. List
     * format is developed in CustomInfoWindowAdapter.</P>
     */
    private void createSnippet(){
        String snippet = "Latitude: " + balloonPath.getLast().latitude + "\n" +
                "Longitude: " + balloonPath.getLast().longitude + "\n" +
                "Altitude" + "\n";

        MarkerOptions options = new MarkerOptions()
                .position(balloonPath.getLast())
                .snippet(snippet);

        mMarker = mMap.addMarker(options);
    }

    /**
     * Adds the most recent GPS coordinate of the balloon to the end of {@link #balloonPath}.
     *
     * @param currentLocation The Gps coordinate of where the balloon currently is.
     *                        The coordinate is retrieved from APRS packets.
     */
    private void updateBalloonPath(LatLng currentLocation) {
        balloonPath.add(currentLocation);
    }

    /**
     * Moves the camera to the most recent balloon location
     */
    public void moveCamera() {
        //The most recent location is the last element in balloonPath
        Log.d(TAG, "moveCamera: moving the camera to: lat:" + balloonPath.getLast().latitude + ", lng: " + balloonPath.getLast().longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(balloonPath.getLast(), DEFAULT_ZOOM));

//        createSnippet();
//        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(mapActivity));
    }

    /**************************************************
    Braden's aprs stuff
     **************************************************/
    public void setList(PacketList list) {
        this.list = list;
        LinkedList<LatLng> newPath = new LinkedList<>();
        if (list != null) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                APRSPacket p = (APRSPacket) iter.next();
                newPath.add(getAPRSlocation(p));
            }
            balloonPath = newPath;
        }
    }

    /**
     * Calls methods necessary to update map when packet is received.
     *
     * <P>When a packet is received through the bluetooth, this method is
     * called.</P>
     */
    public void updateMap() {
        Log.d("PACKET_SERVICE", "Updating map");
        LatLng currentLatLng = getAPRSlocation(list.getCurrent());
        drawBalloonPath();
        moveCamera();
        Log.d("PACKET_SERVICE", "map Updated");
    }

    private LatLng getAPRSlocation(APRSPacket packet)
    {
        PositionPacket posPack = (PositionPacket) packet.getAprsInformation();
        Position pos = posPack.getPosition();
        double lat = pos.getLatitude();
        double lng = pos.getLongitude();
        LatLng place = new LatLng(lat, lng);
        return place;
    }
}

