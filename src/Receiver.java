import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * This class is used by each active client to receive messages from the server. It implements the Runnable interface
 * meaning that it can be executed in a thread by the client so it can send and receive messages at the same time. In
 * order to achieve this, it takes the clients socket as a parameter and gets the inputStream using a Scanner object.
 * @author Christian Grye Skydt
 */

public class Receiver implements Runnable
{
    private Socket socket;

    Receiver(Socket client)
    {
        this.socket = client;
    }

    @Override
    public void run()
    {
        String message = "";
        Scanner input = null;
        try
        {
            input = new Scanner(socket.getInputStream());                                   // Setting up inputStream
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }

        while(true)
        {
            try
            {
                message = input.nextLine();
            }
            catch (NoSuchElementException nse)                                              //Band aid fix
            {
                System.exit(1);
            }

            if (message.startsWith("LIST"))                                                 // receive list of active user names.
            {
                String usernames = message.substring(5);                                    // Cut off the 'LIST' part using substring.
                String[] names = usernames.split(" ");                                 // Split the String into individual user names.
                Client.usernames.clear();                                                   // Clear current user names from Client.
                Client.usernames.addAll(Arrays.asList(names));                              // Add new user names to Client.

                // Print out the updated number of active users in chat
                System.out.println("SERVER> There are currently: " + Client.usernames.size() + " people chatting.");
            }
            else
            {
                System.out.println(message);                                                 // Print chat message.
            }
        }
    }
}