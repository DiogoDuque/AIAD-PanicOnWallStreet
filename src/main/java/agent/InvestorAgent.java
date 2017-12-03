package agent;

import assets.Share;
import com.google.gson.Gson;
import communication.NegotiationMessage;
import communication.Proposal;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.*;
import communication.ComsService;
import communication.IComsService;
import main.Main;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

@RequiredServices({
        @RequiredService(name="coms", type= IComsService.class, binding=@Binding(scope= RequiredServiceInfo.SCOPE_PLATFORM))
})
@ProvidedServices({
        @ProvidedService(type=IComsService.class, implementation=@Implementation(ComsService.class))
})
@Agent
public class InvestorAgent
{
	@Agent
	private IInternalAccess agent;

	@AgentFeature
	private IRequiredServicesFeature reqServ;

	private IComsService coms;

    private int currentMoney;

    private ArrayList<Share> boughtShares;

    @AgentCreated
    public void init(){
        currentMoney = Main.STARTING_MONEY;
        boughtShares = new ArrayList<>();
    }

	@AgentBody
	public void executeBody()
	{
        String myCid = agent.getComponentIdentifier().getName();
		this.coms = (IComsService)reqServ.getRequiredService("coms").get();
        IComsService iComs = SServiceProvider.getService(agent,IComsService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
        ISubscriptionIntermediateFuture<String> sub = iComs.subscribeComs();
        sub.addIntermediateResultListener(new IntermediateDefaultResultListener<String>() {
            @Override
            public void intermediateResultAvailable(String result) {
                NegotiationMessage msg = new Gson().fromJson(result, NegotiationMessage.class);

                if(msg.getSenderCid().equals(agent.getComponentIdentifier().getName())) //if msg was sent by me
                    return;

                switch (msg.getMsgType()){
                    case MANAGER_SHARES:
                        Share[] sharesArr = new Gson().fromJson(msg.getJsonExtra(), Share[].class);
                        ArrayList<Share> shares = new ArrayList<Share>(Arrays.asList(sharesArr));
                        Share chosenShare = shares.get(0);
                        log("Received "+msg.getSenderCid()+" shares. Sending a proposal...");
                        coms.sendProposal(agent.getComponentIdentifier().getName(), msg.getSenderCid(), new Proposal(chosenShare, 10).toJsonStr());
                        break;

                    case PROPOSAL_ACCEPTED:
                        if(!msg.getReceiverCid().equals(agent.getComponentIdentifier().getName())) //if proposal is not for me
                            break;

                        Proposal proposalA = new Gson().fromJson(msg.getJsonExtra(), Proposal.class);
                        log("Proposal was accepted");
                        break;

                    case PROPOSAL_REJECTED:
                        if(!msg.getReceiverCid().equals(agent.getComponentIdentifier().getName())) //if proposal is not for me
                            break;

                        Proposal proposalR = new Gson().fromJson(msg.getJsonExtra(), Proposal.class);
                        log("Proposal was denied");
                        break;

                        default:
                            //log(msg.toJsonStr());
                }
            }
        });

	}

	private void log(String msg){
	    System.out.println(agent.getComponentIdentifier().getName()+": "+msg);
    }

    private void log(String msg, String extra){
        System.out.println(agent.getComponentIdentifier().getName()+": "+msg+"; "+extra);
    }
}
