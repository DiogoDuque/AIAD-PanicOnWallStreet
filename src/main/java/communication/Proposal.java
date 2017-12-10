package communication;

import assets.Share;
import com.google.gson.Gson;

public class Proposal {

    /**
     * Share to which a proposal is being made.
     */
    private Share share;

    /**
     * Value the investor is willing to pay for this share.
     */
    private int value;

    public Proposal(Share share, int value) {
        this.share = share;
        this.value = value;
    }

    public Share getShare() {
        return share;
    }

    public void setShare(Share share) {
        this.share = share;
    }

    public int getValue() {
        return value;
    }

    /**
     * Converts this object to a JSON string.
     * @return JSON string of this object.
     */
    public String toJsonStr() {
        return new Gson().toJson(this);
    }
}
