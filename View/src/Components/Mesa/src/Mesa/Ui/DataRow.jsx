import React from 'react';

import DataCell from 'Mesa/Ui/DataCell';

class DataRow extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    const { row, state } = this.props;
    const { columns } = state;

    return (
      <tr className="DataRow">
        {columns.map(column => (
          <DataCell column={column} row={row} key={column.key} />
        ))}
      </tr>
    )
  }
};

export default DataRow;
