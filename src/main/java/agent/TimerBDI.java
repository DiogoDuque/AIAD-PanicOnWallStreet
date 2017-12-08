package agent;

import assets.Company;
import assets.Share;
import com.google.gson.Gson;
import communication.IComsService;
import communication.IncomeMessage;
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

import java.util.ArrayList;
import java.util.Arrays;

@RequiredServices({
        @RequiredService(name="coms", type=IComsService.class, multiple=true, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM))
})
@Agent
public class TimerBDI {

    /**
     * All the possible game phases.
     */
    public enum GamePhase {
        NEGOTIATION,
        INVESTOR_INCOME,
        MANAGER_INCOME,
        MANAGEMENT_COST_PAYMENT,
        AUCTION
    }

    @Agent
    private IInternalAccess agent;

    @AgentFeature
    private IRequiredServicesFeature reqServ;

    /**
     * Keeps track of the current system time.
     */
    @Belief(updaterate= Main.INFO_REFRESH_RATE)
    protected long currentTime = System.currentTimeMillis();

    /**
     * Will always contain the system time at which the current Game Phase started.
     */
    private long phaseStartTime =-1;

    /**
     * Communication service.
     */
    private IComsService coms;

    /**
     * Current Game Phase.
     */
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
                switch (gamePhase){
                    case INVESTOR_INCOME:
                        IncomeMessage invIncMsg = new Gson().fromJson(result, IncomeMessage.class); //parse message

                        // if message was sent from me or if it's not INVESTOR_INFO, ignore
                        if(invIncMsg.getSenderCid().equals(myCid) || !invIncMsg.getMsgType().equals(IncomeMessage.MessageType.INVESTOR_INFO))
                            break;

                        // calculate income and send it to the investor
                        Share[] invSharesArr = new Gson().fromJson(invIncMsg.getJsonExtra(), Share[].class);
                        ArrayList<Share> invShares = new ArrayList<>(Arrays.asList(invSharesArr));
                        int invIncome = 0;
                        for(Share s: invShares){
                            invIncome += s.getCurrentValue();
                        }
                        coms.sendInvestorIncomeCalculationResult(myCid, invIncMsg.getSenderCid(), new Gson().toJson(new Integer(invIncome)));
                        break;
                }
            }
        });
    }

    @AgentBody
    public void body() {
        String myCid = agent.getComponentIdentifier().getName();
        gamePhase = GamePhase.NEGOTIATION;
        log("Game is about to start");
    }

    /**
     * Called periodically to keep checking when to change game phase. Meanwhile, it might do some periodic calls, depending on the current game phase.
     */
    @Plan(trigger = @Trigger(factchangeds ="currentTime"))
    protected void timedCall(){
        if(phaseStartTime ==-1)
            phaseStartTime =currentTime;

        long timeAfterPhaseStart = currentTime- phaseStartTime;
        String myCid = agent.getComponentIdentifier().getName();

        boolean changePhase = false;
        do {
            if(changePhase)
            changePhase = false;
            switch (gamePhase) {
                case NEGOTIATION:
                    if (timeAfterPhaseStart < Main.NEGOTIATION_PHASE_DURATION)
                        coms.askInfo(myCid);
                    else { //change phase
                        gamePhase = GamePhase.INVESTOR_INCOME;
                        phaseStartTime = -1;
                        timeAfterPhaseStart = -1;
                        changePhase = true;
                    }
                    break;

                case INVESTOR_INCOME:
                    if (timeAfterPhaseStart < Main.INVESTOR_INCOME_PHASE_DURATION) {
                        if(phaseStartTime == -1){ //executed only once
                            log("Started Investor Income Phase");

                            //roll dices
                            log(Main.getCompanies()+"");
                            for(Company c: Main.getCompanies()){
                                c.rollDice();
                            }
                            log(Main.getCompanies()+"");

                            // now send requests
                            phaseStartTime = currentTime;
                            coms.askInvestorForIncomeCalculationInfo(myCid, new Gson().toJson(Main.getCompanies().toArray(new Company[Main.getCompanies().size()])));
                        }
                    } else {
                        gamePhase = GamePhase.MANAGER_INCOME;
                        phaseStartTime = -1;
                        timeAfterPhaseStart = -1;
                        changePhase = true;
                    }
                    break;

                case MANAGER_INCOME:
                    if (timeAfterPhaseStart < Main.MANAGER_INCOME_PHASE_DURATION) {
                        if(phaseStartTime == -1){ //executed only once
                            log("Started Manager Income Phase");

                            // now send requests
                            phaseStartTime = currentTime;
                            coms.askManagerForManagerIncomeCalculation(myCid, new Gson().toJson(Main.getCompanies().toArray(new Company[Main.getCompanies().size()])));
                        }
                    } else {
                        gamePhase = GamePhase.MANAGEMENT_COST_PAYMENT;
                        phaseStartTime = -1;
                        timeAfterPhaseStart = -1;
                        changePhase = true;
                    }
                    break;

                case MANAGEMENT_COST_PAYMENT:
                    if (timeAfterPhaseStart < Main.MANAGEMENT_COSTS_PHASE_DURATION) {
                        if(phaseStartTime == -1){ //executed only once
                            log("Started Manager Costs Phase");

                            // now send requests
                            phaseStartTime = currentTime;
                            coms.askManagerForManagerIncomeCalculation(myCid, new Gson().toJson(Main.getCompanies().toArray(new Company[Main.getCompanies().size()])));
                        }
                    } else {
                        gamePhase = GamePhase.AUCTION;
                        phaseStartTime = -1;
                        timeAfterPhaseStart = -1;
                        changePhase = true;
                        log("Started Auction Phase");
                    }
                    break;

                case AUCTION:
                    // todo start auction. the auction does not have a timeout. instead, it will have a state machine, which will also determine when to pass to the next phase
                    break;
            }
        } while(changePhase); // will loop if change phase
    }

    static GamePhase getGamePhase(){
        return gamePhase;
    }

    /**
     * A logging function. Used mainly for debugging and showing what's happening inside this specific agent.
     * @param msg message to be displayed.
     */
    private void log(String msg){
        System.out.println(agent.getComponentIdentifier().getLocalName()+": "+msg);
    }
}
