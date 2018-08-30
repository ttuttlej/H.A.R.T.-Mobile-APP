package PacketGenerator;

import android.support.annotation.NonNull;
import android.util.Log;

import net.ab0oo.aprs.parser.APRSPacket;
import net.ab0oo.aprs.parser.Parser;
import net.ab0oo.aprs.parser.Position;
import net.ab0oo.aprs.parser.PositionPacket;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

public class PacketList implements Iterable, Serializable {
    private LinkedList<APRSPacket> packetList;
    private String callSign;

    public PacketList() {
        packetList = new LinkedList<>();
        callSign = "";
    }

    public PacketList(String callSign)
    {
        this.callSign = callSign;
        packetList = new LinkedList<>();
    }

    public boolean addPacket(String rawPacket) throws Exception
    { boolean added = false;
            Log.d("PACKET_SERVICE", "parsing: " + rawPacket);
            APRSPacket packet = Parser.parse(rawPacket);
            Log.d("PACKET_SERVICE", "finnished parsing");
            Log.d("PACKET_SERVICE", packet.toString());
            Log.d("PACKET_SERVICE", "APRSPacket callSign: " + packet.getSourceCall());
            if (packet.getSourceCall().equals(this.callSign)) {
                packetList.add(packet);
                Log.d("PACKET_SERVICE", "PacketList added new packet" + packet.toString());
                added = true;
                Log.d("PACKET_SERVICE", "added packet to LIST: " + packet.toString());
            }
        return added;
    }

    public void addPacket (APRSPacket packet) {
        packetList.add(packet);
    }

    public String getCallSign() {return callSign;}

    public APRSPacket getCurrent() {
        return packetList.getLast();
    }

    private int getAltitude() {
        PositionPacket posPack = (PositionPacket) packetList.getLast().getAprsInformation();
        Position pos = posPack.getPosition();
        int altitude = pos.getAltitude();
        return altitude;
    }

    public APRSPacket getLast() {
        return packetList.getLast();
    }

    public int size() {
        return packetList.size();
    }

    private int getSize() {
        return packetList.size();
    }

    @NonNull
    @Override
    public Iterator iterator() {
        return packetList.iterator();
    }

    @Override
    public String toString() {
        String retVal = callSign + ": " + packetList.size();

        return retVal;
    }
}
