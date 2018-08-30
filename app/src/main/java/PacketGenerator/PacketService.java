package PacketGenerator;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.danie.hartapp.R;

import net.ab0oo.aprs.parser.APRSPacket;

import java.util.Iterator;
import java.util.Objects;

public class PacketService extends Service {

    private PacketList list;
    APRSdroidEventReceiver receiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        list = new PacketList();
        receiver = new APRSdroidEventReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("org.aprsdroid.app.POSITION");
        filter.addAction("org.aprsdroid.app.UPDATE");
        filter.addAction("com.example.danie.hartapp.MapDisplay.GET_LIST");
        filter.addAction("com.example.danie.hartapp.Main.GET_LIST");
        filter.addAction("com.example.danie.hartapp.Main.START");
        filter.addAction("com.example.danie.hartapp.Main.STOP");
        filter.addAction("com.example.danie.hartapp.Main.CLEAR");
        filter.addAction("com.example.danie.hartapp.Main.INIT_LIST");
        registerReceiver(receiver, filter);
        list = new PacketList("");
        Log.d("PACKET_SERVICE", "SERVICE STARTED!!!!!!!!!!!!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


//    public int onStartCommand (Intent intent,
//                               int flags,
//                               int startId) {
//        return super.onStartCommand(intent, flags, startId);
//
//        String callSign = intent.getStringExtra("callSign");
//        Log.d("PACKET_SERVICE", "Initialized list: " + callSign);
//        if(!list.getCallSign().equals(callSign)) {
//            list = new PacketList(callSign);
//        }
//
//    }


    public class APRSdroidEventReceiver extends BroadcastReceiver {

        public APRSdroidEventReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("PACKET_SERVICE", intent.getAction() + "\n" + intent.getStringExtra("packet"));

            String a = intent.getAction();


            switch (a) {
                case "org.aprsdroid.app.POSITION":
                    String packetString = intent.getStringExtra("packet");
                    Log.d("PACKET_SERVICE", "received: " + packetString);
                    try {
                        if (list.addPacket(packetString)) {
                            sendList();
                        }
                    }
                    catch (Exception e) {
                        System.out.println("UNABLE TO PARSE");
                        Log.d("PACKET_SERVICE", "UNABLE TO PARSE: " + packetString);
                        e.getStackTrace().toString();
                    }
                    break;

                case "com.example.danie.hartapp.Main.GET_LIST":
                    Intent listIntent = new Intent();
                    listIntent.setAction("PacketGenerator.PacketService.WRITE_LIST");
                    listIntent.putExtra("packetList", list);
                    sendBroadcast(listIntent);
                    break;

                case "com.example.danie.hartapp.Main.INIT_LIST":
                    String callSign = intent.getStringExtra("callSign");
                    Log.d("PACKET_SERVICE", "Initialized list: " + callSign);
                    if (!list.getCallSign().equals(callSign)) {
                        list = new PacketList(callSign);
                    }
                    break;

                case "com.example.danie.hartapp.MapDisplay.GET_LIST":
                    sendList();
                    break;
            }
        }
    }



    private void sendList() {
        Log.d("PACKET_SERVICE", "Sending List: " + list);
        Intent listIntent = new Intent();
        listIntent.setAction("PacketGenerator.PacketService.NEW_LIST");
        listIntent.putExtra("packetList", list);
        sendBroadcast(listIntent);
        Log.d("PACKET_SERVICE", "sent List" + listIntent.toString());
    }
}
