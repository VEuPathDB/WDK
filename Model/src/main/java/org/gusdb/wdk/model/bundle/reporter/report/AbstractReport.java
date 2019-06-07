package org.gusdb.wdk.model.bundle.reporter.report;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.v4.ObjectSchema;
import org.gusdb.fgputil.SortDirection;
import org.gusdb.fgputil.runtime.JvmUtil;

import java.util.*;
import java.util.stream.Collectors;

import static org.gusdb.fgputil.runtime.JvmUtil.OBJECT_SIZE_PADDING_FACTOR;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
  AbstractReport.KEY_TOTAL,
  AbstractReport.KEY_UNIQUE,
  AbstractReport.KEY_NULLS,
  AbstractReport.KEY_VALUES
})
abstract class AbstractReport<T extends Comparable<?>>
{
  static final String
    KEY_VALUES = "values",
    KEY_UNIQUE = "uniqueValues",
    KEY_TOTAL  = "totalValues",
    KEY_NULLS  = "nullValues";

  /**
   * Max byte bytes of unique value array before histogram is disabled.
   */
  private static final int MAX_SIZE = 10485760;

  /**
   * Max number of {@link Pair}s to return in the values
   * array.
   */
  private final long maxVals;

  /**
   * Output value set.
   *
   * This property should be backed by a TreeSet with it's sorting controlled
   * by {@link SortDirection} given on construction of a Report instance.
   */
  private Set<Pair<T>> vals;

  /**
   * Indexed value set (for updates)
   */
  private Map<T, Pair<T>> index;

  /**
   * Total count of rows represented by this report.
   */
  private long total;

  /**
   * Total count of null values encountered in given values.
   */
  private long nulls;

  /**
   * Rough estimate of the current total byte size of all distinct values
   */
  private int bytes;

  private boolean limited;

  AbstractReport(long maxVals, SortDirection sort) {
    this.maxVals = maxVals;
    this.vals    = new TreeSet<Pair<T>>(sort.isAscending()
      ? Comparator.naturalOrder()
      : Comparator.reverseOrder());
    this.index   = new HashMap<>();
  }

  AbstractReport(SortDirection sort) {
    this(-1, sort);
  }

  /**
   * Determine the approximate size in bytes of the given value.
   *
   * @param val
   *   value to size
   *
   * @return an approximation of the bytes of memory used by the given value.
   */
  protected abstract int sizeOf(T val);

  /**
   * Push a new value into this report.
   *
   * @param val row/column value
   */
  public void pushValue(T val) {
    total++;

    if (val == null) {
      nulls++;
      return;
    }

    if (limited)
      return;

    limiter(sizeOf(val));
    if (limited)
      return;

    var pair = index.get(val);
    if (pair == null) {
      pair = new Pair<>(val, 1L);
      index.put(val, pair);
      vals.add(pair);
    } else {
      vals.remove(pair);
      pair.count++;
      vals.add(pair);
    }
  }

  /**
   * @return the total number of rows parsed by this reporter
   */
  @JsonGetter(KEY_TOTAL)
  public long getTotal() {
    return total;
  }

  /**
   * @return either a count of distinct values parsed by this reporter if the
   * data set size in memory is less than {@link AbstractReport#MAX_SIZE} or
   * else null.
   */
  @JsonGetter(KEY_UNIQUE)
  public Integer getUnique() {
    return index == null ? null : index.size();
  }

  /**
   * @return the number of null values encountered
   */
  @JsonGetter(KEY_NULLS)
  public long getNulls() {
    return nulls;
  }

  /**
   * @return if the memory used to store all unique values is less than {@link
   * AbstractReport#MAX_SIZE} then a collection of Pair instances representing a
   * sorted list of distinct values and their occurrences.  If the distinct
   * values would require greater than {@code MAX_SIZE} memory, null will be
   * returned.
   * <p>
   * If a positive or zero max values limit was provided, the output list will
   * contain at most that many items.
   */
  @JsonGetter(KEY_VALUES)
  public Collection<Pair<T>> getValues() {
    if (maxVals < 0 || vals == null)
      return vals;
    if (maxVals > 0)
      return vals.stream().limit(maxVals).collect(Collectors.toList());

    return Collections.emptyList();
  }

  /**
   * Extension point for report output schema building.
   * <p>
   * Value type must be provided by wrapping methods.
   *
   * @return ObjectSchema containing base elements for column reporter output.
   */
  static ObjectSchema outputSpec()
  {
    var schema = Schema.draft4();
    return schema.asObject()
      .additionalProperties(false)
      .optionalProperty(KEY_UNIQUE, schema.asInteger().minimum(0)
        .description("Count of unique, non-null values in the results"))
      .requiredProperty(KEY_TOTAL, schema.asInteger().minimum(0)
        .description("Count of values in the results"))
      .requiredProperty(KEY_NULLS, schema.asInteger().minimum(0)
        .description("Count of null values in the results"));
  }

  static int padSize(int size) {
    var o = (size / OBJECT_SIZE_PADDING_FACTOR) * OBJECT_SIZE_PADDING_FACTOR;
    return size % OBJECT_SIZE_PADDING_FACTOR == 0 ? o : o + 1;
  }

  /**
   * Limits the number of unique values stored by this report based on the
   * estimated memory needed to store those values.
   * <p>
   * If the total estimated memory is greater than the defined {@link
   * AbstractReport#MAX_SIZE}, the internal store of unique values will be
   * dropped and this reporter will stop recording unique values altogether.
   *
   * @param add
   *   the byte size of the last value to be appended to this report.
   */
  private void limiter(int add) {
    bytes += padSize(add + JvmUtil.OBJECT_HEADER_SIZE + Long.BYTES);

    if (bytes <= MAX_SIZE)
      return;

    // If we now exceed the size limits clear out the unique
    // value collections.
    vals = null;
    index = null;
    limited = true;
  }
}
