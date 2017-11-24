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

    @Override
    public ISubscriptionIntermediateFuture<String> subscribeComs() {
        SubscriptionIntermediateFuture<String> sub = new SubscriptionIntermediateFuture<>();
        subscribers.add(sub);
        return sub;
    }

    @Override
    public void broadcast(String msg) {
        for(SubscriptionIntermediateFuture<String> subscriber: subscribers){
            subscriber.addIntermediateResultIfUndone(msg.toString());
        }
    }
}
