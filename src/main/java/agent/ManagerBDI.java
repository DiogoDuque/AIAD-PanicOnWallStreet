package agent;

import assets.Company;
import assets.GameInfo;
import assets.Share;
import com.google.gson.Gson;
import communication.*;
import communication.message.AuctionMessage;
import communication.message.GameOverMessage;
import communication.message.IncomeMessage;
import communication.message.NegotiationMessage;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.IPlan;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.*;
import main.Main;

import java.util.ArrayList;
import java.util.Random;

@RequiredServices({
        @RequiredService(name="coms", type=IComsService.class, multiple=true, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM))
})
@ProvidedServices({
        @ProvidedService(type=IComsService.class, implementation=@Implementation(ComsService.class))
})
@Agent
public class ManagerBDI
{
    @Agent
    private IInternalAccess agent;

    @AgentFeature
    private IRequiredServicesFeature reqServ;

    @AgentFeature
    protected IBDIAgentFeature agentFeature;

    /**
     * Communication service.
     */
    private IComsService coms;

    /**
     * Current amount of money.
     */
    private int currentMoney;

    private int moneyAtBeginningOfAuction = -1;

    /**
     * Owned shares. These shares can be bought by investors, but they stay with the manager for further accounting purposes.
     */
    private ArrayList<Share> ownedShares;

