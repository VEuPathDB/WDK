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
        {rows.map((row, idx) => (
          <DataRow
            key={idx}
            row={row}
            rowIndex={idx}
            {...props}
          />
        ))}
      </tbody>
    );
  }
};

export default DataRowList;
