import React from 'react';

import DataRow from '../Ui/DataRow';

class DataRowList extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    const { props } = this;
    const { rows } = props;

    return (
      <tbody>
        {rows.map((row, rowIndex) => (
          <DataRow
            row={row}
            key={rowIndex}
            rowIndex={rowIndex}
            {...props}
          />
        ))}
      </tbody>
    );
  }
};

export default DataRowList;
