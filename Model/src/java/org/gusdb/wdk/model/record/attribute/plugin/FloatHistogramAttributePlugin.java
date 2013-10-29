package org.gusdb.wdk.model.record.attribute.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In the numeric histogram, the column value must be numeric.
 * 
 * @author jerric
 * 
 */
public class FloatHistogramAttributePlugin extends
    HistogramAttributePlugin {

  private static final String PROP_BIN_SIZE = "bin-size";
  private static final String PROP_BIN_COUNT = "bin-count";

  private static final int DEFAULT_BIN_COUNT = 20;

  @Override
  public String getAxisMode() {
    return "";
  }

  @Override
  protected Map<String, Integer> computeHistogram(Map<String, Integer> data) {
    if (data.size() == 0)
      return data;

    // determine the bin size
    double[] range = getRange(data);
    double binSize;
    if (properties.containsKey(PROP_BIN_SIZE)) {
      binSize = Double.valueOf(properties.get(PROP_BIN_SIZE));
    } else {
      binSize = computeBinSize(range);
    }

    // convert from distinct
    Map<String, Integer> bins = new LinkedHashMap<>();
    for (String str : data.keySet()) {
      String bin = getBin(binSize, range, Double.valueOf(str));
      int count = data.get(str);
      if (bins.containsKey(bin))
        count += bins.get(bin);
      bins.put(bin, count);
    }
    return bins;
  }

  private double[] getRange(Map<String, Integer> data) {
    // look for the min & max values
    double min = Double.valueOf(data.keySet().iterator().next());
    double max = min;
    for (String str : data.keySet()) {
      double value = Double.valueOf(str);
      if (min > value)
        min = value;
      else if (max < value)
        max = value;
    }
    return new double[] { min, max };
  }

  private double computeBinSize(double[] range) {
    if (range[0] == range[1])
      return 1;

    // get the bin count;
    int binCount = DEFAULT_BIN_COUNT;
    if (properties.containsKey(PROP_BIN_COUNT))
      binCount = Integer.valueOf(properties.get(PROP_BIN_COUNT));

    // compute the bin size
    double unroundedBinSize = (range[1] - range[0]) / (binCount - 1);
    double x = Math.ceil(Math.log10(unroundedBinSize) - 1);
    double pow10x = Math.pow(10, x);
    double binSize = Math.ceil(unroundedBinSize / pow10x) * pow10x;

    // adjust the min in the range
    range[0] = binSize * Math.floor(range[0] / binSize);

    return binSize;
  }

  private String getBin(double binSize, double[] range, double value) {
    double from = range[0];
    double to = from + binSize;
    while (to < value) {
      from += binSize;
      to = from + binSize;
    }
    return "[" + Double.toString(Math.round(from * 100) / 100)
           + "-" + Double.toString(Math.round(to * 100) / 100) + ")";
  }
}
