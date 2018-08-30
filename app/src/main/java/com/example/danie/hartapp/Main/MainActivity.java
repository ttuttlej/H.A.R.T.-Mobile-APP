package com.example.danie.hartapp.Main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.example.danie.hartapp.MapDisplay.MapActivity;
import com.example.danie.hartapp.R;
import com.example.danie.hartapp.databinding.ActivityMainBinding;

import PacketGenerator.PacketList;
import PacketGenerator.PacketService;

/**
 * Holds all of the buttons for the APP
 *
 * <P>This Activity is responsible for being the First page seen when the
 * app is opened. Each button on this page is responsible for taking the user
 * to a new module or chain of logic (the map button takes the user to the map).
 * This class is purely responsible for holding the buttons, no logic should take
 * place here. The MainActivity is the first object created by Android upon start
 * up.
 * </P>
 *
 * @see #mPresenter
 * @see MainPresenter
 * @see MainContract
 */
public class MainActivity extends AppCompatActivity implements MainContract.MainView {

    /**
     * Holds the {@link MainPresenter} object.
     *
     * <P> The XML tied to the {@link MainActivity} is bound to the
     * {@link MainPresenter} through binding the XML to this variable.
     * Only ONE should ever be made as this object is responsible for
     * executing the rest of the program.</P>
     *
     * @see MainPresenter
     * @see MainActivity
     */
    private MainPresenter mPresenter;

    private EditText callSign;

    /**
     * Starts overall processes relevant to the entire app.
     *
     * <P>Binding takes place here, along with the creation of {@link #mPresenter}
     * to create a quick transition from the {@link MainActivity} to the
     * {@link MainPresenter}. Services are started to ensure they are already running
     * when buttons are pressed.</P>
     *
     * @param savedInstanceState Created and only used by Android
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Binding the XML to the presenter rather than to the Activity
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mPresenter = new MainPresenter(this);
        binding.setPresenter(mPresenter);

        // launch APRSdroid tracker
        Intent i = new Intent("org.aprsdroid.app.SERVICE").setPackage("org.aprsdroid.app");
        startService(i);
        Intent startTracking = new Intent(this, PacketService.class);
        startService(startTracking);
        MainReceiver receiver = new MainReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("PacketGenerator.PacketService.WRITE_LIST");
        registerReceiver(receiver, filter);
    }



    /**************************************************
     Bracen's aprs stuff
     **************************************************/
    public void startPacketService(String callSign)
    {
        Log.d("PACKET_SERVICE", "Activity call sign: " + callSign);
        Intent intent = new Intent("com.example.danie.hartapp.Main.INIT_LIST");
        intent.putExtra("callSign", callSign);
        Log.d("PACKET_SERVICE", intent.toString());
        sendBroadcast(intent);
    }

    public void stopPacketService() {
        Intent stopTracking = new Intent(this, PacketService.class);
        stopService(stopTracking);
    }

    public void sendRequest() {
        Intent intent = new Intent("com.example.danie.hartapp.Main.GET_LIST");
        sendBroadcast(intent);
    }

    public class MainReceiver extends BroadcastReceiver {

        public MainReceiver()
        {}

        @Override
        public void onReceive(Context context, Intent intent) {

            String a = intent.getAction();
            switch (a) {
                case "PacketGenerator.PacketService.WRITE_LIST":
                    Bundle bundle = intent.getExtras();
                    PacketList list = (PacketList) bundle.getSerializable("packetList");
                    mPresenter.writeList(list);
                    break;

            }
        }
    }
}
