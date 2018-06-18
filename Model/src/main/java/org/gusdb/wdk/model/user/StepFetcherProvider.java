package org.gusdb.wdk.model.user;

import org.gusdb.fgputil.cache.ValueFactory;
import org.gusdb.fgputil.cache.ValueProductionException;
import org.gusdb.wdk.model.WdkModelException;

public class StepFetcherProvider {

  private final StepFactory _stepFactory;

  public StepFetcherProvider(StepFactory stepFactory) {
    _stepFactory = stepFactory;
  }

  public ValueFactory<Long, Step> getFetcher(User user) {
    return new StepFetcher(_stepFactory, user);
  }

  public static class StepFetcher implements ValueFactory<Long, Step> {

    private static final long EXPIRATION_SECS = 20;

    private final StepFactory _stepFactory;
    private final User _user;

    public StepFetcher(StepFactory stepFactory, User user) {
      _stepFactory = stepFactory;
      _user = user;
    }

    @Override
    public Step getNewValue(Long stepId) throws ValueProductionException {
      try {
        return _stepFactory.loadStepNoCache(_user, stepId);
      }
      catch (WdkModelException e) {
        throw new ValueProductionException(e);
      }
    }
  
    @Override
    public Step getUpdatedValue(Long stepId, Step previousVersion) throws ValueProductionException {
      return getNewValue(stepId);
    }
  
    @Override
    public boolean valueNeedsUpdating(Step step) {
      long now = System.currentTimeMillis();
      // if step is too old, then refresh (trying to keep this close to a request-scope cache)
      return (step._objectCreationDate < now - (EXPIRATION_SECS * 1000));
    }
  }
}
