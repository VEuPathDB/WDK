'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Defaults = require('../Defaults');

var _Defaults2 = _interopRequireDefault(_Defaults);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var Utils = {
  stringValue: function stringValue(value) {
    switch (typeof value === 'undefined' ? 'undefined' : _typeof(value)) {
      case 'string':
        if (Utils.isHtml(value)) return Utils.htmlStringValue(value);else return value;
      case 'number':
      case 'boolean':
        return value.toString();
      case 'object':
        if (Array.isArray(value)) return value.map(Utils.stringValue).join(', ');
        if (value === null) return '';else return JSON.stringify(value);
      case 'undefined':
      default:
        return '';
    };
  },
  getRealOffset: function getRealOffset(el) {
    var top = 0;
    var left = 0;
    do {
      top += el.offsetTop || 0;
      left += el.offsetLeft || 0;
      el = el.offsetParent;
    } while (el);
    return { top: top, left: left };
  },
  htmlStringValue: function htmlStringValue(html) {
    var tmp = document.createElement("DIV");
    tmp.innerHTML = html;
    return tmp.textContent || tmp.innerText || '';
  },
  wordCount: function wordCount(text) {
    if (typeof text !== 'string') return undefined;
    return text.trim().split(' ').filter(function (x) {
      return x.length;
    }).length;
  },
  reverseText: function reverseText(text) {
    if (typeof text !== 'string' || !text.length) return text;
    return text.split('').reverse().join('');
  },
  trimInitialPunctuation: function trimInitialPunctuation(text) {
    if (typeof text !== 'string' || !text.length) return text;
    while (text.search(/[a-zA-Z0-9]/) !== 0) {
      text = text.substring(1);
    };
    return text;
  },
  trimPunctuation: function trimPunctuation(text) {
    if (typeof text !== 'string' || !text.length) return text;

    text = Utils.trimInitialPunctuation(text);
    text = Utils.reverseText(text);
    text = Utils.trimInitialPunctuation(text);
    text = Utils.reverseText(text);

    return text;
  },
  truncate: function truncate(text, cutoff) {
    if (typeof text !== 'string' || typeof cutoff !== 'number') return text;
    var count = Utils.wordCount(text);
    if (count < cutoff) return text;

    var words = text.trim().split(' ').filter(function (x) {
      return x.length;
    });
    var threshold = Math.ceil(cutoff * 0.66);
    var short = words.slice(0, threshold).join(' ');

    return Utils.trimPunctuation(short) + '...';
  },
  objectContainsQuery: function objectContainsQuery(obj, query) {
    var searchable = Utils.stringValue(obj).toLowerCase();
    return Utils.stringContainsQuery(searchable, query);
  },
  stringContainsQuery: function stringContainsQuery(str, query) {
    var queryParts = query.toLowerCase().split(' ');
    return queryParts.every(function (part) {
      return str.indexOf(part) >= 0;
    });
  },
  numberSort: function numberSort(items, sortByKey, ascending) {
    var result = items.sort(function (a, b) {
      var A = a[sortByKey] ? parseFloat(a[sortByKey]) : 0;
      var B = b[sortByKey] ? parseFloat(b[sortByKey]) : 0;
      return A === B ? 0 : A < B ? 1 : -1;
      return result;
    });
    return ascending ? result.reverse() : result;
  },
  sortFactory: function sortFactory(accessor) {
    accessor = typeof accessor == 'function' ? accessor : function (value) {
      return value;
    };
    return function (a, b) {
      var A = accessor(a);
      var B = accessor(b);
      return A === B ? 0 : A < B ? 1 : -1;
    };
  },
  textSort: function textSort(items, sortByKey) {
    var ascending = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;

    var result = items.sort(function (a, b) {
      var A = typeof a[sortByKey] === 'string' ? a[sortByKey].trim() : Utils.stringValue(a[sortByKey]);
      var B = typeof b[sortByKey] === 'string' ? b[sortByKey].trim() : Utils.stringValue(b[sortByKey]);
      return A === B ? 0 : A < B ? 1 : -1;
    });
    return ascending ? result.reverse() : result;
  },
  ucFirst: function ucFirst(text) {
    if (typeof text !== 'string' || !text.length) return text;
    return text[0].toUpperCase() + text.slice(1);
  },
  isHtml: function isHtml(text) {
    var strict = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;

    if (typeof text !== 'string') return false;
    if (strict && (text[0] !== '<' || text[text.length - 1] !== '>')) return false;

    var parser = new DOMParser().parseFromString(text, 'text/html');
    return Array.from(parser.body.childNodes).some(function (node) {
      return node.nodeType === 1;
    });
  },
  isNumeric: function isNumeric(value) {
    return !Array.isArray(value) && value - parseFloat(value) + 1 >= 0;
  },
  randomize: function randomize() {
    var low = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 0;
    var high = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 99;

    return Math.floor(Math.random() * (high - low + 1) + low);
  },
  uid: function uid() {
    var len = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 8;

    var output = '';
    while (output.length < len) {
      var index = Utils.randomize(0, 35);
      if (index >= 10) output += String.fromCharCode(87 + index);else output += index.toString();
    };
    return output;
  },
  keysInList: function keysInList(list) {
    var blacklist = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : [];

    if (!Array.isArray(list) || list.some(function (item) {
      return (typeof item === 'undefined' ? 'undefined' : _typeof(item)) !== 'object';
    })) return list;
    return list.reduce(function (keys, currentValue) {
      Object.keys(currentValue).forEach(function (key) {
        return keys.includes(key) || blacklist.includes(key) || keys.push(key);
      });
      return keys;
    }, []);
  },
  createCsv: function createCsv(rows, columns) {
    if (!columns) columns = Utils.keysInList(rows).map(function (key) {
      key;
    });
    columns = columns.filter(function (column) {
      return !column.hidden && !column.disabled;
    });

    var outputLines = [];
    var keys = columns.map(function (_ref) {
      var key = _ref.key;
      return key;
    });
    var names = columns.map(function (column) {
      return column.name ? column.name : column.key;
    });
    outputLines.push(names.join(','));
    rows.forEach(function (row) {
      var values = keys.map(function (key) {
        return Utils.stringValue(row[key]).replace(',', '\,').replace('\n', '');
      });
      outputLines.push(values.join(','));
    });
    return outputLines.join('\n');
  }
};

exports.default = Utils;