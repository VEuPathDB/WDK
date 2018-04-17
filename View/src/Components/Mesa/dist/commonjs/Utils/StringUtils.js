'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

exports.stringValue = stringValue;
exports.isHtml = isHtml;
exports.htmlStringValue = htmlStringValue;
exports.sortFactory = sortFactory;
exports.numberSort = numberSort;
exports.textSort = textSort;
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

function textSort(list, key) {
  var ascending = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;

  var accessor = function accessor(val) {
    return typeof val[key] === 'string' ? val[key].trim() : stringValue(val[key]);
  };
  var result = list.sort(sortFactory(accessor));
  return ascending ? result.reverse() : result;
};