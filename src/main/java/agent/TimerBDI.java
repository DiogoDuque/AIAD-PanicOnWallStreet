package agent;

import communication.IComsService;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.*;
import main.Main;

@RequiredServices({
        @RequiredService(name="coms", type=IComsService.class, multiple=true, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM))
})
@Agent
public class TimerBDI {

    public enum GamePhase {
        NEGOTIATION,
        INVESTOR_INCOME,
        MANAGER_INCOME,
        MANAGEMENT_COST_PAYMENT,
        COMPANY_AUCTION
    }

    @Agent
    private IInternalAccess agent;

    @AgentFeature
    private IRequiredServicesFeature reqServ;

    @Belief(updaterate= Main.INFO_REFRESH_RATE)
    protected long currentTime = System.currentTimeMillis();

    private long phaseStartTime =-1;

    private IComsService coms;

    private static GamePhase gamePhase;

    @AgentCreated
    public void init() {
        String myCid = agent.getComponentIdentifier().getName();
        this.coms = (IComsService)reqServ.getRequiredService("coms").get();
        IComsService iComs = SServiceProvider.getService(agent,IComsService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
        ISubscriptionIntermediateFuture<String> sub = iComs.subscribeComs();
        sub.addIntermediateResultListener(new IntermediateDefaultResultListener<String>() {
            @Override
            public void intermediateResultAvailable(String result) {
            }
        });
    }

    @AgentBody
    public void body() {
        String myCid = agent.getComponentIdentifier().getName();
        gamePhase = GamePhase.NEGOTIATION;
        log("Game is about to start");
    }

    @Plan(trigger = @Trigger(factchangeds ="currentTime"))
    protected void timed(){
        if(phaseStartTime ==-1)
            phaseStartTime =currentTime;

        long timeAfterPhaseStart = currentTime- phaseStartTime;

        switch (gamePhase){
            case NEGOTIATION:
                if(timeAfterPhaseStart < Main.NEGOTIATION_PHASE_DURATION)
                    coms.askInfo(agent.getComponentIdentifier().getName());
                else gamePhase = GamePhase.INVESTOR_INCOME;
                break;

            case INVESTOR_INCOME:
                break;

            case MANAGER_INCOME:
                break;

            case MANAGEMENT_COST_PAYMENT:
                break;

            case COMPANY_AUCTION:
                break;
        }
    }

    static GamePhase getGamePhase(){
        return gamePhase;
    }

    private void log(String msg){
        System.out.println(agent.getComponentIdentifier().getLocalName()+": "+msg);
    }
}
