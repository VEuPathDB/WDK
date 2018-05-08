import React from 'react';
import PropTypes from 'prop-types';
import { partial } from 'lodash';

import { Seq } from 'Utils/IterableUtils';

import { getFilterValueDisplay, shouldAddFilter } from './Utils';

/**
 * List of filters configured by the user.
 *
 * Each filter can be used to update the active field
 * or to remove a filter.
 */
export default class FilterList extends React.Component {

  /**
   * @param {FilterListProps} props
   * @return {React.Component<FilterListProps, void>}
   */
  constructor(props) {
    super(props);
    this.handleFilterSelectClick = this.handleFilterSelectClick.bind(this);
    this.handleFilterRemoveClick = this.handleFilterRemoveClick.bind(this);
  }

  /**
   * @param {Filter} filter
   * @param {Event} event
   */
  handleFilterSelectClick(filter, containerFilter = filter, event) {
    event.preventDefault();
    this.props.onActiveFieldChange(containerFilter.field);
  }

/**
 * @param {Filter} filter
 * @param {Event} event
 */
  handleFilterRemoveClick(filter, containerFilter, event) {
    event.preventDefault();
    if (containerFilter != null) {
      const otherFilters = this.props.filters.filter(f => f !== containerFilter);
      const nextContainerFilter = {
        ...containerFilter,
        value: {
          ...containerFilter.value,
          filters: containerFilter.value.filters.filter(f => f !== filter)
        }
      };
      this.props.onFiltersChange(otherFilters.concat(shouldAddFilter(nextContainerFilter) ? [ nextContainerFilter ] : []));
    }
    else {
      this.props.onFiltersChange(this.props.filters.filter(f => f !== filter));
    }
  }

  render() {
    var { fields, filters, activeField, filteredDataCount, dataCount, displayName, loadingFilteredCount, hideGlobalCounts } = this.props;

    const filteredCount = hideGlobalCounts ? null
      : loadingFilteredCount ? (
        <React.Fragment>
          <i className="fa fa-circle-o-notch fa-spin fa-fw margin-bottom"></i>
          <span className="sr-only">Loading...</span>
        </React.Fragment>
      )
      : filteredDataCount && filteredDataCount.toLocaleString();

    const total = hideGlobalCounts ? null : <span>{dataCount && dataCount.toLocaleString()} {displayName} Total</span>
    const filtered = hideGlobalCounts ? null : <span style={{ marginRight: '1em' }}>{filteredCount} of {dataCount && dataCount.toLocaleString()} {displayName} selected</span>;


    return (
      <div className="filter-items-wrapper">
        <div className="filter-list-total">{total}</div>
        {filters.length === 0 ? null : <div className="filter-list-selected">{filtered}</div>}
        {filters.length === 0
          ? ( hideGlobalCounts ? null : <strong><em>No filters applied</em></strong> )
          : <ul style={{display: 'inline-block'}} className="filter-items">
            {Seq.from(filters)
              .flatMap(filter => filter.type === 'multiFilter'
                ? filter.value.filters.map(leaf => [ leaf, filter ])
                : [ [ filter ] ])
              .map(([ filter, containerFilter ]) => {
              var className = activeField && activeField.term === filter.field ? 'selected' : '';
              var handleSelectClick = partial(this.handleFilterSelectClick, filter, containerFilter);
              var handleRemoveClick = partial(this.handleFilterRemoveClick, filter, containerFilter);
              var field = fields.get(filter.field);
              var containerField = containerFilter && fields.get(containerFilter.field);
              var fieldDisplay = containerField
                ? containerField.display + " > " + field.display
                : field.display;

              var filterDisplay = getFilterValueDisplay(filter);

              return (
                <li key={filter.field} className={className}>
                  <div className="ui-corner-all">
                    <a className="select"
                      onClick={handleSelectClick}
                      href={'#' + filter.field}
                      title={filterDisplay}>{fieldDisplay}</a>
                    {/* Use String.fromCharCode to avoid conflicts with
                        character ecoding. Other methods detailed at
                        http://facebook.github.io/react/docs/jsx-gotchas.html#html-entities
                        cause JSX to encode. String.fromCharCode ensures that
                        the encoding is done in the browser */}
                    <span className="remove"
                      onClick={handleRemoveClick}
                      title="remove restriction">{String.fromCharCode(215)}</span>
                  </div>
                </li>
              );
            })}
          </ul>}
      </div>
    );
  }

}

FilterList.propTypes = {
  onActiveFieldChange: PropTypes.func.isRequired,
  onFiltersChange: PropTypes.func.isRequired,
  fields: PropTypes.instanceOf(Map).isRequired,
  filters: PropTypes.array.isRequired,
  displayName: PropTypes.string.isRequired,
  dataCount: PropTypes.number,
  filteredDataCount: PropTypes.number,
  hideGlobalCounts: PropTypes.bool.isRequired,
  loadingFilteredCount: PropTypes.bool,
  activeField: PropTypes.object
};
