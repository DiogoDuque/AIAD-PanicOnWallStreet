package communication;

import assets.Share;
import com.google.gson.Gson;

import java.util.ArrayList;

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

    /**
     * Calculates how much the investor is worth. Useful for finding out how much better/worse agents are among themselves.
     * @return how much the investor is worth.
     */
    public float evaluate(){
        int money = this.currentMoney;

        float boughtSharesValue = 0;
        for(Share s: boughtShares){
            boughtSharesValue += s.getShareAverageNextValue();
        }

        float proposedSharesValue = 0;
        for(Share s: proposedShares){
            proposedSharesValue += s.getShareAverageNextValue();
        }

        return money*1.1f + boughtSharesValue + proposedSharesValue*0.4f;
    }

    /**
     * Compares two agents based on how much they are worth.
     * @param comparable the investor to compare to.
     * @return 1 if this agent is the richest, -1 if it's the poorest and 0 otherwise.
     */
    public int compareTo(Object comparable) {
        float p1 = this.evaluate();
        float p2 = ((InvestorInfo) comparable).evaluate();

        if (p1 > p2) {
            return 1;
        } else if (p1 < p2){
            return -1;
        } else {
            return 0;
        }
    }
}
