package net.packet;

import java.io.ByteArrayOutputStream;

/**
 * Base of packets in both bounds.
 * This contains data as content of default TCP connection.
 */
public abstract class PacketBase { 
    
    protected int packetState = 0;
    public abstract byte getPacketID();
    public abstract boolean isServerBound();
    public abstract PacketBase readPacket(byte[] rawData); 

    public byte[] writePacket() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(this.getPacketID());
        baos.write(this.packetState);
        return baos.toByteArray();
    };

}