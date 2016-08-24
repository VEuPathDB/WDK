package org.gusdb.wdk.model.user;

import org.gusdb.fgputil.cache.ItemFetcher;
import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.wdk.model.WdkModelException;

public class StepFetcherProvider {

  private final StepFactory _stepFactory;

  public StepFetcherProvider(StepFactory stepFactory) {
    _stepFactory = stepFactory;
  }

  public ItemFetcher<Integer, Step> getFetcher(User user) {
    return new StepFetcher(_stepFactory, user);
  }

  public static class StepFetcher implements ItemFetcher<Integer, Step> {

    private static final long EXPIRATION_SECS = 20;

    private final StepFactory _stepFactory;
    private final User _user;

    public StepFetcher(StepFactory stepFactory, User user) {
      _stepFactory = stepFactory;
      _user = user;
    }
  
    @Override
    public Step fetchItem(Integer stepId) throws UnfetchableItemException {
      try {
        return _stepFactory.loadStepNoCache(_user, stepId);
      }
      catch (WdkModelException e) {
        throw new UnfetchableItemException(e);
      }
    }
  
    @Override
    public Step updateItem(Integer stepId, Step previousVersion) throws UnfetchableItemException {
      return fetchItem(stepId);
    }
  
    @Override
    public boolean itemNeedsUpdating(Step step) {
      long now = System.currentTimeMillis();
      // if step is too old, then refresh (trying to keep this close to a request-scope cache)
      return (step.objectCreationDate < now - (EXPIRATION_SECS * 1000));
    }
  }
}
