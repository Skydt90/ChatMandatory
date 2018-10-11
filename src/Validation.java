import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class is used by both the client and server to validate different types of input and requests. For String
 * values it uses the static 'matches()' method of class 'Pattern' to compare the String with a predefined set of
 * parameters. In order to check active connections it uses get/setKeepAlive() method of the class 'Socket'.
 * @author Christian Grye Skydt
 */

class Validation
{
    private boolean request;

    // Logic for validating user name on client-side
    String validateUsername(String username, InetAddress host, int PORT)
    {
        request = false;
        if(username.length() > 12)
        {
            throw new IllegalArgumentException("ERROR! Username must contain no more than 12 characters!");
        }
        if(!Pattern.matches("[-_a-zA-Z0123456789æÆøØåÅ]+", username))
        {
            throw new IllegalArgumentException("ERROR! Username must only contain letters and digits!");
        }
        request = true;
        return "JOIN " + username + ", " + host.getHostAddress() + ":" + Integer.toString(PORT); // Protocol String
    }

    // Logic for validating chat message
    String validateChatMessage(String message)
    {
        request = false;
        if (message.length() > 250)
        {
            throw new IllegalArgumentException("ERROR! Chat message must contain no more than 250 characters!");
        }
        if (!Pattern.matches("[ -_a-zA-Z0123456789æÆøØåÅ]+", message))
        {
            throw new IllegalArgumentException("ERROR! Chat message must contain only letters and digits!");
        }
        request = true;
        return message;
    }

    // Logic for validating user name on server-side
    boolean validateUsernameOnServer(String requestedUsername, Socket clientSocket)
    {
        // Add client if map is empty
        if (Server.clientInfo.isEmpty())
        {
            request = true;
            Server.clientInfo.put(clientSocket, requestedUsername); // Store client socket as key and username as value.
            return request;
        }
        else    // If map is not empty.
        {
            for(String username : Server.clientInfo.values())
            {
                if (username.equals(requestedUsername))     // if username exists in map.
                {
                    throw new IllegalArgumentException("Error! Username is already taken");
                }
            }
            request = true;
            Server.clientInfo.put(clientSocket, requestedUsername); // Store socket and username in map if passed.
        }
        return request;
    }

    // Logic for validating active clients on the server
    void validateClientConnection()
    {
        List<Socket> toRemove = new ArrayList<>();  // Temp storage array to avoid ConcurrentModException.
        boolean wasModified = false;                // Bool to trigger Server.sendUsernames if map is modified.

        for(Socket client : Server.clientInfo.keySet())
        {
            try
            {
                if (!client.getKeepAlive())  // If false.
                {
                    toRemove.add(client);    // Add to array.
                }
                client.setKeepAlive(false);  // Set to false.
            }
            catch(SocketException se)
            {
                se.printStackTrace();
            }
        }
        if (toRemove.size() > 0)    // Sockets added to list.
        {
            wasModified = true;
        }

        // Remove sockets from map that matches those in the array
        for (Socket clients : toRemove)
        {
            Server.clientInfo.remove(clients);
        }
        if (wasModified)
        {
            Server.sendUsernames(); // Message all remaining clients the new list of active user names.
        }
    }

    boolean getRequest()
    {
        return request;
    }
}
