package tech.quilldev;

public class Packet {

    public final Protocol protocol;
    public final String data;

    /**
     * Construct a new packet
     * @param data to make packet from
     */
    public Packet(String data){
        this.protocol = Protocol.getProtocol(data);
        this.data = getData(data);
    }

    /**
     * If the packet is malformed, return null
     * @return whether the packet is malformed
     */
    public boolean isMalformed(){
        return this.protocol == null;
    }

    /**
     * Checks if the packet has data
     * @return whether the packet has data
     */
    public boolean isDataless(){
        return this.data == null;
    }

    /**
     * Get the data of the current packet
     * @param string to get data from
     * @return the packets data
     */
    public String getData(String string){
        if(this.protocol == null){
            return null;
        }

        var split = string.split("\\{QP:[A-Z_]+}");

        return (split.length <= 1) ? null : split[1];
    }
    @Override
    public String toString(){
        return String.format("Packet: {\n\tProtocol: %s,\n\tData: %s\n}\n", protocol, data);
    }
}
