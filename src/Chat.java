import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

class Chat
{
    private String username;
    private Socket client;
    private Validation validation;
    private boolean isRunning;
    private InetAddress host;                            // Server IP address.
    private int port;

    Chat(Socket client, InetAddress host, int port)
    {
        this.client = client;
        this.host = host;
        this.port = port;
        this.isRunning = true;
        this.username = "";
        this.validation = new Validation();
    }

    String enterUsername()
    {
        Scanner input = new Scanner(System.in);

        do
        {
            username = input.nextLine();
            try
            {
                username = validation.validateUsername(username);  // Attempts to validate username.
            }
            catch (IllegalArgumentException iae)
            {
                System.err.println(iae.getMessage());             // Error message informing of incorrect input.
            }
        }
        while (!validation.getRequest());
        return username;
    }

    // Method that will send requested username to server, and return result to caller as a String
    String tryNameOnServer()
    {
        // Declaring variables
        PrintWriter messageToServer = null;
        Scanner msgFromServer = null;
        String response;
        try
        {
            messageToServer = new PrintWriter(client.getOutputStream(), true);
            msgFromServer = new Scanner(client.getInputStream());
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

        messageToServer.println("JOIN " + username + ", " + host.getHostAddress() + ":" + Integer.toString(port)); // Send message to server.
        response = msgFromServer.nextLine();                                                                       // Receive message from server.
        return response;
    }

    void groupChat()
    {
        // Starting up thread for reading messages from the server
        Thread chatRead = new Thread(new Receiver(client));
        chatRead.start();

        PrintWriter messageToServer = null;
        Scanner input = new Scanner(System.in);
        String message;

        try
        {
            messageToServer = new PrintWriter(client.getOutputStream(), true);               // Will flush buffer each call
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
                message = validation.validateChatMessage(message);                                    // Attempts to validate input using Validation class.
            }
            catch (IllegalArgumentException iae)
            {
                System.err.println(iae.getMessage());                                                 // Error message informing of any incorrect input.
            }
        }
        while (!validation.getRequest());

        // Shutdown logic
        if (message.equals("QUIT"))
        {
            chatRead.interrupt();                                                                      // Shut down receiver thread.
            messageToServer.println("QUIT");                                                           // QUIT message to server.
            System.out.println("Shutting down.");
            isRunning = false;
        }
        messageToServer.println("DATA " + username + ": " + message);                                  // Chat message to server.
    }

    boolean getIsRunning()
    {
        return this.isRunning;
    }
}
