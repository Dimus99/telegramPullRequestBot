import com.jcraft.jsch.JSchException;
import java.io.IOException;
import java.util.Scanner;


public class sshTest {
    public static void main(String[] args) throws JSchException, IOException {
        SshConnection ssh = new SshConnection("root", "7nX8uFB1", "vnc.netangels.ru", 10058);
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
