package agent;

import assets.Company;
import assets.GameInfo;
import assets.Share;
import com.google.gson.Gson;
import communication.message.GameOverMessage;
import communication.message.IncomeMessage;
import communication.message.NegotiationMessage;
import jadex.bdiv3.annotation.*;
import communication.*;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.IPlan;
import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IExecutionFeature;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.*;
import main.Main;

import java.util.*;

@RequiredServices({
        @RequiredService(name="coms", type=IComsService.class, multiple=true, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM))
})
@Agent
public class InvestorBDI
{
	@Agent
	protected IInternalAccess agent;

	@AgentFeature
	private IRequiredServicesFeature reqServ;

    @AgentFeature
    protected IExecutionFeature execFeature;

    @AgentFeature
    protected IBDIAgentFeature agentFeature;

    /**
     * Communication service.
     */
	private IComsService coms;

    /**
     * Agent name.
     */
    private String name;

    /**
     * Current amount of money.
     */
    private int currentMoney;

    /**
     * Shares acquired from a manager.
     */
    private ArrayList<Share> boughtShares;

    /**
     * Shares whose proposals have been accepted by managers. However, they can be rejected at any time, so this is a very volatile list.
     */
    private ArrayList<Share> proposedShares;

    /**
     * Hashmap containing regularly updated info on the other investors. Info can be retrieved with the agent's name as key.
     */
    private HashMap<String, InvestorInfo> investorInfos;

    /**
     * Hashmap containing regularly updated info on manager's shares. Info can be retrieved with the agent's name as key.
     */
    private HashMap<String, ArrayList<Share>> managerInfos;

