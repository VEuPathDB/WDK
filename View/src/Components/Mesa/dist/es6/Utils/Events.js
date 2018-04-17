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
      instance.listenerStore.forEach(function (id) {
        return instance.remove(idPrefix + id);
      });
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VdGlscy9FdmVudHMuanMiXSwibmFtZXMiOlsiaWRQcmVmaXgiLCJFdmVudHNGYWN0b3J5Iiwibm9kZSIsImluc3RhbmNlIiwibGlzdGVuZXJTdG9yZSIsImFkZCIsImV2ZW50TmFtZSIsImNhbGxiYWNrIiwidG9Mb3dlckNhc2UiLCJzaWduYXR1cmUiLCJsZW5ndGgiLCJwdXNoIiwiYWRkRXZlbnRMaXN0ZW5lciIsInVzZSIsIm1hcCIsIk9iamVjdCIsImVudHJpZXMiLCJmb3JFYWNoIiwiZW50cnkiLCJyZW1vdmUiLCJpZCIsIm9mZnNldCIsImluZGV4IiwicGFyc2VJbnQiLCJzdWJzdHJpbmciLCJldmVudCIsInJlbW92ZUV2ZW50TGlzdGVuZXIiLCJjbGVhckFsbCIsIm9uS2V5Iiwia2V5Iiwib25LZXlDb2RlIiwia2V5Q29kZU9yU2V0IiwiaGFuZGxlciIsImUiLCJhY2NlcHRhYmxlIiwiQXJyYXkiLCJpc0FycmF5IiwiaW5jbHVkZXMiLCJrZXlDb2RlIiwiRXZlbnRzIiwid2luZG93Il0sIm1hcHBpbmdzIjoiOzs7Ozs7Ozs7QUFBQTs7Ozs7Ozs7QUFDQSxJQUFNQSxXQUFXLFdBQWpCOztBQUVPLElBQU1DLHdDQUFnQixTQUFoQkEsYUFBZ0IsQ0FBQ0MsSUFBRCxFQUFVO0FBQ3JDLE1BQU1DLFdBQVc7QUFDZkMsbUJBQWUsRUFEQTtBQUVmQyxTQUFLLGFBQUNDLFNBQUQsRUFBWUMsUUFBWixFQUF5QjtBQUM1QkQsa0JBQVlBLFVBQVVFLFdBQVYsRUFBWjtBQUNBLFVBQUlDLFlBQVksQ0FBRUgsU0FBRixFQUFhQyxRQUFiLENBQWhCO0FBQ0EsVUFBSUcsU0FBU1AsU0FBU0MsYUFBVCxDQUF1Qk8sSUFBdkIsQ0FBNEJGLFNBQTVCLENBQWI7QUFDQVAsV0FBS1UsZ0JBQUwsQ0FBc0JOLFNBQXRCLEVBQWlDQyxRQUFqQztBQUNBLGFBQU9QLFdBQVksRUFBRVUsTUFBckI7QUFDRCxLQVJjO0FBU2ZHLFNBQUssZUFBYztBQUFBLFVBQWJDLEdBQWEsdUVBQVAsRUFBTzs7QUFDakJDLGFBQU9DLE9BQVAsQ0FBZUYsR0FBZixFQUFvQkcsT0FBcEIsQ0FBNEI7QUFBQSxlQUFTZCxTQUFTRSxHQUFULG9DQUFnQmEsS0FBaEIsRUFBVDtBQUFBLE9BQTVCO0FBQ0QsS0FYYztBQVlmQyxZQUFRLGdCQUFDQyxFQUFELEVBQVE7QUFDZCxVQUFNQyxTQUFTckIsU0FBU1UsTUFBeEI7QUFDQSxVQUFJWSxRQUFRQyxTQUFTSCxHQUFHSSxTQUFILENBQWFILE1BQWIsQ0FBVCxDQUFaOztBQUZjLGlEQUdZbEIsU0FBU0MsYUFBVCxDQUF1QmtCLEtBQXZCLENBSFo7QUFBQSxVQUdSRyxLQUhRO0FBQUEsVUFHRGxCLFFBSEM7O0FBSWRMLFdBQUt3QixtQkFBTCxDQUF5QkQsS0FBekIsRUFBZ0NsQixRQUFoQztBQUNBLGFBQU9KLFNBQVNDLGFBQVQsQ0FBdUJrQixLQUF2QixDQUFQO0FBQ0QsS0FsQmM7QUFtQmZLLGNBQVUsb0JBQU07QUFDZHhCLGVBQVNDLGFBQVQsQ0FBdUJhLE9BQXZCLENBQStCO0FBQUEsZUFBTWQsU0FBU2dCLE1BQVQsQ0FBZ0JuQixXQUFXb0IsRUFBM0IsQ0FBTjtBQUFBLE9BQS9CO0FBQ0QsS0FyQmM7QUFzQmZRLFdBQU8sZUFBQ0MsR0FBRCxFQUFNdEIsUUFBTixFQUFtQjtBQUN4QixVQUFJLENBQUNzQixHQUFELHNCQUFKLEVBQXNCO0FBQ3RCLGFBQU8xQixTQUFTMkIsU0FBVCxDQUFtQixtQkFBU0QsR0FBVCxDQUFuQixFQUFrQ3RCLFFBQWxDLENBQVA7QUFDRCxLQXpCYztBQTBCZnVCLGVBQVcsbUJBQUNDLFlBQUQsRUFBZXhCLFFBQWYsRUFBNEI7QUFDckMsVUFBSXlCLFVBQVUsU0FBVkEsT0FBVSxDQUFDQyxDQUFELEVBQU87QUFDbkIsWUFBSUMsYUFBYUMsTUFBTUMsT0FBTixDQUFjTCxZQUFkLElBQThCQSxZQUE5QixHQUE2QyxDQUFFQSxZQUFGLENBQTlEO0FBQ0EsWUFBSUcsV0FBV0csUUFBWCxDQUFvQkosRUFBRUssT0FBdEIsQ0FBSixFQUFvQy9CLFNBQVMwQixDQUFUO0FBQ3JDLE9BSEQ7QUFJQSxhQUFPOUIsU0FBU0UsR0FBVCxDQUFhLFNBQWIsRUFBd0IyQixPQUF4QixDQUFQO0FBQ0Q7QUFoQ2MsR0FBakI7QUFrQ0EsU0FBTzdCLFFBQVA7QUFDRCxDQXBDTTs7QUFzQ1AsSUFBTW9DLFNBQVN0QyxjQUFjdUMsTUFBZCxDQUFmO2tCQUNlRCxNIiwiZmlsZSI6IkV2ZW50cy5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBLZXlDb2RlcyBmcm9tICcuL0tleUNvZGVzJztcbmNvbnN0IGlkUHJlZml4ID0gJ2xpc3RlbmVyXyc7XG5cbmV4cG9ydCBjb25zdCBFdmVudHNGYWN0b3J5ID0gKG5vZGUpID0+IHtcbiAgY29uc3QgaW5zdGFuY2UgPSB7XG4gICAgbGlzdGVuZXJTdG9yZTogW10sXG4gICAgYWRkOiAoZXZlbnROYW1lLCBjYWxsYmFjaykgPT4ge1xuICAgICAgZXZlbnROYW1lID0gZXZlbnROYW1lLnRvTG93ZXJDYXNlKCk7XG4gICAgICBsZXQgc2lnbmF0dXJlID0gWyBldmVudE5hbWUsIGNhbGxiYWNrIF07XG4gICAgICBsZXQgbGVuZ3RoID0gaW5zdGFuY2UubGlzdGVuZXJTdG9yZS5wdXNoKHNpZ25hdHVyZSk7XG4gICAgICBub2RlLmFkZEV2ZW50TGlzdGVuZXIoZXZlbnROYW1lLCBjYWxsYmFjayk7XG4gICAgICByZXR1cm4gaWRQcmVmaXggKyAoLS1sZW5ndGgpO1xuICAgIH0sXG4gICAgdXNlOiAobWFwID0ge30pID0+IHtcbiAgICAgIE9iamVjdC5lbnRyaWVzKG1hcCkuZm9yRWFjaChlbnRyeSA9PiBpbnN0YW5jZS5hZGQoLi4uZW50cnkpKTtcbiAgICB9LFxuICAgIHJlbW92ZTogKGlkKSA9PiB7XG4gICAgICBjb25zdCBvZmZzZXQgPSBpZFByZWZpeC5sZW5ndGg7XG4gICAgICBsZXQgaW5kZXggPSBwYXJzZUludChpZC5zdWJzdHJpbmcob2Zmc2V0KSk7XG4gICAgICBsZXQgWyBldmVudCwgY2FsbGJhY2sgXSA9IGluc3RhbmNlLmxpc3RlbmVyU3RvcmVbaW5kZXhdO1xuICAgICAgbm9kZS5yZW1vdmVFdmVudExpc3RlbmVyKGV2ZW50LCBjYWxsYmFjayk7XG4gICAgICBkZWxldGUgaW5zdGFuY2UubGlzdGVuZXJTdG9yZVtpbmRleF07XG4gICAgfSxcbiAgICBjbGVhckFsbDogKCkgPT4ge1xuICAgICAgaW5zdGFuY2UubGlzdGVuZXJTdG9yZS5mb3JFYWNoKGlkID0+IGluc3RhbmNlLnJlbW92ZShpZFByZWZpeCArIGlkKSk7XG4gICAgfSxcbiAgICBvbktleTogKGtleSwgY2FsbGJhY2spID0+IHtcbiAgICAgIGlmICgha2V5IGluIEtleUNvZGVzKSByZXR1cm47XG4gICAgICByZXR1cm4gaW5zdGFuY2Uub25LZXlDb2RlKEtleUNvZGVzW2tleV0sIGNhbGxiYWNrKTtcbiAgICB9LFxuICAgIG9uS2V5Q29kZTogKGtleUNvZGVPclNldCwgY2FsbGJhY2spID0+IHtcbiAgICAgIGxldCBoYW5kbGVyID0gKGUpID0+IHtcbiAgICAgICAgbGV0IGFjY2VwdGFibGUgPSBBcnJheS5pc0FycmF5KGtleUNvZGVPclNldCkgPyBrZXlDb2RlT3JTZXQgOiBbIGtleUNvZGVPclNldCBdO1xuICAgICAgICBpZiAoYWNjZXB0YWJsZS5pbmNsdWRlcyhlLmtleUNvZGUpKSBjYWxsYmFjayhlKTtcbiAgICAgIH07XG4gICAgICByZXR1cm4gaW5zdGFuY2UuYWRkKCdrZXlkb3duJywgaGFuZGxlcik7XG4gICAgfVxuICB9O1xuICByZXR1cm4gaW5zdGFuY2U7XG59O1xuXG5jb25zdCBFdmVudHMgPSBFdmVudHNGYWN0b3J5KHdpbmRvdyk7XG5leHBvcnQgZGVmYXVsdCBFdmVudHM7XG4iXX0=