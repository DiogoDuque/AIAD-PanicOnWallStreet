package assets;

public class Share {

    private final Company company;

    private final String ownerCid;

    private String highestBidder;

    private int highestBidderValue;

    private boolean bought = false;

    public Share(Company company, String ownerCid) {
        this.company = company;
        this.ownerCid = ownerCid;
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

    public boolean isBought() {
        return bought;
    }
}
