package communication;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.SubscriptionIntermediateFuture;

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
        return sub;
    }

    @Override
    public void sendShares(String cid, String shares) {
        broadcast(new NegotiationMessage(cid, NegotiationMessage.NegotiationMessageType.MANAGER_SHARES, shares).toJsonStr());
    }

    @Override
    public void sendProposal(String cid, String proposal) {
        broadcast(new NegotiationMessage(cid, NegotiationMessage.NegotiationMessageType.NEW_PROPOSAL, proposal).toJsonStr());
    }


}
