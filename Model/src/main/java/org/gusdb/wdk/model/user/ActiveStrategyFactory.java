package org.gusdb.wdk.model.user;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * @author xingao
 * 
 */
class ActiveStrategyFactory {

    private static final Logger logger = Logger.getLogger(ActiveStrategyFactory.class);

    /**
     * newly opened strategies is always at the end, which means it has the
     * largest order number, and will be displayed first.
     */
    private ActiveStrategy _root;
    private User _user;

    private String _viewStrategyKey = null;
    private Long _viewStepId = null;
    private Integer _viewPagerOffset = null;

    ActiveStrategyFactory(User user) {
        _user = user;
        _root = new ActiveStrategy(null);
    }

    /**
     * The method returns an array of top strategies; nested strategies are
     * computed later.
     * 
     * @return
     */
    long[] getRootStrategies() {
        long[] ids = new long[_root.children.size()];
        int i = 0;
        for (ActiveStrategy strategy : _root.children.values()) {
            ids[i++] = strategy.strategyId;
        }
        logger.debug("====== ids =====" + Arrays.toString(ids));
        return ids;
    }

    void openActiveStrategy(String strategyKey) throws WdkModelException, WdkUserException {
        logger.debug("Opening strategy: " + strategyKey);
        if (getStrategy(strategyKey) != null) return;

        String parentKey = getParentKey(strategyKey);
        ActiveStrategy parent = getStrategy(parentKey);
        if (!parent.children.containsKey(strategyKey)) {
            ActiveStrategy strategy = new ActiveStrategy(strategyKey);
            strategy.parent = parent;
            parent.children.put(strategyKey, strategy);
        }
    }

    void closeActiveStrategy(String strategyKey) {
        logger.debug("Closing strategy: " + strategyKey);
        ActiveStrategy strategy = getStrategy(strategyKey);
        if (strategy == null) return;
        strategy = strategy.parent.children.remove(strategyKey);
        logger.debug("strategy " + strategy.strategyKey + " closed.");
    }

    /**
     * @param strategyKeys
     *            the strategies in the array should all share the same parents;
     *            that is, they should siblings.
     */
    void orderActiveStrategies(String[] strategyKeys)
            throws WdkUserException {
        if (strategyKeys.length == 0) return;
        ActiveStrategy strategy = getStrategy(strategyKeys[0]);
        if (strategy == null || strategy.strategyKey == null)
            throw new WdkUserException("The strategy '" + strategyKeys[0]
                    + "' is not opened!");
        LinkedHashMap<String, ActiveStrategy> map = new LinkedHashMap<String, ActiveStrategy>();
        map.put(strategy.strategyKey, strategy);
        for (int i = 1; i < strategyKeys.length; i++) {
            ActiveStrategy sibling = getStrategy(strategyKeys[i]);
            if (sibling == null)
                throw new WdkUserException("The strategy '" + strategyKeys[i]
                        + "' is not opened!");
            if (!strategy.parent.equals(sibling.parent))
                throw new WdkUserException("the two strategies '"
                        + strategyKeys[0] + "' and '" + strategyKeys[i]
                        + "' are not under that same parent.");
            map.put(sibling.strategyKey, sibling);
        }
        strategy.parent.children.clear();
        strategy.parent.children.putAll(map);
    }

    void replaceStrategy(long oldId, long newId,
            Map<Long, Long> stepMap) throws WdkModelException, WdkUserException {
        ActiveStrategy oldStrategy = _root.children.get(Long.toString(oldId));
        // if the old strategy is not opened, do nothing.
        if (oldStrategy == null) return;

        ActiveStrategy newStrategy = new ActiveStrategy(Long.toString(newId));
        newStrategy.parent = _root;
        if (stepMap == null) stepMap = new LinkedHashMap<>();
        Strategy strategy = getStrategy(newId);
        replaceStrategy(strategy, oldStrategy, newStrategy, stepMap);

        LinkedHashMap<String, ActiveStrategy> children = new LinkedHashMap<String, ActiveStrategy>();
        for (String strategyKey : _root.children.keySet()) {
            // use new strategy to replace the old one at the same place
            if (oldStrategy.strategyKey.equals(strategyKey)) {
                children.put(newStrategy.strategyKey, newStrategy);
            } else {
                children.put(strategyKey, _root.children.get(strategyKey));
            }
        }
        _root.children = children;
    }

