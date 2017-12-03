package communication;

import jadex.commons.future.ISubscriptionIntermediateFuture;

public interface IComsService {

    ISubscriptionIntermediateFuture<String> subscribeComs();

    void sendShares(String sender, String shares);

    void sendProposal(String sender, String receiver, String proposal);

    void rejectProposal(String sender, String receiver, String proposal);

    void acceptProposal(String sender, String receiver, String proposal);


}
