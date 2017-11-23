package service;

import jadex.commons.future.IFuture;

public interface IComsService {
    public IFuture<Boolean> test(boolean b);
}
