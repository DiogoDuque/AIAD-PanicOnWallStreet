package communication;

import assets.CompanyShare;
import com.google.gson.Gson;
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

    private void broadcast(String jsonMsg) {
        for(SubscriptionIntermediateFuture<String> subscriber: subscribers){
            subscriber.addIntermediateResultIfUndone(jsonMsg);
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




}
