import java.util.*;

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

    public Server(String serverType,String serverID,String state ,String curStartTime
                    ,String core,String mem,String disk,String wJobs,String rJobs ){
        this.serverType = serverType;
        this.serverID = Integer.parseInt(serverID);
        switch (state){
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


class sortServer implements Comparator<Server>{
    public int compare (Server a, Server b){

        int result = a.core - b.core;

        return result;
    }
}