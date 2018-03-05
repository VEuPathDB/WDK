import React from 'react';
import { isRange } from 'Components/AttributeFilter/FilterServiceUtils';
import DateField from 'Components/AttributeFilter/internal/AttributeFilter/DateField';
import NumberField from 'Components/AttributeFilter/internal/AttributeFilter/NumberField';
import { shouldAddFilter } from 'Components/AttributeFilter/internal/AttributeFilter/Utils';
import MembershipField from 'Components/AttributeFilter/internal/AttributeFilter/MembershipField';

export default class SingleFieldFilter extends React.Component {
  constructor(props) {
    super(props);
    this.handleFieldFilterChange = this.handleFieldFilterChange.bind(this);
  }

  /**
   * @param {Field} field Field term id
   * @param {any} value Filter value
   * @param {boolean} includeUnknown Indicate if items with an unknown value for the field should be included.
   */
  handleFieldFilterChange(field, value, includeUnknown, valueCounts) {
    let filters = this.props.filters.filter(f => f.field !== field.term);
    this.props.onFiltersChange(shouldAddFilter(field, value, includeUnknown,
      valueCounts, this.props.selectByDefault)
      ? filters.concat({ field: field.term, type: field.type, isRange: isRange(field), value, includeUnknown })
      : filters
    );
  }

  render() {
    const { activeField, filters } = this.props;

    const FieldDetail = activeField == null ? null
      : isRange(activeField) == false ? MembershipField
      : activeField.type == 'string' ? MembershipField
      : activeField.type == 'number' ? NumberField
      : activeField.type == 'date' ? DateField
      : null;

    const filter = (
      activeField &&
      filters &&
      filters.find(filter => filter.field === activeField.term)
    );

  let fieldDetailProps = {
    filter,
    dataCount: this.props.dataCount,
    filteredDataCount: this.props.filteredDataCount,
    fieldSummary: this.props.activeFieldSummary,
    displayName: this.props.displayName,
    field: this.props.activeField,
    fieldState: this.props.activeFieldState,
    onChange: this.handleFieldFilterChange,
    onSort: this.props.onMemberSort,
    onSearch: this.props.onMemberSearch,
    onRangeScaleChange: this.props.onRangeScaleChange,
    selectByDefault: this.props.selectByDefault
  };

    return (
      <FieldDetail {...fieldDetailProps} />
    );

  }

}
