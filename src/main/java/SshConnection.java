import com.jcraft.jsch.*;


import java.io.*;

public class SshConnection {
    private final Session session;
    private PrintStream ps;
    private InputStream input;
    private OutputStream ops;
    private Channel channel;

    private static final String STRICT_HOSTKEY_CHECKIN_KEY = "StrictHostKeyChecking";
    private static final String STRICT_HOSTKEY_CHECKIN_VALUE = "no";


    private ChannelSftp setupJsch() throws JSchException {

        return (ChannelSftp) session.openChannel("sftp");
    }


    public  SshConnection(String user, String password, String host, Integer port) throws JSchException {

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
        return executeCommand(command, true);
    }

    public String executeCommand(String command, Boolean isWrite)
    {
        StringBuilder outputBuffer = new StringBuilder();

        try
        {
            Channel channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);

                InputStream commandOutput = channel.getInputStream();
                channel.connect();
                System.out.println("START WRITE");
            if (isWrite) {
                int readByte = commandOutput.read();

                while (readByte != 0xffffffff) {
                    outputBuffer.append((char) readByte);
                    readByte = commandOutput.read();
                }
            }
            System.out.println("STOP WRITE");
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



    public void sendToServer(File dir, String remoteDir) throws IOException, JSchException, SftpException {

        String command = "tar -cvf "+dir+".tar.gz -C "+dir+" *";
        Process process = Runtime.getRuntime().exec(command);
        System.out.println("ZIPPED");

        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();


        String localFile = dir+".tar.gz";
        executeCommand("mkdir " + remoteDir);
        channelSftp.put(localFile, remoteDir);
        channelSftp.exit();
        String unzipCommand = "tar -xf " + remoteDir + dir.getName()+".tar.gz" + " -C " + remoteDir;
        System.out.println(unzipCommand);
        executeCommand(unzipCommand);
        executeCommand("cd " + remoteDir + " && rm " + dir.getName()+".tar.gz", false);
    }
}
