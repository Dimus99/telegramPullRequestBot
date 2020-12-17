import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.util.Scanner;
public class sshTest {
    public static void main(String[] args) throws JSchException, IOException {
        SshConnection ssh = new SshConnection("username", "password", "host", 88);
        Scanner input = new Scanner(System.in);
        String command;
        while (true)
        {
            command = input.nextLine();
            System.out.println(ssh.executeCommand(command));
        }
    }


}
