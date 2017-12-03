package communication;

import jadex.commons.future.ISubscriptionIntermediateFuture;

public interface IComsService {

    ISubscriptionIntermediateFuture<String> subscribeComs();

    void sendShares(String cid, String shares);

    void sendProposal(String cid, String proposal);
}
