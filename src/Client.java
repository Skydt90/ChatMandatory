import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
    private static InetAddress host;                            // Server IP address.
    private static final int PORT = 1237;                       // Server Port.
    private static String username;
    private static Validation validation = new Validation();
    static List<String> usernames = new ArrayList<>();          // A list of all active users on the server.

    public static void main(String[] args)
    {
        try
        {
            host = InetAddress.getLocalHost();                  // Getting server IP address.
        }
        catch(UnknownHostException uhe)
        {
            uhe.printStackTrace();
            System.exit(1);
        }
        accessServer();                                         // Initiating server access.
    }

    // Method containing all logic for accessing the server
    private static void accessServer()
    {
        // Declaring variables
        Socket socket;
        String serverResponse;

        try
        {
            socket = new Socket(host, PORT);                        // Instantiating a new socket using above variables.

            System.out.print("Welcome to the chat. Please enter desired username:\n");

            // Loops until a valid username is entered
            do
            {
                username = enterUsername();                         // Initiates method for creating a username.
                serverResponse = usernameTest(socket, username);    // Sends desired username to the server.
                if (serverResponse.startsWith("J_ER"))
                {
                    System.err.println(serverResponse);             // Error response, if username is already taken.
                }
            }
            while (!serverResponse.startsWith("J_OK"));

            // Starting up heartbeat timer
            Heartbeat beat = new Heartbeat(socket);                  // Instantiating the Heartbeat object.
            Timer imAlive = new Timer();                             // Instantiating the Timer object.
            imAlive.scheduleAtFixedRate(beat, 0, 60000); // Scheduling timer to execute now and once per min.

            // Starting up thread for reading messages from the server
            Thread clientRead = new Thread(new Receiver(socket));
            clientRead.start();

            // Starting up the chat service
            int index = username.indexOf(",");
            username = username.substring(5, index);                // Extracting username from JOIN message.
            System.out.println("You connected as: " + "'" + username + "'" + "\nType in 'QUIT' if you wish to leave.");

            while (true)
            {
                chat(socket);
            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    // Method containing all client-side chat logic
    private static void chat(Socket socket)
    {
        // Declaring variables
        PrintWriter messageToServer = null;
        Scanner input = new Scanner(System.in);
        String message;
        try
        {
            messageToServer = new PrintWriter(socket.getOutputStream(), true); // Will flush buffer each call
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        do
        {
            message = input.nextLine();
            try
            {
                message = validation.validateChatMessage(message); // Attempts to validate input using Validation class.
            }
            catch (IllegalArgumentException iae)
            {
                System.err.println(iae.getMessage());              // Error message informing of any incorrect input.
            }
        }
        while (!validation.getRequest());

        // Shutdown logic
        if (message.equals("QUIT"))
        {
            messageToServer.println("QUIT");                       // QUIT message to server.
            System.out.println("Shutting down.");
            System.exit(1);
        }
        messageToServer.println("DATA " + username + ": " + message); // Chat message to server.
    }

    // Method that will send requested username to server, and return result to caller as a String
    private static String usernameTest(Socket socket, String username)
    {
        // Declaring variables
        PrintWriter messageToServer = null;
        Scanner msgFromServer = null;
        String response;
        try
        {
            messageToServer = new PrintWriter(socket.getOutputStream(), true);
            msgFromServer = new Scanner(socket.getInputStream());
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

        messageToServer.println(username);              // Send message to server.
        response = msgFromServer.nextLine();            // Receive message from server.
        return response;
    }

    // Method that takes user input, validates it and returns a String using the Validation class
    private static String enterUsername()
    {
        Scanner input = new Scanner(System.in);
        String username;
        do
        {
            username = input.nextLine();
            try
            {
                username = validation.validateUsername(username, host, PORT);  // Attempts to validate username.
            }
            catch (IllegalArgumentException iae)
            {
                System.err.println(iae.getMessage());             // Error message informing of incorrect input.
            }
        }
        while (!validation.getRequest());
        return username;
    }
}