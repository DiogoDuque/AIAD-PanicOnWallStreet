package communication;

import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.SubscriptionIntermediateFuture;

import java.util.ArrayList;

public interface IComsService {

    ISubscriptionIntermediateFuture<String> subscribeComs();

    void broadcast(String msg);
}