    int getOrder(String strategyKey) {
        ActiveStrategy strategy = getStrategy(strategyKey);
        if (strategy == null || strategy.strategyKey == null) return 0;
        int order = 1;
        logger.debug("current: " + strategyKey + ", parent: "
                + strategy.parent.strategyKey + ", children: "
                + strategy.parent.children.size());
        for (ActiveStrategy sibling : strategy.parent.children.values()) {
            if (strategy.equals(sibling)) return order;
            order++;
        }
        return 0;
    }

    void clear() {
        _root.children.clear();
    }

    private String getParentKey(String strategyKey) throws WdkModelException, WdkUserException {
        int pos = strategyKey.indexOf('_');
        if (pos < 0) return null;
        int strategyId = Integer.parseInt(strategyKey.substring(0, pos));
        int stepId = Integer.parseInt(strategyKey.substring(pos + 1));
        Strategy strategy = getStrategy(strategyId);
        Step parent = strategy.getStepById(stepId).getParentStep();
        while (parent.getNextStep() != null) {
            parent = parent.getNextStep();
        }
        // check if the parent is top level
        if (parent.getParentStep() == null) return Integer.toString(strategyId);
        else return strategyId + "_" + parent.getStepId();
    }

    private Strategy getStrategy(long strategyId) throws WdkModelException, WdkUserException {
      return _user.getWdkModel().getStepFactory().getStrategyById(strategyId);
    }

    private ActiveStrategy getStrategy(String strategyKey) {
        if (strategyKey == null) return _root;
        else return _root.getDescendent(strategyKey);
    }

    private void replaceStrategy(Strategy strategy, ActiveStrategy oldStrategy,
            ActiveStrategy newStrategy, Map<Long, Long> stepMap) {
        logger.debug("current view: " + _viewStrategyKey + ", "
                + _viewStepId);
        logger.debug("replace old: " + oldStrategy.strategyKey
                + ", new: " + newStrategy.strategyKey);
        for (long old : stepMap.keySet()) {
            logger.debug("step " + old + "->" + stepMap.get(old));
        }
        for (ActiveStrategy oldChild : oldStrategy.children.values()) {
            String oldKey = oldChild.strategyKey;
            long oldId = Long.parseLong(oldKey.substring(oldKey.indexOf('_') + 1));
            Long newId = stepMap.get(oldId);
            logger.debug("convert step " + oldId + "->" + newId);
            if (newId == null) {
                Step step;
                try {
                    step = strategy.getStepById(oldId);
                    if (step == null) throw new WdkModelException();
                    newId = oldId;
                } catch (Exception ex) { // step no longer exist
                    logger.debug("step #" + oldId + " has been deleted");
                    continue; // skip this branch
                }
            }
            String newKey = newStrategy.strategyId + "_" + newId;
            ActiveStrategy newChild = new ActiveStrategy(newKey);
            newChild.parent = newStrategy;
            replaceStrategy(strategy, oldChild, newChild, stepMap);
            newStrategy.children.remove(oldKey);
            newStrategy.children.put(newKey, newChild);
        }
        // may also need to update the view
        if (_viewStrategyKey != null
                && _viewStrategyKey.equals(oldStrategy.strategyKey)) {
            _viewStrategyKey = newStrategy.strategyKey;
            if (_viewStepId != null && stepMap.containsKey(_viewStepId))
                _viewStepId = stepMap.get(_viewStepId);
        }
    }

    /**
     * @return the viewStrategyKey
     */
    public String getViewStrategyKey() {
        if (_viewStrategyKey == null) return null;
        if (getStrategy(_viewStrategyKey) == null) return null;
        return _viewStrategyKey;
    }

    /**
     * @param viewStrategyKey
     *            the viewStrategyKey to set
     */
    public void setViewStrategyKey(String viewStrategyKey) {
        _viewStrategyKey = viewStrategyKey;
    }

    /**
     * @return the viewStepId
     */
    public long getViewStepId() {
        if (getViewStrategyKey() == null) return 0;

        // check if the viewStepId belongs to the current strategy
        try {
            ActiveStrategy activeStrategy = getStrategy(_viewStrategyKey);
            Strategy strategy = getStrategy(activeStrategy.strategyId);
            Step step = strategy.getStepById(_viewStepId);
        if (step == null) _viewStepId = strategy.getLatestStepId();
        } catch (Exception ex) {
            return 0;
        }

        return _viewStepId;
    }

    /**
     * @param viewStepId
     *            the viewStepId to set
     */
    public void setViewStepId(Long viewStepId) {
        _viewStepId = viewStepId;
    }

    public Integer getViewPagerOffset() {
        if (getViewStrategyKey() == null) return null;
        return _viewPagerOffset;
    }

    public void setViewPagerOffset(Integer viewPagerOffset) {
        _viewPagerOffset = viewPagerOffset;
    }
}
