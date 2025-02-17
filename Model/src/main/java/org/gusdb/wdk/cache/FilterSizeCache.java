package org.gusdb.wdk.cache;

import static org.gusdb.fgputil.functional.Functions.getMapFromKeys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.cache.InMemoryCache;
import org.gusdb.fgputil.cache.ValueFactory;
import org.gusdb.fgputil.cache.ValueProductionException;
import org.gusdb.fgputil.events.Event;
import org.gusdb.fgputil.events.EventListener;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.events.StepResultsModifiedEvent;
import org.gusdb.wdk.events.StepRevisedEvent;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.User;

/**
 * Cache of (legacy) filter sizes mapped by stepId.
 * 
 * @author rdoherty
 */
public class FilterSizeCache {

  private static final Logger LOG = Logger.getLogger(FilterSizeCache.class);

  // These are the items being cached in the ItemCache.  Depending on the order
  // of calls to getFilterSize() and getFilterSizes(), the sizeMap might be
  // fully or partially populated
  public static class FilterSizeGroup {

    // set to true if the entire size map has been populated by getFilterSizes()
    public boolean allFiltersLoaded = false;

    // map from filter name to filter size (count of records when that filter is applied)
    public Map<String, Integer> sizeMap = new HashMap<>();
  }

  // Fetches size of a single filter on a single step and populates a
  // FilterSizeGroup with the fetched value.  If value is already present for
  // the filter name, itemNeedsUpdating() returns false and no update will occur.
  private static class SingleSizeFetcher implements ValueFactory<Long, FilterSizeGroup> {

    private final String _filterToFetch;
    private final User _requestingUser;

    public SingleSizeFetcher(String filterToFetch, User requestingUser) {
      _filterToFetch = filterToFetch;
      _requestingUser = requestingUser;
    }

    @Override
    public FilterSizeGroup getNewValue(Long stepId) throws ValueProductionException {
      FilterSizeGroup emptyGroup = new FilterSizeGroup();
      return getUpdatedValue(stepId, emptyGroup);
    }

    @Override
    public FilterSizeGroup getUpdatedValue(Long stepId, FilterSizeGroup previousVersion)
        throws ValueProductionException {
      try {
        Step step = new StepFactory(_requestingUser).getStepByValidId(stepId, ValidationLevel.RUNNABLE);
        int size = !step.isRunnable() ? 0 :
          AnswerValueFactory
            .makeAnswer(step.getAnswerSpec().getRunnable().getLeft())
            .getResultSizeFactory()
            .getFilterDisplaySize(_filterToFetch);
        previousVersion.sizeMap.put(_filterToFetch, size);
        return previousVersion;
      }
      catch (WdkModelException e) {
        throw new ValueProductionException(e);
      }
    }

    @Override
    public boolean valueNeedsUpdating(FilterSizeGroup item) {
      // return false if size for this filter has already been retrieved
      return !item.sizeMap.containsKey(_filterToFetch);
    }
  }

  public static class AllSizesFetcher implements ValueFactory<Long, FilterSizeGroup> {

    protected final User _requestingUser;

    public AllSizesFetcher(User requestingUser) {
      _requestingUser = requestingUser;
    }

    @Override
    public FilterSizeGroup getNewValue(Long stepId) throws ValueProductionException {
      FilterSizeGroup emptyGroup = new FilterSizeGroup();
      return getUpdatedValue(stepId, emptyGroup);
    }

    @Override
    public FilterSizeGroup getUpdatedValue(Long stepId, FilterSizeGroup previousVersion)
        throws ValueProductionException {
      try {
        Step step = new StepFactory(_requestingUser).getStepByValidId(stepId, ValidationLevel.RUNNABLE);
        Map<String, Integer> sizes = step.isRunnable() ?
            // if runnable, load filters from result size factory
            AnswerValueFactory
              .makeAnswer(step.getAnswerSpec().getRunnable().getLeft())
              .getResultSizeFactory()
              .getFilterDisplaySizes() :
            // otherwise, fill map with zeroes
            getMapFromKeys(
              step.getAnswerSpec().getQuestion().get().getRecordClass().getFilterMap().keySet(),
              key -> 0);
        previousVersion.sizeMap = sizes;
        previousVersion.allFiltersLoaded = true;
        return previousVersion;
      }
      catch (WdkModelException e) {
        throw new ValueProductionException(e);
      }
    }

