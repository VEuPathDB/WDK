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

  handleMoveColumn(column, newPosition) {
    // TODO Trigger record action
    console.log(`You moved ${column} to column #${newPosition}.`);
  },

  render() {
    var content;
    var { answer, questionName, error, isLoading } = this.props;

    if (isLoading) return <div>Loading...</div>;
    else if (error) return <div>There was an error: {error}</div>;
    else if (answer) return <DataTable {...answer} onMoveColumn={this.handleMoveColumn}/>;
    else return <div>There is nothing to show</div>;
  }

});

export default Answer;
