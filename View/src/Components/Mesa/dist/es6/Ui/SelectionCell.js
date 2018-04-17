'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Checkbox = require('../Components/Checkbox');

var _Checkbox2 = _interopRequireDefault(_Checkbox);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var SelectionCell = function (_React$PureComponent) {
  _inherits(SelectionCell, _React$PureComponent);

  function SelectionCell(props) {
    _classCallCheck(this, SelectionCell);

    var _this = _possibleConstructorReturn(this, (SelectionCell.__proto__ || Object.getPrototypeOf(SelectionCell)).call(this, props));

    _this.selectAllRows = _this.selectAllRows.bind(_this);
    _this.deselectAllRows = _this.deselectAllRows.bind(_this);
    _this.renderPageCheckbox = _this.renderPageCheckbox.bind(_this);
    _this.renderRowCheckbox = _this.renderRowCheckbox.bind(_this);
    return _this;
  }

  _createClass(SelectionCell, [{
    key: 'selectAllRows',
    value: function selectAllRows() {
      var _props = this.props,
          rows = _props.rows,
          options = _props.options,
          eventHandlers = _props.eventHandlers;
      var isRowSelected = options.isRowSelected;
      var onRowSelect = eventHandlers.onRowSelect,
          onMultipleRowSelect = eventHandlers.onMultipleRowSelect;

      var unselectedRows = rows.filter(function (row) {
        return !isRowSelected(row);
      });
      if (onMultipleRowSelect) return onMultipleRowSelect(unselectedRows);
      return unselectedRows.forEach(onRowSelect);
    }
  }, {
    key: 'deselectAllRows',
    value: function deselectAllRows() {
      var _props2 = this.props,
          rows = _props2.rows,
          options = _props2.options,
          eventHandlers = _props2.eventHandlers;
      var isRowSelected = options.isRowSelected;
      var onRowDeselect = eventHandlers.onRowDeselect,
          onMultipleRowDeselect = eventHandlers.onMultipleRowDeselect;

      var selection = rows.filter(isRowSelected);
      if (onMultipleRowDeselect) return onMultipleRowDeselect(selection);
      return selection.forEach(onRowDeselect);
    }
  }, {
    key: 'renderPageCheckbox',
    value: function renderPageCheckbox() {
      var _this2 = this;

      var _props3 = this.props,
          rows = _props3.rows,
          isRowSelected = _props3.isRowSelected,
          eventHandlers = _props3.eventHandlers,
          inert = _props3.inert;

      var selection = rows.filter(isRowSelected);
      var checked = rows.every(isRowSelected);

      var handler = function handler(e) {
        e.stopPropagation();
        return checked ? _this2.deselectAllRows() : _this2.selectAllRows();
      };

      return _react2.default.createElement(
        'th',
        { className: 'SelectionCell', onClick: handler },
        inert ? null : _react2.default.createElement(_Checkbox2.default, { checked: checked })
      );
    }
  }, {
    key: 'renderRowCheckbox',
    value: function renderRowCheckbox() {
      var _props4 = this.props,
          row = _props4.row,
          isRowSelected = _props4.isRowSelected,
          eventHandlers = _props4.eventHandlers,
          inert = _props4.inert;
      var onRowSelect = eventHandlers.onRowSelect,
          onRowDeselect = eventHandlers.onRowDeselect;

      var checked = isRowSelected(row);

      var handler = function handler(e) {
        e.stopPropagation();
        return checked ? onRowDeselect(row) : onRowSelect(row);
      };

      return _react2.default.createElement(
        'td',
        { className: 'SelectionCell', onClick: handler },
        inert ? null : _react2.default.createElement(_Checkbox2.default, { checked: checked })
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var heading = this.props.heading;

      return heading ? this.renderPageCheckbox() : this.renderRowCheckbox();
    }
  }]);

  return SelectionCell;
}(_react2.default.PureComponent);

;

exports.default = SelectionCell;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9TZWxlY3Rpb25DZWxsLmpzeCJdLCJuYW1lcyI6WyJTZWxlY3Rpb25DZWxsIiwicHJvcHMiLCJzZWxlY3RBbGxSb3dzIiwiYmluZCIsImRlc2VsZWN0QWxsUm93cyIsInJlbmRlclBhZ2VDaGVja2JveCIsInJlbmRlclJvd0NoZWNrYm94Iiwicm93cyIsIm9wdGlvbnMiLCJldmVudEhhbmRsZXJzIiwiaXNSb3dTZWxlY3RlZCIsIm9uUm93U2VsZWN0Iiwib25NdWx0aXBsZVJvd1NlbGVjdCIsInVuc2VsZWN0ZWRSb3dzIiwiZmlsdGVyIiwicm93IiwiZm9yRWFjaCIsIm9uUm93RGVzZWxlY3QiLCJvbk11bHRpcGxlUm93RGVzZWxlY3QiLCJzZWxlY3Rpb24iLCJpbmVydCIsImNoZWNrZWQiLCJldmVyeSIsImhhbmRsZXIiLCJlIiwic3RvcFByb3BhZ2F0aW9uIiwiaGVhZGluZyIsIlB1cmVDb21wb25lbnQiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7O0FBQUE7Ozs7QUFFQTs7Ozs7Ozs7Ozs7O0lBRU1BLGE7OztBQUNKLHlCQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsOEhBQ1pBLEtBRFk7O0FBRWxCLFVBQUtDLGFBQUwsR0FBcUIsTUFBS0EsYUFBTCxDQUFtQkMsSUFBbkIsT0FBckI7QUFDQSxVQUFLQyxlQUFMLEdBQXVCLE1BQUtBLGVBQUwsQ0FBcUJELElBQXJCLE9BQXZCO0FBQ0EsVUFBS0Usa0JBQUwsR0FBMEIsTUFBS0Esa0JBQUwsQ0FBd0JGLElBQXhCLE9BQTFCO0FBQ0EsVUFBS0csaUJBQUwsR0FBeUIsTUFBS0EsaUJBQUwsQ0FBdUJILElBQXZCLE9BQXpCO0FBTGtCO0FBTW5COzs7O29DQUVnQjtBQUFBLG1CQUMwQixLQUFLRixLQUQvQjtBQUFBLFVBQ1BNLElBRE8sVUFDUEEsSUFETztBQUFBLFVBQ0RDLE9BREMsVUFDREEsT0FEQztBQUFBLFVBQ1FDLGFBRFIsVUFDUUEsYUFEUjtBQUFBLFVBRVBDLGFBRk8sR0FF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ZWxlY3Rpb25DZWxsLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcblxuaW1wb3J0IENoZWNrYm94IGZyb20gJy4uL0NvbXBvbmVudHMvQ2hlY2tib3gnO1xuXG5jbGFzcyBTZWxlY3Rpb25DZWxsIGV4dGVuZHMgUmVhY3QuUHVyZUNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgICB0aGlzLnNlbGVjdEFsbFJvd3MgPSB0aGlzLnNlbGVjdEFsbFJvd3MuYmluZCh0aGlzKTtcbiAgICB0aGlzLmRlc2VsZWN0QWxsUm93cyA9IHRoaXMuZGVzZWxlY3RBbGxSb3dzLmJpbmQodGhpcyk7XG4gICAgdGhpcy5yZW5kZXJQYWdlQ2hlY2tib3ggPSB0aGlzLnJlbmRlclBhZ2VDaGVja2JveC5iaW5kKHRoaXMpO1xuICAgIHRoaXMucmVuZGVyUm93Q2hlY2tib3ggPSB0aGlzLnJlbmRlclJvd0NoZWNrYm94LmJpbmQodGhpcyk7XG4gIH1cblxuICBzZWxlY3RBbGxSb3dzICgpIHtcbiAgICBjb25zdCB7IHJvd3MsIG9wdGlvbnMsIGV2ZW50SGFuZGxlcnMgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBpc1Jvd1NlbGVjdGVkIH0gPSBvcHRpb25zO1xuICAgIGNvbnN0IHsgb25Sb3dTZWxlY3QsIG9uTXVsdGlwbGVSb3dTZWxlY3QgfSA9IGV2ZW50SGFuZGxlcnM7XG4gICAgY29uc3QgdW5zZWxlY3RlZFJvd3MgPSByb3dzLmZpbHRlcihyb3cgPT4gIWlzUm93U2VsZWN0ZWQocm93KSk7XG4gICAgaWYgKG9uTXVsdGlwbGVSb3dTZWxlY3QpIHJldHVybiBvbk11bHRpcGxlUm93U2VsZWN0KHVuc2VsZWN0ZWRSb3dzKTtcbiAgICByZXR1cm4gdW5zZWxlY3RlZFJvd3MuZm9yRWFjaChvblJvd1NlbGVjdCk7XG4gIH1cblxuICBkZXNlbGVjdEFsbFJvd3MgKCkge1xuICAgIGNvbnN0IHsgcm93cywgb3B0aW9ucywgZXZlbnRIYW5kbGVycyB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IGlzUm93U2VsZWN0ZWQgfSA9IG9wdGlvbnM7XG4gICAgY29uc3QgeyBvblJvd0Rlc2VsZWN0LCBvbk11bHRpcGxlUm93RGVzZWxlY3QgfSA9IGV2ZW50SGFuZGxlcnM7XG4gICAgY29uc3Qgc2VsZWN0aW9uID0gcm93cy5maWx0ZXIoaXNSb3dTZWxlY3RlZCk7XG4gICAgaWYgKG9uTXVsdGlwbGVSb3dEZXNlbGVjdCkgcmV0dXJuIG9uTXVsdGlwbGVSb3dEZXNlbGVjdChzZWxlY3Rpb24pO1xuICAgIHJldHVybiBzZWxlY3Rpb24uZm9yRWFjaChvblJvd0Rlc2VsZWN0KTtcbiAgfVxuXG4gIHJlbmRlclBhZ2VDaGVja2JveCAoKSB7XG4gICAgY29uc3QgeyByb3dzLCBpc1Jvd1NlbGVjdGVkLCBldmVudEhhbmRsZXJzLCBpbmVydCB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBzZWxlY3Rpb24gPSByb3dzLmZpbHRlcihpc1Jvd1NlbGVjdGVkKTtcbiAgICBjb25zdCBjaGVja2VkID0gcm93cy5ldmVyeShpc1Jvd1NlbGVjdGVkKTtcblxuICAgIGxldCBoYW5kbGVyID0gKGUpID0+IHtcbiAgICAgIGUuc3RvcFByb3BhZ2F0aW9uKCk7XG4gICAgICByZXR1cm4gY2hlY2tlZFxuICAgICAgICA/IHRoaXMuZGVzZWxlY3RBbGxSb3dzKClcbiAgICAgICAgOiB0aGlzLnNlbGVjdEFsbFJvd3MoKTtcbiAgICB9O1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDx0aCBjbGFzc05hbWU9XCJTZWxlY3Rpb25DZWxsXCIgb25DbGljaz17aGFuZGxlcn0+XG4gICAgICAgIHtpbmVydCA/IG51bGwgOiA8Q2hlY2tib3ggY2hlY2tlZD17Y2hlY2tlZH0gLz59XG4gICAgICA8L3RoPlxuICAgIClcbiAgfVxuXG4gIHJlbmRlclJvd0NoZWNrYm94ICgpIHtcbiAgICBjb25zdCB7IHJvdywgaXNSb3dTZWxlY3RlZCwgZXZlbnRIYW5kbGVycywgaW5lcnQgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBvblJvd1NlbGVjdCwgb25Sb3dEZXNlbGVjdCB9ID0gZXZlbnRIYW5kbGVycztcbiAgICBjb25zdCBjaGVja2VkID0gaXNSb3dTZWxlY3RlZChyb3cpO1xuXG4gICAgbGV0IGhhbmRsZXIgPSAoZSkgPT4ge1xuICAgICAgZS5zdG9wUHJvcGFnYXRpb24oKTtcbiAgICAgIHJldHVybiBjaGVja2VkXG4gICAgICAgID8gb25Sb3dEZXNlbGVjdChyb3cpXG4gICAgICAgIDogb25Sb3dTZWxlY3Qocm93KTtcbiAgICB9O1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDx0ZCBjbGFzc05hbWU9XCJTZWxlY3Rpb25DZWxsXCIgb25DbGljaz17aGFuZGxlcn0+XG4gICAgICAgIHtpbmVydCA/IG51bGwgOiA8Q2hlY2tib3ggY2hlY2tlZD17Y2hlY2tlZH0gLz59XG4gICAgICA8L3RkPlxuICAgICk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGxldCB7IGhlYWRpbmcgfSA9IHRoaXMucHJvcHM7XG4gICAgcmV0dXJuIGhlYWRpbmcgPyB0aGlzLnJlbmRlclBhZ2VDaGVja2JveCgpIDogdGhpcy5yZW5kZXJSb3dDaGVja2JveCgpO1xuICB9XG59O1xuXG5leHBvcnQgZGVmYXVsdCBTZWxlY3Rpb25DZWxsO1xuIl19