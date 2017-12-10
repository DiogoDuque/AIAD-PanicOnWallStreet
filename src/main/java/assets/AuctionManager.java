package assets;

import communication.IComsService;
import communication.Proposal;
import main.Main;

import java.util.ArrayList;

/**
 * Modified singleton.
 */
public class AuctionManager {

    private static AuctionManager instance = null;

    private final String myCid;

    private IComsService coms;

    private ArrayList<Share> shares;

    private long lastBidTime;

    private AuctionManager(String myCid, IComsService coms, ArrayList<Share> shares){
        this.myCid = myCid;
        this.coms = coms;
        this.shares = shares;
        this.lastBidTime = System.currentTimeMillis();
    }

    public static void instantiate(String myCid, IComsService coms, ArrayList<Share> shares){
        instance = new AuctionManager(myCid, coms, shares);
        coms.auctionShare(myCid,shares.get(0).toJsonStr());
    }

    public static AuctionManager getInstance() {
        return instance;
    }

    public void receivedBid(String sender, Proposal proposal){
        Share share = shares.get(0);
        if(!proposal.getShare().getCompanyName().equals(share.getCompanyName())) {
            log("outdated proposal received");
            coms.rejectBid(myCid,sender,share.toJsonStr());
            return;
        }

        if(proposal.getValue() > share.getHighestBidderValue()){
            log("new bid successfully placed");
            String oldHighestBidder = share.getHighestBidder();
            share.setHighestBidderValue(proposal.getValue());
            share.setHighestBidder(sender);
            if(oldHighestBidder != null)
                coms.rejectBid(myCid, oldHighestBidder, share.toJsonStr());
        }
        lastBidTime = System.currentTimeMillis();
    }

    public void receivedTime(long currTime){
        if((currTime-lastBidTime) > Main.AUCTION_DURATION){
            Share share = shares.get(0);
            if(share.getHighestBidder() != null){ // if share was bidded
                String shareOwner = share.getHighestBidder();
                log("Current share sold to "+shareOwner+" for "+share.getHighestBidderValue());
                share.setHighestBidder(null);
                share.setHighestBidderValue(0);
                coms.shareSold(myCid, shareOwner, share.toJsonStr());
            } else log("Current share was not bidded on. Trashing it...");
            shares.remove(share);

            if(shares.size() != 0){
                coms.auctionShare(myCid, shares.get(0).toJsonStr());
            } else instance = null;
        }
    }

    private void log(String msg){
        System.out.println("AuctionManager: "+msg);
    }
}
