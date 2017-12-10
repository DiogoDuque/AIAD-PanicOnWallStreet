package assets;

import main.Main;

public class GameOverManager {

    private static GameOverManager instance;

    private int numberOfManagersResponded = 0;
    private int numberOfInvestorsResponded = 0;

    private String bestInvestorName = null;
    private int bestInvestorValue = Integer.MIN_VALUE;

    private String bestManagerName = null;
    private int bestManagerValue = Integer.MIN_VALUE;

    public static GameOverManager getInstance() {
        if(instance==null)
            instance = new GameOverManager();

        return instance;
    }

    public void addNewPlayer(String name, int value){
        if(isInvestor(name) && value > bestInvestorValue){ // if best investor so far
            bestInvestorValue = value;
            bestInvestorName = name;
            numberOfInvestorsResponded++;

        } else if(value > bestManagerValue) { // if best manager so far
            bestManagerValue = value;
            bestManagerName = name;
            numberOfManagersResponded++;
        }

        if(numberOfManagersResponded == Main.N_MANAGERS && numberOfInvestorsResponded == Main.N_INVESTORS){
            System.out.println("Game has finished!");
            System.out.println("Best Manager was: "+bestManagerName+" with an amount of "+bestManagerValue);
            System.out.println("Best Investor was: "+bestInvestorName+" with an amount of "+bestInvestorValue);
            System.exit(0);
        }
    }

    private boolean isInvestor(String name){
        return name.startsWith("Investor");
    }
}
