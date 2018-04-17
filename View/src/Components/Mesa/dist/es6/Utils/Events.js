'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.EventsFactory = undefined;

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _KeyCodes = require('./KeyCodes');

var _KeyCodes2 = _interopRequireDefault(_KeyCodes);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

var idPrefix = 'listener_';

var EventsFactory = exports.EventsFactory = function EventsFactory(node) {
  var instance = {
    listenerStore: [],
    add: function add(eventName, callback) {
      eventName = eventName.toLowerCase();
      var signature = [eventName, callback];
      var length = instance.listenerStore.push(signature);
      node.addEventListener(eventName, callback);
      return idPrefix + --length;
    },
    use: function use() {
      var map = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

      Object.entries(map).forEach(function (entry) {
        return instance.add.apply(instance, _toConsumableArray(entry));
      });
    },
    remove: function remove(id) {
      var offset = idPrefix.length;
      var index = parseInt(id.substring(offset));

      var _instance$listenerSto = _slicedToArray(instance.listenerStore[index], 2),
          event = _instance$listenerSto[0],
          callback = _instance$listenerSto[1];

      node.removeEventListener(event, callback);
      delete instance.listenerStore[index];
    },
    clearAll: function clearAll() {
      instance.listenerStore.forEach(instance.remove);
    },
    onKey: function onKey(key, callback) {
      if (!key in _KeyCodes2.default) return;
      return instance.onKeyCode(_KeyCodes2.default[key], callback);
    },
    onKeyCode: function onKeyCode(keyCodeOrSet, callback) {
      var handler = function handler(e) {
        var acceptable = Array.isArray(keyCodeOrSet) ? keyCodeOrSet : [keyCodeOrSet];
        if (acceptable.includes(e.keyCode)) callback(e);
      };
      return instance.add('keydown', handler);
    }
  };
  return instance;
};

var Events = EventsFactory(window);
exports.default = Events;