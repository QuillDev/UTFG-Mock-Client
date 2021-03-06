package tech.quilldev;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 *
 */
public class App 
{

    public static NetworkManager networkManager = new NetworkManager();

    public static void main( String[] args )
    {
        var scheduler = new ScheduledThreadPoolExecutor(1);

        //log the delay
        //scheduler.scheduleWithFixedDelay(App::getState,500, 500, TimeUnit.MILLISECONDS);

        //Test server functions
        scheduler.schedule(App::connect, 1, TimeUnit.SECONDS);
        scheduler.schedule(networkManager::disconnect, 5, TimeUnit.SECONDS);
        scheduler.schedule(App::connect, 7, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(networkManager::writeTest, 8, 1, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(networkManager::keepAlive, 9, 1, TimeUnit.SECONDS);
        
        networkManager.startListening();
    }

    public static void getState(){
        System.out.println("STATE: " + networkManager.getConnectionState());
    }

    //connect to the server
    public static void connect(){
        System.out.println("Connecting");
        networkManager.connect("localhost", 2069);
    }
}