    @AgentCreated
    public void init(){
        currentMoney = Main.STARTING_MONEY;

        Random r = new Random();
        String myCid = agent.getComponentIdentifier().getLocalName();
        ArrayList<Company> companies = Main.getCompanies();
        ownedShares = new ArrayList<>();

        ownedShares.add(new Share(companies.get(r.nextInt(companies.size())), myCid));
        ownedShares.add(new Share(companies.get(r.nextInt(companies.size())), myCid));
        ownedShares.add(new Share(companies.get(r.nextInt(companies.size())), myCid));
        ownedShares.add(new Share(companies.get(r.nextInt(companies.size())), myCid));
        this.coms = (IComsService)reqServ.getRequiredService("coms").get();
        IComsService iComs = SServiceProvider.getService(agent,IComsService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
        ISubscriptionIntermediateFuture<String> sub = iComs.subscribeComs();
        sub.addIntermediateResultListener(new IntermediateDefaultResultListener<String>() {
            @Override
            public void intermediateResultAvailable(String result) {
                switch(TimerBDI.getGamePhase()){
                    case NEGOTIATION:
                        if(moneyAtBeginningOfAuction != -1) //condition to help auction phase
                            moneyAtBeginningOfAuction=-1;
                        NegotiationMessage nMsg = new Gson().fromJson(result, NegotiationMessage.class);
                        parseNegotiationMessage(nMsg);
                        break;

                    case INVESTOR_INCOME:
                        break;

                    case MANAGER_INCOME:
                        IncomeMessage miMsg = new Gson().fromJson(result, IncomeMessage.class);
                        parseManagerIncomeMessage(miMsg);
                        break;

                    case MANAGEMENT_COST_PAYMENT:
                        int costs = ownedShares.size()*Main.MANAGEMENT_COST_PER_SHARE;
                        currentMoney -= costs;
                        break;

                    case AUCTION:
                        if(moneyAtBeginningOfAuction==-1)
                            moneyAtBeginningOfAuction=currentMoney;
                        AuctionMessage aMsg = new Gson().fromJson(result, AuctionMessage.class);
                        parseAuctionMessage(aMsg);
                        break;

                    case GAMEOVER:
                        GameOverMessage goMsg = new Gson().fromJson(result, GameOverMessage.class);
                        if(goMsg.getMsgType().equals(GameOverMessage.MessageType.ASK_GAMEOVER_INFO)) {
                            GameInfo.getInstance().setInfos(TimerBDI.getRound()+1, myCid, currentMoney);
                            coms.sendGameOverInfo(myCid, currentMoney + "");
                        }
                        break;
                }
            }
        });
    }

    @Goal
    public class WinShareAuctionGoal {
        public Share auctionedShare;

        public String myCid;

        public WinShareAuctionGoal(Share auctionedShare) {
            this.auctionedShare = auctionedShare;
            this.myCid = agent.getComponentIdentifier().getLocalName();

            int sharesAvailableAtAuctionEnd = 4*Main.N_MANAGERS + TimerBDI.getRound()*(Main.SHARES_ADDED_PER_ROUND);
            int mySharesCount = ownedShares.size();
            int sharesRatio = mySharesCount/sharesAvailableAtAuctionEnd;

            if(sharesRatio > (1/Main.N_MANAGERS)) // if has above average number of shares
                agentFeature.adoptPlan(new ConservativePlan(this)); //be conservative
            else if(mySharesCount < (sharesAvailableAtAuctionEnd/Main.N_MANAGERS)-Main.SHARES_ADDED_PER_ROUND) // if has number of shares too below average
                agentFeature.adoptPlan(new RiskyPlan(this));//be risky
            else
                agentFeature.adoptPlan(new RegularPlan(this)); //be regular
        }
    }

    @Plan
    public class ConservativePlan {
        protected WinShareAuctionGoal goal;

        public ConservativePlan(WinShareAuctionGoal goal) {
            this.goal = goal;
        }

        @PlanBody
        public void bid(final IPlan plan) {
            log("Starting conservative plan");
            int nextPossibleBid = goal.auctionedShare.getHighestBidderValue() + 10;
            int futureMoney = currentMoney - nextPossibleBid;

            if (futureMoney/moneyAtBeginningOfAuction < 1/2 || currentMoney<0) { // keep at least 1/2 of money overall // spends less overall money
                log("Aborting plan. I don't want to spend more money at this auction.");
                return;
            }
            if (nextPossibleBid/moneyAtBeginningOfAuction > 1/3) { // do not spend more that 1/3 of money on a single share
                log("Aborting plan. I don't want to spend more money on this share.");
                return;
            }

            if (goal.auctionedShare.getShareAverageNextValue() > 20){ // only bids if shareAverageNextValue>20
                log("Bidding");
                coms.bidOnShare(goal.myCid, new Proposal(goal.auctionedShare, nextPossibleBid).toJsonStr());
            } else log("Aborting plan. Share does not seem good enough.");
        }
    }

    @Plan
    public class RegularPlan {
        protected WinShareAuctionGoal goal;

        public RegularPlan(WinShareAuctionGoal goal) {
            this.goal = goal;
        }

        @PlanBody
        public void bid(final IPlan plan) {
            log("Starting regular plan");
            int nextPossibleBid = goal.auctionedShare.getHighestBidderValue()+10;
            int futureMoney = currentMoney - nextPossibleBid;

            if (futureMoney/moneyAtBeginningOfAuction < 1/3 || currentMoney<0) { // keep at least 1/3 of money overall
                log("Aborting plan. I don't want to spend more money at this auction.");
                return;
            }
            if(nextPossibleBid/moneyAtBeginningOfAuction > 1/4) {// do not spend more that 1/4 of money on a single share
                log("Aborting plan. I don't want to spend more money on this share.");
                return;
            }
            if(goal.auctionedShare.getShareAverageNextValue() > 10){ // only bids if shareAverageNextValue>10
                log("Bidding");
                coms.bidOnShare(goal.myCid, new Proposal(goal.auctionedShare, nextPossibleBid).toJsonStr());
            } else log("Aborting plan. Share does not seem good enough.");
        }
    }

    @Plan
    public class RiskyPlan {
        protected WinShareAuctionGoal goal;

        public RiskyPlan(WinShareAuctionGoal goal) {
            this.goal = goal;
        }

        @PlanBody
        public void bid(final IPlan plan) {
            log("Starting risky plan");
            int nextPossibleBid = goal.auctionedShare.getHighestBidderValue()+10;
            int futureMoney = currentMoney - nextPossibleBid;

            if (futureMoney/moneyAtBeginningOfAuction < 1/3 || currentMoney<0) { // keep at least 1/3 of money overall
                log("Aborting plan. I don't want to spend more money at this auction.");
                return;
            }
            if (nextPossibleBid/moneyAtBeginningOfAuction > 1/3) { // do not spend more that 1/3 of money on a single share
                log("Aborting plan. I don't want to spend more money on this share.");
                return;
            }
            if(goal.auctionedShare.getShareAverageNextValue() > 10) {//only bids if shareAverageNextValue>10
                log("Bidding");
                coms.bidOnShare(goal.myCid, new Proposal(goal.auctionedShare, nextPossibleBid).toJsonStr());
            } else log("Aborting plan. Share does not seem good enough.");
        }
    }


    private void parseAuctionMessage(AuctionMessage msg) {
        String myCid = agent.getComponentIdentifier().getLocalName();



        switch (msg.getMsgType()){
            case SHARE_SOLD:
                if(msg.getReceiverCid().equals(myCid)){
                    Share share = new Gson().fromJson(msg.getJsonExtra(), Share.class);
                    ownedShares.add(new Share(share.getCompany(), myCid));
                }
                break;

            case BID_REJECTED:
                if(!msg.getReceiverCid().equals(myCid)) //if not for me, break. else, continue as if receiving SHARE_AUTION
                    break;

            case SHARE_AUCTION:
                Share share = new Gson().fromJson(msg.getJsonExtra(), Share.class);
                agentFeature.getGoals().forEach((goal) -> goal.drop());
                agentFeature.dispatchTopLevelGoal(new WinShareAuctionGoal(share));

        }
    }

    private void parseManagerIncomeMessage(IncomeMessage msg) {
        String myCid = agent.getComponentIdentifier().getLocalName();

        switch (msg.getMsgType()){
            case ASK_MANAGER_INFO:
                Company[] companies = new Gson().fromJson(msg.getJsonExtra(), Company[].class);
                for(Share s: ownedShares){
                    // updates companies in shares
                    for(Company c: companies){
                        if(s.getCompanyName().equals(c.getName())) {
                            s.updateCompany(c);
                            break;
                        }
                    }

                    if(!s.isBought()){ //delete proposals if not bought. else, ask for money
                        s.setHighestBidderValue(0);
                        s.setHighestBidder(null);

                    } else coms.askInvestorForManagerIncome(myCid, s.getHighestBidder(), s.toJsonStr());
                }
                break;

            case MANAGER_INCOME_RESULT:
                if(!msg.getReceiverCid().equals(myCid)) //if msg not for me, ignore
                    break;

                Integer income = new Gson().fromJson(msg.getJsonExtra(), Integer.class);
                currentMoney += income;
                log("I currently have "+currentMoney);
        }
    }

    @AgentBody
	public void executeBody()
	{
	}

    /**
     * Called as a parser of messages in the Negotiation phase. Receives a message and deals with it the best way it can.
     * @param msg negotation message to be parsed.
     */
    private void parseNegotiationMessage(NegotiationMessage msg) {
        String myCid = agent.getComponentIdentifier().getLocalName();
        if(msg.getSenderCid().equals(myCid)) //if msg was sent by me
            return;

        switch (msg.getMsgType()){
            case ASK_INFO:
                GameInfo.getInstance().setInfos(TimerBDI.getRound(), myCid, currentMoney);
                ArrayList<Share> shares = new ArrayList<>();
                for(Share s: ownedShares){
                    if(!s.isBought())
                        shares.add(s);
                }
                coms.sendShares(agent.getComponentIdentifier().getLocalName(), new Gson().toJson(shares.toArray(new Share[shares.size()])));

                break;

            case NEW_PROPOSAL:
                if(!msg.getReceiverCid().equals(myCid)) //if proposal is not for me
                    break;

                Proposal proposal = new Gson().fromJson(msg.getJsonExtra(), Proposal.class);
                Share share = null;
                for(Share s: ownedShares){
                    if(proposal.getShare().equals(s)){
                        share = s;
                        break;
                    }
                }
                if(share == null){
                    log("ERROR: Received proposal for share not owned... Rejecting");
                    coms.rejectProposal(myCid, msg.getSenderCid(), new Proposal(share,proposal.getValue()).toJsonStr());
                    break;
                } else if(share.isBought()){
                    log("Received proposal for share already owned. #sorrynotsorry");
                    coms.rejectProposal(myCid, msg.getSenderCid(), new Proposal(share,proposal.getValue()).toJsonStr());
                } else {
                    log("Received new proposal for " + proposal.getShare());
                    log(proposal.getValue() + " vs. " + share.getHighestBidderValue());
                    if(proposal.getValue() > share.getHighestBidderValue()){ //TODO better decision
                        log("Accepting proposal");
                        share.setHighestBidder(msg.getSenderCid());
                        share.setHighestBidderValue(proposal.getValue());
                        log("updated share: "+share);
                        coms.acceptProposal(agent.getComponentIdentifier().getLocalName(), msg.getSenderCid(), proposal.toJsonStr());
                    } else {
                        log("Rejecting proposal");
                        proposal.setShare(share);
                        coms.rejectProposal(agent.getComponentIdentifier().getLocalName(), msg.getSenderCid(), proposal.toJsonStr());
                    }
                }
                break;
            case CLOSE_DEAL:
                if(!msg.getReceiverCid().equals(myCid)) //if proposal is not for me
                    break;
                Proposal closeProposal = new Gson().fromJson(msg.getJsonExtra(), Proposal.class);
                Share closeShare = null;
                for(Share s: ownedShares){
                    if(s.equals(closeProposal.getShare())){
                        closeShare = s;
                        break;
                    }
                }

                if(closeShare == null){
                    log("ERROR: Received close proposal for share not owned... Rejecting");
                    coms.rejectProposal(myCid, msg.getSenderCid(), new Proposal(closeShare,closeProposal.getValue()).toJsonStr());
                    break;
                } else if(closeShare.isBought()){
                    log("Received close proposal for share already owned. #sorrynotsorry");
                    coms.rejectProposal(myCid, msg.getSenderCid(), new Proposal(closeShare,closeProposal.getValue()).toJsonStr());
                } else {
                    log("Received new close proposal for "+ closeProposal.getShare());
                    long timeSinceStartOfPhase = System.currentTimeMillis() - TimerBDI.getPhaseStartTime();
                    log("Duration so far: " + timeSinceStartOfPhase);

                    float closeThreshold = (closeProposal.getShare().getShareAverageNextValue() + Main.MANAGEMENT_COST_PER_SHARE) / 2 + Main.MANAGEMENT_COST_PER_SHARE;

                    if(msg.getSenderCid().equals(closeShare.getHighestBidder()) &&
                        (closeProposal.getValue() > closeThreshold - closeThreshold * (timeSinceStartOfPhase/Main.NEGOTIATION_PHASE_DURATION))) {
                        log("Accepting close proposal");
                        closeShare.setHighestBidder(msg.getSenderCid());
                        closeShare.setHighestBidderValue(closeProposal.getValue());
                        closeShare.setAsBought();
                        log("updated share: " + closeShare);
                        coms.acceptCloseDeal(agent.getComponentIdentifier().getLocalName(), msg.getSenderCid(), closeProposal.toJsonStr());
                    } else {
                        log("Rejecting proposal");
                        coms.rejectCloseDeal(agent.getComponentIdentifier().getLocalName(), msg.getSenderCid(), closeProposal.toJsonStr());
                    }
                }
                break;

            /*
            case PROPOSAL_REJECTED:
                if(!msg.getReceiverCid().equals(myCid)) //if proposal is not for me
                    break;

                Proposal proposalR = new Gson().fromJson(msg.getJsonExtra(), Proposal.class);
                Share shareR = null;
                for(Share s: ownedShares){
                    if(proposalR.getShare().equals(s)){
                        shareR = s;
                        break;
                    }
                }
                if(shareR == null){
                    log("ERROR: Received rejection proposal for share not owned...");
                    break;
                }

                shareR.setHighestBidder(null);
                shareR.setHighestBidderValue(0);

                break;*/
        }
    }

    /**
     * A logging function. Used mainly for debugging and showing what's happening inside this specific agent.
     * @param msg message to be displayed.
     */
    private void log(String msg){
        System.out.println(agent.getComponentIdentifier().getLocalName()+": "+msg);
    }
}
