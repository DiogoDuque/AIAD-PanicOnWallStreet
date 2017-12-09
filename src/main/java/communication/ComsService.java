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

    /**
     * Contains every subscription made to this service.
     */
    private ArrayList<SubscriptionIntermediateFuture<String>> subscribers = new ArrayList<>();

    /**
     * Broadcasts a message to every subscriber of this service.
     * @param msg message to be broadcasted.
     */
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
    public void askInfo(String sender){
        broadcast(new NegotiationMessage(sender, NegotiationMessage.NegotiationMessageType.ASK_INFO).toJsonStr());
    }

    @Override
    public void sendShares(String sender, String shares) {
        broadcast(new NegotiationMessage(sender, NegotiationMessage.NegotiationMessageType.MANAGER_SHARES, shares).toJsonStr());
    }

    @Override
    public void sendInvestorInfo(String sender, String info){
        broadcast(new NegotiationMessage(sender, NegotiationMessage.NegotiationMessageType.INVESTOR_INFO, info).toJsonStr());
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
    public void askInvestorForIncomeCalculationInfo(String sender, String companies) {
        broadcast(new IncomeMessage(sender, IncomeMessage.MessageType.ASK_INVESTOR_INFO, companies).toJsonStr());
    }

    @Override
    public void sendInfoForInvestorIncomeCalculation(String sender, String shares) {
        broadcast(new IncomeMessage(sender, IncomeMessage.MessageType.INVESTOR_INFO, shares).toJsonStr());
    }

    @Override
    public void sendInvestorIncomeCalculationResult(String sender, String receiver, String money) {
        broadcast(new IncomeMessage(sender, receiver, IncomeMessage.MessageType.INVESTOR_RESULT, money).toJsonStr());
    }

    @Override
    public void askManagerForManagerIncomeCalculation(String sender, String companies) {
        broadcast(new IncomeMessage(sender, IncomeMessage.MessageType.ASK_MANAGER_INFO, companies).toJsonStr());
    }

    @Override
    public void askInvestorForManagerIncome(String sender, String receiver, String share) {
        broadcast(new IncomeMessage(sender, receiver, IncomeMessage.MessageType.ASK_INVESTOR_FOR_MANAGER_INCOME, share).toJsonStr());
    }

    @Override
    public void sendManagerIncome(String sender, String receiver, String money) {
        broadcast(new IncomeMessage(sender, receiver, IncomeMessage.MessageType.MANAGER_INCOME_RESULT, money).toJsonStr());
    }

    @Override
    public void payManagementCosts(String sender){
        broadcast(new IncomeMessage(sender, IncomeMessage.MessageType.ASK_MANAGEMENT_COSTS_PAYMENT).toJsonStr());
    }

    @Override
    public void auctionShare(String sender, String share) {
        broadcast(new AuctionMessage(sender, AuctionMessage.MessageType.SHARE_AUCTION, share).toJsonStr());
    }

    @Override
    public void bidOnShare(String sender, String proposal) {
        broadcast(new AuctionMessage(sender, AuctionMessage.MessageType.BID_ON_SHARE, proposal).toJsonStr());
    }

    @Override
    public void shareSold(String sender, String receiver, String share) {
        broadcast(new AuctionMessage(sender, receiver, AuctionMessage.MessageType.SHARE_SOLD, share).toJsonStr());
    }
}
