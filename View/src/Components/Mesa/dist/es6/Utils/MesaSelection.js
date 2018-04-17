'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.intersectSelection = exports.mapListToIds = exports.isRowSelected = exports.removeIdsFromSelection = exports.removeIdFromSelection = exports.removeRowFromSelection = exports.addIdsToSelection = exports.addIdToSelection = exports.addRowToSelection = exports.selectionFromRows = exports.createSelection = undefined;

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _Errors = require('../Utils/Errors');

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

var createSelection = exports.createSelection = function createSelection() {
  var _selection = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : [];

  if (!Array.isArray(_selection)) return (0, _Errors.badType)('addIdToSelection', '_selection', 'array', typeof _selection === 'undefined' ? 'undefined' : _typeof(_selection));
  var selection = new Set(_selection);
  return [].concat(_toConsumableArray(selection));
};

var selectionFromRows = exports.selectionFromRows = function selectionFromRows(rows, idAccessor) {
  if (typeof idAccessor !== 'function') return (0, _Errors.badType)('selectionFromRows', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
  var idList = mapListToIds(rows, idAccessor);
  return createSelection(idList);
};

var addRowToSelection = exports.addRowToSelection = function addRowToSelection(_selection, row, idAccessor) {
  if (!Array.isArray(_selection)) return (0, _Errors.badType)('addIdToSelection', '_selection', 'array', typeof _selection === 'undefined' ? 'undefined' : _typeof(_selection));
  if (typeof idAccessor !== 'function') return (0, _Errors.badType)('addRowToSelection', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
  var id = idAccessor(row);
  return addIdToSelection(_selection, id);
};

var addIdToSelection = exports.addIdToSelection = function addIdToSelection() {
  var _selection = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : [];

  var id = arguments[1];

  if (!Array.isArray(_selection)) return (0, _Errors.badType)('addIdToSelection', '_selection', 'array', typeof _selection === 'undefined' ? 'undefined' : _typeof(_selection));
  var selection = new Set(_selection);
  selection.add(id);
  return [].concat(_toConsumableArray(selection));
};

var addIdsToSelection = exports.addIdsToSelection = function addIdsToSelection() {
  var _selection = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : [];

  var ids = arguments[1];

  if (!Array.isArray(_selection)) return (0, _Errors.badType)('addIdToSelection', '_selection', 'array', typeof _selection === 'undefined' ? 'undefined' : _typeof(_selection));
  if (!Array.isArray(ids)) return (0, _Errors.badType)('addIdToSelection', 'ids', 'array', typeof ids === 'undefined' ? 'undefined' : _typeof(ids));
  var selection = new Set([].concat(_toConsumableArray(_selection), _toConsumableArray(ids)));
  return [].concat(_toConsumableArray(selection));
};

var removeRowFromSelection = exports.removeRowFromSelection = function removeRowFromSelection(_selection, row, idAccessor) {
  if (!Array.isArray(_selection)) return (0, _Errors.badType)('removeRowFromSelection', '_selection', 'array', typeof _selection === 'undefined' ? 'undefined' : _typeof(_selection));
  if (typeof idAccessor !== 'function') return (0, _Errors.badType)('removeRowFromSelection', 'idAccessor', 'function', typeof idAccessor === 'undefined' ? 'undefined' : _typeof(idAccessor));
  var id = idAccessor(row);
  return removeIdFromSelection(_selection, id);
};

var removeIdFromSelection = exports.removeIdFromSelection = function removeIdFromSelection(_selection, id) {
  if (!Array.isArray(_selection)) return (0, _Errors.badType)('removeIdFromSelection', '_selection', 'array', typeof _selection === 'undefined' ? 'undefined' : _typeof(_selection));
  var selection = new Set(Array.isArray(_selection) ? _selection : []);
  selection.delete(id);
  return [].concat(_toConsumableArray(selection));
};

var removeIdsFromSelection = exports.removeIdsFromSelection = function removeIdsFromSelection(_selection, ids) {
  if (!Array.isArray(_selection)) return (0, _Errors.badType)('removeIdsFromSelection', '_selection', 'array', typeof _selection === 'undefined' ? 'undefined' : _typeof(_selection));
  if (!Array.isArray(ids)) return (0, _Errors.badType)('removeIdsFromSelection', 'ids', 'array', typeof ids === 'undefined' ? 'undefined' : _typeof(ids));
  var selection = new Set(_selection);
  var removable = new Set(ids);
  return [].concat(_toConsumableArray(selection)).filter(function (item) {
    return !removable.has(item);
  });
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VdGlscy9NZXNhU2VsZWN0aW9uLmpzIl0sIm5hbWVzIjpbImNyZWF0ZVNlbGVjdGlvbiIsIl9zZWxlY3Rpb24iLCJBcnJheSIsImlzQXJyYXkiLCJzZWxlY3Rpb24iLCJTZXQiLCJzZWxlY3Rpb25Gcm9tUm93cyIsInJvd3MiLCJpZEFjY2Vzc29yIiwiaWRMaXN0IiwibWFwTGlzdFRvSWRzIiwiYWRkUm93VG9TZWxlY3Rpb24iLCJyb3ciLCJpZCIsImFkZElkVG9TZWxlY3Rpb24iLCJhZGQiLCJhZGRJZHNUb1NlbGVjdGlvbiIsImlkcyIsInJlbW92ZVJvd0Zyb21TZWxlY3Rpb24iLCJyZW1vdmVJZEZyb21TZWxlY3Rpb24iLCJkZWxldGUiLCJyZW1vdmVJZHNGcm9tU2VsZWN0aW9uIiwicmVtb3ZhYmxlIiwiZmlsdGVyIiwiaGFzIiwiaXRlbSIsImlzUm93U2VsZWN0ZWQiLCJpbmNsdWRlcyIsImxpc3QiLCJtYXAiLCJpbnRlcnNlY3RTZWxlY3Rpb24iLCJfbGlzdCIsImludGVyc2VjdGlvbiJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7O0FBQUE7Ozs7QUFFTyxJQUFNQSw0Q0FBa0IsU0FBbEJBLGVBQWtCLEdBQXFCO0FBQUEsTUFBcEJDLFVBQW9CLHVFQUFQLEVBQU87O0FBQ2xELE1BQUksQ0FBQ0MsTUFBTUMsT0FBTixDQUFjRixVQUFkLENBQUwsRUFDRSxPQUFPLHFCQUFRLGtCQUFSLEVBQTRCLFlBQTVCLEVBQTBDLE9BQTFDLFNBQTBEQSxVQUExRCx5Q0FBMERBLFVBQTFELEVBQVA7QUFDRixNQUFNRyxZQUFZLElBQUlDLEdBQUosQ0FBUUosVUFBUixDQUFsQjtBQUNBLHNDQUFXRyxTQUFYO0FBQ0QsQ0FMTTs7QUFPQSxJQUFNRSxnREFBb0IsU0FBcEJBLGlCQUFvQixDQUFDQyxJQUFELEVBQU9DLFVBQVAsRUFBc0I7QUFDckQsTUFBSSxPQUFPQSxVQUFQLEtBQXNCLFVBQTFCLEVBQ0UsT0FBTyxxQkFBUSxtQkFBUixFQUE2QixZ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QUNBLFNBQU9SLFVBQVV1QixRQUFWLENBQW1CZCxFQUFuQixDQUFQO0FBQ0QsQ0FMTTs7QUFPQSxJQUFNSCxzQ0FBZSxTQUFmQSxZQUFlLENBQUNrQixJQUFELEVBQU9wQixVQUFQLEVBQXNCO0FBQ2hELE1BQUksT0FBT0EsVUFBUCxLQUFzQixVQUExQixFQUNFLE9BQU8scUJBQVEsY0FBUixFQUF3QixZQUF4QixFQUFzQyxVQUF0QyxTQUF5REEsVUFBekQseUNBQXlEQSxVQUF6RCxFQUFQO0FBQ0YsU0FBT29CLEtBQUtDLEdBQUwsQ0FBU3JCLFVBQVQsQ0FBUDtBQUNELENBSk07O0FBTUEsSUFBTXNCLGtEQUFxQixTQUFyQkEsa0JBQXFCLENBQUM3QixVQUFELEVBQWE4QixLQUFiLEVBQW9CdkIsVUFBcEIsRUFBbUM7QUFDbkUsTUFBSSxPQUFPQSxVQUFQLEtBQXNCLFVBQTFCLEVBQ0UsT0FBTyxxQkFBUSxvQkFBUixFQUE4QixZQUE5QixFQUE0QyxVQUE1QyxTQUErREEsVUFBL0QseUNBQStEQSxVQUEvRCxFQUFQO0FBQ0YsTUFBTUMsU0FBU0MsYUFBYXFCLEtBQWIsQ0FBZjtBQUNBLE1BQU0zQixZQUFZLElBQUlDLEdBQUosQ0FBUUgsTUFBTUMsT0FBTixDQUFjRixVQUFkLElBQTRCQSxVQUE1QixHQUF5QyxFQUFqRCxDQUFsQjtBQUNBLE1BQU0rQixlQUFlLElBQUkzQixHQUFKLENBQVFJLE1BQVIsQ0FBckI7QUFDQSxTQUFPLDZCQUFJTCxTQUFKLEdBQWVtQixNQUFmLENBQXNCO0FBQUEsV0FBUVMsYUFBYVIsR0FBYixDQUFpQkMsSUFBakIsQ0FBUjtBQUFBLEdBQXRCLENBQVA7QUFDRCxDQVBNIiwiZmlsZSI6Ik1lc2FTZWxlY3Rpb24uanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgeyBmYWlsLCBiYWRUeXBlLCBtaXNzaW5nRnJvbVN0YXRlIH0gZnJvbSAnLi4vVXRpbHMvRXJyb3JzJztcblxuZXhwb3J0IGNvbnN0IGNyZWF0ZVNlbGVjdGlvbiA9IChfc2VsZWN0aW9uID0gW10pID0+IHtcbiAgaWYgKCFBcnJheS5pc0FycmF5KF9zZWxlY3Rpb24pKVxuICAgIHJldHVybiBiYWRUeXBlKCdhZGRJZFRvU2VsZWN0aW9uJywgJ19zZWxlY3Rpb24nLCAnYXJyYXknLCB0eXBlb2YgX3NlbGVjdGlvbik7XG4gIGNvbnN0IHNlbGVjdGlvbiA9IG5ldyBTZXQoX3NlbGVjdGlvbik7XG4gIHJldHVybiBbLi4uc2VsZWN0aW9uXTtcbn07XG5cbmV4cG9ydCBjb25zdCBzZWxlY3Rpb25Gcm9tUm93cyA9IChyb3dzLCBpZEFjY2Vzc29yKSA9PiB7XG4gIGlmICh0eXBlb2YgaWRBY2Nlc3NvciAhPT0gJ2Z1bmN0aW9uJylcbiAgICByZXR1cm4gYmFkVHlwZSgnc2VsZWN0aW9uRnJvbVJvd3MnLCAnaWRBY2Nlc3NvcicsICdmdW5jdGlvbicsIHR5cGVvZiBpZEFjY2Vzc29yKTtcbiAgY29uc3QgaWRMaXN0ID0gbWFwTGlzdFRvSWRzKHJvd3MsIGlkQWNjZXNzb3IpO1xuICByZXR1cm4gY3JlYXRlU2VsZWN0aW9uKGlkTGlzdCk7XG59O1xuXG5leHBvcnQgY29uc3QgYWRkUm93VG9TZWxlY3Rpb24gPSAoX3NlbGVjdGlvbiwgcm93LCBpZEFjY2Vzc29yKSA9PiB7XG4gIGlmICghQXJyYXkuaXNBcnJheShfc2VsZWN0aW9uKSlcbiAgICByZXR1cm4gYmFkVHlwZSgnYWRkSWRUb1NlbGVjdGlvbicsICdfc2VsZWN0aW9uJywgJ2FycmF5JywgdHlwZW9mIF9zZWxlY3Rpb24pO1xuICBpZiAodHlwZW9mIGlkQWNjZXNzb3IgIT09ICdmdW5jdGlvbicpXG4gICAgcmV0dXJuIGJhZFR5cGUoJ2FkZFJvd1RvU2VsZWN0aW9uJywgJ2lkQWNjZXNzb3InLCAnZnVuY3Rpb24nLCB0eXBlb2YgaWRBY2Nlc3Nvcik7XG4gIGNvbnN0IGlkID0gaWRBY2Nlc3Nvcihyb3cpO1xuICByZXR1cm4gYWRkSWRUb1NlbGVjdGlvbihfc2VsZWN0aW9uLCBpZCk7XG59O1xuXG5leHBvcnQgY29uc3QgYWRkSWRUb1NlbGVjdGlvbiA9IChfc2VsZWN0aW9uID0gW10sIGlkKSA9PiB7XG4gIGlmICghQXJyYXkuaXNBcnJheShfc2VsZWN0aW9uKSlcbiAgICByZXR1cm4gYmFkVHlwZSgnYWRkSWRUb1NlbGVjdGlvbicsICdfc2VsZWN0aW9uJywgJ2FycmF5JywgdHlwZW9mIF9zZWxlY3Rpb24pO1xuICBjb25zdCBzZWxlY3Rpb24gPSBuZXcgU2V0KF9zZWxlY3Rpb24pO1xuICBzZWxlY3Rpb24uYWRkKGlkKTtcbiAgcmV0dXJuIFsuLi5zZWxlY3Rpb25dO1xufVxuXG5leHBvcnQgY29uc3QgYWRkSWRzVG9TZWxlY3Rpb24gPSAoX3NlbGVjdGlvbiA9IFtdLCBpZHMpID0+IHtcbiAgaWYgKCFBcnJheS5pc0FycmF5KF9zZWxlY3Rpb24pKVxuICAgIHJldHVybiBiYWRUeXBlKCdhZGRJZFRvU2VsZWN0aW9uJywgJ19zZWxlY3Rpb24nLCAnYXJyYXknLCB0eXBlb2YgX3NlbGVjdGlvbik7XG4gIGlmICghQXJyYXkuaXNBcnJheShpZHMpKVxuICAgIHJldHVybiBiYWRUeXBlKCdhZGRJZFRvU2VsZWN0aW9uJywgJ2lkcycsICdhcnJheScsIHR5cGVvZiBpZHMpO1xuICBjb25zdCBzZWxlY3Rpb24gPSBuZXcgU2V0KFsuLi5fc2VsZWN0aW9uLCAuLi5pZHNdKTtcbiAgcmV0dXJuIFsuLi5zZWxlY3Rpb25dO1xufVxuXG5leHBvcnQgY29uc3QgcmVtb3ZlUm93RnJvbVNlbGVjdGlvbiA9IChfc2VsZWN0aW9uLCByb3csIGlkQWNjZXNzb3IpID0+IHtcbiAgaWYgKCFBcnJheS5pc0FycmF5KF9zZWxlY3Rpb24pKVxuICAgIHJldHVybiBiYWRUeXBlKCdyZW1vdmVSb3dGcm9tU2VsZWN0aW9uJywgJ19zZWxlY3Rpb24nLCAnYXJyYXknLCB0eXBlb2YgX3NlbGVjdGlvbik7XG4gIGlmICh0eXBlb2YgaWRBY2Nlc3NvciAhPT0gJ2Z1bmN0aW9uJylcbiAgICByZXR1cm4gYmFkVHlwZSgncmVtb3ZlUm93RnJvbVNlbGVjdGlvbicsICdpZEFjY2Vzc29yJywgJ2Z1bmN0aW9uJywgdHlwZW9mIGlkQWNjZXNzb3IpO1xuICBjb25zdCBpZCA9IGlkQWNjZXNzb3Iocm93KTtcbiAgcmV0dXJuIHJlbW92ZUlkRnJvbVNlbGVjdGlvbihfc2VsZWN0aW9uLCBpZCk7XG59O1xuXG5leHBvcnQgY29uc3QgcmVtb3ZlSWRGcm9tU2VsZWN0aW9uID0gKF9zZWxlY3Rpb24sIGlkKSA9PiB7XG4gIGlmICghQXJyYXkuaXNBcnJheShfc2VsZWN0aW9uKSlcbiAgICByZXR1cm4gYmFkVHlwZSgncmVtb3ZlSWRGcm9tU2VsZWN0aW9uJywgJ19zZWxlY3Rpb24nLCAnYXJyYXknLCB0eXBlb2YgX3NlbGVjdGlvbik7XG4gIGNvbnN0IHNlbGVjdGlvbiA9IG5ldyBTZXQoQXJyYXkuaXNBcnJheShfc2VsZWN0aW9uKSA/IF9zZWxlY3Rpb24gOiBbXSk7XG4gIHNlbGVjdGlvbi5kZWxldGUoaWQpO1xuICByZXR1cm4gWy4uLnNlbGVjdGlvbl07XG59XG5cbmV4cG9ydCBjb25zdCByZW1vdmVJZHNGcm9tU2VsZWN0aW9uID0gKF9zZWxlY3Rpb24gLGlkcykgPT4ge1xuICBpZiAoIUFycmF5LmlzQXJyYXkoX3NlbGVjdGlvbikpXG4gICAgcmV0dXJuIGJhZFR5cGUoJ3JlbW92ZUlkc0Zyb21TZWxlY3Rpb24nLCAnX3NlbGVjdGlvbicsICdhcnJheScsIHR5cGVvZiBfc2VsZWN0aW9uKTtcbiAgaWYgKCFBcnJheS5pc0FycmF5KGlkcykpXG4gICAgcmV0dXJuIGJhZFR5cGUoJ3JlbW92ZUlkc0Zyb21TZWxlY3Rpb24nLCAnaWRzJywgJ2FycmF5JywgdHlwZW9mIGlkcyk7XG4gIGNvbnN0IHNlbGVjdGlvbiA9IG5ldyBTZXQoX3NlbGVjdGlvbik7XG4gIGNvbnN0IHJlbW92YWJsZSA9IG5ldyBTZXQoaWRzKTtcbiAgcmV0dXJuIFsuLi5zZWxlY3Rpb25dLmZpbHRlcihpdGVtID0+ICFyZW1vdmFibGUuaGFzKGl0ZW0pKTtcbn1cblxuZXhwb3J0IGNvbnN0IGlzUm93U2VsZWN0ZWQgPSAoc2VsZWN0aW9uLCByb3csIGlkQWNjZXNzb3IpID0+IHtcbiAgaWYgKHR5cGVvZiBpZEFjY2Vzc29yICE9PSAnZnVuY3Rpb24nKVxuICAgIHJldHVybiBiYWRUeXBlKCdpc1Jvd1NlbGVjdGVkJywgJ2lkQWNjZXNzb3InLCAnZnVuY3Rpb24nLCB0eXBlb2YgaWRBY2Nlc3Nvcik7XG4gIGNvbnN0IGlkID0gaWRBY2Nlc3Nvcihyb3cpO1xuICByZXR1cm4gc2VsZWN0aW9uLmluY2x1ZGVzKGlkKTtcbn07XG5cbmV4cG9ydCBjb25zdCBtYXBMaXN0VG9JZHMgPSAobGlzdCwgaWRBY2Nlc3NvcikgPT4ge1xuICBpZiAodHlwZW9mIGlkQWNjZXNzb3IgIT09ICdmdW5jdGlvbicpXG4gICAgcmV0dXJuIGJhZFR5cGUoJ21hcExpc3RUb0lkcycsICdpZEFjY2Vzc29yJywgJ2Z1bmN0aW9uJywgdHlwZW9mIGlkQWNjZXNzb3IpO1xuICByZXR1cm4gbGlzdC5tYXAoaWRBY2Nlc3Nvcik7XG59O1xuXG5leHBvcnQgY29uc3QgaW50ZXJzZWN0U2VsZWN0aW9uID0gKF9zZWxlY3Rpb24sIF9saXN0LCBpZEFjY2Vzc29yKSA9PiB7XG4gIGlmICh0eXBlb2YgaWRBY2Nlc3NvciAhPT0gJ2Z1bmN0aW9uJylcbiAgICByZXR1cm4gYmFkVHlwZSgnaW50ZXJzZWN0U2VsZWN0aW9uJywgJ2lkQWNjZXNzb3InLCAnZnVuY3Rpb24nLCB0eXBlb2YgaWRBY2Nlc3Nvcik7XG4gIGNvbnN0IGlkTGlzdCA9IG1hcExpc3RUb0lkcyhfbGlzdCk7XG4gIGNvbnN0IHNlbGVjdGlvbiA9IG5ldyBTZXQoQXJyYXkuaXNBcnJheShfc2VsZWN0aW9uKSA/IF9zZWxlY3Rpb24gOiBbXSk7XG4gIGNvbnN0IGludGVyc2VjdGlvbiA9IG5ldyBTZXQoaWRMaXN0KTtcbiAgcmV0dXJuIFsuLi5zZWxlY3Rpb25dLmZpbHRlcihpdGVtID0+IGludGVyc2VjdGlvbi5oYXMoaXRlbSkpO1xufTtcbiJdfQ==