package packet;

public class P0Handshake extends PacketBase {

    
    @Override
    public byte getPacketID() {
        return 0x00;
    }

}
