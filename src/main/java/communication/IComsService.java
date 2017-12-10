package communication;

import jadex.commons.future.ISubscriptionIntermediateFuture;

public interface IComsService {

    // NEGOTIATION PHASE

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

    // INVESTOR INCOME PHASE

    /**
     * Used by timer. It is sent at the beginning of the Investor Income Phase, asking for every Investor to send their bought shares so as to calculate its income.
     * @param sender sender's cid.
     * @param companies updated companies.
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

    // MANAGER INCOME PHASE

    /**
     * Used by timer. It is sent at the beginning of the Manager Income Phase, asking for every Manager to talk with the Investors to receive their income.
     * @param sender sender's cid.
     * @param companies updated companies.
     */
    void askManagerForManagerIncomeCalculation(String sender, String companies);

    /**
     * Used by managers. Requests money from investors.
     * @param sender sender's cid.
     * @param receiver receiver's cid.
     * @param share sender's share.
     */
    void askInvestorForManagerIncome(String sender, String receiver, String share);

    /**
     * Used by investors. Contains the income for a manager, regarding a specific share.
     * @param sender sender's cid.
     * @param receiver receiver's cid.
     * @param money amount of money resulting from the income.
     */
    void sendManagerIncome(String sender, String receiver, String money);

    // MANAGEMENT COSTS PHASE

    /**
     * Sent by timer to managers, indicating they should pay the costs of owning shares.
     */
    void payManagementCosts(String sender);

    // AUCTION PHASE

    /**
     * Sent by timer. Announces a share to auction.
     * @param sender sender's cid.
     * @param share share being auctioned.
     */
    void auctionShare(String sender, String share);

    /**
     * Sent by managers. Represents an intention to buy a share for a certain price.
     * @param sender sender's cid.
     * @param proposal proposal for share being auctioned.
     */
    void bidOnShare(String sender, String proposal);

    /**
     * Sent by timer. Notifies the manager that his bid was rejected.
     * @param sender sender's cid.
     * @param receiver receiver's cid.
     * @param share rejected share.
     */
    void rejectBid(String sender, String receiver, String share);

    /**
     * Sent by timer. States that share was sold to a specific manager.
     * @param sender sender's cid.
     * @param receiver share's owner cid.
     * @param share share that was bought.
     */
    void shareSold(String sender, String receiver, String share);

    // GAMEOVER PHASE

    /**
     * Asks information to calculate winners.
     * @param sender sender's cid.
     */
    void askGameOverInfo(String sender);

    /**
     * Sends information relevant to calculate winners of the game.
     * @param sender sender's cid.
     * @param money amount of money the sender has made.
     */
    void sendGameOverInfo(String sender, String money);
}
