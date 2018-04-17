'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.intersectSelection = exports.mapListToIds = exports.isRowSelected = exports.removeRowFromSelection = exports.addRowToSelection = exports.selectionFromRows = exports.createSelection = undefined;

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _Errors = require('../Utils/Errors');

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

var createSelection = exports.createSelection = function createSelection() {
  var _selection = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : [];

  var selection = new Set(_selection);
  return [].concat(_toConsumableArray(selection));
};

var selectionFromRows = exports.selectionFromRows = function selectionFromRows(rows, idAccessor) {
  if (typeof idAccessor !== 'function') return (0, _Errors.badType)('selectionFromRows', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
  var idList = mapListToIds(rows, idAccessor);
  return createSelection(idList);
};

var addRowToSelection = exports.addRowToSelection = function addRowToSelection(_selection, row, idAccessor) {
  if (typeof idAccessor !== 'function') return (0, _Errors.badType)('addRowToSelection', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
  var selection = new Set(_selection);
  var id = idAccessor(row);
  selection.add(id);
  return [].concat(_toConsumableArray(selection));
};

var removeRowFromSelection = exports.removeRowFromSelection = function removeRowFromSelection(_selection, row, idAccessor) {
  if (typeof idAccessor !== 'function') return (0, _Errors.badType)('removeRowFromSelection', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
  var selection = new Set(_selection);
  var id = idAccessor(row);
  selection.delete(id);
  return [].concat(_toConsumableArray(selection));
};

var isRowSelected = exports.isRowSelected = function isRowSelected(_selection, row, idAccessor) {
  if (typeof idAccessor !== 'function') return (0, _Errors.badType)('isRowSelected', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
  var selection = new Set(_selection);
  var id = idAccessor(row);
  return selection.has(id);
};

var mapListToIds = exports.mapListToIds = function mapListToIds(list, idAccessor) {
  if (typeof idAccessor !== 'function') return (0, _Errors.badType)('mapListToIds', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
  return list.map(idAccessor);
};

var intersectSelection = exports.intersectSelection = function intersectSelection(_selection, _list, idAccessor) {
  if (typeof idAccessor !== 'function') return (0, _Errors.badType)('intersectSelection', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
  var idList = mapListToIds(_list);
  var selection = new Set(_selection);
  var intersection = new Set(idList);
  return [].concat(_toConsumableArray(selection)).filter(function (item) {
    return intersection.has(item);
  });
};

/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */
/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */

var MesaSelection = function () {
  function MesaSelection(idAccessor) {
    _classCallCheck(this, MesaSelection);

    if (typeof idAccessor !== 'function') return (0, _Errors.badType)('MesaSelection:constructor', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
    this.idAccessor = idAccessor;
    this.selection = new Set();

    this.getSelection = this.getSelection.bind(this);
    this.onRowSelect = this.onRowSelect.bind(this);
    this.onMultiRowSelect = this.onMultiRowSelect.bind(this);
    this.onRowDeselect = this.onRowDeselect.bind(this);
    this.onMultiRowDeselect = this.onMultiRowDeselect.bind(this);
    this.isRowSelected = this.isRowSelected.bind(this);
  }

  _createClass(MesaSelection, [{
    key: 'getSelection',
    value: function getSelection() {
      return this.selection;
    }
  }, {
    key: 'onRowSelect',
    value: function onRowSelect(row) {
      var id = this.idAccessor(row);
      this.selection.add(id);
      return this.selection;
    }
  }, {
    key: 'onMultiRowSelect',
    value: function onMultiRowSelect(rows) {
      var _this = this;

      rows.forEach(function (row) {
        return _this.selection.add(_this.idAccessor(row));
      });
      return this.selection;
    }
  }, {
    key: 'onRowDeselect',
    value: function onRowDeselect(row) {
      var id = this.idAccessor(row);
      this.selection.delete(id);
      return this.selection;
    }
  }, {
    key: 'onMultiRowDeselect',
    value: function onMultiRowDeselect(rows) {
      var _this2 = this;

      rows.forEach(function (row) {
        return _this2.selection.delete(_this2.idAccessor(row));
      });
      return this.selection;
    }
  }, {
    key: 'intersectWith',
    value: function intersectWith(rows) {
      var _this3 = this;

      var rowIds = rows.map(this.idAccessor);
      this.selection.forEach(function (row) {
        return rowIds.includes(row) ? null : _this3.selection.delete(row);
      });
      return this.selection;
    }
  }, {
    key: 'isRowSelected',
    value: function isRowSelected(row) {
      var id = this.idAccessor(row);
      return this.selection.has(id);
    }
  }]);

  return MesaSelection;
}();

;

exports.default = MesaSelection;