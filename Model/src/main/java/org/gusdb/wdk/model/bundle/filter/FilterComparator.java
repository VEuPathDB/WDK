package org.gusdb.wdk.model.bundle.filter;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

/**
 * Comparators used by column filters to represent and construct the various
 * possible operations.
 *
 * Each value holds a key which can be used to represent a comparator accross
 * application boundaries as well as a string template for the SQL comparator
 * operation itself.
 */
public enum FilterComparator {
  EQUALS         ( "eq",  "%s = %s"  ),
  NOT_EQUALS     ( "neq", "%s <> %s" ),
  GREATER_THAN   ( "gt",  "%s > %s"  ),
  LESS_THAN      ( "lt",  "%s < %s"  ),
  GREATER_EQUALS ( "gte", "%s >= %s" ),
  LESS_EQUALS    ( "lte", "%s <= %s" );

  final String key;
  final String sql;

  FilterComparator(final String key, final String sql) {
    this.key = key;
    this.sql = sql;
  }

  /**
   * @return this comparator's text representation/lookup key.
   */
  String key() {
    return key;
  }

  /**
   * Returns whether or not the given value is a valid possible key value.
   *
   * @param in
   *   value to check
   *
   * @return whether or not that value is a valid key.
   */
  static boolean isKey(final String in) {
    return in != null
      && Arrays.stream(values())
        .map(FilterComparator::key)
        .anyMatch(in::equals);
  }

  /**
   * Returns the FilterComparator matching the given argument.
   *
   * @param key
   *   comparator key string
   *
   * @return comparator matching the given key
   *
   * @throws IllegalArgumentException
   *   if the given string is not a valid key string.  To confirm whether or not
   *   a key is valid before calling this method see {@link #isKey(String)}
   */
  @JsonCreator
  static FilterComparator fromKey(final String key) {
    for (var e : values())
      if (e.key.equals(key))
        return e;
    throw new IllegalArgumentException();
  }

  /**
   * Returns a simple representation of this enum as an array of keys.
   *
   * This can be used for easier conversion into a serialization format such as
   * XML or JSON for communication across I/O boundaries.
   *
   * @return a simple representation of this enum as an array of it's keys.
   */
  static String[] simpleValue() {
    return Arrays.stream(values())
      .map(FilterComparator::key)
      .toArray(String[]::new);
  }
}
