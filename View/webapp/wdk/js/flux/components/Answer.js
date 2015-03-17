/* global $ */
/**
 * Renders a set of WDK records in a table using the RecordTable component.
 *
 * This is a reusable component. A Controller-View should be responsible for
 * passing the Answer resource to this component. This is currently used by the
 * AnswerPage, but will also be used for Step results.
 */

import find from 'lodash/collection/find';
import React from 'react';
import RecordTable from './RecordTable';
import RecordList from './RecordList';

const PropTypes = React.PropTypes;

/**
 * Calculate the offset of `node` relative to the top of the document.
 */
const getOffsetTop = (node, sum = 0) => node.offsetTop === 0
  ? sum
  : getOffsetTop(node.offsetParent, sum + node.offsetTop);

const Answer = React.createClass({

  // Some of the objects below can be further detailed. Some of this will be
  // obviated when we better incorporate JSON-schema and validate in the
  // ServiceAPI calls.
  propTypes: {
    answer: PropTypes.shape({
      meta: PropTypes.object,
      records: PropTypes.array
    }).isRequired,
    displayInfo: PropTypes.shape({
      pagination: PropTypes.object,
      attributes: PropTypes.array,
      tables: PropTypes.array,
      sorting: PropTypes.array
    }).isRequired,
    answerEvents: PropTypes.shape({
      onSort: PropTypes.func,
      onMoveColumn: PropTypes.func,
      onChangeColumns: PropTypes.func,
      onNewPage: PropTypes.func,
      onAttributeClick: PropTypes.func
    }).isRequired,
    format: PropTypes.string.isRequired,
    position: PropTypes.number
  },

  getInitialState() {
    return {
      height: 0
    };
  },

  componentDidMount() {
    this._updateHeight();
    $(window).on('resize', this._updateHeight);
  },

  componentWillUnmount() {
    $(window).off('resize', this._updateHeight);
  },

  handleFilter(e) {
    e.preventDefault();
    var value = this.refs.filterInput.getDOMNode().value;
    this.props.answerEvents.onFilter(value);
  },

  /**
   * Render various aspects of state:
   *   - Show spinner of loading.
   *   - Show error, if there is one.
   *   - Render table if answer is not empty (similar to Java's definition).
   *
   * XXX: Should loading and error states be handled globally? The AppStore can
   *      store this state and AppPage.js can display the spinner or error
   *      message. This could potentially reduce some boilerplate and also
   *      provide a good mechanism for displaying otherwise unhandled errors.
   */
  render() {
    const {
      answer,
      recordClass,
      displayInfo,
      answerEvents,
      format,
      position,
      filterTerm,
      filteredRecords
    } = this.props;

    const displayName = recordClass.get('displayName');
    const meta = answer.get('meta');
    const pagination = displayInfo.get('pagination');
    const firstRec = pagination.get('offset') + 1;
    const lastRec = Math.min(pagination.get('offset') + pagination.get('numRecords'),
                             meta.get('count'), filteredRecords.size);
    const Records = format === 'list' ? RecordList : RecordTable;

    return (
      <div className="wdk-Answer">
        <div className="wdk-Answer-filter">
          <form onSubmit={this.handleFilter}>
            <input
              ref="filterInput"
              style={{ padding: '.5em', width: '25em'}}
              defaultValue={filterTerm}
              placeholder={`Filter ${displayName} records`}
            />
            <button className="wdk-Answer-filterButton">
              <i className="fa fa-search fa-lg"/>
            </button>
          </form>
        </div>
        <p>
          Showing {firstRec} - {lastRec} of {meta.get('count')} {displayName} records
        </p>
        <Records
          ref="records"
          height={this.state.height}
          meta={meta}
          records={filteredRecords}
          {...answerEvents}
          displayInfo={displayInfo}
          position={position}
        />
      </div>
    );
  },

  _updateHeight() {
    if (this.refs.records) {
      const node = this.refs.records.getDOMNode();
      const nodeOffsetTop = getOffsetTop(node);
      const calculatedHeight = window.innerHeight - nodeOffsetTop - 20;
      const minHeight = 335;
      this.setState({
        height: Math.max(calculatedHeight, minHeight)
      });
    }
  }

});

/**
 * Overview information with filter box and sorting that is common to both
 * table and list views.
 */
/*
const AnswerOverview = React.createClass({

  propTypes: {
    format: PropTypes.string,
    answer: PropTypes.object,
    displayInfo: PropTypes.object,
    onSort: PropTypes.func,
    onToggleFormat: PropTypes.func
  },

  handleToggleFormat(event) {
    event.preventDefault();
    this.props.onToggleFormat();
  },

  handleSort() {
    const { sortSelect, directionSelect } = this.refs;
    const attrName = sortSelect.getDOMNode().value;
    const sortDir = directionSelect.getDOMNode().value;
    const attr = find(this.props.answer.meta.attributes, { name: attrName });
    this.props.onSort(attr, sortDir);
  },

  render() {
    const {format, answer, displayInfo} = this.props;
    const {meta} = answer;
    const {pagination, sorting} = displayInfo;
    const sortSpec = sorting[0];
    const firstRec = pagination.offset + 1;
    const lastRec = Math.min(pagination.offset + pagination.numRecords,
                           meta.count);
    const newFormat = format === 'table' ? 'List' : 'Table';
    return (
      <div>
        <input style={{ padding: '.5em', width: '25em'}}
          placeholder="Find Datasets: Not currently working"/>

        <b> Order by: </b>
        <select ref="sortSelect" onChange={this.handleSort} value={sortSpec.attributeName}>
          {meta.attributes.map(attr => {
            return (
              <option value={attr.name}>{attr.displayName}</option>
              );
          })}
        </select>
        <b> direction: </b>
        <select ref="directionSelect" onChange={this.handleSort} value={sortSpec.direction}>
          <option value="ASC">Ascending</option>
          <option value="DESC">Descending</option>
        </select>
        <p>
          Showing {firstRec} - {lastRec} of {meta.count} {meta.class} records
          <a style={{marginLeft: '1em'}} href="#" onClick={this.handleToggleFormat}>Show as {newFormat}</a>
        </p>
      </div>
    );
  }

});
*/

export default Answer;
