import React from 'react';

import DataRow from 'Mesa/Ui/DataRow';
import RowUtils from 'Mesa/Utils/RowUtils';
import HeadingRow from 'Mesa/Ui/HeadingRow';
import EmptyState from 'Mesa/Ui/EmptyState';

class TableBody extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    let { dispatch, state, currentPage, onPageChange, filteredRows } = this.props;
    let { columns, options } = state;
    let colSpan = columns.filter(column => !column.hidden).length
    let pages = RowUtils.getPageCount(filteredRows, options);
    let rows = RowUtils.getRowsByPage(filteredRows, currentPage, options);

    return (
      <div className="TableBody">
        <table cellSpacing="0" cellPadding="0">
          <tbody>
            <HeadingRow
              dispatch={dispatch}
              state={state}
            />
            {rows.length
              ? rows.map((row, idx) => (
                <DataRow
                  key={idx}
                  row={row}
                  dispatch={dispatch}
                  state={state}
                />
              ))
              : (
                <EmptyState
                  state={state}
                  dispatch={dispatch}
                />
              )
            }
          </tbody>
        </table>
      </div>
    );
  }
};

export default TableBody;
