package agent;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.*;
import service.ComsService;
import service.IComsService;

@RequiredServices({
        @RequiredService(name="coms", type= IComsService.class, binding=@Binding(scope= RequiredServiceInfo.SCOPE_PLATFORM))
})
@Agent
public class InvestorAgent
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
		System.out.println("Hello world! I'm "+agent.getComponentIdentifier().getName());
		return IFuture.DONE;
	}
}
