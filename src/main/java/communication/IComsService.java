package communication;

import assets.CompanyShare;
import jadex.commons.future.ISubscriptionIntermediateFuture;

import java.util.ArrayList;

public interface IComsService {

    ISubscriptionIntermediateFuture<String> subscribeComs();

    void sendShares(String cid, String shares);
}
