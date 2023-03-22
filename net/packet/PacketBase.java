package net.packet;

/**
 * Base of packets in both bounds.
 * This contains data as content of default TCP connection.
 */
public abstract class PacketBase {

    protected int packetState = 0;
    public abstract byte getPacketID();
    


}