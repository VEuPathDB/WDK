package org.gusdb.wdk.model.user;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

public class UserSession {

  private static final Logger logger = Logger.getLogger(UserSession.class);

  private final User _user;

  // keep track in session , but don't serialize:
  // currently open strategies
  private ActiveStrategyFactory _activeStrategyFactory;

  // keep track of most recent front end action
  private String _frontAction = null;
  private Long _frontStrategy = null;
  private Long _frontStep = null;

  public UserSession(User user) {
    _user = user;
    _activeStrategyFactory = new ActiveStrategyFactory(user);
  }

  public User getUser() {
    return _user;
  }

  public String getFrontAction() {
    return _frontAction;
  }

  public Long getFrontStrategy() {
    return _frontStrategy;
  }

  public Long getFrontStep() {
    return _frontStep;
  }

  public void setFrontAction(String frontAction) {
    this._frontAction = frontAction;
  }

  public void setFrontStrategy(long frontStrategy) {
    System.out.println("Setting frontStrategy.");
    this._frontStrategy = Long.valueOf(frontStrategy);
    System.out.println("Done.");
  }

  public void setFrontStep(long frontStep) {
    this._frontStep = Long.valueOf(frontStep);
  }

  public void resetFrontAction() {
    _frontAction = null;
    _frontStrategy = null;
    _frontStep = null;
  }

  public Strategy[] getActiveStrategies() {
    long[] ids = _activeStrategyFactory.getRootStrategies();
    List<Strategy> strategies = new ArrayList<Strategy>();
    for (long id : ids) {
      try {
        Strategy strategy = _user.getWdkModel().getStepFactory()
            .getStrategyById(id, ValidationLevel.RUNNABLE).orElse(null);
        if (strategy != null) {
          // only add to active if found
          strategies.add(strategy);
        }
      }
      catch (WdkModelException ex) {
        // something wrong with loading a strat, probably the strategy
        // doesn't exist anymore
        logger.warn("something wrong with loading a strat, probably " +
            "the strategy doesn't exist anymore. Please " + "investigate:\nUser #" + _user.getUserId() +
            ", strategy display Id: " + id + "\nException: ", ex);
      }
    }
    Strategy[] array = new Strategy[strategies.size()];
    strategies.toArray(array);
    return array;
  }

  public void addActiveStrategy(String strategyKey) throws WdkModelException, WdkUserException {
    _activeStrategyFactory.openActiveStrategy(strategyKey);
    int pos = strategyKey.indexOf('_');
    if (pos >= 0)
      strategyKey = strategyKey.substring(0, pos);
    int strategyId = Integer.parseInt(strategyKey);
    _user.getWdkModel().getStepFactory().updateStrategyViewTime(strategyId);
  }

  public void removeActiveStrategy(String strategyKey) {
    _activeStrategyFactory.closeActiveStrategy(strategyKey);
  }

  public void replaceActiveStrategy(long oldStrategyId, long newStrategyId, Map<Long, Long> stepIdsMap)
      throws WdkModelException, WdkUserException {
    _activeStrategyFactory.replaceStrategy(oldStrategyId, newStrategyId, stepIdsMap);
  }

  public void setViewResults(String strategyKey, long stepId, int pagerOffset) {
    this._activeStrategyFactory.setViewStrategyKey(strategyKey);
    this._activeStrategyFactory.setViewStepId(stepId);
    this._activeStrategyFactory.setViewPagerOffset(pagerOffset);
  }

  public void resetViewResults() {
    this._activeStrategyFactory.setViewStrategyKey(null);
    this._activeStrategyFactory.setViewStepId(null);
    this._activeStrategyFactory.setViewPagerOffset(null);
  }

  public String getViewStrategyKey() {
    return this._activeStrategyFactory.getViewStrategyKey();
  }

  public long getViewStepId() {
    return this._activeStrategyFactory.getViewStepId();
  }

  public Integer getViewPagerOffset() {
    return this._activeStrategyFactory.getViewPagerOffset();
  }

  public int getStrategyOrder(String strategyKey) {
    int order = _activeStrategyFactory.getOrder(strategyKey);
    logger.debug("strat " + strategyKey + " order: " + order);
    return order;
  }

  public long[] getActiveStrategyIds() {
    return _activeStrategyFactory.getRootStrategies();
  }

  /**
   * this method is only called by UserFactory during the login process, it merges the existing history of the
   * current guest user into the logged-in user.
   * 
   * @param guestUser
   * @throws WdkModelException
   * @throws  
   */
  public void mergeUser(User guestUser) throws WdkModelException {

    logger.debug("Merging user #" + guestUser.getUserId() + " into user #" + _user.getUserId() + "...");

    // first of all we import all the strategies
    StepFactory stepFactory = guestUser.getWdkModel().getStepFactory();
    Set<Long> importedSteps = new LinkedHashSet<>();
    Map<Long, Long> strategiesMap = new LinkedHashMap<>();
    Map<Long, Long> stepsMap = new LinkedHashMap<>();
    for (Strategy strategy : stepFactory.getStrategies(guestUser.getUserId(), ValidationLevel.NONE).values()) {
      // the root step is considered as imported
      Step rootStep = strategy.getRootStep();

      // import the strategy
      Strategy newStrategy = stepFactory.copyStrategy(_user, strategy, stepsMap);

      importedSteps.add(rootStep.getStepId());
      strategiesMap.put(strategy.getStrategyId(), newStrategy.getStrategyId());
    }

    // get the guest session to copy relevant data
    UserSession guestSession = guestUser.getSession();

    // the current implementation can only keep the root level of the
    // imported strategies open;
    try {
      long[] oldActiveStrategies = guestSession.getActiveStrategyFactory().getRootStrategies();
      for (long oldStrategyId : oldActiveStrategies) {
        long newStrategyId = strategiesMap.get(oldStrategyId);
        _activeStrategyFactory.openActiveStrategy(Long.toString(newStrategyId));
      }
    }
    catch (WdkUserException e) { // eventually this will not be needed
      throw new WdkModelException(e);
    }

    // no need to import steps that don't belong to any strategies, since they won't be referenced in any way.

    // if a front action is specified, copy it over and update ids
    if (guestUser.getSession().getFrontAction() != null) {
      setFrontAction(guestSession.getFrontAction());
      if (strategiesMap.containsKey(guestSession.getFrontStrategy())) {
        setFrontStrategy(strategiesMap.get(guestSession.getFrontStrategy()));
      }
      if (stepsMap.containsKey(guestSession.getFrontStep())) {
        setFrontStep(stepsMap.get(guestSession.getFrontStep()));
      }
    }
  }

  public ActiveStrategyFactory getActiveStrategyFactory() {
    return _activeStrategyFactory;
  }
}