    @Override
    public boolean valueNeedsUpdating(FilterSizeGroup item) {
      return !item.allFiltersLoaded;
    }
  }

  private final InMemoryCache<Long, FilterSizeGroup> _cache = new InMemoryCache<>();

  public FilterSizeCache() {
    subscribeToEvents();
  }

  InMemoryCache<Long, FilterSizeGroup> getCache() {
    return _cache;
  }

  // Must subscribe to step-revised events so we know when to expire counts in the cache.
  //
  // FIXME: currently any step revise (change that impacts the result), including the
  // application of a legacy filter, will expire counts for that step.  But the application
  // of a new filter does not affect the legacy filter counts.  We should add the ability
  // to differentiate between revise events due to param or other changes (which would
  // affect filter counts) and the application of a filter (which should not expire the
  // filter size numbers in the cache.
  private void subscribeToEvents() {
    // when steps are revised, we must clear their filter sizes from the cache
    Events.subscribe(new EventListener(){
      @Override
      public void eventTriggered(Event event) throws Exception {
        if (event instanceof StepRevisedEvent) {
          long stepId = ((StepRevisedEvent)event).getRevisedStep().getStepId();
          LOG.info("Notification of step revision, step ID: " + stepId + " and question: " +
              ((StepRevisedEvent)event).getRevisedStep().getAnswerSpec().getQuestionName() );
          _cache.expireEntries(stepId);
        }
        else if (event instanceof StepResultsModifiedEvent) {
          List<Long> stepIds = ((StepResultsModifiedEvent)event).getStepIds();
          LOG.info("Notification of steps modification, step IDs: " + FormatUtil.arrayToString(stepIds.toArray()));
          _cache.expireEntries(stepIds.toArray(new Long[stepIds.size()]));
        }
      }
    }, StepRevisedEvent.class, StepResultsModifiedEvent.class);
  }

  public int getFilterSize(User requestingUser, long stepId, String filterName)
      throws WdkModelException, WdkUserException {
    LOG.debug("getFilterSize:  filterName : " + filterName +" and stepID: " + stepId);
    try {
      return _cache.getValue(stepId, new SingleSizeFetcher(filterName, requestingUser)).sizeMap.get(filterName);
    }
    catch (ValueProductionException e) {
      return handleUnfetchableItem(e, Integer.class);
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Integer> getFilterSizes(long stepId, AllSizesFetcher fetcher)
      throws WdkModelException, WdkUserException {
    try {
      return _cache.getValue(stepId, fetcher).sizeMap;
    }
    catch (ValueProductionException e) {
      return handleUnfetchableItem(e, Map.class);
    }
    
  }

  public Map<String, Integer> getFilterSizes(int stepId, User requestingUser)
      throws WdkModelException, WdkUserException {
    return getFilterSizes(stepId, new AllSizesFetcher(requestingUser));
  }

  /**
   * @param e exception wrapping underlying cause
   * @param returnClass the type we tell the compiler we are returning so the result of this method can be
   * returned by any method (and we don't have to have a separate return)
   * @return never returns anything; only throws exceptions
   * @throws WdkModelException if underlying exception is WdkModelException
   * @throws WdkUserException if underlying exception is WdkUserException
   */
  private <T> T handleUnfetchableItem(ValueProductionException e, Class<T> returnClass)
      throws WdkModelException, WdkUserException {
    try {
      throw e.getCause();
    }
    catch (WdkModelException e2) { throw e2; }
    catch (WdkUserException e2) { throw e2; }
    catch (Throwable e2) { throw new WdkRuntimeException(e2); }
  }
}
