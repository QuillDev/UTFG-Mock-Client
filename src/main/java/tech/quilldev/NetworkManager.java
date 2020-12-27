package tech.quilldev;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class NetworkManager {

    // booleans for managing connection state
    private Socket clientSocket;

    // the connection state of the socket
    private ConnectionState connectionState;

    // create the network manager
    public NetworkManager() {

        // start off as a disconnected socket
        this.connectionState = ConnectionState.DISCONNECTED;

        // create the socket
        this.clientSocket = null;
    }

    /**
     * Try to connect to the given host and port
     * @param host to connect to
     * @param port of the host
     */
    public void connect(String host, int port) {

        new Thread(() -> {
            // if we're anything but disconnected, return
            if (!this.connectionState.equals(ConnectionState.DISCONNECTED)) {
                return;
            }

            try {

                // set the socket state to connecting
                this.connectionState = ConnectionState.CONNECTING;

                // address to connect with
                var address = new InetSocketAddress(host, port);

                //close the current socket connection if there is one
                this.closeSocket();

                //try to connect to the new address
                this.clientSocket.connect(address, 1000);

                // if we connect set the connection state to connected
                this.connectionState = ConnectionState.CONNECTED;

            } catch (IOException ignored) {
                this.closeSocket();
                this.connectionState = ConnectionState.DISCONNECTED;
            }
        }).start();
    }

    /**
     * Write a packet to the server syncronously (not on a independent thread)
     * @param data to send to the server as a string
     */
    public void writeSync(String data) {
        if (!connectionState.equals(ConnectionState.CONNECTED)) {
            return;
        }

        // if there's no data, return
        if (data == null || data.length() <= 0) {
            return;
        }

        try {
            // get the output string to write to
            var stream = this.clientSocket.getOutputStream();
            stream.write(data.getBytes());
            
        } catch (IOException ignored) {
            System.out.println("FAILED WRITE");
        }

    }

    /**
     * Write data to the socket asyncronously (on an independent thread)
     * @param data to write to the server
     */
    public void write(String data) {
        // try to write to the stream on a new thread
        new Thread(() -> {
            this.writeSync(data);
        }).start();
    }

    /**
     * Write the given data with a newline character at the end
     */
    public void writeLine(String data){
        this.write(data + "\n");
    }

    /**
     * Write the given data with a newline character at the end
     */
    public void writeLineSync(String data){
        this.writeSync(data + "\n");
    }

    /**
     * Send the keep alive packet
     */
    public void keepAlive(){
        this.writeLine("{QP:KEEP_ALIVE}");
    }

    public void readSocket(){
        if(!connectionState.equals(ConnectionState.CONNECTED)){
            return;
        }

        // if the socket is null, return
        if(this.clientSocket == null){
            return;
        }
        
        try {
            var stream = this.clientSocket.getInputStream();

            var available = stream.available();

            //if there are no bytes available, return
            if(available == 0){
                return;
            }

            //read all available bytes from the stream
            var bytes = stream.readNBytes(available);

            //byte string builder
            var byteStringBuilder = new StringBuilder();

            //convert all bytes to characters
            for(var b : bytes){
                byteStringBuilder.append((char) b);
            }

            //get the data string
            var dataString = byteStringBuilder.toString();

            var commands = dataString.split("\n");

            //print all commands
            for(var command : commands){
                System.out.println(command);
            }
        }catch(IOException ignored){}
    }
    /**
     * Disconnect the given socket from the server
     */
    public void disconnect() {

        // if we're not connected, just return
        if (!connectionState.equals(ConnectionState.CONNECTED)) {
            return;
        }

        try {
            // write the end socket protocol
            this.writeLineSync("{QP:ES}");

            //close the socket
            this.clientSocket.close();

            // setup the client socket as a fresh new socket
            this.clientSocket = new Socket();
            this.connectionState = ConnectionState.DISCONNECTED;

        } catch (IOException ignored) {
            System.out.println("Failed to close connection... somehow?");
        }

    }

    /**
     * Get the connection state
     * 
     * @return the connection state
     */
    public ConnectionState getConnectionState() {
        return this.connectionState;
    }

    /**
     * Close the clinet socket and replace it
     */
    public void closeSocket() {
        try {

            //if there is an existing socket close it
            if(this.clientSocket != null){
                this.clientSocket.close();
            }
            
            //override the current socket with a new one
            this.clientSocket = new Socket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write test for the server
     */
    public void writeTest() {
        this.writeLine("test");
    }
}
