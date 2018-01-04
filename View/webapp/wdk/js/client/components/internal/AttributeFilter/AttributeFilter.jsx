import $ from 'jquery';
import { find, sortBy, } from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import { findDOMNode } from 'react-dom';

import { isRange } from '../../../utils/FilterServiceUtils';
import { shouldAddFilter } from './Utils';

import FieldFilter from './FieldFilter';
import FieldList from './FieldList';
import FilterList from './FilterList';
import FilteredData from './FilteredData';
import InvalidFilterList from './InvalidFilterList';

/**
 * Primary component
 */
export default class AttributeFilter extends React.Component {

  constructor(props) {
    super(props);
    this.handleSelectFieldClick = this.handleSelectFieldClick.bind(this);
    this.handleCollapseClick = this.handleCollapseClick.bind(this);
    this.handleExpandClick = this.handleExpandClick.bind(this);
    this.handleFieldsChange = this.handleFieldsChange.bind(this);
    this.handleIgnored = this.handleIgnored.bind(this);
    this.handleSort = this.handleSort.bind(this);
    this.handleFilterRemove = this.handleFilterRemove.bind(this);
    this.handleFieldFilterChange = this.handleFieldFilterChange.bind(this);

    this.state = {
      sortTerm: '__primary_key__',
      sortDirection: 'ASC',
      collapsed: false
    };
  }

  componentDidMount() {
    var $node = $(findDOMNode(this));
    $node.find('.filter-param-tabs').tabs({
      activate: (event, ui) => {
        this.setState({
          tabWidth: ui.newPanel.width()
        });
      }
    });
  }

  /**
   * @param {string} field
   * @param {Event} event
   */
  handleSelectFieldClick(field, event) {
    event.preventDefault();
    this.props.onActiveFieldChange(field);
  }

/**
 * @param {Event} event
 */
  handleCollapseClick(event) {
    event.preventDefault();
    this.setState({
      collapsed: true
    });
  }

/**
 * @param {Event} event
 */
  handleExpandClick(event) {
    event.preventDefault();
    this.setState({
      collapsed: false
    });
  }

/**
 * Columns in data table change
 * @param {*} fields
 */
  handleFieldsChange(fields) {
    this.props.onColumnsChange(fields);
  }

  /**
   * @deprecated
   */
  handleIgnored(datum, ignored) {
    let ignoredData = ignored
      ? this.props.ignoredData.concat(datum)
      : this.props.ignoredData.filter(d => d !== datum);
    this.props.onIgnoredDataChange(ignoredData);
  }

/**
 *
 * @param {string} fieldTerm
 */
  handleSort(fieldTerm) {
    let { sortTerm, sortDirection } = this.state;
    let direction = fieldTerm == sortTerm && sortDirection == 'ASC'
      ? 'DESC' : 'ASC';
    this.setState({
      sortTerm: fieldTerm,
      sortDirection: direction
    });
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

  render() {
    var {
      dataCount,
      filteredData,
      fields,
      columns,
      ignoredData,
      filters,
      invalidFilters,
      activeField,
      activeFieldSummary,
      fieldMetadataMap
    } = this.props;

    let {
      tabWidth,
      sortTerm,
      sortDirection
    } = this.state;

    var displayName = this.props.displayName;
    var selectedFilter = find(filters, filter => {
      return filter.field === activeField.term;
    });

    var filteredNotIgnored = filteredData.filter(datum => ignoredData.indexOf(datum) === -1);

    var sortedFilteredData = sortBy(filteredData, function(datum) {
      var term = datum.term;
      return sortTerm == '__primary_key__' ? term : fieldMetadataMap[sortTerm][term];
    });

    if (sortDirection == 'DESC') sortedFilteredData.reverse();

    return (
      <div>
        <FilterList
          onFilterSelect={this.props.onActiveFieldChange}
          onFilterRemove={this.handleFilterRemove}
          filters={filters}
          filteredDataCount={filteredNotIgnored.length}
          dataCount={dataCount}
          fields={fields}
          selectedField={activeField}
          renderSelectionInfo={this.props.renderSelectionInfo}
        />

        <InvalidFilterList filters={invalidFilters}/>

        <div className="filter-view">
          <button onClick={this.handleExpandClick}
            style={{
              display: !this.state.collapsed ? 'none' : 'block'
            }} >Select {displayName}</button>

          {/* Tabs */}

          <div className="filter-param-tabs" style={{ display: this.state.collapsed ? 'none' : 'block' }}>
            <ul className="wdk-AttributeFilter-TabNav">
              <li><a href="#filters">Select {displayName}</a></li>
              <li><a href="#data">View selected {displayName} ({filteredData.length})</a></li>
              {this.props.collapsible && (
                <li>
                  <span
                    className="wdk-AttributeFilter-Collapse link"
                    title="Hide selection tool"
                    onClick={this.handleCollapseClick}
                  >Collapse</span>
                </li>
              )}

            </ul>


            {/* Main selection UI */}
            <div id="filters">
              <div className="filters ui-helper-clearfix">
                <FieldList
                  fields={fields}
                  onFieldSelect={this.props.onActiveFieldChange}
                  selectedField={activeField}
                />

                <FieldFilter
                  displayName={displayName}
                  field={activeField}
                  filter={selectedFilter}
                  filteredDataCount={filteredNotIgnored.length}
                  dataCount={dataCount}
                  fieldSummary={activeFieldSummary}
                  onChange={this.handleFieldFilterChange}
                  addTopPadding
                  selectByDefault={this.props.selectByDefault}
                />
              </div>
            </div>

            {/* Results table */}

            <div id="data">
              <FilteredData
                tabWidth={tabWidth}
                displayName={displayName}
                onFieldsChange={this.handleFieldsChange}
                onIgnored={this.handleIgnored}
                onSort={this.handleSort}
                sortTerm={sortTerm}
                sortDirection={sortDirection}
                filteredData={sortedFilteredData}
                totalSize={dataCount}
                selectedFields={columns}
                fields={fields}
                ignoredData={ignoredData}
                metadata={fieldMetadataMap}/>
            </div>
          </div>
        </div>
      </div>
    );
  }

}

AttributeFilter.propTypes = {

  displayName: PropTypes.string,
  collapsible: PropTypes.bool,
  selectByDefault: PropTypes.bool,

  // state
  fields: PropTypes.instanceOf(Map).isRequired,
  filters: PropTypes.array.isRequired,
  dataCount: PropTypes.number.isRequired,
  filteredData: PropTypes.array.isRequired,
  ignoredData: PropTypes.array.isRequired,
  columns: PropTypes.array.isRequired,
  activeField: PropTypes.object,
  activeFieldSummary: PropTypes.array,
  fieldMetadataMap: PropTypes.object.isRequired,
  renderSelectionInfo: PropTypes.func,

  // not sure if these belong here
  isLoading: PropTypes.bool,
  invalidFilters: PropTypes.array,  // derivable?

  // event handlers
  onActiveFieldChange: PropTypes.func.isRequired,
  onFiltersChange: PropTypes.func.isRequired,
  onColumnsChange: PropTypes.func.isRequired,
  onIgnoredDataChange: PropTypes.func.isRequired

};

AttributeFilter.defaultProps = {
  displayName: 'Items',
  collapsible: true,
  selectByDefault: true
};
