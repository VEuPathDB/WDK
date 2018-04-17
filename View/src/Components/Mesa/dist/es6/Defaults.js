'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.UiStateDefaults = exports.OptionsDefaults = exports.ColumnDefaults = undefined;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Icon = require('./Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var ColumnDefaults = exports.ColumnDefaults = {
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

var OptionsDefaults = exports.OptionsDefaults = {
  title: null,
  toolbar: true,
  inline: false,
  className: null,
  showCount: true,
  errOnOverflow: false,
  editableColumns: true,
  overflowHeight: '16em',
  searchPlaceholder: 'Search This Table',
  isRowSelected: function isRowSelected(row, indexx) {
    return false;
  }
};

var UiStateDefaults = exports.UiStateDefaults = {
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