/**
 * Renders a set of WDK records in a table using the RecordTable component.
 *
 * This is a reusable component. A Controller-View should be responsible for
 * passing the Answer resource to this component. This is currently used by the
 * AnswerPage, but will also be used for Step results.
 */

import React from 'react';
import RecordTable from './RecordTable';
import Loading from './Loading';

var PropTypes = React.PropTypes;

var Answer = React.createClass({

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
      onNewPage: PropTypes.func
    })
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
    var { answer, error, isLoading, displayInfo, answerEvents } = this.props;

    return (
      <div className="wdkAnswer">
        {isLoading ? <Loading/> : ''}
        {error ? <div className="wdkAnswerError">{error}</div> : ''}
        {answer && answer.records ? <RecordTable {...answer} {...answerEvents} displayInfo={displayInfo}/> : ''}
      </div>
    );
  }

});

export default Answer;
