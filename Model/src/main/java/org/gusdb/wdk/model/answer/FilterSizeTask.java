package org.gusdb.wdk.model.answer;

import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

/**
 * Computes application of various filters to the same answer value in parallel to get result sizes
 * 
 * May eventually be deprecated if we do away with the filter grid (the only place we really load filtered result sizes)
 * 
 * @author jerric
 */
public class FilterSizeTask implements Runnable {

  private static final Logger LOG = Logger.getLogger(FilterSizeTask.class);

  private final ResultSizeFactory _factory;
  private final ConcurrentMap<String, Integer> _sizes;
  private final String _filterName;
  private final boolean _useDisplay;

  public FilterSizeTask(ResultSizeFactory factory, ConcurrentMap<String, Integer> sizes, String filterName, boolean useDisplay) {
    _factory = factory;
    _sizes = sizes;
    _filterName = filterName;
    _useDisplay = useDisplay;
  }

  @Override
  public void run() {
    try {
      int size = (_useDisplay ?
          _factory.getFilterDisplaySize(_filterName) :
          _factory.getFilterSize(_filterName));
      _sizes.put(_filterName, size);
    }
    catch (Exception ex) {
      LOG.error("Could not determine filter size for filter '" + _filterName + "'.", ex);
      _sizes.put(_filterName, -1);
    }
  }

}
