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
    var content;
    var { answer, questionName, error, isLoading } = this.props;

    if (isLoading) return <div>Loading...</div>;
    else if (error) return <div>There was an error: {error}</div>;
    else return <DataTable {...answer}/>;
  }

});

export default Answer;
