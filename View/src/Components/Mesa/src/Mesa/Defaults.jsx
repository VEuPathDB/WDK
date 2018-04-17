import React from 'react';

import Utils from 'Mesa/Utils/Utils';
import Icon from 'Mesa/Components/Icon';

export const ColumnDefaults = {
  primary: false,
  searchable: true,
  sortable: true,
  editable: false,
  resizeable: true,
  truncated: true,

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
  search: true,
  inline: false,
  className: null,
  paginate: false,
  rowsPerPage: 20,
  selectAllPages: false,
  editableColumns: true,
  overflowHeight: '16em',
  useDefaultActions: false,
  searchPlaceholder: 'Search This Table'
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
