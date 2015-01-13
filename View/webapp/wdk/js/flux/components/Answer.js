/**
 * Renders a set of Records associated with an Answer in a table.
 */


import React from 'react';

var PropTypes = React.PropTypes;


export default React.createClass({

  propTypes: {
    questionName: PropTypes.string.isRequired
  },

  render() {
    var { questionName, answer } = this.props;
    var { meta, records } = answer;

    return (
      <div>
        <h1>{questionName}</h1>
        <table>
          <thead>
            {meta.attributes.map(attribute => {
              return <tr key={attribute.name}><th>{attribute.displayName}</th></tr>
            })}
          </thead>
          <tbody>
            {records.map(record => {
              return (
                <tr key={record.id}>
                  {record.attributes.map(attribute => {
                    return <td>{attribute.value}</td>
                  })}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    );
  }

});
