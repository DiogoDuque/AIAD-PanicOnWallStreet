package assets;

import main.Main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameInfo {
    private static GameInfo instance;

    private HashMap<Integer, HashMap<String, Integer>> infos; //money = infos.get(round).get(agentCid)

    private GameInfo(){
        infos = new HashMap<>();
    }

    public static GameInfo getInstance() {
        if(instance==null)
            instance = new GameInfo();
        return instance;
    }

    public synchronized void setInfos(int round, String cid, int money){
        HashMap<String, Integer> roundInfos = infos.get(round);
        if(roundInfos==null){
            roundInfos = new HashMap<>();
            infos.put(round, roundInfos);
        }

        if(roundInfos.get(cid)==null)
            roundInfos.put(cid, money);
    }

    public void writeToFile(){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(Main.INFO_FILENAME));
            String str = "# Each line was generated during the negotiation phase, except for the last ones, which was generated right before the end of the program.\r\n\r\n";

            for(Map.Entry<Integer, HashMap<String, Integer>> roundEntry: infos.entrySet()){
                int round = roundEntry.getKey();
                for(Map.Entry<String, Integer> agentEntry: roundEntry.getValue().entrySet()){
                    String agent = agentEntry.getKey();
                    int money = agentEntry.getValue();
                    str += "Round: "+round+"; player: "+agent+"; money:"+money+"\r\n";
                }
            }

            writer.write(str);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
