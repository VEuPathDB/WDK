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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VdGlscy9FdmVudHMuanMiXSwibmFtZXMiOlsiaWRQcmVmaXgiLCJFdmVudHNGYWN0b3J5Iiwibm9kZSIsImluc3RhbmNlIiwibGlzdGVuZXJTdG9yZSIsImFkZCIsImV2ZW50TmFtZSIsImNhbGxiYWNrIiwidG9Mb3dlckNhc2UiLCJzaWduYXR1cmUiLCJsZW5ndGgiLCJwdXNoIiwiYWRkRXZlbnRMaXN0ZW5lciIsInVzZSIsIm1hcCIsIk9iamVjdCIsImVudHJpZXMiLCJmb3JFYWNoIiwiZW50cnkiLCJyZW1vdmUiLCJpZCIsIm9mZnNldCIsImluZGV4IiwicGFyc2VJbnQiLCJzdWJzdHJpbmciLCJldmVudCIsInJlbW92ZUV2ZW50TGlzdGVuZXIiLCJjbGVhckFsbCIsIm9uS2V5Iiwia2V5Iiwib25LZXlDb2RlIiwia2V5Q29kZU9yU2V0IiwiaGFuZGxlciIsImUiLCJhY2NlcHRhYmxlIiwiQXJyYXkiLCJpc0FycmF5IiwiaW5jbHVkZXMiLCJrZXlDb2RlIiwiRXZlbnRzIiwid2luZG93Il0sIm1hcHBpbmdzIjoiOzs7Ozs7Ozs7QUFBQTs7Ozs7Ozs7QUFDQSxJQUFNQSxXQUFXLFdBQWpCOztBQUVPLElBQU1DLHdDQUFnQixTQUFoQkEsYUFBZ0IsQ0FBQ0MsSUFBRCxFQUFVO0FBQ3JDLE1BQU1DLFdBQVc7QUFDZkMsbUJBQWUsRUFEQTtBQUVmQyxTQUFLLGFBQUNDLFNBQUQsRUFBWUMsUUFBWixFQUF5QjtBQUM1QkQsa0JBQVlBLFVBQVVFLFdBQVYsRUFBWjtBQUNBLFVBQUlDLFlBQVksQ0FBRUgsU0FBRixFQUFhQyxRQUFiLENBQWhCO0FBQ0EsVUFBSUcsU0FBU1AsU0FBU0MsYUFBVCxDQUF1Qk8sSUFBdkIsQ0FBNEJGLFNBQTVCLENBQWI7QUFDQVAsV0FBS1UsZ0JBQUwsQ0FBc0JOLFNBQXRCLEVBQWlDQyxRQUFqQztBQUNBLGFBQU9QLFdBQVksRUFBRVUsTUFBckI7QUFDRCxLQVJjO0FBU2ZHLFNBQUssZUFBYztBQUFBLFVBQWJDLEdBQWEsdUVBQVAsRUFBTzs7QUFDakJDLGFBQU9DLE9BQVAsQ0FBZUYsR0FBZixFQUFvQkcsT0FBcEIsQ0FBNEI7QUFBQSxlQUFTZCxTQUFTRSxHQUFULG9DQUFnQmEsS0FBaEIsRUFBVDtBQUFBLE9BQTVCO0FBQ0QsS0FYYztBQVlmQyxZQUFRLGdCQUFDQyxFQUFELEVBQVE7QUFDZCxVQUFNQyxTQUFTckIsU0FBU1UsTUFBeEI7QUFDQSxVQUFJWSxRQUFRQyxTQUFTSCxHQUFHSSxTQUFILENBQWFILE1BQWIsQ0FBVCxDQUFaOztBQUZjLGlEQUdZbEIsU0FBU0MsYUFBVCxDQUF1QmtCLEtBQXZCLENBSFo7QUFBQSxVQUdSRyxLQUhRO0FBQUEsVUFHRGxCLFFBSEM7O0FBSWRMLFdBQUt3QixtQkFBTCxDQUF5QkQsS0FBekIsRUFBZ0NsQixRQUFoQztBQUNBLGFBQU9KLFNBQVNDLGFBQVQsQ0FBdUJrQixLQUF2QixDQUFQO0FBQ0QsS0FsQmM7QUFtQmZLLGNBQVUsb0JBQU07QUFDZHhCLGVBQVNDLGFBQVQsQ0FBdUJhLE9BQXZCLENBQStCZCxTQUFTZ0IsTUFBeEM7QUFDRCxLQXJCYztBQXNCZlMsV0FBTyxlQUFDQyxHQUFELEVBQU10QixRQUFOLEVBQW1CO0FBQ3hCLFVBQUksQ0FBQ3NCLEdBQUQsc0JBQUosRUFBc0I7QUFDdEIsYUFBTzFCLFNBQVMyQixTQUFULENBQW1CLG1CQUFTRCxHQUFULENBQW5CLEVBQWtDdEIsUUFBbEMsQ0FBUDtBQUNELEtBekJjO0FBMEJmdUIsZUFBVyxtQkFBQ0MsWUFBRCxFQUFleEIsUUFBZixFQUE0QjtBQUNyQyxVQUFJeUIsVUFBVSxTQUFWQSxPQUFVLENBQUNDLENBQUQsRUFBTztBQUNuQixZQUFJQyxhQUFhQyxNQUFNQyxPQUFOLENBQWNMLFlBQWQsSUFBOEJBLFlBQTlCLEdBQTZDLENBQUVBLFlBQUYsQ0FBOUQ7QUFDQSxZQUFJRyxXQUFXRyxRQUFYLENBQW9CSixFQUFFSyxPQUF0QixDQUFKLEVBQW9DL0IsU0FBUzBCLENBQVQ7QUFDckMsT0FIRDtBQUlBLGFBQU85QixTQUFTRSxHQUFULENBQWEsU0FBYixFQUF3QjJCLE9BQXhCLENBQVA7QUFDRDtBQWhDYyxHQUFqQjtBQWtDQSxTQUFPN0IsUUFBUDtBQUNELENBcENNOztBQXNDUCxJQUFNb0MsU0FBU3RDLGNBQWN1QyxNQUFkLENBQWY7a0JBQ2VELE0iLCJmaWxlIjoiRXZlbnRzLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IEtleUNvZGVzIGZyb20gJy4vS2V5Q29kZXMnO1xuY29uc3QgaWRQcmVmaXggPSAnbGlzdGVuZXJfJztcblxuZXhwb3J0IGNvbnN0IEV2ZW50c0ZhY3RvcnkgPSAobm9kZSkgPT4ge1xuICBjb25zdCBpbnN0YW5jZSA9IHtcbiAgICBsaXN0ZW5lclN0b3JlOiBbXSxcbiAgICBhZGQ6IChldmVudE5hbWUsIGNhbGxiYWNrKSA9PiB7XG4gICAgICBldmVudE5hbWUgPSBldmVudE5hbWUudG9Mb3dlckNhc2UoKTtcbiAgICAgIGxldCBzaWduYXR1cmUgPSBbIGV2ZW50TmFtZSwgY2FsbGJhY2sgXTtcbiAgICAgIGxldCBsZW5ndGggPSBpbnN0YW5jZS5saXN0ZW5lclN0b3JlLnB1c2goc2lnbmF0dXJlKTtcbiAgICAgIG5vZGUuYWRkRXZlbnRMaXN0ZW5lcihldmVudE5hbWUsIGNhbGxiYWNrKTtcbiAgICAgIHJldHVybiBpZFByZWZpeCArICgtLWxlbmd0aCk7XG4gICAgfSxcbiAgICB1c2U6IChtYXAgPSB7fSkgPT4ge1xuICAgICAgT2JqZWN0LmVudHJpZXMobWFwKS5mb3JFYWNoKGVudHJ5ID0+IGluc3RhbmNlLmFkZCguLi5lbnRyeSkpO1xuICAgIH0sXG4gICAgcmVtb3ZlOiAoaWQpID0+IHtcbiAgICAgIGNvbnN0IG9mZnNldCA9IGlkUHJlZml4Lmxlbmd0aDtcbiAgICAgIGxldCBpbmRleCA9IHBhcnNlSW50KGlkLnN1YnN0cmluZyhvZmZzZXQpKTtcbiAgICAgIGxldCBbIGV2ZW50LCBjYWxsYmFjayBdID0gaW5zdGFuY2UubGlzdGVuZXJTdG9yZVtpbmRleF07XG4gICAgICBub2RlLnJlbW92ZUV2ZW50TGlzdGVuZXIoZXZlbnQsIGNhbGxiYWNrKTtcbiAgICAgIGRlbGV0ZSBpbnN0YW5jZS5saXN0ZW5lclN0b3JlW2luZGV4XTtcbiAgICB9LFxuICAgIGNsZWFyQWxsOiAoKSA9PiB7XG4gICAgICBpbnN0YW5jZS5saXN0ZW5lclN0b3JlLmZvckVhY2goaW5zdGFuY2UucmVtb3ZlKTtcbiAgICB9LFxuICAgIG9uS2V5OiAoa2V5LCBjYWxsYmFjaykgPT4ge1xuICAgICAgaWYgKCFrZXkgaW4gS2V5Q29kZXMpIHJldHVybjtcbiAgICAgIHJldHVybiBpbnN0YW5jZS5vbktleUNvZGUoS2V5Q29kZXNba2V5XSwgY2FsbGJhY2spO1xuICAgIH0sXG4gICAgb25LZXlDb2RlOiAoa2V5Q29kZU9yU2V0LCBjYWxsYmFjaykgPT4ge1xuICAgICAgbGV0IGhhbmRsZXIgPSAoZSkgPT4ge1xuICAgICAgICBsZXQgYWNjZXB0YWJsZSA9IEFycmF5LmlzQXJyYXkoa2V5Q29kZU9yU2V0KSA/IGtleUNvZGVPclNldCA6IFsga2V5Q29kZU9yU2V0IF07XG4gICAgICAgIGlmIChhY2NlcHRhYmxlLmluY2x1ZGVzKGUua2V5Q29kZSkpIGNhbGxiYWNrKGUpO1xuICAgICAgfTtcbiAgICAgIHJldHVybiBpbnN0YW5jZS5hZGQoJ2tleWRvd24nLCBoYW5kbGVyKTtcbiAgICB9XG4gIH07XG4gIHJldHVybiBpbnN0YW5jZTtcbn07XG5cbmNvbnN0IEV2ZW50cyA9IEV2ZW50c0ZhY3Rvcnkod2luZG93KTtcbmV4cG9ydCBkZWZhdWx0IEV2ZW50cztcbiJdfQ==