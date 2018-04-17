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

  var message = 'parameter "' + parameter + '"  is not of type ' + expected + ' (got ' + actual + ')';
  return fatal ? fail(fn, message, TypeError) : warn(fn, message);
};

var missingFromState = exports.missingFromState = function missingFromState(fn, missing) {
  var obj = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
  var fatal = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : false;

  var present = Object.keys(obj).join(', ');
  var message = 'state branch "' + missing + '" not found in state. Found sibling keys: [' + present + ']';
  return fatal ? fail(fn, message, ReferenceError) : warn(fn, message);
};