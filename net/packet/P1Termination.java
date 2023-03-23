package net.packet;

public class P1Termination extends PacketBase {

    @Override
    public byte getPacketID() {
        return 0x01;
    }

    @Override
    public boolean isServerBound() {
        return this.packetState == 0;
    }

    @Override
    public PacketBase readPacket(byte[] rawData) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readPacket'");
    }

    @Override
    public byte[] writePacket() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writePacket'");
    }
    
}