    @AgentCreated
    public void init(){
        name = agent.getComponentIdentifier().getLocalName();

        currentMoney = Main.STARTING_MONEY;
        boughtShares = new ArrayList<>();
        proposedShares = new ArrayList<>();

        investorInfos = new HashMap<>();
        managerInfos = new HashMap<>();

        String myCid = agent.getComponentIdentifier().getName();
        this.coms = (IComsService)reqServ.getRequiredService("coms").get();
        IComsService iComs = SServiceProvider.getService(agent,IComsService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
        ISubscriptionIntermediateFuture<String> sub = iComs.subscribeComs();
        sub.addIntermediateResultListener(new IntermediateDefaultResultListener<String>() {
            @Override
            public void intermediateResultAvailable(String result) {
                switch(TimerBDI.getGamePhase()){
                    case NEGOTIATION:
                        NegotiationMessage nMsg = new Gson().fromJson(result, NegotiationMessage.class);
                        parseNegotiationMessage(nMsg);
                        break;

                    case INVESTOR_INCOME:
                        IncomeMessage iiMsg = new Gson().fromJson(result, IncomeMessage.class);
                        parseInvestorIncomeMessage(iiMsg);
                        break;

                    case MANAGER_INCOME:
                        IncomeMessage miMsg = new Gson().fromJson(result, IncomeMessage.class);
                        parseManagerIncomeMessage(miMsg);
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
    public class BeTheRichestInvestorGoal {
        protected ArrayList<InvestorInfo> currentInvestorsInfo;

        protected float ownEvaluation;

        protected float differenceToRichest;

        protected float advantageToNext;

        /**
         * Returns whether the investor is the richest or not.
         * @return true if the investor is the richest investor or has the same amount of money as the richest investor and false otherwise.
         */
        private boolean amITheRichest() {
            float richestEvaluation = this.currentInvestorsInfo.get(this.currentInvestorsInfo.size() - 1).evaluate();
            return richestEvaluation == ownEvaluation;
        }

        /**
         * Returns whether the investor is the poorest or not.
         * @return true if the investor is the poorest investor or has the same amount of money as the poorest investor and false otherwise.
         */
        private boolean amIThePoorest() {
            float poorestEvaluation = this.currentInvestorsInfo.get(0).evaluate();
            return poorestEvaluation == ownEvaluation;
        }

        protected void setDifferenceToRichest() {
            if (amITheRichest() || this.currentInvestorsInfo.size() <= 1) {
                this.differenceToRichest = 0;
            } else {
                float richestEvaluation = this.currentInvestorsInfo.get(this.currentInvestorsInfo.size() - 1).evaluate();
                this.differenceToRichest = richestEvaluation - ownEvaluation;
            }
        }

        protected void setAdvantageToNext() {
            if (amIThePoorest() || this.currentInvestorsInfo.size() <= 1) {
                this.differenceToRichest = 0;
            } else {
                for (int investorIndex = 0; investorIndex < this.currentInvestorsInfo.size(); investorIndex++) {
                    InvestorInfo info = this.currentInvestorsInfo.get(investorIndex);
                    if (info.getInvestorName().equals(InvestorBDI.this.name)) {
                        InvestorInfo nextInfo = this.currentInvestorsInfo.get(investorIndex - 1);
                        this.advantageToNext = info.evaluate() - nextInfo.evaluate();
                    }
                }
            }
        }

        public float getDifferenceToRichest() {
            return differenceToRichest;
        }

        public float getAdvantageToNext() {
            return advantageToNext;
        }

        public BeTheRichestInvestorGoal() {
            this.currentInvestorsInfo = new ArrayList<>(InvestorBDI.this.investorInfos.values());


            // Sorts based on evaluation, see InvestorInfo.compareTo() method
            Collections.sort(this.currentInvestorsInfo);

            this.ownEvaluation = investorInfos.get(name).evaluate();
            this.setDifferenceToRichest();
            this.setAdvantageToNext();

            log("Picking a plan...");
            if (amITheRichest() && amIThePoorest()) {
                InvestorBDI.this.agentFeature.adoptPlan(new RegularPlan(this));
            } else if (amITheRichest()) {
                InvestorBDI.this.agentFeature.adoptPlan(new ConservativePlan(this));
            } else if (amIThePoorest()) {
                InvestorBDI.this.agentFeature.adoptPlan(new RiskyPlan(this));
            } else {
                InvestorBDI.this.agentFeature.adoptPlan(new RegularPlan(this));
            }
        }
    }

    public abstract class InvestPlan {
        protected BeTheRichestInvestorGoal goal;

        public InvestPlan(BeTheRichestInvestorGoal goal) {
            this.goal = goal;
        }

        public void invest(final IPlan plan) {
            ArrayList<Share> orderedShares = orderSharesAverage();
            ArrayList<Share> sharesToPropose = pickShares(orderedShares);
            sendProposals(sharesToPropose);
        }

        protected abstract ArrayList<Share> orderSharesAverage();
        protected abstract ArrayList<Share> pickShares(ArrayList<Share> allShares);

        protected void sendProposals(ArrayList<Share> shares) {
            for (Share share : shares) {
                int proposalValue = share.getHighestBidderValue() + 1;
                Proposal proposal = new Proposal(share, proposalValue);
                String proposalString = proposal.toJsonStr();
                String manager = share.getOwnerCid();
                log("Sending proposal to "+manager);
                coms.sendProposal(name, manager, proposalString);
            }
        }
    }

    @Plan
    public class ConservativePlan extends InvestPlan {
        public ConservativePlan(BeTheRichestInvestorGoal goal) {
            super(goal);
        }

        @PlanBody
        public void invest(final IPlan plan) {
            log("Adopted conservative plan.");
            super.invest(plan);
        }

        protected ArrayList<Share> orderSharesAverage() {
            ArrayList<Share> allShares = new ArrayList<>();
            for(Map.Entry<String, ArrayList<Share>> entry: managerInfos.entrySet()){
                allShares.addAll(entry.getValue());
            }

            // Sorts based on ShareAverageValue, MinPossibleValue and HighestBidderValue
            Collections.sort(allShares, (share1, share2) -> {
                double share1Value = share1.getShareAverageNextValue()*0.8 + share1.getMinPossibleValue()*0.2 - share1.getHighestBidderValue();
                double share2Value = share2.getShareAverageNextValue()*0.8 + share2.getMinPossibleValue()*0.2 - share2.getHighestBidderValue();
                return Double.compare(share2Value, share1Value);
            });

            return allShares;
        }

        protected ArrayList<Share> pickShares(ArrayList<Share> allShares) {
            ArrayList<Share> shares = new ArrayList<>();
            double toSpendLimit = goal.getAdvantageToNext();
            int availableShares = allShares.size();
            int currentShareIndex = 0;

            while (toSpendLimit > 0 && availableShares > 0) {
                Share currentShare = allShares.get(currentShareIndex);
                if (toSpendLimit > currentShare.getHighestBidderValue() &&
                        currentShare.getShareAverageNextValue() > currentShare.getHighestBidderValue()) {
                    shares.add(currentShare);
                    toSpendLimit -= currentShare.getHighestBidderValue();
                    availableShares--;
                }
            }

            return shares;
        }
    }

    @Plan
    public class RegularPlan extends InvestPlan {
        public RegularPlan(BeTheRichestInvestorGoal goal) {
            super(goal);
        }

        @PlanBody
        public void invest(final IPlan plan) {
            log("Adopted regular plan.");
            super.invest(plan);
        }

        protected ArrayList<Share> orderSharesAverage() {
            ArrayList<Share> allShares = new ArrayList<>();
            for(Map.Entry<String, ArrayList<Share>> entry: managerInfos.entrySet()){
                allShares.addAll(entry.getValue());
            }

            // Sorts based on ShareAverageValue and HighestBidderValue
            Collections.sort(allShares, (share1, share2) -> {
                double share1Value = share1.getShareAverageNextValue() - share1.getHighestBidderValue();
                double share2Value = share2.getShareAverageNextValue() - share2.getHighestBidderValue();
                return Double.compare(share2Value, share1Value);
            });

            return allShares;
        }

        protected ArrayList<Share> pickShares(ArrayList<Share> allShares) {
            ArrayList<Share> shares = new ArrayList<>();
            double money = currentMoney * 0.75;
            int availableShares = allShares.size();

            int currentShareIndex = 0;

            while (money > 0 && availableShares > 0) {
                Share currentShare = allShares.get(currentShareIndex);
                if (money > currentShare.getHighestBidderValue() &&
                        currentShare.getShareAverageNextValue() > currentShare.getHighestBidderValue()) {
                    shares.add(currentShare);
                    money -= currentShare.getHighestBidderValue();
                    availableShares--;
                }
            }

            return shares;
        }
    }

    @Plan
    public class RiskyPlan extends InvestPlan {
        public RiskyPlan(BeTheRichestInvestorGoal goal) {
            super(goal);
        }

        @PlanBody
        public void	invest(final IPlan plan) {
            log("Adopted risky plan.");
            super.invest(plan);
        }

        protected ArrayList<Share> orderSharesAverage() {
            ArrayList<Share> allShares = new ArrayList<>();
            for(Map.Entry<String, ArrayList<Share>> entry: managerInfos.entrySet()){
                allShares.addAll(entry.getValue());
            }

            // Sorts based on ShareAverageValue, MaxPossibleValue and HighestBidderValue
            Collections.sort(allShares, (share1, share2) -> {
                double share1Value = share1.getShareAverageNextValue()*0.8 + share1.getMaxPossibleValue()*0.2 - share1.getHighestBidderValue();
                double share2Value = share2.getShareAverageNextValue()*0.8 + share2.getMaxPossibleValue()*0.2 - share2.getHighestBidderValue();
                return Double.compare(share2Value, share1Value);
            });

            return allShares;
        }

        protected ArrayList<Share> pickShares(ArrayList<Share> allShares) {
            ArrayList<Share> shares = new ArrayList<>();

            float earningsGoal = goal.getDifferenceToRichest();
            int money = currentMoney;
            int availableShares = allShares.size();

            int currentShareIndex = 0;

            while (earningsGoal > 0 && money > 0 && availableShares > 0) {
                Share currentShare = allShares.get(currentShareIndex);
                if (money > currentShare.getHighestBidderValue()) {
                    shares.add(currentShare);
                    money -= currentShare.getHighestBidderValue();
                    earningsGoal -= currentShare.getHighestBidderValue();
                    availableShares--;
                }
            }

            return shares;
        }
    }

    @AgentBody
	public void executeBody() {}

    /**
     * Called as a parser of messages in the Negotiation phase. Receives a message and deals with it the best way it can.
     * @param msg negotation message to be parsed.
     */
    private void parseNegotiationMessage(NegotiationMessage msg) {
        String myCid = agent.getComponentIdentifier().getLocalName();
        if(msg.getSenderCid().equals(myCid)) { //if msg was sent by me
            return;
        }

        switch (msg.getMsgType()){
            case ASK_INFO:
                GameInfo.getInstance().setInfos(TimerBDI.getRound(), myCid, currentMoney);
                InvestorInfo info = new InvestorInfo(name, currentMoney, boughtShares, proposedShares);
                this.investorInfos.put(name, info);
                coms.sendInvestorInfo(myCid, info.toJsonStr());
                break;

            case MANAGER_SHARES:
                Share[] sharesArr = new Gson().fromJson(msg.getJsonExtra(), Share[].class);
                ArrayList<Share> shares = new ArrayList<Share>(Arrays.asList(sharesArr));
                this.managerInfos.put(msg.getSenderCid(), shares);
                /*Share chosenShare = shares.get(0);
                log("Received "+msg.getSenderCid()+" shares. Sending a proposal for share "+chosenShare);
                coms.sendProposal(myCid, msg.getSenderCid(), new Proposal(chosenShare, 10).toJsonStr());*/
                break;

            case INVESTOR_INFO:
                InvestorInfo investorInfo = new Gson().fromJson(msg.getJsonExtra(), InvestorInfo.class);
                this.investorInfos.put(msg.getSenderCid(), investorInfo);
                agentFeature.dispatchTopLevelGoal(new BeTheRichestInvestorGoal());
                break;

            case PROPOSAL_ACCEPTED:
                if(!msg.getReceiverCid().equals(myCid)) //if proposal is not for me
                    break;

                Proposal proposalA = new Gson().fromJson(msg.getJsonExtra(), Proposal.class);
                proposedShares.add(proposalA.getShare());
                log("Proposal was accepted");
                break;

            case PROPOSAL_REJECTED:
                if(!msg.getReceiverCid().equals(myCid)) //if proposal is not for me
                    break;

                Proposal proposalR = new Gson().fromJson(msg.getJsonExtra(), Proposal.class);
                proposedShares.remove(proposalR.getShare());
                log("Proposal was denied");
                break;

            default:
                break;
        }
    }

    /**
     * Called as a parser of messages in the Investor Income phase. Receives a message and deals with it the best way it can.
     * @param msg negotation message to be parsed.
     */
    private void parseInvestorIncomeMessage(IncomeMessage msg) {
        String myCid = agent.getComponentIdentifier().getLocalName();
        if(msg.getSenderCid().equals(myCid)) { //if msg was sent by me
            return;
        }

        switch (msg.getMsgType()){
            case ASK_INVESTOR_INFO:
                proposedShares.clear();
                investorInfos.clear();
                Company[] companies = new Gson().fromJson(msg.getJsonExtra(),Company[].class);
                for(Share s: boughtShares){
                    for(Company c: companies){
                        if(s.getCompanyName().equals(c.getName())){
                            s.updateCompany(c);
                        }
                    }
                }
                coms.sendInfoForInvestorIncomeCalculation(myCid,new Gson().toJson(boughtShares.toArray(new Share[boughtShares.size()])));
                break;

            case INVESTOR_RESULT:
                if(!msg.getReceiverCid().equals(myCid))
                    break;
                Integer income = new Gson().fromJson(msg.getJsonExtra(),Integer.class);
                currentMoney += income;
                log("My final balance after the Income Phase is "+currentMoney);
                break;

            default:
                break;
        }
    }

    /**
     * Called as a parser of messages in the Manager Income phase. Receives a message and deals with it the best way it can.
     * @param msg negotation message to be parsed.
     */
    private void parseManagerIncomeMessage(IncomeMessage msg){
        String myCid = agent.getComponentIdentifier().getLocalName();

        switch (msg.getMsgType()){
            case ASK_INVESTOR_FOR_MANAGER_INCOME:
                if(!msg.getReceiverCid().equals(myCid)) //if msg not for me, ignore
                    break;
                Share share = new Gson().fromJson(msg.getJsonExtra(), Share.class);
                int income = share.getHighestBidderValue();
                if(income > currentMoney){
                    log("WARNING, I'm in debt!");
                }
                coms.sendManagerIncome(myCid, msg.getSenderCid(), new Gson().toJson(new Integer(income)));

                break;
            
            default:
                break;
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
