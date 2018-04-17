import React from 'react';

import DataCell from 'Mesa/Ui/DataCell';

class DataRow extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    const { row, state } = this.props;
    const { columns, options } = state;

    let rowStyle = !options.inline ? {} : { whiteSpace: 'nowrap' };

    return (
      <tr className="DataRow" style={rowStyle} >
        {columns.map(column => (
          <DataCell column={column} row={row} state={state} key={column.key} />
        ))}
      </tr>
    )
  }
};

export default DataRow;
