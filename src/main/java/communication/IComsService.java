package communication;

import jadex.commons.future.ISubscriptionIntermediateFuture;

public interface IComsService {

    /**
     * Gets a subscription to this communications channel.
     * @return subscription. A listener can be added to it.
     */
    ISubscriptionIntermediateFuture<String> subscribeComs();

    /**
     * Asks every investor and manager to broadcast their current game state. Managers will send their shares' state, and investors will send their current money and their deals' state.
     * @param sender sender's cid.
     */
    void askInfo(String sender);

    /**
     * Used by managers. Sends a copy of the manager's shares.
     * @param sender sender's cid.
     * @param shares JSON string with a Share[].
     * @see assets.Share
     */
    void sendShares(String sender, String shares);

    /**
     * Used by investors. Sends a copy of their current state in the game.
     * @param sender sender's cid.
     * @param info JSON string with a InvestorInfo.
     * @see InvestorInfo
     */
    void sendInvestorInfo(String sender, String info);

    /**
     * Used by investors. Sends a proposal, regarding a specific share, to a manager.
     * @param sender sender's cid.
     * @param receiver receiver's cid.
     * @param proposal JSON string with a Proposal.
     * @see Proposal
     */
    void sendProposal(String sender, String receiver, String proposal);

    /**
     * Sends a rejection to a previously accorded proposal, regarding a specific share.
     * @param sender sender's cid.
     * @param receiver receiver's cid.
     * @param proposal JSON string with a Proposal.
     * @see Proposal
     */
    void rejectProposal(String sender, String receiver, String proposal);

    /**
     * Used by managers. Accepts a proposal, regarding a specific share, made by an investor.
     * @param sender sender's cid.
     * @param receiver receiver's cid.
     * @param proposal JSON string with a Proposal.
     * @see Proposal
     */
    void acceptProposal(String sender, String receiver, String proposal);

    /**
     * Used by investors. Sends an intention to finalize a proposal, regarding a specific share.
     * @param sender sender's cid.
     * @param receiver receiver's cid.
     * @param proposal JSON string with a Proposal.
     * @see Proposal
     */
    void attemptCloseDeal(String sender, String receiver, String proposal);

    /**
     * Used by managers. Accepts the intention to finalize a proposal, regarding a specific share.
     * @param sender sender's cid.
     * @param receiver receiver's cid.
     * @param proposal JSON string with a Proposal.
     * @see Proposal
     */
    void acceptCloseDeal(String sender, String receiver, String proposal);

    /**
     * Used by managers. Rejects the intention to finalize a proposal, regarding a specific share.
     * @param sender sender's cid.
     * @param receiver receiver's cid.
     * @param proposal JSON string with a Proposal.
     * @see Proposal
     */
    void rejectCloseDeal(String sender, String receiver, String proposal);

    /**
     * Used by timer. It is sent at the beginning of the Investor Income Phase, asking for every Investor to send their bought shares so as to calculate its income.
     * @param sender sender's cid.
     */
    void askInvestorForIncomeCalculationInfo(String sender, String companies);

    /**
     * Used by investors. Contains every shares bought by an investor.
     * @param sender sender's cid.
     * @param shares sender's shares.
     */
    void sendInfoForInvestorIncomeCalculation(String sender, String shares);

    /**
     * Used by timer. Contains the income for a specific investor.
     * @param sender sender's cid.
     * @param receiver receiver's cid.
     * @param money amount of money resulting from the income.
     */
    void sendInvestorIncomeCalculationResult(String sender, String receiver, String money);
}
