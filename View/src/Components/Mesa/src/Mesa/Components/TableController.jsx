import React from 'react';

import Store from 'Mesa/State/Store';
import Utils from 'Mesa/Utils/Utils';
import Icon from 'Mesa/Components/Icon';
import Table from 'Mesa/Components/Table';
import TableSearch from 'Mesa/Components/TableSearch';
import ColumnEditor from 'Mesa/Components/ColumnEditor';
import { autoWidth, autoFactor } from 'Mesa/Utils/Breakpoints';

class TableController extends React.Component {
  constructor (props) {
    super(props);
    let initialState = Store.getState();
    let { columns } = this.props;
    let hiddenColumns = columns.filter(column => column.hidden);
    this.state = Object.assign({}, initialState, { hiddenColumns });

    this.componentDidMount = this.componentDidMount.bind(this);
    this.getColumnList = this.getColumnList.bind(this);
    this.getRowList = this.getRowList.bind(this);
    this.searchRowsForQuery = this.searchRowsForQuery.bind(this);
  }

  componentDidMount () {
    Store.subscribe(() => {
      let state = Store.getState();
      if (state === this.state) return;
      this.setState(state);
    });
  }

  getColumnList () {
    let { columns } = this.props;
    let { filter, sort, hiddenColumns } = this.state;
    columns = columns
      .filter(column => !column.disabled)
      .filter(column => hiddenColumns.indexOf(column) < 0);
    return columns;
  }

  searchRowsForQuery (rows, searchQuery) {
    if (!searchQuery || !rows || !rows.length) return rows;
    let { columns } = this.props;
    let searchableKeys = columns.filter(col => col.searchable).map(col => col.key);
    return rows.filter(row => {
      let searchable = {};
      searchableKeys.forEach(key => key in row ? searchable[key] = row[key] : null);
      searchable = Utils.stringValue(searchable);
      return Utils.objectContainsQuery(searchable, searchQuery);
    });
  }

  sortRowsByColumn (rows, byColumn, ascending) {
    if (!byColumn || !rows || !rows.length) return rows;
    if (byColumn.sortable) {
      switch (byColumn.type) {
        case 'number':
        case 'numeric':
          rows = Utils.numberSort(rows, byColumn.key, ascending);
          break;
        case 'html':
        case 'text':
        default:
          rows = Utils.textSort(rows, byColumn.key, ascending);
      }
    }
    return rows;
  }

  getRowList () {
    let { rows, columns } = this.props;
    let { sort, searchQuery } = this.state;
    let { byColumn, ascending } = sort;

    rows = this.searchRowsForQuery(rows, searchQuery);
    rows = this.sortRowsByColumn(rows, byColumn, ascending);
    return rows;
  }

  render () {
    const { searchQuery, filter, sort, hiddenColumns } = this.state;
    const { title, columns } = this.props;
    const filteredRows = this.getRowList();
    const filteredColumns = this.getColumnList();

    return (
      <div className="TableController">
        <div className="Table-Toolbar">
          {title && <h1 className="Table-Title">{title}</h1>}
          <TableSearch />
          <ColumnEditor columns={columns}>
            <button><Icon fa={'columns'} />Add/Remove Columns</button>
          </ColumnEditor>
        </div>
        <Table rows={filteredRows} columns={filteredColumns} />
      </div>
    );
  }
};

export default TableController;
