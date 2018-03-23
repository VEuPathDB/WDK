import React from 'react';
import { omit } from 'lodash';
import { isRange } from './Utils/FilterServiceUtils';
import DateField from './DateField';
import NumberField from './NumberField';
import { shouldAddFilter } from './Utils';
import MembershipField from './MembershipField';

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

    const restProps = omit(this.props, ['filter']);

    return (
      <React.Fragment>
        <FieldDetail
          filter={filter}
          onChange={this.handleFieldFilterChange}
          {...restProps}
         />
      </React.Fragment>
    );

  }

}
