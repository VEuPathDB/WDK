import React from 'react';

import RowUtils from 'Mesa/Utils/RowUtils';
import TableToolbar from 'Mesa/Ui/TableToolbar';
import TableBody from 'Mesa/Ui/TableBody';

class TableController extends React.PureComponent {
  constructor (props) {
    super(props);
    this.getFilteredRows = this.getFilteredRows.bind(this);
  }

  getFilteredRows () {
    let { state } = this.props;
    let { rows, ui, columns } = state;
    let { searchQuery, sort } = ui;

    if (searchQuery && searchQuery.length)
      rows = RowUtils.searchRowsForQuery(rows, columns, searchQuery);
    if (sort.byColumn)
      rows = RowUtils.sortRowsByColumn(rows, sort.byColumn, sort.ascending);

    return rows;
  }

  render () {
    let { state, dispatch } = this.props;
    let filtered = { rows: this.getFilteredRows() };
    let filteredState = Object.assign({}, state, filtered);

    return (
      <div className="TableController">
        {state.options.toolbar && (
          <TableToolbar
            state={state}
            dispatch={dispatch}
          />
        )}
        <TableBody
          state={filteredState}
          dispatch={dispatch}
        />
      </div>
    );
  }
};

export default TableController;
