package org.gusdb.wdk.model.user;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.gusdb.fgputil.SortDirection;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Step Display Preference
 *
 * Per step preferences for how a step should be rendered.  These do not
 * directly influence the display of a step, they simply provide a hint to the
 * client as to the options the user last manually selected.
 */
@JsonInclude(Include.NON_EMPTY)
public class StepDisplayPreference {

  public static final int MAX_SORT_COLUMNS = 3;

  public static class StepDisplayPreferenceBuilder {
    private final Collection<String> _columnSelection;
    private final Map<String, SortDirection> _sortColumns;

    public StepDisplayPreferenceBuilder(StepDisplayPreference prefs) {
      _columnSelection = new ArrayList<>(prefs.getColumnSelection());
      _sortColumns = new LinkedHashMap<>(prefs.getSortColumns());
    }

    public StepDisplayPreferenceBuilder() {
      _columnSelection = new ArrayList<>();
      _sortColumns = new LinkedHashMap<>();
    }

    public StepDisplayPreferenceBuilder setColumnSelection(Collection<String> cols) {
      _columnSelection.clear();
      _columnSelection.addAll(cols);
      return this;
    }

    /**
     * Set
     * @param cols
     * @return
     */
    @JsonIgnore
    public StepDisplayPreferenceBuilder setSortColumns(Map<String, SortDirection> cols) {
      _sortColumns.clear();
      _sortColumns.putAll(cols);
      return this;
    }

    /**
     * Set sorting columns by direct translation from JSON.
     *
     * @param cols Array of 2 entry key/value arrays structured as
     *             <code>[Column, SortDirection]</code>
     *
     * @return The current builder instance;
     */
    @JsonSetter
    public StepDisplayPreferenceBuilder setSortColumns(Collection<Map<String,SortDirection>> cols) {
      _sortColumns.clear();
      cols.forEach(_sortColumns::putAll);
      return this;
    }

    /**
     * Build a StepDisplayPreference instance from the current builder
     * configuration.  The current values will be validated based on the
     * following:
     *
     * <dl>
     *   <dt><code>lvl == ValidationLevel.NONE</code></dt>
     *   <dd>No checks will be performed.  Resulting StepDisplayPreference
     *   instance may be invalid.</dd>
     *   <dt><code>lvl != ValidationLevel.NONE</code></dt>
     *   <dd>The length of the sortColumns will be verified to be no longer than
     *   then max lengh defined by MAX_SORT_COLUMNS</dd>
     * </dl>
     *
     * @param lvl validation level
     *
     * @return Constructed StepDisplayPreference instance
     * @throws WdkModelException
     */
    public StepDisplayPreference build(ValidationLevel lvl)
        throws WdkModelException {
      if (lvl == ValidationLevel.NONE)
        return new StepDisplayPreference(_columnSelection, _sortColumns);

      if (_sortColumns.size() > MAX_SORT_COLUMNS)
        throw new WdkModelException(String.format(
            "Cannot sort by more than %d columns.", MAX_SORT_COLUMNS));

      return new StepDisplayPreference(_columnSelection, _sortColumns);
    }
  }

  private final Collection<String> _columnSelection;
  private final Map<String, SortDirection> _sortColumns;

  private StepDisplayPreference(Collection<String> columnSelections,
      Map<String, SortDirection> sortColumns) {
    _columnSelection = Collections.unmodifiableCollection(columnSelections);
    _sortColumns = Collections.unmodifiableMap(sortColumns);
  }

  /**
   * @return an immutable collection of user selected display columns.
   */
  public Collection<String> getColumnSelection() {
    return _columnSelection;
  }

  /**
   * @return an immutable map of sort columns
   */
  @JsonIgnore
  public Map<String, SortDirection> getSortColumns() {
    return _sortColumns;
  }

  @JsonGetter("sortColumns")
  public Collection<Map<String,SortDirection>> getSortColumnsAsCollection() {
    return _sortColumns.entrySet()
      .stream()
      .map(e -> new HashMap<String,SortDirection>(){{
        put(e.getKey(), e.getValue());
      }})
      .collect(Collectors.toList());
  }

  public static StepDisplayPreferenceBuilder builder() {
    return new StepDisplayPreferenceBuilder();
  }

  public static StepDisplayPreferenceBuilder builder(StepDisplayPreference pref) {
    return new StepDisplayPreferenceBuilder(pref);
  }
}
