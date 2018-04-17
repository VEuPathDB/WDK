'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.intersectSelection = exports.mapListToIds = exports.isRowSelected = exports.removeIdFromSelection = exports.removeRowFromSelection = exports.addIdToSelection = exports.addRowToSelection = exports.selectionFromRows = exports.createSelection = undefined;

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _Errors = require('../Utils/Errors');

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

var createSelection = exports.createSelection = function createSelection() {
  var _selection = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : [];

  var selection = new Set(Array.isArray(_selection) ? _selection : []);
  return [].concat(_toConsumableArray(selection));
};

var selectionFromRows = exports.selectionFromRows = function selectionFromRows(rows, idAccessor) {
  if (typeof idAccessor !== 'function') return (0, _Errors.badType)('selectionFromRows', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
  var idList = mapListToIds(rows, idAccessor);
  return createSelection(idList);
};

var addRowToSelection = exports.addRowToSelection = function addRowToSelection(_selection, row, idAccessor) {
  if (typeof idAccessor !== 'function') return (0, _Errors.badType)('addRowToSelection', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
  var id = idAccessor(row);
  return addIdToSelection(_selection, id);
};

var addIdToSelection = exports.addIdToSelection = function addIdToSelection(_selection, id) {
  var selection = new Set(Array.isArray(_selection) ? _selection : []);
  selection.add(id);
  return [].concat(_toConsumableArray(selection));
};

var removeRowFromSelection = exports.removeRowFromSelection = function removeRowFromSelection(_selection, row, idAccessor) {
  if (typeof idAccessor !== 'function') return (0, _Errors.badType)('removeRowFromSelection', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
  var id = idAccessor(row);
  return removeIdFromSelection(_selection, id);
};

var removeIdFromSelection = exports.removeIdFromSelection = function removeIdFromSelection(_selection, id) {
  var selection = new Set(Array.isArray(_selection) ? _selection : []);
  selection.delete(id);
  return [].concat(_toConsumableArray(selection));
};

var isRowSelected = exports.isRowSelected = function isRowSelected(selection, row, idAccessor) {
  if (typeof idAccessor !== 'function') return (0, _Errors.badType)('isRowSelected', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
  var id = idAccessor(row);
  return selection.includes(id);
};

var mapListToIds = exports.mapListToIds = function mapListToIds(list, idAccessor) {
  if (typeof idAccessor !== 'function') return (0, _Errors.badType)('mapListToIds', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
  return list.map(idAccessor);
};

var intersectSelection = exports.intersectSelection = function intersectSelection(_selection, _list, idAccessor) {
  if (typeof idAccessor !== 'function') return (0, _Errors.badType)('intersectSelection', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
  var idList = mapListToIds(_list);
  var selection = new Set(Array.isArray(_selection) ? _selection : []);
  var intersection = new Set(idList);
  return [].concat(_toConsumableArray(selection)).filter(function (item) {
    return intersection.has(item);
  });
};