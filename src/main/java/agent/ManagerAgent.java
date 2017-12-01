package agent;

import com.google.gson.Gson;
import communication.Message;
import communication.NegotiationMessage;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.IFuture;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.*;
import communication.ComsService;
import communication.IComsService;

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
    protected IInternalAccess agent;

    @AgentFeature
    private IRequiredServicesFeature reqServ;

    protected IComsService coms;

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

                if(msg.sentByInvestor() && msg.getMsgType()== NegotiationMessage.NegotiationMessageType.NEW_PROPOSAL){
                    coms.broadcast(new NegotiationMessage(agent.getComponentIdentifier().getName(), NegotiationMessage.NegotiationMessageType.PROPOSAL_ACCEPTED).toJsonStr());
                }
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
