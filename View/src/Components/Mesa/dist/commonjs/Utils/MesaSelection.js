'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _Errors = require('../Utils/Errors');

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var MesaSelection = function () {
  function MesaSelection(idAccessor) {
    _classCallCheck(this, MesaSelection);

    if (typeof idAccessor !== 'function') return (0, _Errors.badType)('selectionFactory', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
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
      var id = idAccessor(row);
      this.selection.add(id);
      return this.selection;
    }
  }, {
    key: 'onMultiRowSelect',
    value: function onMultiRowSelect(rows) {
      var _this = this;

      rows.forEach(function (row) {
        return _this.selection.add(idAccessor(row));
      });
      return this.selection;
    }
  }, {
    key: 'onRowDeselect',
    value: function onRowDeselect(row) {
      var id = idAccessor(row);
      this.selection.delete(id);
      return this.selection;
    }
  }, {
    key: 'onMultiRowDeselect',
    value: function onMultiRowDeselect(rows) {
      var _this2 = this;

      rows.forEach(function (row) {
        return _this2.selection.delete(idAccessor(row));
      });
      return this.selection;
    }
  }, {
    key: 'intersectWith',
    value: function intersectWith(rows) {
      var _this3 = this;

      var rowIds = rows.map(idAccessor);
      this.selection.forEach(function (row) {
        if (!rowIds.includes(row)) _this3.selection.delete(row);
      });
    }
  }, {
    key: 'isRowSelected',
    value: function isRowSelected(row) {
      var id = idAccessor(row);
      return this.selection.has(id);
    }
  }]);

  return MesaSelection;
}();

;

exports.default = MesaSelection;