package communication;

import assets.Share;
import com.google.gson.Gson;
import main.Main;

import java.util.ArrayList;

public class InvestorInfo {

    private final String investorName;
    private final int currentMoney;
    private final ArrayList<Share> boughtShares;
    private final ArrayList<Share> proposedShares;

    public InvestorInfo(String investorName, int currentMoney, ArrayList<Share> boughtShares, ArrayList<Share> proposedShares){
        this.currentMoney = currentMoney;
        this.boughtShares = boughtShares;
        this.proposedShares = proposedShares;
        this.investorName = investorName;
    }

    public int getCurrentMoney() {
        return currentMoney;
    }

    public ArrayList<Share> getBoughtShares() {
        return boughtShares;
    }

    public ArrayList<Share> getProposedShares() {
        return proposedShares;
    }

    public String getInvestorName() {
        return investorName;
    }

    public String toJsonStr() {
        return new Gson().toJson(this);
    }
}
