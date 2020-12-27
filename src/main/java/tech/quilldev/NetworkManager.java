package tech.quilldev;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class NetworkManager {

    // booleans for managing connection state
    private Socket clientSocket;

    // the connection state of the socket
    private ConnectionState connectionState;

    //Time in seconds before timing out
    private final int timeout = 10;
    private long lastSuccess;

    // create the network manager
    public NetworkManager() {

        // start off as a disconnected socket
        this.connectionState = ConnectionState.DISCONNECTED;

        // create the socket
        this.clientSocket = null;
        this.lastSuccess = getCurrentTimeSeconds();
    }

    /**
     * Try to connect to the given host and port
     * @param host to connect to
     * @param port of the host
     */
    public void connect(String host, int port) {

        // if we're anything but disconnected, return
        if (!this.connectionState.equals(ConnectionState.DISCONNECTED)) {
            return;
        }

        // set the socket state to connecting
        this.connectionState = ConnectionState.CONNECTING;

        //print that we're connecting
        System.out.println("CONNECTING");

        try {
            // address to connect with
            var address = new InetSocketAddress(host, port);

            //close the current socket connection if there is one
            this.closeSocket();

            //try to connect to the new address
            this.clientSocket.connect(address, timeout * 1000);

            //set the last success to the current time
            this.lastSuccess = getCurrentTimeSeconds();

            // if we connect set the connection state to connected
            this.connectionState = ConnectionState.CONNECTED;

        } catch (IOException ignored) {
            this.closeSocket();
            this.connectionState = ConnectionState.DISCONNECTED;
        }
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
            stream.flush();

            //set the last success time to the current time
            this.lastSuccess = getCurrentTimeSeconds();

        } catch (SocketException socketException) {
            this.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Try to reconnect to the server
     */
    public void reconnect(){

        //if the client socket is null, return
        if(clientSocket == null || clientSocket.getInetAddress() == null){
            return;
        }

        //get the host and port
        var host = this.clientSocket.getInetAddress().getHostName();
        var port = this.clientSocket.getPort();

        //disconnect the current socket
        this.disconnect();

        //try to connect to the port we were connected to
        this.connect(host, port);

        //if we fail to connect, the connection timed out
        if(!this.connectionState.equals(ConnectionState.CONNECTED)){
            System.out.println("CONNECTION TIMED OUT");
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

        if(!connectionState.equals(ConnectionState.CONNECTED)){
            return;
        }

        //send the keep alive packet
        this.writeLine("{QP:KEEP_ALIVE}");
    }

    /**
     * Read the socket
     */
    public void readSocket(){
        new Thread( () -> {
            // if the socket is null, return
            if (clientSocket == null) {
                return;
            }


            try {
                var stream = this.clientSocket.getInputStream();

                var available = stream.available();

                //if there are no bytes available, return
                if (available == 0) {
                    return;
                }

                //read all available bytes from the stream
                var bytes = stream.readNBytes(available);

                //byte string builder
                var byteStringBuilder = new StringBuilder();

                //convert all bytes to characters
                for (var b : bytes) {
                    byteStringBuilder.append((char) b);
                }

                //get the data string
                var commands = byteStringBuilder.toString().split("\n");

                //print all commands
                for (var command : commands) {
                    var packet = new Packet(command);

                    //if the packet is malformed, skip it
                    if(packet.isMalformed()){
                        continue;
                    }

                    //print the packet
                    System.out.println(packet);

                }
            }
            catch (SocketException ignored){
                this.reconnect();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
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
            //close the socket
            this.clientSocket.close();

            // setup the client socket as a fresh new socket
            this.clientSocket = new Socket();
            this.connectionState = ConnectionState.DISCONNECTED;

            System.out.println("DISCONNECTED");

        } catch (IOException ignored) {
            System.out.println("Failed to close connection... somehow?");
        }

    }

    /**
     * Get the connection state
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

    /**
     * Start listening for incoming data
     */
    public void startListening() {

        //Start listening on the socket
        System.out.println("STARTED LISTENING TO SOCKET");

        //start the new thread
        new Thread(() -> {

            while (true) {
                this.readSocket();
            }
        }).start();
    }

    /**
     * Get the current time in seconds
     * @return the current time in seconds
     */
    public long getCurrentTimeSeconds(){
        return System.currentTimeMillis() / 1000;
    }
}
