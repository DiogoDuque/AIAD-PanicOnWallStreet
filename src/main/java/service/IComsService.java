package service;

import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.SubscriptionIntermediateFuture;

import java.util.ArrayList;

public interface IComsService {

    ArrayList<SubscriptionIntermediateFuture<String>> subscribers = new ArrayList<>();

    public ISubscriptionIntermediateFuture<String> subscribeComs();

    public void broadcast(String msg);
}
