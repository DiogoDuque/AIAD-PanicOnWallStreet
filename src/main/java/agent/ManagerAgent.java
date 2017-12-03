package agent;

import assets.Company;
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

import java.util.ArrayList;
import java.util.Random;

@RequiredServices({
        @RequiredService(name="coms", type=IComsService.class, multiple=true, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM, dynamic=true))
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
        ownedShares = new ArrayList<>();
        ArrayList<Company> companies = Main.getCompanies();
        ownedShares.add(new Share(companies.get(r.nextInt(companies.size()))));
        ownedShares.add(new Share(companies.get(r.nextInt(companies.size()))));
        ownedShares.add(new Share(companies.get(r.nextInt(companies.size()))));
        ownedShares.add(new Share(companies.get(r.nextInt(companies.size()))));
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
                    case NEW_PROPOSAL:
                        if(!msg.getReceiverCid().equals(agent.getComponentIdentifier().getName())) //if proposal is not for me
                            break;

                        Proposal proposal = new Gson().fromJson(msg.getJsonExtra(), Proposal.class);
                        log("Received new proposal for "+proposal.getShare());
                        Random r = new Random();
                        if(r.nextInt(2)==0){
                            log("Accepting proposal");
                            coms.acceptProposal(agent.getComponentIdentifier().getName(), msg.getSenderCid(), proposal.toJsonStr());
                        } else {
                            log("Rejecting proposal");
                            coms.rejectProposal(agent.getComponentIdentifier().getName(), msg.getSenderCid(), proposal.toJsonStr());
                        }
                }
            }
        });

        coms.sendShares(agent.getComponentIdentifier().getName(), new Gson().toJson(ownedShares.toArray(new Share[ownedShares.size()])));
	}

    private void log(String msg){
        System.out.println(agent.getComponentIdentifier().getName()+": "+msg);
    }

    private void log(String msg, String extra){
        System.out.println(agent.getComponentIdentifier().getName()+": "+msg+"; "+extra);
    }
}
