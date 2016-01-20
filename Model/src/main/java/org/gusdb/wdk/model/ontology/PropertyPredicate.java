package org.gusdb.wdk.model.ontology;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Predicate;

public class PropertyPredicate implements Predicate<OntologyNode> {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(PropertyPredicate.class);

  private final Map<String, String> _criteria;

  public PropertyPredicate(Map<String, String> criteria) {
    _criteria = criteria;
  }

  @Override
  public boolean test(OntologyNode obj) {
    //LOG.info("Checking " + obj + " against criteria " + FormatUtil.prettyPrint(_criteria));
    for (Entry<String, String> criterium : _criteria.entrySet()) {
      // get list of node values behind this key
      List<String> nodeValues = obj.get(criterium.getKey());
      if (nodeValues == null || !nodeValues.contains(criterium.getValue())) {
        return false;
      }
    }
    return true;
  }
}
