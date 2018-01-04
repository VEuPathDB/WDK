import React from 'react';
import PropTypes from 'prop-types';
import { map, partial } from 'lodash';

import { getFilterValueDisplay } from '../../../utils/FilterServiceUtils';

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
  handleFilterSelectClick(filter, event) {
    event.preventDefault();
    this.props.onFilterSelect(filter.field);
  }

/**
 * @param {Filter} filter
 * @param {Event} event
 */
  handleFilterRemoveClick(filter, event) {
    event.preventDefault();
    this.props.onFilterRemove(filter);
  }

  render() {
    var { fields, filters, selectedField } = this.props;

    return (
      <div className="filter-items-wrapper">
        {this.props.renderSelectionInfo(this.props)}
        <ul style={{display: 'inline-block', paddingLeft: '.2em'}} className="filter-items">
          {map(filters, filter => {
            var className = selectedField && selectedField.term === filter.field ? 'selected' : '';
            var handleSelectClick = partial(this.handleFilterSelectClick, filter);
            var handleRemoveClick = partial(this.handleFilterRemoveClick, filter);
            var field = fields.get(filter.field);
            var display = getFilterValueDisplay(field, filter);

            return (
              <li key={filter.field} className={className}>
                <div className="ui-corner-all">
                  <a className="select"
                    onClick={handleSelectClick}
                    href={'#' + filter.field}
                    title={display}>{field.display}</a>
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
        </ul>
      </div>
    );
  }

}

FilterList.propTypes = {
  onFilterSelect: PropTypes.func.isRequired,
  onFilterRemove: PropTypes.func.isRequired,
  fields: PropTypes.instanceOf(Map).isRequired,
  filters: PropTypes.array.isRequired,
  selectedField: PropTypes.object,
  renderSelectionInfo: PropTypes.func
};

FilterList.defaultProps = {
  renderSelectionInfo(parentProps) {
    const { filteredDataCount, dataCount } = parentProps;
    return(
      <span style={{ fontWeight: 'bold', padding: '.6em 0 .8em 0', display: 'inline-block' }}>
        {filteredDataCount} of {dataCount} selected
      </span>
    );
  }
};

