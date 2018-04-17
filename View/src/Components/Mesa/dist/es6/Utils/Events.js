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
      var clear = function clear(listener, index) {
        return instance.remove(idPrefix + index);
      };
      instance.listenerStore.forEach(clear);
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VdGlscy9FdmVudHMuanMiXSwibmFtZXMiOlsiaWRQcmVmaXgiLCJFdmVudHNGYWN0b3J5Iiwibm9kZSIsImluc3RhbmNlIiwibGlzdGVuZXJTdG9yZSIsImFkZCIsImV2ZW50TmFtZSIsImNhbGxiYWNrIiwidG9Mb3dlckNhc2UiLCJzaWduYXR1cmUiLCJsZW5ndGgiLCJwdXNoIiwiYWRkRXZlbnRMaXN0ZW5lciIsInVzZSIsIm1hcCIsIk9iamVjdCIsImVudHJpZXMiLCJmb3JFYWNoIiwiZW50cnkiLCJyZW1vdmUiLCJpZCIsIm9mZnNldCIsImluZGV4IiwicGFyc2VJbnQiLCJzdWJzdHJpbmciLCJldmVudCIsInJlbW92ZUV2ZW50TGlzdGVuZXIiLCJjbGVhckFsbCIsImNsZWFyIiwibGlzdGVuZXIiLCJvbktleSIsImtleSIsIm9uS2V5Q29kZSIsImtleUNvZGVPclNldCIsImhhbmRsZXIiLCJlIiwiYWNjZXB0YWJsZSIsIkFycmF5IiwiaXNBcnJheSIsImluY2x1ZGVzIiwia2V5Q29kZSIsIkV2ZW50cyIsIndpbmRvdyJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7O0FBQUE7Ozs7Ozs7O0FBQ0EsSUFBTUEsV0FBVyxXQUFqQjs7QUFFTyxJQUFNQyx3Q0FBZ0IsU0FBaEJBLGFBQWdCLENBQUNDLElBQUQsRUFBVTtBQUNyQyxNQUFNQyxXQUFXO0FBQ2ZDLG1CQUFlLEVBREE7QUFFZkMsU0FBSyxhQUFDQyxTQUFELEVBQVlDLFFBQVosRUFBeUI7QUFDNUJELGtCQUFZQSxVQUFVRSxXQUFWLEVBQVo7QUFDQSxVQUFJQyxZQUFZLENBQUVILFNBQUYsRUFBYUMsUUFBYixDQUFoQjtBQUNBLFVBQUlHLFNBQVNQLFNBQVNDLGFBQVQsQ0FBdUJPLElBQXZCLENBQTRCRixTQUE1QixDQUFiO0FBQ0FQLFdBQUtVLGdCQUFMLENBQXNCTixTQUF0QixFQUFpQ0MsUUFBakM7QUFDQSxhQUFPUCxXQUFZLEVBQUVVLE1BQXJCO0FBQ0QsS0FSYztBQVNmRyxTQUFLLGVBQWM7QUFBQSxVQUFiQyxHQUFhLHVFQUFQLEVBQU87O0FBQ2pCQyxhQUFPQyxPQUFQLENBQWVGLEdBQWYsRUFBb0JHLE9BQXBCLENBQTRCO0FBQUEsZUFBU2QsU0FBU0UsR0FBVCxvQ0FBZ0JhLEtBQWhCLEVBQVQ7QUFBQSxPQUE1QjtBQUNELEtBWGM7QUFZZkMsWUFBUSxnQkFBQ0MsRUFBRCxFQUFRO0FBQ2QsVUFBTUMsU0FBU3JCLFNBQVNVLE1BQXhCO0FBQ0EsVUFBSVksUUFBUUMsU0FBU0gsR0FBR0ksU0FBSCxDQUFhSCxNQUFiLENBQVQsQ0FBWjs7QUFGYyxpREFHWWxCLFNBQVNDLGFBQVQsQ0FBdUJrQixLQUF2QixDQUhaO0FBQUEsVUFHUkcsS0FIUTtBQUFBLFVBR0RsQixRQUhDOztBQUlkTCxXQUFLd0IsbUJBQUwsQ0FBeUJELEtBQXpCLEVBQWdDbEIsUUFBaEM7QUFDQSxhQUFPSixTQUFTQyxhQUFULENBQXVCa0IsS0FBdkIsQ0FBUDtBQUNELEtBbEJjO0FBbUJmSyxjQUFVLG9CQUFNO0FBQ2QsVUFBTUMsUUFBUSxTQUFSQSxLQUFRLENBQUNDLFFBQUQsRUFBV1AsS0FBWDtBQUFBLGVBQXFCbkIsU0FBU2dCLE1BQVQsQ0FBZ0JuQixXQUFXc0IsS0FBM0IsQ0FBckI7QUFBQSxPQUFkO0FBQ0FuQixlQUFTQyxhQUFULENBQXVCYSxPQUF2QixDQUErQlcsS0FBL0I7QUFDRCxLQXRCYztBQXVCZkUsV0FBTyxlQUFDQyxHQUFELEVBQU14QixRQUFOLEVBQW1CO0FBQ3hCLFVBQUksQ0FBQ3dCLEdBQUQsc0JBQUosRUFBc0I7QUFDdEIsYUFBTzVCLFNBQVM2QixTQUFULENBQW1CLG1CQUFTRCxHQUFULENBQW5CLEVBQWtDeEIsUUFBbEMsQ0FBUDtBQUNELEtBMUJjO0FBMkJmeUIsZUFBVyxtQkFBQ0MsWUFBRCxFQUFlMUIsUUFBZixFQUE0QjtBQUNyQyxVQUFJMkIsVUFBVSxTQUFWQSxPQUFVLENBQUNDLENBQUQsRUFBTztBQUNuQixZQUFJQyxhQUFhQyxNQUFNQyxPQUFOLENBQWNMLFlBQWQsSUFBOEJBLFlBQTlCLEdBQTZDLENBQUVBLFlBQUYsQ0FBOUQ7QUFDQSxZQUFJRyxXQUFXRyxRQUFYLENBQW9CSixFQUFFSyxPQUF0QixDQUFKLEVBQW9DakMsU0FBUzRCLENBQVQ7QUFDckMsT0FIRDtBQUlBLGFBQU9oQyxTQUFTRSxHQUFULENBQWEsU0FBYixFQUF3QjZCLE9BQXhCLENBQVA7QUFDRDtBQWpDYyxHQUFqQjtBQW1DQSxTQUFPL0IsUUFBUDtBQUNELENBckNNOztBQXVDUCxJQUFNc0MsU0FBU3hDLGNBQWN5QyxNQUFkLENBQWY7a0JBQ2VELE0iLCJmaWxlIjoiRXZlbnRzLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IEtleUNvZGVzIGZyb20gJy4vS2V5Q29kZXMnO1xuY29uc3QgaWRQcmVmaXggPSAnbGlzdGVuZXJfJztcblxuZXhwb3J0IGNvbnN0IEV2ZW50c0ZhY3RvcnkgPSAobm9kZSkgPT4ge1xuICBjb25zdCBpbnN0YW5jZSA9IHtcbiAgICBsaXN0ZW5lclN0b3JlOiBbXSxcbiAgICBhZGQ6IChldmVudE5hbWUsIGNhbGxiYWNrKSA9PiB7XG4gICAgICBldmVudE5hbWUgPSBldmVudE5hbWUudG9Mb3dlckNhc2UoKTtcbiAgICAgIGxldCBzaWduYXR1cmUgPSBbIGV2ZW50TmFtZSwgY2FsbGJhY2sgXTtcbiAgICAgIGxldCBsZW5ndGggPSBpbnN0YW5jZS5saXN0ZW5lclN0b3JlLnB1c2goc2lnbmF0dXJlKTtcbiAgICAgIG5vZGUuYWRkRXZlbnRMaXN0ZW5lcihldmVudE5hbWUsIGNhbGxiYWNrKTtcbiAgICAgIHJldHVybiBpZFByZWZpeCArICgtLWxlbmd0aCk7XG4gICAgfSxcbiAgICB1c2U6IChtYXAgPSB7fSkgPT4ge1xuICAgICAgT2JqZWN0LmVudHJpZXMobWFwKS5mb3JFYWNoKGVudHJ5ID0+IGluc3RhbmNlLmFkZCguLi5lbnRyeSkpO1xuICAgIH0sXG4gICAgcmVtb3ZlOiAoaWQpID0+IHtcbiAgICAgIGNvbnN0IG9mZnNldCA9IGlkUHJlZml4Lmxlbmd0aDtcbiAgICAgIGxldCBpbmRleCA9IHBhcnNlSW50KGlkLnN1YnN0cmluZyhvZmZzZXQpKTtcbiAgICAgIGxldCBbIGV2ZW50LCBjYWxsYmFjayBdID0gaW5zdGFuY2UubGlzdGVuZXJTdG9yZVtpbmRleF07XG4gICAgICBub2RlLnJlbW92ZUV2ZW50TGlzdGVuZXIoZXZlbnQsIGNhbGxiYWNrKTtcbiAgICAgIGRlbGV0ZSBpbnN0YW5jZS5saXN0ZW5lclN0b3JlW2luZGV4XTtcbiAgICB9LFxuICAgIGNsZWFyQWxsOiAoKSA9PiB7XG4gICAgICBjb25zdCBjbGVhciA9IChsaXN0ZW5lciwgaW5kZXgpID0+IGluc3RhbmNlLnJlbW92ZShpZFByZWZpeCArIGluZGV4KTtcbiAgICAgIGluc3RhbmNlLmxpc3RlbmVyU3RvcmUuZm9yRWFjaChjbGVhcik7XG4gICAgfSxcbiAgICBvbktleTogKGtleSwgY2FsbGJhY2spID0+IHtcbiAgICAgIGlmICgha2V5IGluIEtleUNvZGVzKSByZXR1cm47XG4gICAgICByZXR1cm4gaW5zdGFuY2Uub25LZXlDb2RlKEtleUNvZGVzW2tleV0sIGNhbGxiYWNrKTtcbiAgICB9LFxuICAgIG9uS2V5Q29kZTogKGtleUNvZGVPclNldCwgY2FsbGJhY2spID0+IHtcbiAgICAgIGxldCBoYW5kbGVyID0gKGUpID0+IHtcbiAgICAgICAgbGV0IGFjY2VwdGFibGUgPSBBcnJheS5pc0FycmF5KGtleUNvZGVPclNldCkgPyBrZXlDb2RlT3JTZXQgOiBbIGtleUNvZGVPclNldCBdO1xuICAgICAgICBpZiAoYWNjZXB0YWJsZS5pbmNsdWRlcyhlLmtleUNvZGUpKSBjYWxsYmFjayhlKTtcbiAgICAgIH07XG4gICAgICByZXR1cm4gaW5zdGFuY2UuYWRkKCdrZXlkb3duJywgaGFuZGxlcik7XG4gICAgfVxuICB9O1xuICByZXR1cm4gaW5zdGFuY2U7XG59O1xuXG5jb25zdCBFdmVudHMgPSBFdmVudHNGYWN0b3J5KHdpbmRvdyk7XG5leHBvcnQgZGVmYXVsdCBFdmVudHM7XG4iXX0=