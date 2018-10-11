import java.util.TimerTask;

/**
 * This class is used by the server to remove any inactive clients. It extends the TimerTask class, meaning
 * that it can be executed as a task by the Timer class to execute at fixed intervals. To check if a client is
 * inactive, it uses an object of type 'Validation' which will execute the necessary logic required.
 * @author Christian Grye Skydt
 */

public class Connections extends TimerTask
{
    private Validation validation;

    Connections()
    {
        this.validation = new Validation();
    }

    @Override
    public void run()
    {
        System.out.println("Checking active users");
        validation.validateClientConnection();
        System.out.println("Server currently has: " + Server.clientInfo.size() + " active connections." );
    }
}
