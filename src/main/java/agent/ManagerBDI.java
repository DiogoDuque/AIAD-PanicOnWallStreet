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
import java.util.*;

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

    /**
     * Communication service.
     */
    private IComsService coms;

    /**
     * Current amount of money.
     */
    private int currentMoney;

    /**
     * Owned shares. These shares can be bought by investors, but they stay with the manager for further accounting purposes.
     */
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
        sub.addIntermediateResultListener(new IntermediateDefaultResultListener<String>() {
            @Override
            public void intermediateResultAvailable(String result) {
                switch(TimerBDI.getGamePhase()){
                    case NEGOTIATION:
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
                        break;
                }
            }
        });
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
                        s.setHighestBidderValue(-1);
                        s.setHighestBidder(null);

                    } else coms.askInvestorForManagerIncome(myCid, s.getHighestBidder(), s.toJsonStr());
                }
                break;

            case MANAGER_INCOME_RESULT:
                if(!msg.getReceiverCid().equals(myCid)) //if msg not for me, ignore
                    break;

                Integer income = new Gson().fromJson(msg.getJsonExtra(), Integer.class);
                currentMoney += income;
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
        String myCid = agent.getComponentIdentifier().getName();
        if(msg.getSenderCid().equals(myCid)) //if msg was sent by me
            return;

        switch (msg.getMsgType()){
            case ASK_INFO:
                ArrayList<Share> shares = new ArrayList<>();
                for(Share s: ownedShares){
                    if(!s.isBought())
                        shares.add(s);
                }
                coms.sendShares(agent.getComponentIdentifier().getName(), new Gson().toJson(shares.toArray(new Share[shares.size()])));

                break;

            /*case NEW_PROPOSAL:
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
