'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _displayUnits;

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

exports.stringValue = stringValue;
exports.isHtml = isHtml;
exports.htmlStringValue = htmlStringValue;
exports.sortFactory = sortFactory;
exports.numberSort = numberSort;
exports.arraysMatch = arraysMatch;
exports.textSort = textSort;
exports.makeClassifier = makeClassifier;
exports.randomize = randomize;
exports.uid = uid;
exports.getUnitValue = getUnitValue;
exports.combineWidths = combineWidths;

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function stringValue(value) {
  switch (typeof value === 'undefined' ? 'undefined' : _typeof(value)) {
    case 'string':
      if (isHtml(value)) {
        return htmlStringValue(value);
      } else {
        return value;
      }
    case 'number':
    case 'boolean':
      return value.toString();
    case 'object':
      if (Array.isArray(value)) {
        return value.map(stringValue).join(', ');
      } else if (value === null) {
        return '';
      } else {
        return JSON.stringify(value);
      }
    case 'undefined':
    default:
      return '';
  };
};

function isHtml(text) {
  var strict = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;

  if (typeof text !== 'string') return false;
  if (strict && (text[0] !== '<' || text[text.length - 1] !== '>')) return false;

  var parser = new DOMParser().parseFromString(text, 'text/html');
  return Array.from(parser.body.childNodes).some(function (node) {
    return node.nodeType === 1;
  });
}

function htmlStringValue(html) {
  var tmp = document.createElement("DIV");
  tmp.innerHTML = html;
  return tmp.textContent || tmp.innerText || '';
};

function sortFactory(accessor) {
  accessor = typeof accessor == 'function' ? accessor : function (value) {
    return value;
  };
  return function (a, b) {
    var A = accessor(a);
    var B = accessor(b);
    return A === B ? 0 : A < B ? 1 : -1;
  };
};

function numberSort(list, key) {
  var ascending = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;

  var accessor = function accessor(val) {
    return val[key] ? parseFloat(val[key]) : 0;
  };
  var result = list.sort(sortFactory(accessor));
  return ascending ? result.reverse() : result;
};

function arraysMatch(a, b) {
  if (!Array.isArray(a) || !Array.isArray(b)) return undefined;
  if (a.length !== b.length) return false;
  while (a.length) {
    if (a.shift() !== b.shift()) return false;
  }
  return true;
}

function textSort(_list, key) {
  var ascending = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;

  var list = [].concat(_toConsumableArray(_list));
  var accessor = function accessor(val) {
    return typeof val[key] === 'string' ? val[key].trim().toLowerCase() : stringValue(val[key]).toLowerCase();
  };
  var preSort = list.map(accessor);
  var sorted = list.sort(sortFactory(accessor));
  var postSort = sorted.map(accessor);
  var result = arraysMatch(preSort, postSort) ? list : sorted;
  return ascending ? result.reverse() : result;
};

function makeClassifier(namespace, globalNamespace) {
  return function (element, modifiers) {
    if (Array.isArray(element)) element = element.join('-');
    var base = (globalNamespace ? globalNamespace + '-' : '') + (namespace ? namespace : '') + (element ? '-' + element : '');
    if (!modifiers || !modifiers.length) return base;
    if (!Array.isArray(modifiers)) modifiers = [modifiers];
    return modifiers.reduce(function (output, modifier) {
      var addendum = ' ' + base + '--' + modifier;
      return output + addendum;
    }, base);
  };
}

function randomize() {
  var low = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 0;
  var high = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 99;

  return Math.floor(Math.random() * (high - low + 1) + low);
};

function uid() {
  var len = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 8;

  var output = '';
  while (output.length < len) {
    var index = randomize(0, 35);
    if (index >= 10) output += String.fromCharCode(87 + index);else output += index.toString();
  };
  return output;
};

var displayUnits = exports.displayUnits = (_displayUnits = {
  px: /[0-9]+(px)?$/,
  vw: /[0-9]+vw$/
}, _defineProperty(_displayUnits, 'vw', /[0-9]+vw$/), _defineProperty(_displayUnits, 'em', /[0-9]+em$/), _defineProperty(_displayUnits, 'rem', /[0-9]+rem$/), _defineProperty(_displayUnits, 'percent', /[0-9]+%$/), _displayUnits);

function getUnitValue(size) {
  if (typeof size !== 'string') throw new TypeError('<getUnitValue>: invalid "size" string param:', size);
  return parseInt(size.match(/[0-9]+/)[0]);
}

function combineWidths() {
  for (var _len = arguments.length, widths = Array(_len), _key = 0; _key < _len; _key++) {
    widths[_key] = arguments[_key];
  }

  if (!Array.isArray(widths)) return null;
  if (widths.length === 1 && Array.isArray(widths[0])) widths = widths.shift();
  if (!Array.isArray(widths)) throw new TypeError('<combineWidths>: invalid widths provided:', widths);

  var totals = {};

  widths.forEach(function (width) {
    if (typeof width === 'number') return totals.px = typeof totals.px === 'number' ? totals.px + width : width;
    if (typeof width !== 'string') return;else width = width.toLowerCase();

    Object.entries(displayUnits).forEach(function (_ref) {
      var _ref2 = _slicedToArray(_ref, 2),
          unit = _ref2[0],
          pattern = _ref2[1];

      if (pattern.test(width)) {
        totals[unit] = typeof totals[unit] === 'number' ? totals[unit] + getUnitValue(width) : getUnitValue(width);
      }
    });
  });

  return Object.keys(totals).reduce(function (outputString, unit, index) {
    if (!totals[unit]) return outputString;
    var displayUnit = unit === 'percent' ? '%' : unit;
    var value = totals[unit];
    if (index === 0) return value + displayUnit;else return 'calc(' + outputString + ' + ' + (value + displayUnit) + ')';
  }, '');
};