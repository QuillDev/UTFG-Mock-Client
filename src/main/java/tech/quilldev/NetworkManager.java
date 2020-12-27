package tech.quilldev;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

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
     * Write data to the socket
     * 
     * @param data to write
     */
    public void write(String data) {
        // try to write to the stream on a new thread
        new Thread(() -> {
            this.writeSync(data);
            ;
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
            // write the end socket protocol
            this.writeSync("{QP:ES}");

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

    public void writeTest() {
        this.write("test");
    }

    /**
     * Try to connect to the given host and port
     * 
     * @param host to connect to
     * @param port to connect to
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
}
