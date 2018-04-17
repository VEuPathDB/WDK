import React from 'react';

import DataRow from '../Ui/DataRow';
import { makeClassifier } from '../Utils/Utils';

const dataRowListClass = makeClassifier('DataRowList');

class DataRowList extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    const { props } = this;
    const { rows } = props;

    return (
      <tbody className={dataRowListClass()}>
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
