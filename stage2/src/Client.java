import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
public class Client {

    private static String HELO = "HELO";
    private static String AUTH = "AUTH";
    private static String REDY = "REDY";
    // private static String NONE = "NONE";
    private static String OK = "OK";
    private static String GET = "GETS ";
    private static String SCHD = "SCHD";
    private static String QUIT = "QUIT";
    // private static int runs =0;
    // private static String bigboy;

    DataInputStream din;
    DataOutputStream dout;
    InputStreamReader ip;
    BufferedReader br;
    Socket s;

    int globalJobId;

    ArrayList<Server> servers;

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

    private void setup() {
        send(HELO);
        readBuf();
        send(AUTH + " " + "aidan");
        readBuf();
        return;
    }

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

    private void command_schd(String type, int id){
        send(SCHD + " " + globalJobId + " " + type + " " + id);
        readBuf();
    }

    private void run(){
        setup();

        while (true) {
            send(REDY);
            String response = readBuf();
            String[] resFields = response.split(" ");
            switch (resFields[0]) {
                case "JOBN":
                    globalJobId = Integer.parseInt(resFields[2]);
                    ArrayList<Server> servers = command_get("Capable " + resFields[4] +" "+resFields[5] +" "+ resFields[6]); 
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
    }

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


    public static void main(String args[])throws Exception{
            Client client = new Client();
            client.run();
        }

}
