package org.gusdb.wdk.model.toolbundle.reporter.report;

import com.fasterxml.jackson.annotation.*;
import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import org.gusdb.fgputil.SortDirection;
import org.gusdb.fgputil.runtime.JvmUtil;
import org.gusdb.wdk.model.WdkModelException;

import java.math.BigDecimal;

@JsonPropertyOrder({
  NumberReport.KEY_MIN_VAL,
  NumberReport.KEY_MAX_VAL,
  AbstractReport.KEY_TOTAL,
  AbstractReport.KEY_UNIQUE,
  AbstractReport.KEY_NULLS,
  AbstractReport.KEY_VALUES
})
public class NumberReport extends AbstractReport<BigDecimal> {

  static final String
    KEY_MAX_VAL = "maxValue",
    KEY_MIN_VAL = "minValue";

  /**
   * Static part of the size of a BigDecimal instance.
   * <p>
   * Size consists of:
   * <ul>
   * <li>the BD object header
   * <li>the BD "intCompact" long field
   * <li>the BD "precision" int field
   * <li>the BD "scale" int field
   * <li>the BD "intVal" BigInteger reference (may be null).
   *     Size here is based on native pointer size and does
   *     not account for any context based JVM optimizations
   * </ul>
   */
  private static final byte BD_SIZE_PREFIX = (byte) (JvmUtil.OBJECT_HEADER_SIZE
    + Long.BYTES + Integer.BYTES * 2 + (JvmUtil.IS_64BIT ? 8 : 4));

  /**
   * Static part of the size of a BigInteger instance.
   * <p>
   * Size consists of:
   * <ul>
   * <li>the object header of the BI itself
   * <li>the object header for the internal int array for the BI
   * <li>the int length field of the internal array
   * <li>the BI "signum" int field
   * <li>the BI "bitCountPlusOne" int field
   * <li>the BI "bitLengthPlusOne" int field
   * <li>the BI "lowestSetBitPlusTwo" int field
   * <li>the BI "firstNonzeroIntNumPlusTwo" int field
   * </ul>
   */
  private static final byte BI_SIZE_PREFIX = (byte) (
    JvmUtil.OBJECT_HEADER_SIZE * 2 + Integer.BYTES * 6);

  private BigDecimal max;
  private BigDecimal min;

  public NumberReport(long maxValues, SortDirection sort) {
    super(maxValues, sort);
  }

  @Override
  public BigDecimal parse(String raw) throws WdkModelException {
    if (raw == null) return null;
    try { return new BigDecimal(raw); }
    catch (Exception e) { throw new WdkModelException(e); }
  }

  @Override
  protected int sizeOf(BigDecimal val) {
    return val.precision() > 18
      ? padSize(BD_SIZE_PREFIX + BI_SIZE_PREFIX
        + val.unscaledValue().bitLength() / Integer.BYTES // mag array
        + Integer.BYTES)                                  // overflow safety
      : padSize(BD_SIZE_PREFIX);
  }

  @Override
  public void pushValue(BigDecimal val) {
    super.pushValue(val);

    if (max == null || val.compareTo(max) > 0)
      max = val;
    if (min == null || val.compareTo(min) < 0)
      min = val;
  }

  @JsonGetter(KEY_MAX_VAL)
  public BigDecimal getMax() {
    return max;
  }

  @JsonGetter(KEY_MIN_VAL)
  public BigDecimal getMin() {
    return min;
  }

  public static SchemaBuilder outputSchema() {
    var js = Schema.draft4();
    return AbstractReport.outputSpec()
      .requiredProperty(KEY_MAX_VAL, js.asNumber()
        .description("Highest value that appears in the results"))
      .requiredProperty(KEY_MIN_VAL, js.asNumber()
        .description("Lowest value that appears in the results"))
      .requiredProperty(KEY_VALUES, js.asArray()
        .items(js.asObject()
          .requiredProperty(Pair.KEY_VALUE, js.asNumber())
          .requiredProperty(Pair.KEY_COUNT, js.asInteger().minimum(0)))
        .description("An array distinct values and their frequency")
        .uniqueItems(true));
  }
}
