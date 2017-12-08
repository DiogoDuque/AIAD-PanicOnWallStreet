package agent;

import assets.Share;
import com.google.gson.Gson;
import communication.InvestorInfo;
import communication.NegotiationMessage;
import communication.Proposal;
import jadex.bdiv3.annotation.*;
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
import communication.IComsService;
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
                }
            }
        });
    }

    @Goal
    public class BeTheRichestInvestorGoal {
        @GoalResult
        protected ArrayList<InvestorInfo> currentInvestorsInfo;

        @GoalResult
        protected int differenceToRichest;

        private boolean amITheRichest() {
            if (this.currentInvestorsInfo.size() < 1) {
                return false;
            }

            String richestInvestorName = this.currentInvestorsInfo.get(this.currentInvestorsInfo.size() - 1).getInvestorName();

            return richestInvestorName.equals(InvestorBDI.this.name);
        }

        private boolean amIThePoorest() {
            if (this.currentInvestorsInfo.size() < 1) {
                return false;
            }
            String poorestInvestorName = this.currentInvestorsInfo.get(0).getInvestorName();

            return poorestInvestorName.equals(InvestorBDI.this.name);
        }

        protected void setDifferenceToRichest() {
            if (amITheRichest() || this.currentInvestorsInfo.size() <= 1) {
                this.differenceToRichest = 0;
            } else {
                int richestInvestorMoney = this.currentInvestorsInfo.get(this.currentInvestorsInfo.size() - 1).getCurrentMoney();

                for (InvestorInfo info : this.currentInvestorsInfo) {
                    if (info.getInvestorName().equals(InvestorBDI.this.name)) {
                        this.differenceToRichest = richestInvestorMoney - info.getCurrentMoney();
                    }
                }
            }
        }

        public BeTheRichestInvestorGoal() {
            this.currentInvestorsInfo = new ArrayList<InvestorInfo>(InvestorBDI.this.investorInfos.values());

            // Sorts based on evaluation, see InvestorInfo.compareTo() method
            Collections.sort(this.currentInvestorsInfo);

            this.setDifferenceToRichest();

            log("Picking a plan...");
            if (amITheRichest()) {
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

        public void invest(final IPlan plan) {}
    }

    @Plan
    public class ConservativePlan extends InvestPlan {
        public ConservativePlan(BeTheRichestInvestorGoal goal) {
            super(goal);
        }

        @PlanBody
        public void	invest(final IPlan plan) {
            log("Adopted conservative plan.");
        }
    }

    @Plan
    public class RegularPlan extends InvestPlan {
        public RegularPlan(BeTheRichestInvestorGoal goal) {
            super(goal);
        }

        @PlanBody
        public void	invest(final IPlan plan) {
            log("Adopted regular plan.");
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
        }
    }

    @AgentBody
	public void executeBody()
	{
        agentFeature.dispatchTopLevelGoal(new BeTheRichestInvestorGoal());
	}

    /**
     * Called as a parser of messages in the Negotiation phase. Receives a message and deals with it the best way it can.
     * @param msg negotation message to be parsed.
     */
    private void parseNegotiationMessage(NegotiationMessage msg) {
        String myCid = agent.getComponentIdentifier().getLocalName();
        if(msg.getSenderCid().equals(myCid)) { //if msg was sent by me
            //log("Received my message");
            return;
        }// else log("Received "+msg.getMsgType()+" from "+msg.getSenderCid());

        switch (msg.getMsgType()){
            case ASK_INFO:
                InvestorInfo info = new InvestorInfo(msg.getSenderCid(), currentMoney, boughtShares, proposedShares);
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
                break;

            /*case PROPOSAL_ACCEPTED:
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
                break;*/

            default:
                log(msg.toJsonStr());
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
