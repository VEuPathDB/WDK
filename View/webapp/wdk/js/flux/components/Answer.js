/**
 * Renders a set of Records associated with an Answer in a table.
 */


import React from 'react';
import DataTable from './DataTable';

var PropTypes = React.PropTypes;

var Answer = React.createClass({

  propTypes: {
    questionName: PropTypes.string.isRequired
  },

  render() {
    var { questionName, answer: { meta, records } } = this.props;

    return (
      <div>
        <h1>{questionName}</h1>
        <DataTable {...this.props.answer}/>
      </div>
    );
  }

});

export default Answer;
