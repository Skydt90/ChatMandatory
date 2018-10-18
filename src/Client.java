import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * This is the Client class and representative of a computer that connects to the server.
 * It contains an object of type 'Validation' which it uses to validate a users input before
 * sending it to the server. It also executes a thread using an object of type 'Receiver'
 * which enables it to send and receive messages at the same time and a Timer to execute a
 * 'heartbeat' message to the server once every minute.
 * @author Christian Grye Skydt
 */

public class Client
{
    private static InetAddress host;                                            // Server IP address.
    private static final int PORT = 1237;                                       // Server Port.
    static List<String> usernames = new ArrayList<>();                          // A list of all active users on the server.

    public static void main(String[] args)
    {
        try
        {
            host = InetAddress.getLocalHost();                                  // Getting server IP address.
        }
        catch(UnknownHostException uhe)
        {
            uhe.printStackTrace();
            System.exit(1);
        }
        accessServer();                                                         // Initiating server access.
    }

    // Method containing all logic for accessing the server
    private static void accessServer()
    {
        // Declaring variables
        Socket socket;
        String tryUsername;
        String username;
        Chat chat;

        try
        {
            socket = new Socket(host, PORT);                                    // Instantiating a new socket using above variables.
            chat = new Chat(socket, host, PORT);

            System.out.print("Welcome to the chat. Please enter desired username:\n");
            // Loops until a valid username is entered
            do
            {
                username = chat.enterUsername();                                // Initiates method for creating a username.
                tryUsername = chat.tryNameOnServer();                           // Sends desired username to the server.

                if (tryUsername.startsWith("J_ER"))
                {
                    String response = tryUsername.substring(5);
                    System.err.println(response);                               // Error response, if username is already taken.
                }
            }
            while (!tryUsername.startsWith("J_OK"));

            // Starting up heartbeat timer
            Heartbeat beat = new Heartbeat(socket);                             // Instantiating the Heartbeat object.
            Timer imAlive = new Timer();                                        // Instantiating the Timer object.
            imAlive.scheduleAtFixedRate(beat, 0, 30000);            // Scheduling timer to execute now and once per min.

            // Starting up the chat service
            System.out.println("You connected as: " + "'" + username + "'" + "\nType in 'QUIT' if you wish to leave.");

            while (chat.getIsRunning())
            {
                chat.groupChat();
            }

            System.out.println("You have successfully left the chat.");
            imAlive.cancel();

        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}