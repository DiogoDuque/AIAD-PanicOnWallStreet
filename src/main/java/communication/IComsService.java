package communication;

import jadex.commons.future.ISubscriptionIntermediateFuture;

public interface IComsService {

    ISubscriptionIntermediateFuture<String> subscribeComs();

    void sendShares(String sender, String shares);

    void sendInvestorInfo(String sender, String info);

    void sendProposal(String sender, String receiver, String proposal);

    void rejectProposal(String sender, String receiver, String proposal);

    void acceptProposal(String sender, String receiver, String proposal);

    void attemptCloseDeal(String sender, String receiver, String proposal);

    void rejectCloseDeal(String sender, String receiver, String proposal);

    void acceptCloseDeal(String sender, String receiver, String proposal);

    boolean askInfo(String sender);
}
