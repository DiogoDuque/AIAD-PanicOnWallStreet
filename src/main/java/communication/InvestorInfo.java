package communication;

import assets.Share;
import com.google.gson.Gson;

import java.util.ArrayList;

public class InvestorInfo {

    /**
     * Current money.
     */
    private final int currentMoney;

    /**
     * Shares acquired from a manager.
     */
    private final ArrayList<Share> boughtShares;

    /**
     * Shares whose proposals have been accepted by managers. However, they can be rejected at any time, so this is a very volatile list.
     */
    private final ArrayList<Share> proposedShares;

    public InvestorInfo(int currentMoney, ArrayList<Share> boughtShares, ArrayList<Share> proposedShares){
        this.currentMoney = currentMoney;
        this.boughtShares = boughtShares;
        this.proposedShares = proposedShares;
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

    public String toJsonStr() {
        return new Gson().toJson(this);
    }
}
