package org.gusdb.wdk.model.user;

import java.util.LinkedHashMap;

/**
 * @author xingao
 * 
 */
class ActiveStrategy {

    int _strategyId;
    String _strategyKey;
    ActiveStrategy _parent;
    LinkedHashMap<String, ActiveStrategy> _children;

    ActiveStrategy(String strategyKey) {
        this._strategyKey = strategyKey;
        if (strategyKey != null) {
            int pos = strategyKey.indexOf('_');
            if (pos < 0) _strategyId = Integer.parseInt(strategyKey);
            else _strategyId = Integer.parseInt(strategyKey.substring(0, pos));
        }
        _children = new LinkedHashMap<String, ActiveStrategy>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ActiveStrategy)) return false;
        ActiveStrategy strategy = (ActiveStrategy) obj;
        if (_strategyKey == null) {
          return (strategy._strategyKey == null);
        } else {
          return _strategyKey.equals(strategy._strategyKey);
        }
    }

    @Override
    public int hashCode() {
        return (_strategyKey == null) ? 0 : _strategyKey.hashCode();
    }

    ActiveStrategy getDescendent(String strategyKey) {
        for (ActiveStrategy child : _children.values()) {
            if (child._strategyKey.equals(strategyKey)) return child;
            ActiveStrategy strategy = child.getDescendent(strategyKey);
            if (strategy != null) return strategy;
        }
        return null;
    }

    @Override
    public String toString() {
      return toString(0);
    }

    private String toString(int indent) {
      String tree = "";
      for (int i = 0; i < indent; i++) {
        tree += "  ";
      }
      tree += _strategyKey + "\n";
      for (ActiveStrategy child: _children.values()) {
        tree += child.toString(indent + 1);
      }
      return tree;
    }
}