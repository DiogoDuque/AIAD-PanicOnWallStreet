package assets;

import com.google.gson.Gson;

public class Share {

    private final Company company;

    private String highestBidder;
    private String highestBidderValue;

    public Share(Company company) {
        this.company = company;
    }

    public int getValue() {
        return company.getCompanyValue();
    }

    public String getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(String highestBidder) {
        this.highestBidder = highestBidder;
    }

    public String getHighestBidderValue() {
        return highestBidderValue;
    }

    public void setHighestBidderValue(String highestBidderValue) {
        this.highestBidderValue = highestBidderValue;
    }

    @Override
    public String toString() {
        return "Share{" + company +
                '}';
    }
}
