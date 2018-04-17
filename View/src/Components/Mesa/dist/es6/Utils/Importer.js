'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _Utils = require('../Utils/Utils');

var _Utils2 = _interopRequireDefault(_Utils);

var _Defaults = require('../Defaults');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

var Importer = {
  homogenizeColumn: function homogenizeColumn() {
    var column = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
    var rows = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : [];
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};

    if (!column || !column.key) throw new Error('Cannot homogenize a column without a `.key`');
    if (!column.type && rows.length) {
      var isHtmlColumn = rows.some(function (row) {
        return _Utils2.default.isHtml(row[column.key]);
      });
      var isNumberColumn = rows.every(function (row) {
        return !row[column.key] || !row[column.key].length || _Utils2.default.isNumeric(column.key);
      });
      if (isHtmlColumn) column.type = 'html';else if (isNumberColumn) column.type = 'number';
    }
    if (column.primary) column.hideable = false;
    var optionalDefaults = options && 'columnDefaults' in options ? options.columnDefaults : {};
    return Object.assign({}, _Defaults.ColumnDefaults, optionalDefaults, column);
  },
  columnsFromRows: function columnsFromRows() {
    var rows = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : [];
    var options = arguments[1];

    if (!Array.isArray(rows)) return [];
    var keys = _Utils2.default.keysInList(rows, ['__id']);
    return keys.map(function (key) {
      return Importer.homogenizeColumn({ key: key }, rows, options);
    });
  },
  processColumns: function processColumns(columns, rows, options) {
    if (!Array.isArray(columns)) return null;
    return columns.map(function (column) {
      return Importer.homogenizeColumn(column, rows, options);
    }).filter(function (col) {
      return !col.disabled;
    });
  },
  columnsFromMap: function columnsFromMap() {
    var map = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
    var rows = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : [];
    var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};

    var columns = [];
    for (var key in map) {
      var column = { key: key };
      var value = map[key];
      switch (typeof value === 'undefined' ? 'undefined' : _typeof(value)) {
        case 'string':
          column.name = value;
        case 'object':
          column = Object.assign({}, column, value);
      }
      columns.push(column);
    };
    return Importer.processColumns(columns, rows, options);
  },
  importColumns: function importColumns(columns, rows, options) {
    if (!columns || (typeof columns === 'undefined' ? 'undefined' : _typeof(columns)) !== 'object' || Array.isArray(columns) && !columns.length) {
      if (Array.isArray(rows) && rows.length) return Importer.columnsFromRows(rows, options);else return [];
    } else {
      if (!Array.isArray(columns)) return Importer.columnsFromMap(columns, rows, options);
      return Importer.processColumns(columns, rows, options);
    }
  },
  importRows: function importRows(rows) {
    if (!rows || !Array.isArray(rows) || rows.some(function (row) {
      return (typeof row === 'undefined' ? 'undefined' : _typeof(row)) !== 'object';
    })) return [];
    rows = rows.map(function (row) {
      var __id = _Utils2.default.uid();
      return Object.assign({}, row, { __id: __id });
    });
    return rows;
  },
  importOptions: function importOptions() {
    var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

    var result = Object.assign({}, _Defaults.OptionsDefaults, options);
    return result;
  },
  importActions: function importActions(actions) {
    var options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

    if (!actions || !Array.isArray(actions) || actions.some(function (action) {
      return (typeof action === 'undefined' ? 'undefined' : _typeof(action)) !== 'object';
    })) return [];
    actions = options.useDefaultActions ? [].concat(_toConsumableArray(actions), _toConsumableArray(_Defaults.ActionDefaults)) : actions;
    actions = actions.map(function (action) {
      var __id = action.__id ? action.__id : _Utils2.default.uid();
      return Object.assign({}, action, { __id: __id });
    });
    return actions;
  }
};

exports.default = Importer;