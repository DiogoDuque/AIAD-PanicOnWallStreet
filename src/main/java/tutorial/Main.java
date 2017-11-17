package tutorial;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.IFuture;
import jadex.commons.future.ITuple2Future;

import java.util.Map;

public class Main
{
	public static void main(String[] args)
	{
		IFuture<IExternalAccess> fut = Starter.createPlatform();
		PlatformConfiguration platformConfig = PlatformConfiguration.getDefaultNoGui(); //init configs with a default
		platformConfig.setPlatformName("AIAD-PoWS"); // set platform name
		platformConfig.setDebugFutures(true); // enables stacktraces of exceptions
		IExternalAccess platform = fut.get(); //assign configs

        IFuture<IComponentManagementService> fut1 = SServiceProvider.getService(platform, IComponentManagementService.class);

        IComponentManagementService cms = fut1.get();
        ITuple2Future<IComponentIdentifier, Map<String,Object>> tupfutM1 = cms.createComponent("Manager1", "tutorial.HelloAgent.class", null); //starts component (agent)
        IComponentIdentifier cidM1 = tupfutM1.getFirstResult();
        System.out.println("Started component: " + cidM1);
	}
}
