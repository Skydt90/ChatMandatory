import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * This class is effectively responsible for handling all the logic related to each individual connected client.
 * It implements the Runnable interface meaning that it can be executed in a thread by the server
 * each time a new client connects. In order to broadcast to multiple recipients it loops through every socket in the
 * Servers map and sends out a message using an object of type 'PrintWriter'. Finally it utilizes the static method
 * sendUsernames() from class Server, to send out active user names to connected clients every times the list changes.
 * @author Christian Grye Skydt
 */

public class ClientHandler implements Runnable
{
    private Socket clientSocket;
    private Scanner input;
    private PrintWriter output;
    private String messageFromClient;
    private Validation validation;
    private String username;
    private boolean running = true;

    ClientHandler(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
        this.validation = new Validation();
        this.username = "";
        try
        {
            this.input  = new Scanner(clientSocket.getInputStream());
            this.output = new PrintWriter(clientSocket.getOutputStream(),true);
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        while (running)
        {
            try
            {
                messageFromClient = input.nextLine();                   // Wait for client input.
            }
            catch(NoSuchElementException nse)
            {
                running = false;                                        // break loop if exception is thrown, to terminate.
            }

            // Logic for joining a client
            if (messageFromClient.startsWith("JOIN"))                   // JOIN protocol from client.
            {
                int index = messageFromClient.indexOf(",");
                username = messageFromClient.substring(5, index);       // Extract user name from JOIN using substring.
                try
                {
                    if (validation.validateUsernameOnServer(username))  // If user name is ok.
                    {
                        Server.clientInfo.put(clientSocket, username);  // Add clientSocket and username to server map.
                        output.println("J_OK");                         // J_OK protocol to client.
                        Server.sendUsernames();                         // Send out updated list of user names to clients.
                    }
                }
                catch (IllegalArgumentException iae)
                {
                    output.println("J_ER " + iae.getMessage() +": Username is already taken");
                }
            }

            // Logic for terminating a client
            if (messageFromClient.startsWith("QUIT"))                   // QUIT protocol.
            {
                try
                {
                    System.out.println("Closing Connection...");
                    clientSocket.close();                               // Close the socket.
                    System.out.println("Connection Closed");
                    Server.clientInfo.remove(clientSocket);             // Remove socket from map.
                    Server.sendUsernames();                             // Send out updated list of user names to clients.
                    running = false;
                }
                catch (IOException ioe)
                {
                    System.out.println("Unable to disconnect");
                    System.exit(1);
                }
            }

            // Logic for keeping a client alive
            if(messageFromClient.startsWith("IMAV"))                    // IMAV protocol.
            {
                try
                {
                    clientSocket.setKeepAlive(true);                    // Client is active, so set true.
                }
                catch (SocketException se)
                {
                    se.getMessage();
                }
            }

            // Trigger for broadcasting
            else if (messageFromClient.startsWith("DATA"))              // DATA protocol.
            {
                broadcast();
            }
        }
    }

    // Logic for broadcasting to all clients
    private void broadcast()
    {
        try
        {
            for (Socket client: Server.clientInfo.keySet())
            {
                output = new PrintWriter(client.getOutputStream(),true);
                output.println(messageFromClient.substring(5));         // Extract the message & send.
            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}