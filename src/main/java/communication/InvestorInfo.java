package communication;

import assets.Share;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Comparator;

public class InvestorInfo implements Comparable {
    /**
     * Investor Name.
     */
    private final String investorName;

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

    public InvestorInfo(String investorName, int currentMoney, ArrayList<Share> boughtShares, ArrayList<Share> proposedShares) {
        this.investorName = investorName;
        this.currentMoney = currentMoney;
        this.boughtShares = boughtShares;
        this.proposedShares = proposedShares;
    }

    public String getInvestorName() {
        return investorName;
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

    public int compareTo(Object obj2) {
        int p1 = this.currentMoney;
        int p2 = ((InvestorInfo) obj2).currentMoney;

        if (p1 > p2) {
            return 1;
        } else if (p1 < p2){
            return -1;
        } else {
            return 0;
        }
    }
}
