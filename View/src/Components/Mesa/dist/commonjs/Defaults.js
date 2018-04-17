'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.ActionDefaults = exports.UiStateDefaults = exports.OptionsDefaults = exports.ColumnDefaults = undefined;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Utils = require('./Utils/Utils');

var _Utils2 = _interopRequireDefault(_Utils);

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

var ActionDefaults = exports.ActionDefaults = [{
  element: function element(rows) {
    var text = rows.length ? _react2.default.createElement(
      'span',
      null,
      'Export ',
      _react2.default.createElement(
        'b',
        null,
        rows.length
      ),
      ' rows as .csv'
    ) : _react2.default.createElement(
      'span',
      null,
      'Export all rows as .csv'
    );
    var icon = _react2.default.createElement(_Icon2.default, { fa: 'table' });
    return _react2.default.createElement(
      'button',
      null,
      text,
      ' ',
      icon
    );
  },
  callback: function callback(selectedRows, columns, rows) {
    var exportable = selectedRows.length ? selectedRows : rows;
    console.log(_Utils2.default.createCsv(exportable, columns));
  }
}];