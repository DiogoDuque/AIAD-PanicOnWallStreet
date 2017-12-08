package assets;

import com.google.gson.Gson;

public class Share {

    /**
     * Company to which this share is associated.
     */
    private Company company;

    /**
     * Owner's cid.
     */
    private final String ownerCid;

    /**
     * Name of the agent whose proposal value was highest.
     */
    private String highestBidder;

    /**
     * Value of the highest proposal.
     */
    private int highestBidderValue;

    /**
     * Stores whether this share is already bought or not.
     */
    private boolean bought = false;

    public Share(Company company, String ownerCid) {
        this.company = company;
        this.ownerCid = ownerCid;
        this.highestBidderValue = 0;
    }

    public int getCurrentValue() {
        return company.getCurrentValue();
    }

    public String getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(String highestBidder) {
        this.highestBidder = highestBidder;
    }

    public int getHighestBidderValue() {
        return highestBidderValue;
    }

    public void setHighestBidderValue(int highestBidderValue) {
        this.highestBidderValue = highestBidderValue;
    }

    public void setAsBought() {
        bought = true;
    }

    public boolean isBought() {
        return bought;
    }

    public String getCompanyName() {
        return company.getName();
    }

    /**
     * Calculates the average value that will come out when the dices are rolled.
     * @return average of the next value after dice roll.
     */
    public float getShareAverageValue(){
        return company.getAverageNextValue();
    }

    public int getMaxPossibleValue(){
        return company.maxValue();
    }

    public int getMinPossibleValue(){
        return company.minValue();
    }

    @Override
    public String toString() {
        return "Share{" + ownerCid + " -> " + company +
                "; " + highestBidder + "(" + highestBidderValue + ")" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Share share = (Share) o;

        return company.equals(share.company) && ownerCid.equals(share.ownerCid);
    }

    public void updateCompany(Company c) {
        this.company = c;
    }

    public String toJsonStr() {
        return new Gson().toJson(this);
    }
}
