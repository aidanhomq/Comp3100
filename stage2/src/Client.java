import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
public class Client {
    // global variables initialised for sending messages according to user guide
    private static String HELO = "HELO";
    private static String AUTH = "AUTH";
    private static String REDY = "REDY";
    private static String OK = "OK";
    private static String GET = "GETS ";
    private static String SCHD = "SCHD";
    private static String QUIT = "QUIT";

    // global variables initialised for Client function
    DataInputStream din;
    DataOutputStream dout;
    InputStreamReader ip;
    BufferedReader br;
    Socket s;

    // global variables initialised for Job Id
    int globalJobId;

    // global variables initialised ArrayList for Server Information
    ArrayList<Server> servers;

     //function which initialises the socket so that a connection can be established
     //and Classes like DatainputStream, Dataoutputstream, Inputstreamreader and
     //Bufferreader to read and send primitive data between the client and server
    public Client(){
        try {
            s = new Socket ("127.0.0.1", 50000);
            din = new DataInputStream(s.getInputStream());
            dout = new DataOutputStream(s.getOutputStream());
            ip = new InputStreamReader(din);
            br = new BufferedReader(ip);
        }
        catch(Exception e){
            System.out.println("Error");
        }
        
    }
    private void send(String msg) {
        try {
            dout.write(( msg +"\n").getBytes());
            dout.flush();
            return;
        }
        catch(Exception e){
            System.out.println("Error");
        }
    }

    private String readBuf() {
        try {
            String str = br.readLine();
            System.out.println("Server: "+str);
            return str;
        }
        catch(Exception e){
            System.out.println("Error");
            return "";
        }
    }

    //Sends the initial handshake to the server to start the scheudling 
    private void setup() {
        send(HELO);
        readBuf();
        send(AUTH + " " + "aidan");
        readBuf();
        return;
    }

    //Calls command Gets Capable in order to find the servers capable to run the job
    //Stores server information in an ArrayList called Serverlist, goes through a loop
    //until all the capable servers are stored
    //Calls function sortServer() using Collection.sort class where it sorts serverlist
    //In ascending order by cores.
    private ArrayList<Server> command_get(String msg) {
        send(GET + msg);

        String str = readBuf();
        String[] dataFields = str.split(" ");
        int numLines = Integer.parseInt(dataFields[1]);
        send(OK);


        ArrayList<Server> serverList = new ArrayList<Server>();
        for (int i =0; i <numLines; i++) {
            str = readBuf();
            String[] serverData = str.split(" ");
            serverList.add(new Server(serverData[0], serverData[1],serverData[2],serverData[3]
            ,serverData[4],serverData[5],serverData[6],serverData[7],serverData[8]));
        }
        send(OK);
        readBuf();

        Collections.sort(serverList, new sortServer());
        
        return serverList;
    }

    //Schedlues the job 
    private void command_schd(String type, int id){
        send(SCHD + " " + globalJobId + " " + type + " " + id);
        readBuf();
    }

    //Calls the initial handshake function setup()
    //Deals with the job information that is sent by the server 
    //Uses a swtich case to deal with the Job sent
    /*If the server sends a string JOBN, it will store the Job id in a Global integer
    * Then calls the function command_gets to call the command GETS Capable to get
    * the server information and store it in an array list and sorts in by the number of
    * cores the server has. After returning the serverlist it will go through the 
    *cost reduction algorithm to return the server thats best in reducing cost. 
    *The last step is to scheudle the job through the function command_schd().
    */

    /*If the server sends a string JCPL, it provids information on the most recent
    * job completed. It will break out of the switch case and send another Job message
    * again. 
     */

    /* Once there is no more jobs to schedule and all jobs are completed in scheudling
    * the server will send None. This is triggered in the switch case to call fucnction
    * quit() where it will close the socket, bufferreader and dataoutputstream. Closing
    * the distrubted system. 
    */
    private void run(){
        setup();

        while (true) {
            send(REDY);
            String response = readBuf();
            String[] resFields = response.split(" ");
            switch (resFields[0]) {
                case "JOBN":
                    globalJobId = Integer.parseInt(resFields[2]);
                    servers = command_get("Capable " + resFields[4] +" "+resFields[5] +" "+ resFields[6]); 
                    Server costReductionServer=CostReduction(servers, Integer.parseInt(resFields[4]), Integer.parseInt(resFields[5]), Integer.parseInt(resFields[6]));
                    command_schd(costReductionServer.serverType, costReductionServer.serverID);
                    break;
                case "JCPL": 
                    break;
                case "NONE":
                    quit();

            } 
        }
    }

    //The CostRedcution Algorithm is called upon once the serverlist is returned and sorted 
    //It loop through the serverlist again with three conditions
    //First it wants to find servers in the state of booting, active or idle, returning the first server that has sufficient resources
    //If no servers meet this condition it will find inactive servers to scheudle the job into that have sufficient core
    //Lastly if there is no servers that have enough cores to meet the requirement, it will look through how many waiting jobs there are in each server
    //It will schedule the job if it has the least amount of waiting jobs. Since the list of servers are capable servers, all servers can run the job
    //But will have to wait until there is enough avaialble cores.
    private Server CostReduction (ArrayList<Server> temp_server, int core, int mem, int disk){
        int countWaitJobs = Integer.MAX_VALUE;

        Server tempOne = null;
        Server tempTwo = null;
        Server tempThree = null;

        for(Server s:temp_server){
            if(s.state ==1 || s.state ==2 ||s.state == 3){
                if(s.core >= core && s.mem >=mem && s.disk>=disk){
                    tempOne = s;
                    break;    
                } 
            }

            if(s.core >= core && s.mem >=mem && s.disk>=disk && s.state == 0){
                if(tempTwo == null){
                    tempTwo =s;
                } 
            }

            if (s.wJobs < countWaitJobs){
                countWaitJobs = s.wJobs;
                tempThree = s;    
            }



        }

        if(tempOne != null){
            return tempOne;
        }else if(tempTwo != null){
            return tempTwo;
        }else{
            return tempThree;
        }
    }

    //Quit() function is called when server sends None, meaning no more jobs to schedule 
    //and all jobs are completed. It will close the server and close the client.
    private void quit(){
        try {
            send(QUIT);
            readBuf();
            s.close();
            dout.close();
            br.close();
            System.exit(0);
        }
        catch(Exception e){
            System.out.println("Error");
        }
        
    }

    //Main function creates the client class and starts client-server through function run()
    public static void main(String args[])throws Exception{
            Client client = new Client();
            client.run();
        }

}
