import com.jcraft.jsch.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;

public class SshConnection {
    private String user;
    private String password;
    private String host;
    private Integer port;


    private Session session;
    private PrintStream ps;
    private InputStream input;
    private OutputStream ops;
    private Channel channel;

    private static final String STRICT_HOSTKEY_CHECKIN_KEY = "StrictHostKeyChecking";
    private static final String STRICT_HOSTKEY_CHECKIN_VALUE = "no";

    public  SshConnection(String user, String password, String host, Integer port) throws JSchException {
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;

        final JSch connection = new JSch();

        session = connection.getSession(user, host, port);
        session.setPassword(password);
        session.setConfig(STRICT_HOSTKEY_CHECKIN_KEY,
                STRICT_HOSTKEY_CHECKIN_VALUE);

        System.out.println("-- Try to connect to the server " + host + ":" + port
                + " with user " + user);
        session.connect();
        System.out.println("-- Connexion OK");

    }

    public String executeCommand(String command)
    {
        StringBuilder outputBuffer = new StringBuilder();

        try
        {
            Channel channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            InputStream commandOutput = channel.getInputStream();
            channel.connect();
            int readByte = commandOutput.read();

            while(readByte != 0xffffffff)
            {
                outputBuffer.append((char)readByte);
                readByte = commandOutput.read();
            }

            channel.disconnect();
        }
        catch(IOException ioX)
        {
            System.out.println(ioX.getMessage());
            return null;
        }
        catch(JSchException jschX)
        {
            System.out.println(jschX.getMessage());
            return null;
        }

        return outputBuffer.toString();
    }




}
