package agent;

import assets.CompanyShare;
import com.google.gson.Gson;
import communication.NegotiationMessage;
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

    private ArrayList<CompanyShare> boughtShares;

    @AgentCreated
    public void init(){
        currentMoney = Main.STARTING_MONEY;
        boughtShares = new ArrayList<>();

        log("Just finished init!");
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

                log(msg.toJsonStr());
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
