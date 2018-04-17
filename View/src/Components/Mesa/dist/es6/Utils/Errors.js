'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
/*    Error Handlers   */
var fail = exports.fail = function fail(fn, message) {
  var Err = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : Error;

  throw new Err('<' + fn + '>: ' + message);
  return undefined;
};

var warn = exports.warn = function warn(fn, message) {
  console.warn('<' + fn + '>: ' + message);
  return undefined;
};

var badType = exports.badType = function badType(fn, parameter, expected, actual) {
  var fatal = arguments.length > 4 && arguments[4] !== undefined ? arguments[4] : false;

  var message = 'parameter "' + parameter + '"  is not of type ' + expected + ' (got ' + actual + ')' + (fatal ? '' : '; using empty ' + expected);
  return fatal ? fail(fn, message, TypeError) : warn(fn, message);
};

var missingFromState = exports.missingFromState = function missingFromState(fn, missing) {
  var obj = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
  var fatal = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : false;

  var present = Object.keys(obj).join(', ');
  var message = 'state branch "' + missing + '" not found in state. Found sibling keys: [' + present + ']';
  return fatal ? fail(fn, message, ReferenceError) : warn(fn, message);
};
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VdGlscy9FcnJvcnMuanMiXSwibmFtZXMiOlsiZmFpbCIsImZuIiwibWVzc2FnZSIsIkVyciIsIkVycm9yIiwidW5kZWZpbmVkIiwid2FybiIsImNvbnNvbGUiLCJiYWRUeXBlIiwicGFyYW1ldGVyIiwiZXhwZWN0ZWQiLCJhY3R1YWwiLCJmYXRhbCIsIlR5cGVFcnJvciIsIm1pc3NpbmdGcm9tU3RhdGUiLCJtaXNzaW5nIiwib2JqIiwicHJlc2VudCIsIk9iamVjdCIsImtleXMiLCJqb2luIiwiUmVmZXJlbmNlRXJyb3IiXSwibWFwcGluZ3MiOiI7Ozs7O0FBQUE7QUFDTyxJQUFNQSxzQkFBTyxTQUFQQSxJQUFPLENBQUNDLEVBQUQsRUFBS0MsT0FBTCxFQUE4QjtBQUFBLE1BQWhCQyxHQUFnQix1RUFBVkMsS0FBVTs7QUFDaEQsUUFBTSxJQUFJRCxHQUFKLE9BQVlGLEVBQVosV0FBb0JDLE9BQXBCLENBQU47QUFDQSxTQUFPRyxTQUFQO0FBQ0QsQ0FITTs7QUFLQSxJQUFNQyxzQkFBTyxTQUFQQSxJQUFPLENBQUNMLEVBQUQsRUFBS0MsT0FBTCxFQUFpQjtBQUNuQ0ssVUFBUUQsSUFBUixPQUFpQkwsRUFBakIsV0FBeUJDLE9BQXpCO0FBQ0EsU0FBT0csU0FBUDtBQUNELENBSE07O0FBS0EsSUFBTUcsNEJBQVUsU0FBVkEsT0FBVSxDQUFDUCxFQUFELEVBQUtRLFNBQUwsRUFBZ0JDLFFBQWhCLEVBQTBCQyxNQUExQixFQUFvRDtBQUFBLE1BQWxCQyxLQUFrQix1RUFBVixLQUFVOztBQUN6RSxNQUFNVixVQUFVLGdCQUFjTyxTQUFkLDBCQUE0Q0MsUUFBNUMsY0FBNkRDLE1BQTdELFVBQTBFQyxRQUFRLEVBQVIsc0JBQThCRixRQUF4RyxDQUFoQjtBQUNBLFNBQU9FLFFBQVFaLEtBQUtDLEVBQUwsRUFBU0MsT0FBVCxFQUFrQlcsU0FBbEIsQ0FBUixHQUF1Q1AsS0FBS0wsRUFBTCxFQUFTQyxPQUFULENBQTlDO0FBQ0QsQ0FITTs7QUFLQSxJQUFNWSw4Q0FBbUIsU0FBbkJBLGdCQUFtQixDQUFDYixFQUFELEVBQUtjLE9BQUwsRUFBMEM7QUFBQSxNQUE1QkMsR0FBNEIsdUVBQXRCLEVBQXNCO0FBQUEsTUFBbEJKLEtBQWtCLHVFQUFWLEtBQVU7O0FBQ3hFLE1BQU1LLFVBQVVDLE9BQU9DLElBQVAsQ0FBWUgsR0FBWixFQUFpQkksSUFBakIsQ0FBc0IsSUFBdEIsQ0FBaEI7QUFDQSxNQUFNbEIsNkJBQTJCYSxPQUEzQixtREFBZ0ZFLE9BQWhGLE1BQU47QUFDQSxTQUFPTCxRQUFRWixLQUFLQyxFQUFMLEVBQVNDLE9BQVQsRUFBa0JtQixjQUFsQixDQUFSLEdBQTRDZixLQUFLTCxFQUFMLEVBQVNDLE9BQVQsQ0FBbkQ7QUFDRCxDQUpNIiwiZmlsZSI6IkVycm9ycy5qcyIsInNvdXJjZXNDb250ZW50IjpbIi8qICAgIEVycm9yIEhhbmRsZXJzICAgKi9cbmV4cG9ydCBjb25zdCBmYWlsID0gKGZuLCBtZXNzYWdlLCBFcnIgPSBFcnJvcikgPT4ge1xuICB0aHJvdyBuZXcgRXJyKGA8JHtmbn0+OiAke21lc3NhZ2V9YCk7XG4gIHJldHVybiB1bmRlZmluZWQ7XG59XG5cbmV4cG9ydCBjb25zdCB3YXJuID0gKGZuLCBtZXNzYWdlKSA9PiB7XG4gIGNvbnNvbGUud2FybihgPCR7Zm59PjogJHttZXNzYWdlfWApO1xuICByZXR1cm4gdW5kZWZpbmVkO1xufVxuXG5leHBvcnQgY29uc3QgYmFkVHlwZSA9IChmbiwgcGFyYW1ldGVyLCBleHBlY3RlZCwgYWN0dWFsLCBmYXRhbCA9IGZhbHNlKSA9PiB7XG4gIGNvbnN0IG1lc3NhZ2UgPSBgcGFyYW1ldGVyIFwiJHtwYXJhbWV0ZXJ9XCIgIGlzIG5vdCBvZiB0eXBlICR7ZXhwZWN0ZWR9IChnb3QgJHthY3R1YWx9KWAgKyAoZmF0YWwgPyAnJyA6IGA7IHVzaW5nIGVtcHR5ICR7ZXhwZWN0ZWR9YCk7XG4gIHJldHVybiBmYXRhbCA/IGZhaWwoZm4sIG1lc3NhZ2UsIFR5cGVFcnJvcikgOiB3YXJuKGZuLCBtZXNzYWdlKTtcbn07XG5cbmV4cG9ydCBjb25zdCBtaXNzaW5nRnJvbVN0YXRlID0gKGZuLCBtaXNzaW5nLCBvYmogPSB7fSwgZmF0YWwgPSBmYWxzZSkgPT4ge1xuICBjb25zdCBwcmVzZW50ID0gT2JqZWN0LmtleXMob2JqKS5qb2luKCcsICcpO1xuICBjb25zdCBtZXNzYWdlID0gYHN0YXRlIGJyYW5jaCBcIiR7bWlzc2luZ31cIiBub3QgZm91bmQgaW4gc3RhdGUuIEZvdW5kIHNpYmxpbmcga2V5czogWyR7cHJlc2VudH1dYDtcbiAgcmV0dXJuIGZhdGFsID8gZmFpbChmbiwgbWVzc2FnZSwgUmVmZXJlbmNlRXJyb3IpIDogd2FybihmbiwgbWVzc2FnZSk7XG59O1xuIl19