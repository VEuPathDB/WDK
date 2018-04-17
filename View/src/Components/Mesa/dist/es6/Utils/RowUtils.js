'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _Utils = require('../Utils/Utils');

var _Utils2 = _interopRequireDefault(_Utils);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var RowUtils = {
  searchRowsForQuery: function searchRowsForQuery(rows, columns, searchQuery) {
    if (!searchQuery || !rows || !rows.length) return rows;
    var searchableKeys = columns.filter(function (col) {
      return col.searchable;
    }).map(function (col) {
      return col.key;
    });
    return rows.filter(function (row) {
      var searchable = {};
      searchableKeys.forEach(function (key) {
        return key in row ? searchable[key] = row[key] : null;
      });
      searchable = _Utils2.default.stringValue(searchable);
      return _Utils2.default.objectContainsQuery(searchable, searchQuery);
    });
  },
  sortRowsByColumn: function sortRowsByColumn(rows, byColumn, ascending) {
    if (!byColumn || !rows || !rows.length) return rows;
    if (byColumn.sortable) {
      switch (byColumn.type) {
        case 'number':
        case 'numeric':
          rows = _Utils2.default.numberSort(rows, byColumn.key, ascending);
          break;
        case 'html':
        case 'text':
        default:
          rows = _Utils2.default.textSort(rows, byColumn.key, ascending);
      }
    }
    return rows;
  },
  filterRowsByColumns: function filterRowsByColumns(rows, columns) {
    var filters = columns.filter(function (column) {
      return column.filterable && column.filterState.enabled;
    }).map(function (_ref) {
      var key = _ref.key,
          filterState = _ref.filterState;
      var blacklist = filterState.blacklist;

      return { key: key, blacklist: blacklist };
    });
    return rows.filter(function (row) {
      var result = !filters.some(function (_ref2) {
        var key = _ref2.key,
            blacklist = _ref2.blacklist;
        return blacklist.includes(row[key]);
      });
      return result;
    });
  }
};

exports.default = RowUtils;