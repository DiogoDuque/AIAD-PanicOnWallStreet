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
            System.out.println("AuctionManager: outdated proposal received");
            return;
        }

        if(proposal.getValue() > share.getHighestBidderValue()){
            share.setHighestBidderValue(proposal.getValue());
            share.setHighestBidder(sender);
        }
        lastBidTime = System.currentTimeMillis();
    }

    public void receivedTime(long currTime){
        if((currTime-lastBidTime) > Main.AUCTION_DURATION){
            Share share = shares.get(0);
            if(share.getHighestBidder() != null){ // if share was bidded
                String shareOwner = share.getHighestBidder();
                share.setHighestBidder(null);
                share.setHighestBidderValue(0);
                coms.shareSold(myCid, shareOwner, share.toJsonStr());
            }
            shares.remove(share);

            if(shares.size() != 0){
                coms.auctionShare(myCid, shares.get(0).toJsonStr());
            } else instance = null;
        }
    }
}
