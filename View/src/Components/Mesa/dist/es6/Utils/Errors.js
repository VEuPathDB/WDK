'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
/*    Error Handlers   */
var fail = exports.fail = function fail(fn, message) {
  var Err = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : Error;

  console.error('<' + fn + '>: ' + message);
  return undefined;
};

var badType = exports.badType = function badType(fn, parameter, expected, actual) {
  var message = 'parameter "' + parameter + '"  is not of type ' + expected + ' (got ' + actual + ')';
  return fail(fn, message, TypeError);
};

var missingFromState = exports.missingFromState = function missingFromState(fn, missing) {
  var obj = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};

  var present = Object.keys(obj).join(', ');
  var message = 'state branch "' + missing + '" not found in state. Found sibling keys: [' + present + ']';
  return fail(fn, message, ReferenceError);
};