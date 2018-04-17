import React from 'react';

import Utils from './Utils/Utils';
import Icon from './Components/Icon';

export const ColumnDefaults = {
  primary: false,
  searchable: true,
  sortable: true,
  resizeable: true,
  truncated: false,

  filterable: false,
  filterState: {
    enabled: false,
    visible: false,
    blacklist: []
  },

  hideable: true,
  hidden: false,

  disabled: false,
  type: 'text'
};

export const OptionsDefaults = {
  title: null,
  toolbar: true,
  inline: false,
  className: null,
  showCount: true,
  errOnOverflow: false,
  editableColumns: true,
  overflowHeight: '16em',
  searchPlaceholder: 'Search This Table',
  isRowSelected: (row, indexx) => {
    return false;
  }
};

export const UiStateDefaults = {
  searchQuery: null,
  filteredRowCount: 0,
  sort: {
    columnKey: null,
    direction: 'asc'
  },
  pagination: {
    currentPage: 1,
    totalPages: null,
    totalRows: null,
    rowsPerPage: 20
  }
};

export const ActionDefaults = [
  {
    element (rows) {
      let text = rows.length
        ? <span>Export <b>{rows.length}</b> rows as .csv</span>
        : <span>Export all rows as .csv</span>;
      let icon = <Icon fa="table" />;
      return (<button>{text} {icon}</button>);
    },
    callback (selectedRows, columns, rows) {
      const exportable = selectedRows.length ? selectedRows : rows;
      console.log(Utils.createCsv(exportable, columns));
    }
  }
];
