import React from 'react';

import DataRow from '../Ui/DataRow';
import RowUtils from '../Utils/RowUtils';
import HeadingRow from '../Ui/HeadingRow';
import EmptyState from '../Ui/EmptyState';
import PaginatedList from '../Ui/PaginatedList';

class TableBody extends React.Component {
  constructor (props) {
    super(props);
    this.renderDataRow = this.renderDataRow.bind(this);
  }

  renderDataRow (row, idx) {
    const { state, dispatch } = this.props;
    return (
      <DataRow
        key={row.__id}
        row={row}
        state={state}
        dispatch={dispatch}
      />
    );
  }

  render () {
    let { dispatch, state, filteredRows } = this.props;
    let { columns, options, ui } = state;
    let { pagination } = ui;

    let content;
    if (!filteredRows.length)
      content = (<tbody><EmptyState state={state} dispatch={dispatch} /></tbody>);
    else if (!options.paginate)
      content = (<tbody>{filteredRows.map(this.renderDataRow)}</tbody>);
    else
      content = (
        <PaginatedList
          container="tbody"
          list={filteredRows}
          pagination={pagination}
          renderItem={this.renderDataRow}
        />
      );

    return (
      <div className="TableBody">
        <table cellSpacing="0" cellPadding="0">
          <tbody>
            <HeadingRow
              dispatch={dispatch}
              state={state}
              filteredRows={filteredRows}
            />
          </tbody>
          {content}
        </table>
      </div>
    );
  }
};

export default TableBody;
