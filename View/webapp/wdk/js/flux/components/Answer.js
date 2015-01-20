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
    var { questionName, answer, isLoading } = this.props;

    if (isLoading) {
      content = <div>Loading...</div>
    } else {
      content = <DataTable {...this.props.answer}/>
    }

    return (
      <div>
        <h1>{questionName}</h1>
        {content}
      </div>
    );
  }

});

export default Answer;
