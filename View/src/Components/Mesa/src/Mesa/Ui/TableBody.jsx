import React from 'react';

import DataRow from 'Mesa/Ui/DataRow';
import HeadingRow from 'Mesa/Ui/HeadingRow';
import EmptyState from 'Mesa/Ui/EmptyState';

class TableBody extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    let { dispatch, state, filteredRows } = this.props;
    let rows = filteredRows;

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
