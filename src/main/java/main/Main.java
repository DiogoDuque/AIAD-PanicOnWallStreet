package main;

import agent.InvestorBDI;
import assets.Company;
import communication.InvestorInfo;
import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.IFuture;
import jadex.commons.future.ITuple2Future;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static final int STARTING_MONEY = 120;
    public static final int MANAGEMENT_COST_PER_SHARE = 10; // cost a manager needs to pay per share on the Management Costs phase

    public static final int N_INVESTORS = 4; // number of investors in the game
    public static final int N_MANAGERS = 2; // number of managers in the game
    public static final int N_ROUNDS = 5; // number of rounds the game will have
    public static final int SHARES_ADDED_PER_ROUND = (2*N_MANAGERS) - 1;

    public static final int INFO_REFRESH_RATE = 500; // in milis // 0.5s

    public static final int NEGOTIATION_PHASE_DURATION = 10000; // in milis // 10s
    public static final int INVESTOR_INCOME_PHASE_DURATION = 2000; // in milis // 2s
    public static final int MANAGER_INCOME_PHASE_DURATION = 2000; // in milis // 2s
    public static final int MANAGEMENT_COSTS_PHASE_DURATION = 1000; // in milis // 1s
    public static final int AUCTION_DURATION = 1500; // in milis // 1.5s

    public static final String INFO_FILENAME = "PanicOnWallStreet-roundInfo.txt";

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
            ITuple2Future<IComponentIdentifier, Map<String,Object>> tupfutM = cms.createComponent("Manager"+i, "agent.ManagerBDI.class", null); //starts component (agent)
            IComponentIdentifier cidM = tupfutM.getFirstResult();
            System.out.println("Main: started " + cidM);
        }

        for(int i=1; i<=N_INVESTORS; i++){
            ITuple2Future<IComponentIdentifier, Map<String,Object>> tupfutI = cms.createComponent("Investor"+i, "agent.InvestorBDI.class", null); //starts component (agent)
            IComponentIdentifier cidI = tupfutI.getFirstResult();
            System.out.println("Main: started " + cidI);
        }

        ITuple2Future<IComponentIdentifier, Map<String,Object>> tupfutT = cms.createComponent("Timer", "agent.TimerBDI.class", null); //starts component (agent)
        IComponentIdentifier cidT = tupfutT.getFirstResult();
        System.out.println("Main: started " + cidT);
	}

    public static ArrayList<Company> getCompanies() {
        return companies;
    }
}
