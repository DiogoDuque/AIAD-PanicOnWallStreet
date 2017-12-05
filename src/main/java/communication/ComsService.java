package communication;

import agent.TimerAgent;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.SubscriptionIntermediateFuture;
import main.Main;

import java.util.ArrayList;

@Service
public class ComsService implements IComsService {

    @ServiceComponent
    private IInternalAccess agent;

    private ArrayList<SubscriptionIntermediateFuture<String>> subscribers = new ArrayList<>();

    private void broadcast(String msg) {
        for(SubscriptionIntermediateFuture<String> subscriber: subscribers){
            subscriber.addIntermediateResultIfUndone(msg);
        }
    }

    @Override
    public ISubscriptionIntermediateFuture<String> subscribeComs() {
        SubscriptionIntermediateFuture<String> sub = new SubscriptionIntermediateFuture<>();
        subscribers.add(sub);
        System.out.println(subscribers.size());
        return sub;
    }

    @Override
    public void sendShares(String sender, String shares) {
        broadcast(new NegotiationMessage(sender, NegotiationMessage.NegotiationMessageType.MANAGER_SHARES, shares).toJsonStr());
    }

    @Override
    public void sendProposal(String sender, String receiver, String proposal) {
        broadcast(new NegotiationMessage(sender, receiver, NegotiationMessage.NegotiationMessageType.NEW_PROPOSAL, proposal).toJsonStr());
    }

    @Override
    public void rejectProposal(String sender, String receiver, String proposal) {
        broadcast(new NegotiationMessage(sender, receiver, NegotiationMessage.NegotiationMessageType.PROPOSAL_REJECTED, proposal).toJsonStr());
    }

    @Override
    public void acceptProposal(String sender, String receiver, String proposal) {
        broadcast(new NegotiationMessage(sender, receiver, NegotiationMessage.NegotiationMessageType.PROPOSAL_ACCEPTED, proposal).toJsonStr());
    }

    @Override
    public void attemptCloseDeal(String sender, String receiver, String proposal) {
        broadcast(new NegotiationMessage(sender, receiver, NegotiationMessage.NegotiationMessageType.CLOSE_DEAL, proposal).toJsonStr());
    }

    @Override
    public void rejectCloseDeal(String sender, String receiver, String proposal) {
        broadcast(new NegotiationMessage(sender, receiver, NegotiationMessage.NegotiationMessageType.CLOSE_DEAL_REJECT, proposal).toJsonStr());
    }

    @Override
    public void acceptCloseDeal(String sender, String receiver, String proposal) {
        broadcast(new NegotiationMessage(sender, receiver, NegotiationMessage.NegotiationMessageType.CLOSE_DEAL_ACCEPT, proposal).toJsonStr());
    }

    @Override
    public boolean askShares(String sender){
        if(subscribers.size() < Main.N_INVESTORS + Main.N_MANAGERS + 1)
            return false;
        broadcast(new NegotiationMessage(sender, NegotiationMessage.NegotiationMessageType.ASK_SHARES).toJsonStr());
        return true;
    }
}
