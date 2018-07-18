package org.gusdb.wdk.model.answer.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.gusdb.fgputil.validation.Validateable;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationBundle.ValidationBundleBuilder;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.spec.FilterOption.FilterOptionBuilder;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.filter.Filter.FilterType;
import org.gusdb.wdk.model.question.Question;

public class FilterOptionList implements Iterable<FilterOption>, Validateable {

  public static class FilterOptionListBuilder {

    private List<FilterOptionBuilder> _options = new ArrayList<>();

    private FilterOptionListBuilder() {}

    public FilterOptionListBuilder addFilterOption(FilterOptionBuilder filter) {
      _options.add(filter);
      return this;
    }

    public FilterOptionListBuilder fromFilterOptionList(FilterOptionList filters) {
      for (FilterOption filter : filters) {
        _options.add(FilterOption.builder().fromFilterOption(filter));
      }
      return this;
    }

    public FilterOptionList buildInvalid() {
      List<FilterOption> options = new ArrayList<>();
      for (FilterOptionBuilder filterBuilder : _options) {
        options.add(filterBuilder.buildInvalid());
      }
      return new FilterOptionList(options, ValidationBundle.builder(ValidationLevel.NONE)
          .addError("No question present to validate bundle").build());
    }

    public FilterOptionList buildValidated(Question question, FilterType containerType, ValidationLevel level) {
      ValidationBundleBuilder validation = ValidationBundle.builder(level);
      List<FilterOption> options = new ArrayList<>();
      for (FilterOptionBuilder filterBuilder : _options) {
        FilterOption filterOption = filterBuilder.buildValidated(question, level);
        Filter filter = filterOption.getFilter();
        if (filter != null && !containerType.containerSupports(filter.getFilterType())) {
          validation.addError("Filter with name '" + filter.getKey() + "' was declared " +
              filter.getFilterType() + " but was found in the " + containerType + " filter list.");
        }
        options.add(filterOption);
      }
      validation.aggregateStatus(options.toArray(new FilterOption[options.size()]));
      return new FilterOptionList(options, validation.build());
      
    }

    public boolean hasFilter(String name) {
      return _options.stream().anyMatch(option -> name.equals(option.getFilterName()));
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

  @Override
  public Iterator<FilterOption> iterator() {
    return Collections.unmodifiableList(_options).iterator();
  }

  @Override
  public ValidationBundle getValidationBundle() {
    return _validationBundle;
  }
}
