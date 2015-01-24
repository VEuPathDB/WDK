/**
 * Renders a set of Records associated with an Answer in a table.
 */

import React from 'react';
import _ from 'lodash';
import RecordTable from './RecordTable';
import Loading from './Loading';

var PropTypes = React.PropTypes;

var Answer = React.createClass({

  propTypes: {
    questionName: PropTypes.string.isRequired,
    answerEvents: PropTypes.shape({
      onSort: PropTypes.func,
      onMoveColumn: PropTypes.func,
      onChangeColumns: PropTypes.func,
      onNewPage: PropTypes.func
    })
  },

  render() {
    var { answer, error, isLoading, displayInfo, answerEvents } = this.props;

    return (
      <div className="wdkAnswer">
        {isLoading ? <Loading/> : ''}
        {error ? <div className="wdkAnswerError">{error}</div> : ''}
        {!_.isEmpty(answer) ? <RecordTable {...answer} {...answerEvents} displayInfo={displayInfo}/> : ''}
      </div>
    );
  }

});

export default Answer;
