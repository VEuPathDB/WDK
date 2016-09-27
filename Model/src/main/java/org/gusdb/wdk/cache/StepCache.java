package org.gusdb.wdk.cache;

import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.cache.ItemCache;
import org.gusdb.fgputil.events.Event;
import org.gusdb.fgputil.events.EventListener;
import org.gusdb.fgputil.events.Events;
import org.gusdb.wdk.events.StepRevisedEvent;
import org.gusdb.wdk.events.StepResultsModifiedEvent;
import org.gusdb.wdk.model.user.Step;

public class StepCache extends ItemCache<Integer, Step> {

  private static final Logger LOG = Logger.getLogger(StepCache.class);

  public StepCache() {
    subscribeToEvents();
  }

  private void subscribeToEvents() {
    // when steps are revised, we must clear their filter sizes from the cache
    Events.subscribe(new EventListener(){
      @Override
      public void eventTriggered(Event event) throws Exception {
        if (event instanceof StepRevisedEvent) {
          int stepId = ((StepRevisedEvent)event).getRevisedStep().getStepId();
          LOG.info("Notification of step revision, step ID: " + stepId + " and question: " + ((StepRevisedEvent)event).getRevisedStep().getQuestionName() );
          expireCachedItems(stepId);
        }
        else if (event instanceof StepResultsModifiedEvent) {
          List<Integer> stepIds = ((StepResultsModifiedEvent)event).getStepIds();
          LOG.info("Notification of steps modification, step IDs: " + FormatUtil.arrayToString(stepIds.toArray()));
          expireCachedItems(stepIds.toArray(new Integer[stepIds.size()]));
        }
      }
    }, StepRevisedEvent.class, StepResultsModifiedEvent.class);
  }
}
