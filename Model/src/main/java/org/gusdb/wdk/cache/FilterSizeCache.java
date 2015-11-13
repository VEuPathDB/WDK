package org.gusdb.wdk.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.cache.ItemCache;
import org.gusdb.fgputil.cache.ItemFetcher;
import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.fgputil.events.Event;
import org.gusdb.fgputil.events.EventListener;
import org.gusdb.fgputil.events.Events;
import org.gusdb.wdk.events.StepRevisedEvent;
import org.gusdb.wdk.events.StepsModifiedEvent;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.user.Step;

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
  private static class FilterSizeGroup {

    // set to true if the entire size map has been populated by getFilterSizes()
    boolean allFiltersLoaded = false;

    // map from filter name to filter size (count of records when that filter is applied)
    Map<String, Integer> sizeMap = new HashMap<>();
  }

  // Fetches size of a single filter on a single step and populates a
  // FilterSizeGroup with the fetched value.  If value is already present for
  // the filter name, itemNeedsUpdating() returns false and no update will occur.
  private static class SingleSizeFetcher implements ItemFetcher<Integer, FilterSizeGroup> {

    private final String _filterToFetch;
    private final WdkModel _wdkModel;

    public SingleSizeFetcher(String filterToFetch, WdkModel wdkModel) {
      _filterToFetch = filterToFetch;
      _wdkModel = wdkModel;
    }

    @Override
    public FilterSizeGroup fetchItem(Integer id) throws UnfetchableItemException {
      FilterSizeGroup emptyGroup = new FilterSizeGroup();
      return updateItem(id, emptyGroup);
    }

    @Override
    public FilterSizeGroup updateItem(Integer id, FilterSizeGroup previousVersion)
        throws UnfetchableItemException {
      try {
        Step step = _wdkModel.getStepFactory().getStepById(id);
        AnswerValue answerValue = step.getAnswerValue(false, false);
        int size = answerValue.getFilterSize(_filterToFetch);
        previousVersion.sizeMap.put(_filterToFetch, size);
        return previousVersion;
      }
      catch (WdkUserException | WdkModelException e) {
        throw new UnfetchableItemException(e);
      }
    }

    @Override
    public boolean itemNeedsUpdating(FilterSizeGroup item) {
      // return false if size for this filter has already been retrieved
      return !item.sizeMap.containsKey(_filterToFetch);
    }
  }

  private static class AllSizesFetcher implements ItemFetcher<Integer, FilterSizeGroup> {

    private final WdkModel _wdkModel;

    public AllSizesFetcher(WdkModel wdkModel) {
      _wdkModel = wdkModel;
    }

    @Override
    public FilterSizeGroup fetchItem(Integer id) throws UnfetchableItemException {
      FilterSizeGroup emptyGroup = new FilterSizeGroup();
      return updateItem(id, emptyGroup);
    }

    @Override
    public FilterSizeGroup updateItem(Integer id, FilterSizeGroup previousVersion)
        throws UnfetchableItemException {
      try {
        Step step = _wdkModel.getStepFactory().getStepById(id);
        AnswerValue answer = step.getAnswerValue(false, false);
        Map<String, Integer> sizes = answer.getFilterSizes();
        previousVersion.sizeMap = sizes;
        previousVersion.allFiltersLoaded = true;
        return previousVersion;
      }
      catch (WdkUserException | WdkModelException e) {
        throw new UnfetchableItemException(e);
      }
    }

    @Override
    public boolean itemNeedsUpdating(FilterSizeGroup item) {
      return !item.allFiltersLoaded;
    }
  }

  private final ItemCache<Integer, FilterSizeGroup> _cache = new ItemCache<>();

  public FilterSizeCache() {
    subscribeToEvents();
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
          int stepId = ((StepRevisedEvent)event).getRevisedStep().getStepId();
          LOG.info("Notification of step revision, step ID: " + stepId);
          _cache.expireCachedItems(stepId);
        }
        else if (event instanceof StepsModifiedEvent) {
          List<Integer> stepIds = ((StepsModifiedEvent)event).getStepIds();
          LOG.info("Notification of steps revision, step IDs: " + FormatUtil.arrayToString(stepIds.toArray()));
          _cache.expireCachedItems(stepIds.toArray(new Integer[stepIds.size()]));
        }
      }
    }, StepRevisedEvent.class, StepsModifiedEvent.class);
  }

  public int getFilterSize(int stepId, String filterName, WdkModel wdkModel)
      throws WdkModelException, WdkUserException {
    try {
      return _cache.getItem(stepId, new SingleSizeFetcher(filterName, wdkModel)).sizeMap.get(filterName);
    }
    catch (UnfetchableItemException e) {
      return handleUnfetchableItem(e, Integer.class);
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Integer> getFilterSizes(int stepId, WdkModel wdkModel)
    throws WdkModelException, WdkUserException {
    try {
      return _cache.getItem(stepId, new AllSizesFetcher(wdkModel)).sizeMap;
    }
    catch (UnfetchableItemException e) {
      return handleUnfetchableItem(e, Map.class);
    }
  }

  private <T> T handleUnfetchableItem(UnfetchableItemException e, Class<T> returnClass)
      throws WdkModelException, WdkUserException {
    try {
      throw e.getCause();
    }
    catch (WdkModelException e2) { throw e2; }
    catch (WdkUserException e2) { throw e2; }
    catch (Throwable e2) { throw new WdkRuntimeException(e2); }
  }
}
