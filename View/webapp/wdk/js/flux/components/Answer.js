/**
 * Renders a set of WDK records in a table using the RecordTable component.
 *
 * This is a reusable component. A Controller-View should be responsible for
 * passing the Answer resource to this component. This is currently used by the
 * AnswerPage, but will also be used for Step results.
 */

import React from 'react';
import RecordTable from './RecordTable';
import RecordList from './RecordList';
import Loading from './Loading';

const PropTypes = React.PropTypes;

const Answer = React.createClass({

  // Some of the objects below can be further detailed. Some of this will be
  // obviated when we better incorporate JSON-schema and validate in the
  // ServiceAPI calls.
  propTypes: {
    questionName: PropTypes.string.isRequired,
    error: PropTypes.string,
    isLoading: PropTypes.bool,
    answer: PropTypes.shape({
      meta: PropTypes.object,
      records: PropTypes.array
    }),
    displayInfo: PropTypes.shape({
      pagination: PropTypes.object,
      attributes: PropTypes.array,
      tables: PropTypes.array,
      sorting: PropTypes.array
    }),
    answerEvents: PropTypes.shape({
      onSort: PropTypes.func,
      onMoveColumn: PropTypes.func,
      onChangeColumns: PropTypes.func,
      onNewPage: PropTypes.func,
      onAttributeClick: PropTypes.func
    }),
    format: PropTypes.string,
    position: PropTypes.number
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
    const { answer, error, isLoading, displayInfo, answerEvents, format, position } = this.props;
    const Records = format === 'list' ? RecordList : RecordTable;

    return (
      <div className="wdkAnswer">
        {isLoading ? <Loading/> : null}
        {error ? <div className="wdkAnswerError">{error}</div> : null}
        {answer && answer.records ? (
          <div>
            <AnswerOverview
              format={format}
              answer={answer}
              displayInfo={displayInfo}
              onSort={answerEvents.onSort}
              onToggleFormat={answerEvents.onToggleFormat}
            />
            <Records
              {...answer}
              {...answerEvents}
              displayInfo={displayInfo}
              position={position}
            />
          </div>
        ) : null}
      </div>
    );
  }

});

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
    const attr = _.find(this.props.answer.meta.attributes, { name: attrName });
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

export default Answer;
