import React from 'react';

import TableBody from 'Mesa/Ui/TableBody';
import RowUtils from 'Mesa/Utils/RowUtils';
import TableToolbar from 'Mesa/Ui/TableToolbar';
import ActionToolbar from 'Mesa/Ui/ActionToolbar';
import PaginationMenu from 'Mesa/Ui/PaginationMenu';
import { setEmptinessCulprit } from 'Mesa/State/Actions';

class TableController extends React.Component {
  constructor (props) {
    super(props);
    this.getFilteredRows = this.getFilteredRows.bind(this);
  }

  getFilteredRows () {
    let { state, dispatch } = this.props;
    let { rows, ui, columns } = state;
    let { searchQuery, sort, emptinessCulprit } = ui;

    if (!rows.length) {
      if (emptinessCulprit && emptinessCulprit !== 'nodata') dispatch(setEmptinessCulprit('nodata'));
      return [];
    }

    if (searchQuery && searchQuery.length)
      rows = RowUtils.searchRowsForQuery(rows, columns, searchQuery);
    if (!rows.length) {
      if (emptinessCulprit !== 'search') dispatch(setEmptinessCulprit('search'));
      return [];
    }

    rows = RowUtils.filterRowsByColumns(rows, columns);
    if (!rows.length) {
      if (emptinessCulprit !== 'filters') dispatch(setEmptinessCulprit('filters'));
      return [];
    }

    if (sort.byColumn) rows = RowUtils.sortRowsByColumn(rows, sort.byColumn, sort.ascending);

    return rows;
  }

  render () {
    let { state, dispatch, children } = this.props;
    let { ui, options, actions } = state;
    let { pagination } = ui;

    let filteredRows = this.getFilteredRows();

    let PageNav = () => !options.paginate ? null : (
      <PaginationMenu
        dispatch={dispatch}
        list={filteredRows}
        pagination={pagination}
      />
    );

    return (
      <div className="TableController">
        {!options.toolbar
          ? <div>{children}</div>
          : <TableToolbar
              state={state}
              dispatch={dispatch}
              filteredRows={filteredRows}>
              {children}
            </TableToolbar>
        }

        {!actions.length
          ? null
          : <ActionToolbar
              state={state}
              dispatch={dispatch}
              filteredRows={filteredRows}
            />
        }

        <PageNav />

        <TableBody
          state={state}
          dispatch={dispatch}
          filteredRows={filteredRows}
        />

        <PageNav />

      </div>
    );
  }
};

export default TableController;
