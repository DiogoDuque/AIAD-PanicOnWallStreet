package agent;

import assets.Share;
import com.google.gson.Gson;
import communication.NegotiationMessage;
import communication.Proposal;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IExecutionFeature;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.*;
import communication.ComsService;
import communication.IComsService;
import main.Main;

import java.util.ArrayList;
import java.util.Arrays;

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

	private IComsService coms;

	@Belief
    private int currentMoney;

    private ArrayList<Share> boughtShares, proposedShares;

    @AgentCreated
    public void init(){
        currentMoney = Main.STARTING_MONEY;
        boughtShares = new ArrayList<>();
        proposedShares = new ArrayList<>();

        String myCid = agent.getComponentIdentifier().getName();
        this.coms = (IComsService)reqServ.getRequiredService("coms").get();
        IComsService iComs = SServiceProvider.getService(agent,IComsService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
        ISubscriptionIntermediateFuture<String> sub = iComs.subscribeComs();
        log("subscribed");
        sub.addIntermediateResultListener(new IntermediateDefaultResultListener<String>() {
            @Override
            public void intermediateResultAvailable(String result) {

                switch(TimerAgent.getGamePhase()){
                    case NEGOTIATION:
                        NegotiationMessage nMsg = new Gson().fromJson(result, NegotiationMessage.class);
                        parseNegotiationMessage(nMsg);
                        break;
                }
            }
        });
    }

    @Goal(recur=true)
    public class BeTheRichestInvestorGoal {
        public BeTheRichestInvestorGoal() {

        }
    }

    @Plan(trigger=@Trigger(goals=BeTheRichestInvestorGoal.class))
    protected void conservativePlan(BeTheRichestInvestorGoal goal) {
        // get current money
        // get diff between richest and me
        // get current available shares (with current proposal - or 0 when none)
        // order shares with algorithm
        // choose shares to propose to cover difference
        // send proposals
    }

    @Plan(trigger=@Trigger(goals=BeTheRichestInvestorGoal.class))
    protected void riskyPlan(BeTheRichestInvestorGoal goal) {
        // get current money
        // get diff between richest and me
        // get current available shares (with current proposal - or 0 when none)
        // order shares with algorithm
        // choose shares to propose to cover difference
        // send proposals
    }

    @Plan(trigger=@Trigger(goals=BeTheRichestInvestorGoal.class))
    protected void regularPlan(BeTheRichestInvestorGoal goal) {
        // get current money
        // get diff between richest and me
        // get current available shares (with current proposal - or 0 when none)
        // order shares with algorithm
        // choose shares to propose to cover difference
        // send proposals
    }


    @AgentBody
	public void executeBody()
	{
        agentFeature.dispatchTopLevelGoal(new BeTheRichestInvestorGoal());
	}

    private void parseNegotiationMessage(NegotiationMessage msg) {
        String myCid = agent.getComponentIdentifier().getLocalName();
        if(msg.getSenderCid().equals(agent.getComponentIdentifier().getName())) //if msg was sent by me
            return;

        switch (msg.getMsgType()){
            case MANAGER_SHARES:
                Share[] sharesArr = new Gson().fromJson(msg.getJsonExtra(), Share[].class);
                ArrayList<Share> shares = new ArrayList<Share>(Arrays.asList(sharesArr));
                Share chosenShare = shares.get(0);
                log("Received "+msg.getSenderCid()+" shares. Sending a proposal for share "+chosenShare);
                coms.sendProposal(myCid, msg.getSenderCid(), new Proposal(chosenShare, 10).toJsonStr());
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
                //log(msg.toJsonStr());
                break;
        }
    }

    private void log(String msg){
	    System.out.println(agent.getComponentIdentifier().getLocalName()+": "+msg);
    }
}
