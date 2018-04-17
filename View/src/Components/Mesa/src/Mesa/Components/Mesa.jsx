import React from 'react';

import 'Mesa/Style/Mesa.scss';

import Utils from 'Mesa/Utils/Utils';
import TableController from 'Mesa/Components/TableController';

class Mesa extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    let { rows, columns, title } = this.props;

    if (!columns || typeof columns !== 'object') {
      columns = Utils.columnsFromRows(rows);
    } else if (!Array.isArray(columns)) {
      columns = Utils.columnsFromMap(columns);
    } else {
      columns = Utils.processColumns(columns);
    };

    return (
      <div>
        <TableController rows={rows} title={title} columns={columns} />
      </div>
    );
  }
};

export default Mesa;
