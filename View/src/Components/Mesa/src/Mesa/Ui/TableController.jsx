import React from 'react';

import TableBody from 'Mesa/Ui/TableBody';
import RowUtils from 'Mesa/Utils/RowUtils';
import Pagination from 'Mesa/Ui/Pagination';
import TableToolbar from 'Mesa/Ui/TableToolbar';
import { setEmptinessCulprit } from 'Mesa/State/Actions';

class TableController extends React.PureComponent {
  constructor (props) {
    super(props);
    this.state = { currentPage: 1 };
    this.getFilteredRows = this.getFilteredRows.bind(this);
    this.handlePageChange = this.handlePageChange.bind(this);
    this.renderPageNav = this.renderPageNav.bind(this);
  }

  getFilteredRows () {
    let { state, dispatch } = this.props;
    let { rows, ui, columns } = state;
    let { searchQuery, sort, emptinessCulprit } = ui;

    if (!rows.length) {
      if (emptinessCulprit !== 'nodata') dispatch(setEmptinessCulprit('nodata'));
      return rows;
    }

    if (searchQuery && searchQuery.length)
      rows = RowUtils.searchRowsForQuery(rows, columns, searchQuery);
    if (!rows.length) {
      if (emptinessCulprit !== 'search') dispatch(setEmptinessCulprit('search'));
      return rows;
    }

    if (columns.some(column => column.filterState.enabled))
      rows = RowUtils.filterRowsByColumns(rows, columns);
    if (!rows.length) {
      if (emptinessCulprit !== 'filters') dispatch(setEmptinessCulprit('filters'));
      return rows;
    }

    if (sort.byColumn)
      rows = RowUtils.sortRowsByColumn(rows, sort.byColumn, sort.ascending);

    return rows;
  }

  handlePageChange (currentPage) {
    let { state } = this.props;
    let { options } = state;
    let filteredRows = this.getFilteredRows();
    let pageCount = RowUtils.getPageCount(filteredRows, options);
    if (currentPage > pageCount) currentPage = pageCount;
    if (currentPage < 1) currentPage = 1;
    this.setState({ currentPage });
  }

  renderPageNav (filteredRows) {
    let { state, dispatch, children } = this.props;
    let { options } = state;
    let { currentPage } = this.state;
    let pageCount = RowUtils.getPageCount(filteredRows, options);
    if (!options.paginate || pageCount <= 1) return null;
    return (
      <Pagination
        pages={pageCount}
        currentPage={currentPage}
        onPageChange={this.handlePageChange}
      />
    );
  }

  render () {
    let { state, dispatch, children } = this.props;
    let { options } = state;
    let { currentPage } = this.state;

    let filteredRows = this.getFilteredRows();
    let pageRows = RowUtils.getRowsByPage(filteredRows, currentPage, options);
    let pageCount = RowUtils.getPageCount(filteredRows, options);
    let pageNav = this.renderPageNav(filteredRows);

    return (
      <div className="TableController">
        {!options.toolbar
          ? <div>{children}</div>
          : <TableToolbar
              state={state}
              dispatch={dispatch}
              filteredRows={filteredRows}
            >
              {children}
            </TableToolbar>
        }
        {pageNav}
        <TableBody
          state={state}
          dispatch={dispatch}
          filteredRows={pageRows}
        />
        {pageNav}
      </div>
    );
  }
};

export default TableController;
