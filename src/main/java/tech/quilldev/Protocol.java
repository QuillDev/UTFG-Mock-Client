package tech.quilldev;

public enum Protocol {
    KEEP_ALIVE("{QS:KEEP_ALIVE}"),
    END_SERVER("{QS:END_SERVER}"),
    ;

    private final String packet;

    /**
     * Create a new protocol
     * @param packet of the protocol
     */
    Protocol(String packet){
        this.packet = packet;
    }

    /**
     * Get the protocol matching the given string
     * @param compare the protocol
     * @return the protocol matching the string
     */
    public static Protocol getProtocol(String compare){
        for(var protocol : Protocol.values()){
            if(compare.contains(protocol.packet)){
                return protocol;
            }
        }

        return null;
    }
}



