package agent;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.IFuture;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.*;
import service.ComsService;
import service.IComsService;

@RequiredServices({
        @RequiredService(name="coms", type= IComsService.class, multiple=true, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM, dynamic=true))
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
	public IFuture<Void> executeBody()
	{
	    this.coms = (IComsService)reqServ.getRequiredService("coms").get();
        IComsService iComs = SServiceProvider.getService(agent,IComsService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
        ISubscriptionIntermediateFuture<String> sub = iComs.subscribeComs();
        sub.addIntermediateResultListener(new IntermediateDefaultResultListener<String>() {
            @Override
            public void intermediateResultAvailable(String result) {
                //super.intermediateResultAvailable(result);
                log(result);
            }
        });

        SServiceProvider.getService(agent,IComsService.class, RequiredServiceInfo.SCOPE_PLATFORM).get().broadcast("Manager says hi");
		return IFuture.DONE;
	}

    private void log(String msg){
        System.out.println(agent.getComponentIdentifier().getName()+" -> "+msg);
    }
}
