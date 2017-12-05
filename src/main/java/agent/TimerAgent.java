package agent;

import communication.ComsService;
import communication.IComsService;
import communication.NegotiationMessage;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.*;

@RequiredServices({
        @RequiredService(name="coms", type=IComsService.class, multiple=true, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM))
})
@Agent
public class TimerAgent {

    public enum GamePhase {
        NEGOTIATION(NegotiationMessage.class);

        private Class c;

        GamePhase(Class c) {
            this.c=c;
        }

        public Class getMessageClass() {
            return c;
        }
    }

    @Agent
    private IInternalAccess agent;

    @AgentFeature
    private IRequiredServicesFeature reqServ;

    private IComsService coms;

    private static GamePhase gamePhase;

    @AgentCreated
    public void init() {
        String myCid = agent.getComponentIdentifier().getName();
        this.coms = (IComsService)reqServ.getRequiredService("coms").get();
        IComsService iComs = SServiceProvider.getService(agent,IComsService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
        ISubscriptionIntermediateFuture<String> sub = iComs.subscribeComs();
        log("subscribed");
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
        log("Game is about to start. Asking for shares to be publicly displayed");
        while(!coms.askShares(myCid));
    }

    public static GamePhase getGamePhase(){
        return gamePhase;
    }

    private void log(String msg){
        System.out.println(agent.getComponentIdentifier().getLocalName()+": "+msg);
    }
}
