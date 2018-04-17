import React from 'react';

import HeadingRow from 'Mesa/Ui/HeadingRow';
import DataRow from 'Mesa/Ui/DataRow';

class TableBody extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    let { dispatch, state } = this.props;
    let { ui, rows, columns, options } = state;
    let EmptyState = 'div';

    return (
      <div className="TableBody">
        <table cellSpacing="0" cellPadding="0">
          <tbody>
            <HeadingRow
              dispatch={dispatch}
              state={state}
            />
            {rows.length && rows.map((row, idx) => (
              <DataRow
                key={idx}
                row={row}
                dispatch={dispatch}
                state={state}
              />
            ))}
            {/*
            <EmptyState
              state={state}
              dispatch={dispatch}
            /> */}
          </tbody>
        </table>
      </div>
    );
  }
};

export default TableBody;
