package main;

import assets.Company;
import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.IFuture;
import jadex.commons.future.ITuple2Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    public static final int STARTING_MONEY = 0;

    public static final int N_INVESTORS = 1;
    public static final int N_MANAGERS = 1;

    private static ArrayList<Company> companies;

	public static void main(String[] args)
	{
	    companies = new ArrayList<>();
	    companies.add(new Company("Weenie Tube",
                                new int[]{-20, -10, 0, 30, 40, 50, 60, 70},
                                new int[]{-7, -3, -2, 2, 3, 7}));
        companies.add(new Company("Beach Kit",
                                new int[]{-10, 0, 0, 30, 40, 40, 60, 60},
                                new int[]{-3, -2, -1, 1, 2, 3}));
        companies.add(new Company("Antisnore Pillow",
                                new int[]{0, 10, 20, 30, 30, 40, 50, 60},
                                new int[]{-2, -1, 0, 0, 1, 2}));
        companies.add(new Company("Hamster Radio",
                                new int[]{20, 20, 20, 30, 30, 30, 40, 40},
                                new int[]{-1, -1, 0, 0, 1, 1}));

		IFuture<IExternalAccess> fut = Starter.createPlatform();
		PlatformConfiguration platformConfig = PlatformConfiguration.getDefaultNoGui(); //init configs with a default
		platformConfig.setPlatformName("AIAD-PoWS"); // set platform name
		platformConfig.setDebugFutures(true); // enables stacktraces of exceptions
		IExternalAccess platform = fut.get(); //assign configs

        IFuture<IComponentManagementService> fut1 = SServiceProvider.getService(platform, IComponentManagementService.class);
        IComponentManagementService cms = fut1.get();

        for(int i=1; i<=N_MANAGERS; i++){
            ITuple2Future<IComponentIdentifier, Map<String,Object>> tupfutM = cms.createComponent("Manager"+i, "agent.ManagerAgent.class", null); //starts component (agent)
            IComponentIdentifier cidM = tupfutM.getFirstResult();
            System.out.println("Started: " + cidM);
        }

        for(int i=1; i<=N_INVESTORS; i++){
            ITuple2Future<IComponentIdentifier, Map<String,Object>> tupfutI = cms.createComponent("Investor"+i, "agent.InvestorAgent.class", null); //starts component (agent)
            IComponentIdentifier cidI = tupfutI.getFirstResult();
            System.out.println("Started: " + cidI);
        }

	}

    public static ArrayList<Company> getCompanies() {
        return companies;
    }
}
