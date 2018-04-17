import React from 'react';

import Store from 'Mesa/State/Store';
import Row from 'Mesa/Components/Row';
import HeadingRow from 'Mesa/Components/HeadingRow';

class Table extends React.Component {
  constructor (props) {
    super(props);
    this.makeRow = this.makeRow.bind(this);
  }

  makeRow (row, index) {
    const { columns } = this.props;
    return (<Row columns={columns} row={row} key={index} />);
  }

  rowShouldAppear (row, filter) {
    if (!filter || !filter.byColumn) return true;
    let { byColumn, valueWhitelist } = filter;
    let value = row[byColumn.key];
    return valueWhitelist.includes(value);
  }

  render () {
    const { rows, columns } = this.props;
    const { filter } = Store.getState();
    const emptyRows = !Array.isArray(rows) || !rows.length;
    const rowList = emptyRows ? null : rows
      .filter(row => this.rowShouldAppear(row, filter))
      .map(this.makeRow);

    return (
      <div className="Table-Wrapper">
        <table className="Table" cellSpacing="0" cellPadding="0">
          <tbody>
            <HeadingRow rows={rows} columns={columns} />
            {rowList}
          </tbody>
        </table>
      </div>
    );
  }
};

export default Table;
