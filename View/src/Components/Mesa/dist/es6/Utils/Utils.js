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
exports.repositionItemInList = repositionItemInList;
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
};

function repositionItemInList(list, fromIndex, toIndex) {
  if (!list || !list.length) return list;
  if (fromIndex === toIndex) return list;
  if (fromIndex < 0 || toIndex < 0) return list;
  toIndex = toIndex < fromIndex ? toIndex + 1 : toIndex;
  var updatedList = [].concat(_toConsumableArray(list));
  var item = updatedList[fromIndex];
  updatedList.splice(fromIndex, 1);
  updatedList.splice(toIndex, 0, item);
  return [].concat(_toConsumableArray(updatedList));
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VdGlscy9VdGlscy5qcyJdLCJuYW1lcyI6WyJzdHJpbmdWYWx1ZSIsImlzSHRtbCIsImh0bWxTdHJpbmdWYWx1ZSIsInNvcnRGYWN0b3J5IiwibnVtYmVyU29ydCIsImFycmF5c01hdGNoIiwicmVwb3NpdGlvbkl0ZW1Jbkxpc3QiLCJ0ZXh0U29ydCIsIm1ha2VDbGFzc2lmaWVyIiwicmFuZG9taXplIiwidWlkIiwiZ2V0VW5pdFZhbHVlIiwiY29tYmluZVdpZHRocyIsInZhbHVlIiwidG9TdHJpbmciLCJBcnJheSIsImlzQXJyYXkiLCJtYXAiLCJqb2luIiwiSlNPTiIsInN0cmluZ2lmeSIsInRleHQiLCJzdHJpY3QiLCJsZW5ndGgiLCJwYXJzZXIiLCJET01QYXJzZXIiLCJwYXJzZUZyb21TdHJpbmciLCJmcm9tIiwiYm9keSIsImNoaWxkTm9kZXMiLCJzb21lIiwibm9kZSIsIm5vZGVUeXBlIiwiaHRtbCIsInRtcCIsImRvY3VtZW50IiwiY3JlYXRlRWxlbWVudCIsImlubmVySFRNTCIsInRleHRDb250ZW50IiwiaW5uZXJUZXh0IiwiYWNjZXNzb3IiLCJhIiwiYiIsIkEiLCJCIiwibGlzdCIsImtleSIsImFzY2VuZGluZyIsInZhbCIsInBhcnNlRmxvYXQiLCJyZXN1bHQiLCJzb3J0IiwicmV2ZXJzZSIsInVuZGVmaW5lZCIsInNoaWZ0IiwiZnJvbUluZGV4IiwidG9JbmRleCIsInVwZGF0ZWRMaXN0IiwiaXRlbSIsInNwbGljZSIsIl9saXN0IiwidHJpbSIsInRvTG93ZXJDYXNlIiwicHJlU29ydCIsInNvcnRlZCIsInBvc3RTb3J0IiwibmFtZXNwYWNlIiwiZ2xvYmFsTmFtZXNwYWNlIiwiZWxlbWVudCIsIm1vZGlmaWVycyIsImJhc2UiLCJyZWR1Y2UiLCJvdXRwdXQiLCJtb2RpZmllciIsImFkZGVuZHVtIiwibG93IiwiaGlnaCIsIk1hdGgiLCJmbG9vciIsInJhbmRvbSIsImxlbiIsImluZGV4IiwiU3RyaW5nIiwiZnJvbUNoYXJDb2RlIiwiZGlzcGxheVVuaXRzIiwicHgiLCJ2dyIsInNpemUiLCJUeXBlRXJyb3IiLCJwYXJzZUludCIsIm1hdGNoIiwid2lkdGhzIiwidG90YWxzIiwiZm9yRWFjaCIsIndpZHRoIiwiT2JqZWN0IiwiZW50cmllcyIsInVuaXQiLCJwYXR0ZXJuIiwidGVzdCIsImtleXMiLCJvdXRwdXRTdHJpbmciLCJkaXNwbGF5VW5pdCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7Ozs7O1FBQWdCQSxXLEdBQUFBLFc7UUF5QkFDLE0sR0FBQUEsTTtRQVFBQyxlLEdBQUFBLGU7UUFNQUMsVyxHQUFBQSxXO1FBU0FDLFUsR0FBQUEsVTtRQVFBQyxXLEdBQUFBLFc7UUFTQUMsb0IsR0FBQUEsb0I7UUFZQUMsUSxHQUFBQSxRO1FBWUFDLGMsR0FBQUEsYztRQWlCQUMsUyxHQUFBQSxTO1FBSUFDLEcsR0FBQUEsRztRQW1CQUMsWSxHQUFBQSxZO1FBTUFDLGEsR0FBQUEsYTs7Ozs7O0FBdklULFNBQVNaLFdBQVQsQ0FBc0JhLEtBQXRCLEVBQTZCO0FBQ2xDLGlCQUFlQSxLQUFmLHlDQUFlQSxLQUFmO0FBQ0UsU0FBSyxRQUFMO0FBQ0UsVUFBSVosT0FBT1ksS0FBUCxDQUFKLEVBQW1CO0FBQ2pCLGVBQU9YLGdCQUFnQlcsS0FBaEIsQ0FBUDtBQUNELE9BRkQsTUFFTztBQUNMLGVBQU9BLEtBQVA7QUFDRDtBQUNILFNBQUssUUFBTDtBQUNBLFNBQUssU0FBTDtBQUNFLGFBQU9BLE1BQU1DLFFBQU4sRUFBUDtBQUNGLFNBQUssUUFBTDtBQUNFLFVBQUlDLE1BQU1DLE9BQU4sQ0FBY0gsS0FBZCxDQUFKLEVBQTBCO0FBQ3hCLGVBQU9BLE1BQU1JLEdBQU4sQ0FBVWpCLFdBQVYsRUFBdUJrQixJQUF2QixDQUE0QixJQUE1QixDQUFQO0FBQ0QsT0FGRCxNQUVPLElBQUlMLFVBQVUsSUFBZCxFQUFvQjtBQUN6QixlQUFPLEVBQVA7QUFDRCxPQUZNLE1BRUE7QUFDTCxlQUFPTSxLQUFLQyxTQUFMLENBQWVQLEtBQWYsQ0FB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UCxHQUFZUyxLQUE1QyxHQUFvREEsS0FBeEU7QUFDRixRQUFJLE9BQU9BLEtBQVAsS0FBaUIsUUFBckIsRUFDRSxPQURGLEtBRUtBLFFBQVFBLE1BQU1oQyxXQUFOLEVBQVI7O0FBRUxpQyxXQUFPQyxPQUFQLENBQWVaLFlBQWYsRUFBNkJTLE9BQTdCLENBQXFDLGdCQUF1QjtBQUFBO0FBQUEsVUFBcEJJLElBQW9CO0FBQUEsVUFBZEMsT0FBYzs7QUFDMUQsVUFBSUEsUUFBUUMsSUFBUixDQUFhTCxLQUFiLENBQUosRUFBeUI7QUFDdkJGLGVBQU9LLElBQVAsSUFDRSxPQUFPTCxPQUFPSyxJQUFQLENBQVAsS0FBd0IsUUFBeEIsR0FDSUwsT0FBT0ssSUFBUCxJQUFldEYsYUFBYW1GLEtBQWIsQ0FEbkIsR0FFSW5GLGFBQWFtRixLQUFiLENBSE47QUFLRDtBQUNGLEtBUkQ7QUFTRCxHQWhCRDs7QUFrQkEsU0FBT0MsT0FBT0ssSUFBUCxDQUFZUixNQUFaLEVBQ0pyQixNQURJLENBQ0csVUFBQzhCLFlBQUQsRUFBZUosSUFBZixFQUFxQmhCLEtBQXJCLEVBQStCO0FBQ3JDLFFBQUksQ0FBQ1csT0FBT0ssSUFBUCxDQUFMLEVBQW1CLE9BQU9JLFlBQVA7QUFDbkIsUUFBSUMsY0FBZUwsU0FBUyxTQUFULEdBQXFCLEdBQXJCLEdBQTJCQSxJQUE5QztBQUNBLFFBQUlwRixRQUFRK0UsT0FBT0ssSUFBUCxDQUFaO0FBQ0EsUUFBSWhCLFVBQVUsQ0FBZCxFQUFpQixPQUFRcEUsUUFBUXlGLFdBQWhCLENBQWpCLEtBQ0ssaUJBQWVELFlBQWYsWUFBaUN4RixRQUFReUYsV0FBekM7QUFDTixHQVBJLEVBT0YsRUFQRSxDQUFQO0FBUUQiLCJmaWxlIjoiVXRpbHMuanMiLCJzb3VyY2VzQ29udGVudCI6WyJleHBvcnQgZnVuY3Rpb24gc3RyaW5nVmFsdWUgKHZhbHVlKSB7XG4gIHN3aXRjaCAodHlwZW9mIHZhbHVlKSB7XG4gICAgY2FzZSAnc3RyaW5nJzpcbiAgICAgIGlmIChpc0h0bWwodmFsdWUpKSB7XG4gICAgICAgIHJldHVybiBodG1sU3RyaW5nVmFsdWUodmFsdWUpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgcmV0dXJuIHZhbHVlO1xuICAgICAgfVxuICAgIGNhc2UgJ251bWJlcic6XG4gICAgY2FzZSAnYm9vbGVhbic6XG4gICAgICByZXR1cm4gdmFsdWUudG9TdHJpbmcoKTtcbiAgICBjYXNlICdvYmplY3QnOlxuICAgICAgaWYgKEFycmF5LmlzQXJyYXkodmFsdWUpKSB7XG4gICAgICAgIHJldHVybiB2YWx1ZS5tYXAoc3RyaW5nVmFsdWUpLmpvaW4oJywgJyk7XG4gICAgICB9IGVsc2UgaWYgKHZhbHVlID09PSBudWxsKSB7XG4gICAgICAgIHJldHVybiAnJztcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHJldHVybiBKU09OLnN0cmluZ2lmeSh2YWx1ZSk7XG4gICAgICB9XG4gICAgY2FzZSAndW5kZWZpbmVkJzpcbiAgICBkZWZhdWx0OlxuICAgICAgcmV0dXJuICcnO1xuICB9O1xufTtcblxuZXhwb3J0IGZ1bmN0aW9uIGlzSHRtbCAodGV4dCwgc3RyaWN0ID0gZmFsc2UpIHtcbiAgaWYgKHR5cGVvZiB0ZXh0ICE9PSAnc3RyaW5nJykgcmV0dXJuIGZhbHNlO1xuICBpZiAoc3RyaWN0ICYmICh0ZXh0WzBdICE9PSAnPCcgfHwgdGV4dFt0ZXh0Lmxlbmd0aCAtIDFdICE9PSAnPicpKSByZXR1cm4gZmFsc2U7XG5cbiAgY29uc3QgcGFyc2VyID0gbmV3IERPTVBhcnNlcigpLnBhcnNlRnJvbVN0cmluZyh0ZXh0LCAndGV4dC9odG1sJyk7XG4gIHJldHVybiBBcnJheS5mcm9tKHBhcnNlci5ib2R5LmNoaWxkTm9kZXMpLnNvbWUobm9kZSA9PiBub2RlLm5vZGVUeXBlID09PSAxKTtcbn1cblxuZXhwb3J0IGZ1bmN0aW9uIGh0bWxTdHJpbmdWYWx1ZSAoaHRtbCkge1xuICBjb25zdCB0bXAgPSBkb2N1bWVudC5jcmVhdGVFbGVtZW50KFwiRElWXCIpO1xuICB0bXAuaW5uZXJIVE1MID0gaHRtbDtcbiAgcmV0dXJuIHRtcC50ZXh0Q29udGVudCB8fCB0bXAuaW5uZXJUZXh0IHx8ICcnO1xufTtcblxuZXhwb3J0IGZ1bmN0aW9uIHNvcnRGYWN0b3J5IChhY2Nlc3Nvcikge1xuICBhY2Nlc3NvciA9ICh0eXBlb2YgYWNjZXNzb3IgPT0gJ2Z1bmN0aW9uJyA/IGFjY2Vzc29yIDogKHZhbHVlKSA9PiB2YWx1ZSk7XG4gIHJldHVybiBmdW5jdGlvbiAoYSwgYikge1xuICAgIGxldCBBID0gYWNjZXNzb3IoYSk7XG4gICAgbGV0IEIgPSBhY2Nlc3NvcihiKTtcbiAgICByZXR1cm4gQSA9PT0gQiA/IDAgOihBIDwgQiA/IDEgOiAtMSk7XG4gIH07XG59O1xuXG5leHBvcnQgZnVuY3Rpb24gbnVtYmVyU29ydCAobGlzdCwga2V5LCBhc2NlbmRpbmcgPSB0cnVlKSB7XG4gIGNvbnN0IGFjY2Vzc29yID0gKHZhbCkgPT4gdmFsW2tleV1cbiAgICA/IHBhcnNlRmxvYXQodmFsW2tleV0pXG4gICAgOiAwO1xuICBjb25zdCByZXN1bHQgPSBsaXN0LnNvcnQoc29ydEZhY3RvcnkoYWNjZXNzb3IpKTtcbiAgcmV0dXJuIGFzY2VuZGluZyA/IHJlc3VsdC5yZXZlcnNlKCkgOiByZXN1bHQ7XG59O1xuXG5leHBvcnQgZnVuY3Rpb24gYXJyYXlzTWF0Y2ggKGEsIGIpIHtcbiAgaWYgKCFBcnJheS5pc0FycmF5KGEpIHx8ICFBcnJheS5pc0FycmF5KGIpKSByZXR1cm4gdW5kZWZpbmVkO1xuICBpZiAoYS5sZW5ndGggIT09IGIubGVuZ3RoKSByZXR1cm4gZmFsc2U7XG4gIHdoaWxlIChhLmxlbmd0aCkge1xuICAgIGlmIChhLnNoaWZ0KCkgIT09IGIuc2hpZnQoKSkgcmV0dXJuIGZhbHNlO1xuICB9XG4gIHJldHVybiB0cnVlO1xufTtcblxuZXhwb3J0IGZ1bmN0aW9uIHJlcG9zaXRpb25JdGVtSW5MaXN0IChsaXN0LCBmcm9tSW5kZXgsIHRvSW5kZXgpIHtcbiAgaWYgKCFsaXN0IHx8ICFsaXN0Lmxlbmd0aCkgcmV0dXJuIGxpc3Q7XG4gIGlmIChmcm9tSW5kZXggPT09IHRvSW5kZXgpIHJldHVybiBsaXN0O1xuICBpZiAoZnJvbUluZGV4IDwgMCB8fCB0b0luZGV4IDwgMCkgcmV0dXJuIGxpc3Q7XG4gIHRvSW5kZXggPSAodG9JbmRleCA8IGZyb21JbmRleCA/IHRvSW5kZXggKyAxIDogdG9JbmRleCk7XG4gIGNvbnN0IHVwZGF0ZWRMaXN0ID0gWy4uLmxpc3RdO1xuICBjb25zdCBpdGVtID0gdXBkYXRlZExpc3RbZnJvbUluZGV4XTtcbiAgdXBkYXRlZExpc3Quc3BsaWNlKGZyb21JbmRleCwgMSk7XG4gIHVwZGF0ZWRMaXN0LnNwbGljZSh0b0luZGV4LCAwLCBpdGVtKTtcbiAgcmV0dXJuIFsuLi51cGRhdGVkTGlzdF07XG59XG5cbmV4cG9ydCBmdW5jdGlvbiB0ZXh0U29ydCAoX2xpc3QsIGtleSwgYXNjZW5kaW5nID0gdHJ1ZSkge1xuICBjb25zdCBsaXN0ID0gWy4uLl9saXN0XTtcbiAgY29uc3QgYWNjZXNzb3IgPSAodmFsKSA9PiB0eXBlb2YgdmFsW2tleV0gPT09ICdzdHJpbmcnXG4gICAgPyB2YWxba2V5XS50cmltKCkudG9Mb3dlckNhc2UoKVxuICAgIDogc3RyaW5nVmFsdWUodmFsW2tleV0pLnRvTG93ZXJDYXNlKCk7XG4gIGNvbnN0IHByZVNvcnQgPSBsaXN0Lm1hcChhY2Nlc3Nvcik7XG4gIGNvbnN0IHNvcnRlZCA9IGxpc3Quc29ydChzb3J0RmFjdG9yeShhY2Nlc3NvcikpO1xuICBjb25zdCBwb3N0U29ydCA9IHNvcnRlZC5tYXAoYWNjZXNzb3IpO1xuICBjb25zdCByZXN1bHQgPSBhcnJheXNNYXRjaChwcmVTb3J0LCBwb3N0U29ydCkgPyBsaXN0IDogc29ydGVkO1xuICByZXR1cm4gYXNjZW5kaW5nID8gcmVzdWx0LnJldmVyc2UoKSA6IHJlc3VsdDtcbn07XG5cbmV4cG9ydCBmdW5jdGlvbiBtYWtlQ2xhc3NpZmllciAobmFtZXNwYWNlLCBnbG9iYWxOYW1lc3BhY2UpIHtcbiAgcmV0dXJuIChlbGVtZW50LCBtb2RpZmllcnMpID0+IHtcbiAgICBpZiAoQXJyYXkuaXNBcnJheShlbGVtZW50KSkgZWxlbWVudCA9IGVsZW1lbnQuam9pbignLScpO1xuICAgIGxldCBiYXNlID0gKFxuICAgICAgKGdsb2JhbE5hbWVzcGFjZSA/IGdsb2JhbE5hbWVzcGFjZSArICctJyAgOiAnJykgK1xuICAgICAgKG5hbWVzcGFjZSA/IG5hbWVzcGFjZSA6ICcnKSArXG4gICAgICAoZWxlbWVudCA/ICctJyArIGVsZW1lbnQgOiAnJylcbiAgICApO1xuICAgIGlmICghbW9kaWZpZXJzIHx8ICFtb2RpZmllcnMubGVuZ3RoKSByZXR1cm4gYmFzZTtcbiAgICBpZiAoIUFycmF5LmlzQXJyYXkobW9kaWZpZXJzKSkgbW9kaWZpZXJzID0gW21vZGlmaWVyc107XG4gICAgcmV0dXJuIG1vZGlmaWVycy5yZWR1Y2UoKG91dHB1dCwgbW9kaWZpZXIpID0+IHtcbiAgICAgIGxldCBhZGRlbmR1bSA9ICcgJyArIGJhc2UgKyAnLS0nICsgbW9kaWZpZXI7XG4gICAgICByZXR1cm4gb3V0cHV0ICsgYWRkZW5kdW07XG4gICAgfSwgYmFzZSk7XG4gIH07XG59XG5cbmV4cG9ydCBmdW5jdGlvbiByYW5kb21pemUgKGxvdyA9IDAsIGhpZ2ggPSA5OSkge1xuICByZXR1cm4gTWF0aC5mbG9vcihNYXRoLnJhbmRvbSgpICogKGhpZ2ggLSBsb3cgKyAxKSArIGxvdyk7XG59O1xuXG5leHBvcnQgZnVuY3Rpb24gdWlkIChsZW4gPSA4KSB7XG4gIGxldCBvdXRwdXQgPSAnJztcbiAgd2hpbGUgKG91dHB1dC5sZW5ndGggPCBsZW4pIHtcbiAgICBsZXQgaW5kZXggPSByYW5kb21pemUoMCwgMzUpO1xuICAgIGlmIChpbmRleCA+PSAxMCkgb3V0cHV0ICs9IFN0cmluZy5mcm9tQ2hhckNvZGUoODcgKyBpbmRleCk7XG4gICAgZWxzZSBvdXRwdXQgKz0gaW5kZXgudG9TdHJpbmcoKTtcbiAgfTtcbiAgcmV0dXJuIG91dHB1dDtcbn07XG5cbmV4cG9ydCBjb25zdCBkaXNwbGF5VW5pdHMgPSB7XG4gIHB4OiAvWzAtOV0rKHB4KT8kLyxcbiAgdnc6IC9bMC05XSt2dyQvLFxuICB2dzogL1swLTldK3Z3JC8sXG4gIGVtOiAvWzAtOV0rZW0kLyxcbiAgcmVtOiAvWzAtOV0rcmVtJC8sXG4gIHBlcmNlbnQ6IC9bMC05XSslJC9cbn07XG5cbmV4cG9ydCBmdW5jdGlvbiBnZXRVbml0VmFsdWUgKHNpemUpIHtcbiAgaWYgKHR5cGVvZiBzaXplICE9PSAnc3RyaW5nJylcbiAgICB0aHJvdyBuZXcgVHlwZUVycm9yKCc8Z2V0VW5pdFZhbHVlPjogaW52YWxpZCBcInNpemVcIiBzdHJpbmcgcGFyYW06Jywgc2l6ZSk7XG4gIHJldHVybiBwYXJzZUludChzaXplLm1hdGNoKC9bMC05XSsvKVswXSk7XG59XG5cbmV4cG9ydCBmdW5jdGlvbiBjb21iaW5lV2lkdGhzICguLi53aWR0aHMpIHtcbiAgaWYgKCFBcnJheS5pc0FycmF5KHdpZHRocykpXG4gICAgcmV0dXJuIG51bGw7XG4gIGlmICh3aWR0aHMubGVuZ3RoID09PSAxICYmIEFycmF5LmlzQXJyYXkod2lkdGhzWzBdKSlcbiAgICB3aWR0aHMgPSB3aWR0aHMuc2hpZnQoKTtcbiAgaWYgKCFBcnJheS5pc0FycmF5KHdpZHRocykpXG4gICAgdGhyb3cgbmV3IFR5cGVFcnJvcignPGNvbWJpbmVXaWR0aHM+OiBpbnZhbGlkIHdpZHRocyBwcm92aWRlZDonLCB3aWR0aHMpO1xuXG4gIGNvbnN0IHRvdGFscyA9IHt9O1xuXG4gIHdpZHRocy5mb3JFYWNoKHdpZHRoID0+IHtcbiAgICBpZiAodHlwZW9mIHdpZHRoID09PSAnbnVtYmVyJylcbiAgICAgIHJldHVybiB0b3RhbHMucHggPSAodHlwZW9mIHRvdGFscy5weCA9PT0gJ251bWJlcicgPyB0b3RhbHMucHggKyB3aWR0aCA6IHdpZHRoKTtcbiAgICBpZiAodHlwZW9mIHdpZHRoICE9PSAnc3RyaW5nJylcbiAgICAgIHJldHVybjtcbiAgICBlbHNlIHdpZHRoID0gd2lkdGgudG9Mb3dlckNhc2UoKTtcblxuICAgIE9iamVjdC5lbnRyaWVzKGRpc3BsYXlVbml0cykuZm9yRWFjaCgoWyB1bml0LCBwYXR0ZXJuIF0pID0+IHtcbiAgICAgIGlmIChwYXR0ZXJuLnRlc3Qod2lkdGgpKSB7XG4gICAgICAgIHRvdGFsc1t1bml0XSA9IChcbiAgICAgICAgICB0eXBlb2YgdG90YWxzW3VuaXRdID09PSAnbnVtYmVyJ1xuICAgICAgICAgICAgPyB0b3RhbHNbdW5pdF0gKyBnZXRVbml0VmFsdWUod2lkdGgpXG4gICAgICAgICAgICA6IGdldFVuaXRWYWx1ZSh3aWR0aClcbiAgICAgICAgKTtcbiAgICAgIH1cbiAgICB9KTtcbiAgfSk7XG5cbiAgcmV0dXJuIE9iamVjdC5rZXlzKHRvdGFscylcbiAgICAucmVkdWNlKChvdXRwdXRTdHJpbmcsIHVuaXQsIGluZGV4KSA9PiB7XG4gICAgICBpZiAoIXRvdGFsc1t1bml0XSkgcmV0dXJuIG91dHB1dFN0cmluZztcbiAgICAgIGxldCBkaXNwbGF5VW5pdCA9ICh1bml0ID09PSAncGVyY2VudCcgPyAnJScgOiB1bml0KTtcbiAgICAgIGxldCB2YWx1ZSA9IHRvdGFsc1t1bml0XTtcbiAgICAgIGlmIChpbmRleCA9PT0gMCkgcmV0dXJuICh2YWx1ZSArIGRpc3BsYXlVbml0KTtcbiAgICAgIGVsc2UgcmV0dXJuIGBjYWxjKCR7b3V0cHV0U3RyaW5nfSArICR7dmFsdWUgKyBkaXNwbGF5VW5pdH0pYDtcbiAgICB9LCAnJyk7XG59O1xuIl19