import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * This is the server class and responsible for dealing with all the clients connecting to it.
 * It contains a static synchronized HashMap, for storing client sockets and their user names. In
 * order to handle multiple clients at the same time, the server 'listens' for a client to connect
 * and then executes a thread using an object of type 'ClientHandler' to deal with that connection. It
 * also uses the 'Timer' class to iterate over the map once every minute and remove any inactive clients.
 * Finally it contains a static method, which will send all active user names to every connected client once called.
 * @author Christian Grye Skydt
 */

public class Server
{
    private static ServerSocket server;                                                     // Server socket.
    private static final int PORT = 1237;                                                   // Server port.
    static Map<Socket, String> clientInfo = Collections.synchronizedMap(new HashMap<>());   // Synchronized map.

    public static void main(String[] args)
    {
        try
        {
            server = new ServerSocket(PORT);                    // Instantiating serverSocket with above port number.
            System.out.println("Server initialized!");
        }
        catch (IOException ioe)
        {
            System.out.println("Unable to setup port");
            System.exit(1);
        }
        serverRun();                                            // Initiating server logic.
    }

    // Method containing servers run logic
    private static void serverRun()
    {
        // Starting up timer to control active connections
        Connections users = new Connections();                     // Instantiating Connection object.
        Timer isActive = new Timer();                              // Instantiating Timer object.
        isActive.scheduleAtFixedRate(users, 0, 30000); // Scheduling timer to execute now and once per min.

        while (true)
        {
            try
            {
                Socket clientSocket = server.accept();             // 'Listens' for incoming connection.
                System.out.println(clientSocket.getInetAddress().getHostName() + " connected");

                // Starting up new thread for handling the new client
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }

    // Method containing logic for sending active user names to connected clients
    static void sendUsernames()
    {
        PrintWriter output;
        ArrayList<String> usernames = new ArrayList<>(Server.clientInfo.values()); // Adding all user names to ArrayList.

        String names = "LIST ";                               // Start of LIST protocol.
        for (String name : usernames)                         // Concatenates user names into String.
        {
            names += name + " ";
        }
        try
        {
            // Send String to all connected clients
            for (Socket client : Server.clientInfo.keySet())
            {
                output = new PrintWriter(client.getOutputStream(), true);
                output.println(names);
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}