/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.LinkedHashMap;

/**
 * @author xingao
 * 
 */
class ActiveStrategy {

    int strategyId;
    String strategyKey;
    ActiveStrategy parent;
    LinkedHashMap<String, ActiveStrategy> children;

    /**
     * 
     */
    ActiveStrategy(String strategyKey) {
        this.strategyKey = strategyKey;
        if (strategyKey != null) {
            int pos = strategyKey.indexOf('_');
            if (pos < 0) strategyId = Integer.parseInt(strategyKey);
            else strategyId = Integer.parseInt(strategyKey.substring(0, pos));
        }
        children = new LinkedHashMap<String, ActiveStrategy>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ActiveStrategy)) return false;
        ActiveStrategy strategy = (ActiveStrategy) obj;
        if (strategyKey == null) {
          return (strategy.strategyKey == null);
        } else {
          return strategyKey.equals(strategy.strategyKey);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (strategyKey == null) ? 0 : strategyKey.hashCode();
    }

    ActiveStrategy getDescendent(String strategyKey) {
        for (ActiveStrategy child : children.values()) {
            if (child.strategyKey.equals(strategyKey)) return child;
            ActiveStrategy strategy = child.getDescendent(strategyKey);
            if (strategy != null) return strategy;
        }
        return null;
    }
    
    public String toString() {
      return toString(0);
    }
    
    private String toString(int indent) {
      String tree = "";
      for (int i = 0; i < indent; i++) {
        tree += "  ";
      }
      tree += strategyKey + "\n";
      for (ActiveStrategy child: children.values()) {
        tree += child.toString(indent + 1);
      }
      return tree;
    }
}