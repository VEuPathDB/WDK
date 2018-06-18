package org.gusdb.wdk.cache;

import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.cache.InMemoryCache;
import org.gusdb.fgputil.events.Event;
import org.gusdb.fgputil.events.EventListener;
import org.gusdb.fgputil.events.Events;
import org.gusdb.wdk.events.StepRevisedEvent;
import org.gusdb.wdk.events.StepResultsModifiedEvent;
import org.gusdb.wdk.model.user.Step;

public class StepCache extends InMemoryCache<Long, Step> {

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
          long stepId = ((StepRevisedEvent)event).getRevisedStep().getStepId();
          LOG.info("Notification of step revision, step ID: " + stepId + " and question: " + ((StepRevisedEvent)event).getRevisedStep().getQuestionName() );
          expireEntries(stepId);
        }
        else if (event instanceof StepResultsModifiedEvent) {
          List<Long> stepIds = ((StepResultsModifiedEvent)event).getStepIds();
          LOG.info("Notification of steps modification, step IDs: " + FormatUtil.arrayToString(stepIds.toArray()));
          expireEntries(stepIds.toArray(new Long[stepIds.size()]));
        }
      }
    }, StepRevisedEvent.class, StepResultsModifiedEvent.class);
  }
}
