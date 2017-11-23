package service;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;
import jadex.bridge.service.annotation.ServiceStart;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

@Service
public class ComsService implements IComsService {

    @ServiceComponent
    private IInternalAccess agent;

    @Override
    public IFuture<Boolean> test(boolean b) {
        System.out.println(agent.getComponentIdentifier().getLocalName());
        return new Future<>(!b);
    }

}
