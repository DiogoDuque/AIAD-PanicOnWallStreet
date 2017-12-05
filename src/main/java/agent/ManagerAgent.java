package agent;

import assets.Company;
import assets.Share;
import com.google.gson.Gson;
import communication.*;
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
public class ManagerAgent
{
    @Agent
    private IInternalAccess agent;

    @AgentFeature
    private IRequiredServicesFeature reqServ;

    private IComsService coms;

    private int currentMoney;

    private ArrayList<Share> ownedShares;

    @AgentCreated
    public void init(){
        currentMoney = Main.STARTING_MONEY;

        Random r = new Random();
        String myCid = agent.getComponentIdentifier().getName();
        ArrayList<Company> companies = Main.getCompanies();
        ownedShares = new ArrayList<>();

        ownedShares.add(new Share(companies.get(r.nextInt(companies.size())), myCid));
        ownedShares.add(new Share(companies.get(r.nextInt(companies.size())), myCid));
        ownedShares.add(new Share(companies.get(r.nextInt(companies.size())), myCid));
        ownedShares.add(new Share(companies.get(r.nextInt(companies.size())), myCid));

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

	@AgentBody
	public void executeBody()
	{
	}

    private void parseNegotiationMessage(NegotiationMessage msg) {
        String myCid = agent.getComponentIdentifier().getName();
        if(msg.getSenderCid().equals(myCid)) //if msg was sent by me
            return;

        switch (msg.getMsgType()){
            case ASK_SHARES:
                coms.sendShares(agent.getComponentIdentifier().getName(), new Gson().toJson(ownedShares.toArray(new Share[ownedShares.size()])));
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
                }

                log("Received new proposal for "+proposal.getShare());
                Random r = new Random();
                if(r.nextInt(2)==0){ //TODO better decision
                    log("Accepting proposal");
                    share.setHighestBidder(msg.getSenderCid());
                    share.setHighestBidderValue(proposal.getValue());
                    log("updated share: "+share);
                    coms.acceptProposal(agent.getComponentIdentifier().getName(), msg.getSenderCid(), proposal.toJsonStr());
                } else {
                    log("Rejecting proposal");
                    coms.rejectProposal(agent.getComponentIdentifier().getName(), msg.getSenderCid(), proposal.toJsonStr());
                }
                break;

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

                break;
        }
    }

    private void log(String msg){
        System.out.println(agent.getComponentIdentifier().getLocalName()+": "+msg);
    }
}
