import java.util.*;

//Holds Server Information
public class Server{  
    String serverType;
    int serverID;
    int state; 
    int curStartTime;
    int core;
    int mem;
    long disk;
    int wJobs;
    int rJobs;

    //Server Constructor
    public Server(String serverType,String serverID,String state ,String curStartTime
                    ,String core,String mem,String disk,String wJobs,String rJobs ){
        this.serverType = serverType;
        this.serverID = Integer.parseInt(serverID);
        switch (state){ //Switch statement to give state value integer values
            case "inactive":
                this.state =0;
                break;
            case "booting":
                this.state =1;
                break;
            case "idle":
                this.state =2;
                break;
            case "active":
                this.state =3;
                break;
            case "unavailable":
                this.state =4;
                break;
        }
        this.curStartTime = Integer.parseInt(curStartTime);
        this.core = Integer.parseInt(core);
        this.mem = Integer.parseInt(mem);
        this.disk = Long.parseLong(disk);
        this.wJobs = Integer.parseInt(wJobs);
        this.rJobs = Integer.parseInt(rJobs);
    }
}

//Comparator provides static method for sorting the elements of a collection
//The collection being the ServerList information 
//It will compare the first object with the second object being cores
//Returning a negative value, the list will sort by ascending order of cores
class sortServer implements Comparator<Server>{
    public int compare (Server a, Server b){

        int result = a.core - b.core;

        return result;
    }
}