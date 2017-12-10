package agent;

import assets.AuctionManager;
import assets.Company;
import assets.GameOverManager;
import assets.Share;
import com.google.gson.Gson;
import communication.*;
import communication.message.AuctionMessage;
import communication.message.GameOverMessage;
import communication.message.IncomeMessage;
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
import java.util.Random;

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
        AUCTION,

        GAMEOVER
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
    private static long phaseStartTime =-1;

    /**
     * Communication service.
     */
    private IComsService coms;

    /**
     * Current round of the game.
     */
    private static int round;

    /**
     * Current Game Phase.
     */
    private static GamePhase gamePhase;

    @AgentCreated
    public void init() {
        round = 1;
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

                    case AUCTION:
                        AuctionMessage aMsg = new Gson().fromJson(result, AuctionMessage.class);
                        if(aMsg.getMsgType() == AuctionMessage.MessageType.BID_ON_SHARE) {
                            AuctionManager auctionManager = AuctionManager.getInstance();
                            if(auctionManager == null){
                                log("WARNING: AuctionManager not found");
                                break;
                            }
                            Proposal proposal = new Gson().fromJson(aMsg.getJsonExtra(), Proposal.class);
                            auctionManager.receivedBid(aMsg.getSenderCid(), proposal);
                            break;
                        }
                        break;

                    case GAMEOVER:
                        GameOverMessage goMsg = new Gson().fromJson(result, GameOverMessage.class);
                        if(goMsg.getMsgType().equals(GameOverMessage.MessageType.SEND_GAMEOVER_INFO)){
                            GameOverManager.getInstance().addNewPlayer(goMsg.getSenderCid(), new Gson().fromJson(goMsg.getJsonExtra(), Integer.class));
                        }
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
        if(phaseStartTime == -1) {
            log("Started Negotiation Phase");
            phaseStartTime = currentTime;
        }

        long timeAfterPhaseStart = currentTime - phaseStartTime;
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
                            for(Company c: Main.getCompanies()){
                                c.rollDice();
                            }
                            log("Rolled dices. New company results: "+Main.getCompanies());

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
                        if(round >= Main.N_ROUNDS){
                            gamePhase = GamePhase.GAMEOVER;
                            log("Started GameOver Phase");
                            coms.askGameOverInfo(myCid);
                        } else {
                            gamePhase = GamePhase.AUCTION;
                            phaseStartTime = -1;
                            timeAfterPhaseStart = -1;
                            changePhase = true;
                            log("Started AuctionManager Phase");
                        }
                    }
                    break;

                case AUCTION:
                    // the auctionManager is different from the other phases, as it does not have a timeout.
                    // Instead, it will have a "kind-of state machine",
                    // which will also determine when to pass to the next phase.
                    if(phaseStartTime == -1){ // executed only once
                        Random r = new Random();
                        ArrayList<Company> companies = Main.getCompanies();
                        ArrayList<Share> shares = new ArrayList<>();
                        for(int i = 0; i < Main.SHARES_ADDED_PER_ROUND; i++){
                            Share share = new Share(companies.get(r.nextInt(companies.size())), myCid);
                            shares.add(share);
                        }
                        AuctionManager.instantiate(myCid, coms, shares);
                        phaseStartTime = 0;

                    } else { // executed periodically
                        AuctionManager auctionManager = AuctionManager.getInstance();
                        if(auctionManager != null)
                            auctionManager.receivedTime(currentTime);
                        else { // if round finished
                            round++;
                            phaseStartTime = -1;
                            gamePhase = GamePhase.NEGOTIATION;
                        }
                    }

                    break;
            }
        } while(changePhase); // will loop if change phase
    }

    static GamePhase getGamePhase(){
        return gamePhase;
    }

    static int getRound(){
        return round;
    }

    static long getPhaseStartTime () {
        return phaseStartTime;
    }

    /**
     * A logging function. Used mainly for debugging and showing what's happening inside this specific agent.
     * @param msg message to be displayed.
     */
    private void log(String msg){
        System.out.println(agent.getComponentIdentifier().getLocalName()+": "+msg);
    }
}
