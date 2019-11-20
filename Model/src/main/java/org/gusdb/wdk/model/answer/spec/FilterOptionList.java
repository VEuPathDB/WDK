package org.gusdb.wdk.model.answer.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.gusdb.fgputil.validation.Validateable;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationBundle.ValidationBundleBuilder;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.spec.FilterOption.FilterOptionBuilder;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.filter.Filter.FilterType;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONObject;

public class FilterOptionList implements Iterable<FilterOption>, Validateable<FilterOptionList> {

  public static class FilterOptionListBuilder extends ArrayList<FilterOptionBuilder> {

    private static final long serialVersionUID = 1L;

    private FilterOptionListBuilder() {}

    public FilterOptionListBuilder addFilterOption(FilterOptionBuilder filter) {
      add(filter);
      return this;
    }

    public FilterOptionListBuilder addFilterOption(String filterName, JSONObject filterValue) {
      add(FilterOption.builder().setFilterName(filterName).setValue(filterValue));
      return this;
    }

    public FilterOptionListBuilder addAllFilters(FilterOptionList filters) {
      for (FilterOption filter : filters) {
        add(FilterOption.builder(filter));
      }
      return this;
    }

    public FilterOptionListBuilder removeAll(Predicate<FilterOptionBuilder> predicate) {
      for (int i = 0; i < size(); i++) {
        if (predicate.test(get(i))) {
          remove(i);
          i--;
        }
      }
      return this;
    }

    public FilterOptionList buildInvalid() {
      List<FilterOption> options = new ArrayList<>();
      for (FilterOptionBuilder filterBuilder : this) {
        options.add(filterBuilder.buildInvalid());
      }
      return new FilterOptionList(options, ValidationBundle.builder(ValidationLevel.NONE)
          .addError("No question present to validate bundle").build());
    }

    public FilterOptionList buildValidated(Question question, FilterType containerType, ValidationLevel level) {
      ValidationBundleBuilder validation = ValidationBundle.builder(level);
      List<FilterOption> options = new ArrayList<>();
      for (FilterOptionBuilder filterBuilder : this) {
        FilterOption filterOption = filterBuilder.buildValidated(question, level);
        if (!filterOption.isValid()) {
          validation.aggregateStatus(filterOption);
        }
        else {
          // filter option valid against question; is it the correct type?
          Filter filter = filterOption.getFilter();
          if (!containerType.containerSupports(filter.getFilterType())) {
            validation.addError("Filter with name '" + filter.getKey() + "' was declared " +
                filter.getFilterType() + " but was found in the " + containerType + " filter list.");
          }
        }
        options.add(filterOption);
      }
      return new FilterOptionList(options, validation.build());
    }

    public boolean hasFilter(String name) {
      return stream().anyMatch(option -> name.equals(option.getFilterName()));
    }
  }

  public static FilterOptionListBuilder builder() {
    return new FilterOptionListBuilder();
  }

  private List<FilterOption> _options;
  private ValidationBundle _validationBundle;

  private FilterOptionList(List<FilterOption> options, ValidationBundle validationBundle) {
    _options = options;
    _validationBundle = validationBundle;
  }

  public boolean isFiltered(SimpleAnswerSpec simpleAnswerSpec) throws WdkModelException {
    if (simpleAnswerSpec.getQuestion() == null) return false;
    for (FilterOption option : _options) {
      if (!option.isDisabled() && option.getFilter() != null && !option.isSetToDefaultValue(simpleAnswerSpec)) {
        return true;
      }
    }
    return false;
  }

  public int getSize() {
    return _options.size();
  }

  public Stream<FilterOption> stream() {
    return _options.stream();
  }

  @Override
  public Iterator<FilterOption> iterator() {
    return Collections.unmodifiableList(_options).iterator();
  }

  @Override
  public ValidationBundle getValidationBundle() {
    return _validationBundle;
  }

  public boolean isEmpty() {
    return getSize() == 0;
  }

  public Optional<FilterOption> getFirst(Predicate<FilterOption> predicate) {
    return _options.stream().filter(predicate).findFirst();
  }
}
