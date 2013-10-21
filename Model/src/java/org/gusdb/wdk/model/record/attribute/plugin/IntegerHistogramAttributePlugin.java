package org.gusdb.wdk.model.record.attribute.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In the numeric histogram, the column value must be numeric.
 * 
 * @author jerric
 * 
 */
public class IntegerHistogramAttributePlugin extends
    HistogramAttributePlugin {

  private static final String PROP_BIN_SIZE = "bin-size";
  private static final String PROP_BIN_COUNT = "bin-count";

  private static final int DEFAULT_BIN_COUNT = 20;

  @Override
  protected Map<String, Integer> computeHistogram(Map<String, Integer> data) {
    if (data.size() == 0)
      return data;

    // determine the bin size
    int[] range = getRange(data);
    int binSize;
    if (properties.containsKey(PROP_BIN_SIZE)) {
      binSize = Integer.valueOf(properties.get(PROP_BIN_SIZE));
    } else {
      binSize = computeBinSize(range);
    }

    // convert from distinct
    Map<String, Integer> bins = new LinkedHashMap<>();
    for (String str : data.keySet()) {
      String bin = getBin(binSize, range, Integer.valueOf(str));
      int count = data.get(str);
      if (bins.containsKey(bin))
        count += bins.get(bin);
      bins.put(bin, count);
    }
    return bins;
  }

  private int[] getRange(Map<String, Integer> data) {
    // look for the min & max values
    int min = Integer.valueOf(data.keySet().iterator().next());
    int max = min;
    for (String str : data.keySet()) {
      int value = Integer.valueOf(str);
      if (min > value)
        min = value;
      else if (max < value)
        max = value;
    }
    return new int[] { min, max };
  }

  private int computeBinSize(int[] range) {
    if (range[0] == range[1])
      return 1;

    // get the bin count;
    int binCount = DEFAULT_BIN_COUNT;
    if (properties.containsKey(PROP_BIN_COUNT))
      binCount = Integer.valueOf(properties.get(PROP_BIN_COUNT));

    // compute the bin size
    int binSize = (int) Math.ceil((range[1] - range[0] + 1) / (double) binCount);

    return binSize;
  }

  private String getBin(int binSize, int[] range, int value) {
    int from = range[0];
    int to = from + binSize - 1;
    while (value > to) {
      from += binSize;
      to = from + binSize - 1;
    }
    return "[" + from + "-" + to + "]";
  }
}
