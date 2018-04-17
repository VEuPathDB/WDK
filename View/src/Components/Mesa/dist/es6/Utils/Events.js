'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _KeyCodes = require('./KeyCodes');

var _KeyCodes2 = _interopRequireDefault(_KeyCodes);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var idPrefix = 'listener_';

var Events = {
  listenerStore: [],
  add: function add(event, callback) {
    var signature = [event, callback];
    var length = Events.listenerStore.push(signature);
    window.addEventListener(event, callback);
    return idPrefix + --length;
  },
  remove: function remove(id) {
    var offset = idPrefix.length;
    var index = parseInt(id.substring(offset));

    var _Events$listenerStore = _slicedToArray(Events.listenerStore[index], 2),
        event = _Events$listenerStore[0],
        callback = _Events$listenerStore[1];

    window.removeEventListener(event, callback);
    delete Events.listenerStore[index];
  },
  onKey: function onKey(key, callback) {
    if (!key in _KeyCodes2.default) return;
    return Events.onKeyCode(_KeyCodes2.default[key], callback);
  },
  onKeyCode: function onKeyCode(keyCodeOrSet, callback) {
    var handler = function handler(e) {
      var acceptable = Array.isArray(keyCodeOrSet) ? keyCodeOrSet : [keyCodeOrSet];
      if (acceptable.includes(e.keyCode)) callback(e);
    };
    return Events.add('keydown', handler);
  }
};

exports.default = Events;