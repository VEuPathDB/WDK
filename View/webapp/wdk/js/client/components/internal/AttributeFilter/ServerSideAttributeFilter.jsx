import React from 'react';
import PropTypes from 'prop-types';
import { find } from 'lodash';

import { isRange } from '../../../utils/FilterServiceUtils';

import { shouldAddFilter } from './Utils';

import FieldFilter from './FieldFilter';
import FieldList from './FieldList';
import FilterList from './FilterList';
import InvalidFilterList from './InvalidFilterList';

/**
 * Filtering UI for server-side filtering.
 */
export default class ServerSideAttributeFilter extends React.Component {

  constructor(props) {
    super(props);
    this.handleSelectFieldClick = this.handleSelectFieldClick.bind(this);
    this.handleFilterRemove = this.handleFilterRemove.bind(this);
    this.handleFieldFilterChange = this.handleFieldFilterChange.bind(this);
    this.handleMemberSort = this.handleMemberSort.bind(this);
    this.handleRangeScaleChange = this.handleRangeScaleChange.bind(this);
  }

  handleSelectFieldClick(field, event) {
    event.preventDefault();
    this.props.onActiveFieldChange(field);
  }

  handleFilterRemove(filter) {
    let filters = this.props.filters.filter(f => f !== filter);
    this.props.onFiltersChange(filters);
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

  handleMemberSort(field, state) {
    this.props.onMemberSort(field, state);
  }

  handleRangeScaleChange(field, state) {
    this.props.onRangeScaleChange(field, state);
  }

  render() {
    var {
      autoFocus,
      hideFilterPanel,
      hideFieldPanel,
      dataCount,
      filteredDataCount,
      fields,
      filters,
      invalidFilters,
      activeField,
      activeFieldState,
      activeFieldSummary
    } = this.props;

    var displayName = this.props.displayName;
    var selectedFilter = activeField && find(filters, filter => {
      return filter.field === activeField.term;
    });

    return (
      <div>
        {hideFilterPanel || (
          <FilterList
            onFilterSelect={this.props.onActiveFieldChange}
            onFilterRemove={this.handleFilterRemove}
            filters={filters}
            fields={fields}
            filteredDataCount={filteredDataCount}
            dataCount={dataCount}
            selectedField={activeField}
            renderSelectionInfo={this.props.renderSelectionInfo}
          />
        )}

        <InvalidFilterList filters={invalidFilters}/>

        {/* Main selection UI */}
        <div className="filters ui-helper-clearfix">
          {hideFieldPanel || (
            <FieldList
              autoFocus={autoFocus}
              fields={fields}
              onFieldSelect={this.props.onActiveFieldChange}
              selectedField={activeField}
            />
          )}

          <FieldFilter
            displayName={displayName}
            filteredDataCount={filteredDataCount}
            dataCount={dataCount}
            field={activeField}
            fieldState={activeFieldState}
            fieldSummary={activeFieldSummary}
            filter={selectedFilter}
            onChange={this.handleFieldFilterChange}
            onMemberSort={this.handleMemberSort}
            onRangeScaleChange={this.handleRangeScaleChange}
            useFullWidth={hideFieldPanel}
            selectByDefault={this.props.selectByDefault}
          />
        </div>
      </div>
    );
  }

}

ServerSideAttributeFilter.propTypes = {

  // options
  displayName: PropTypes.string,
  autoFocus: PropTypes.bool,
  hideFilterPanel: PropTypes.bool,
  hideFieldPanel: PropTypes.bool,
  renderSelectionInfo: PropTypes.func,
  selectByDefault: PropTypes.bool, // affects UI state for when no filter is applied

  // state
  fields: PropTypes.instanceOf(Map).isRequired,
  filters: PropTypes.array.isRequired,
  dataCount: PropTypes.number,
  filteredDataCount: PropTypes.number,

  activeField: FieldFilter.propTypes.field,
  activeFieldState: FieldFilter.propTypes.fieldState,
  activeFieldSummary: FieldFilter.propTypes.fieldSummary,

  // not sure if these belong here
  isLoading: PropTypes.bool,
  invalidFilters: PropTypes.array,  // derivable?

  // event handlers
  onActiveFieldChange: PropTypes.func.isRequired,
  onFiltersChange: PropTypes.func.isRequired,
  onMemberSort: PropTypes.func.isRequired,
  onRangeScaleChange: PropTypes.func.isRequired

};

ServerSideAttributeFilter.defaultProps = {
  displayName: 'Items',
  hideFilterPanel: false,
  hideFieldPanel: false,
  selectByDefault: true
};

