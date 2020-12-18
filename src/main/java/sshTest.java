import com.jcraft.jsch.JSchException;
import java.io.IOException;
import java.util.Scanner;


public class sshTest {
    public static void main(String[] args) throws JSchException, IOException {
        SshConnection ssh = new SshConnection("root", "pwd", "213.189.217.23", 22);
        Scanner input = new Scanner(System.in);
        String command;
        System.out.println(ssh.executeCommand("cd .. && ls"));
        while (true)
        {
            command = input.nextLine();
            System.out.println(ssh.executeCommand(command));
        }
    }
}
