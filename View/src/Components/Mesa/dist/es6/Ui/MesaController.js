'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _DataTable = require('../Ui/DataTable');

var _DataTable2 = _interopRequireDefault(_DataTable);

var _TableToolbar = require('../Ui/TableToolbar');

var _TableToolbar2 = _interopRequireDefault(_TableToolbar);

var _ActionToolbar = require('../Ui/ActionToolbar');

var _ActionToolbar2 = _interopRequireDefault(_ActionToolbar);

var _PaginationMenu = require('../Ui/PaginationMenu');

var _PaginationMenu2 = _interopRequireDefault(_PaginationMenu);

var _EmptyState = require('../Ui/EmptyState');

var _EmptyState2 = _interopRequireDefault(_EmptyState);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var MesaController = function (_React$Component) {
  _inherits(MesaController, _React$Component);

  function MesaController(props) {
    _classCallCheck(this, MesaController);

    var _this = _possibleConstructorReturn(this, (MesaController.__proto__ || Object.getPrototypeOf(MesaController)).call(this, props));

    _this.renderToolbar = _this.renderToolbar.bind(_this);
    _this.renderActionBar = _this.renderActionBar.bind(_this);
    _this.renderEmptyState = _this.renderEmptyState.bind(_this);
    _this.renderPaginationMenu = _this.renderPaginationMenu.bind(_this);
    return _this;
  }

  _createClass(MesaController, [{
    key: 'renderPaginationMenu',
    value: function renderPaginationMenu() {
      var _props = this.props,
          uiState = _props.uiState,
          eventHandlers = _props.eventHandlers;

      var _ref = uiState ? uiState : {},
          pagination = _ref.pagination;

      var _ref2 = pagination ? pagination : {},
          currentPage = _ref2.currentPage,
          totalPages = _ref2.totalPages,
          rowsPerPage = _ref2.rowsPerPage;

      var _ref3 = eventHandlers ? eventHandlers : {},
          onPageChange = _ref3.onPageChange,
          onRowsPerPageChange = _ref3.onRowsPerPageChange;

      if (!onPageChange) return null;

      var props = { currentPage: currentPage, totalPages: totalPages, rowsPerPage: rowsPerPage, onPageChange: onPageChange, onRowsPerPageChange: onRowsPerPageChange };
      return _react2.default.createElement(_PaginationMenu2.default, props);
    }
  }, {
    key: 'renderToolbar',
    value: function renderToolbar() {
      var _props2 = this.props,
          rows = _props2.rows,
          options = _props2.options,
          columns = _props2.columns,
          uiState = _props2.uiState,
          eventHandlers = _props2.eventHandlers,
          children = _props2.children;

      var props = { rows: rows, options: options, columns: columns, uiState: uiState, eventHandlers: eventHandlers, children: children };
      if (!options || !options.toolbar) return null;

      return _react2.default.createElement(_TableToolbar2.default, props);
    }
  }, {
    key: 'renderActionBar',
    value: function renderActionBar() {
      var _props3 = this.props,
          rows = _props3.rows,
          options = _props3.options,
          actions = _props3.actions,
          eventHandlers = _props3.eventHandlers,
          children = _props3.children;

      var props = { rows: rows, options: options, actions: actions, eventHandlers: eventHandlers };
      if (!actions || !actions.length) return null;
      if (!this.renderToolbar() && children) props = Object.assign({}, props, { children: children });

      return _react2.default.createElement(_ActionToolbar2.default, props);
    }
  }, {
    key: 'renderEmptyState',
    value: function renderEmptyState() {
      var _props4 = this.props,
          uiState = _props4.uiState,
          options = _props4.options;

      var _ref4 = uiState ? uiState : {},
          emptinessCulprit = _ref4.emptinessCulprit;

      var _ref5 = options ? options : {},
          renderEmptyState = _ref5.renderEmptyState;

      return renderEmptyState ? renderEmptyState() : _react2.default.createElement(_EmptyState2.default, { culprit: emptinessCulprit });
    }
  }, {
    key: 'render',
    value: function render() {
      var _props5 = this.props,
          rows = _props5.rows,
          filteredRows = _props5.filteredRows,
          options = _props5.options,
          columns = _props5.columns,
          actions = _props5.actions,
          uiState = _props5.uiState,
          eventHandlers = _props5.eventHandlers;

      if (!filteredRows) filteredRows = [].concat(_toConsumableArray(rows));
      var props = { rows: rows, filteredRows: filteredRows, options: options, columns: columns, actions: actions, uiState: uiState, eventHandlers: eventHandlers };

      var Body = this.renderBody;
      var Toolbar = this.renderToolbar;
      var ActionBar = this.renderActionBar;
      var PageNav = this.renderPaginationMenu;
      var Empty = this.renderEmptyState;

      return _react2.default.createElement(
        'div',
        { className: 'Mesa MesaComponent' },
        _react2.default.createElement(Toolbar, null),
        _react2.default.createElement(ActionBar, null),
        _react2.default.createElement(PageNav, null),
        filteredRows.length ? _react2.default.createElement(_DataTable2.default, props) : _react2.default.createElement(Empty, null),
        _react2.default.createElement(PageNav, null)
      );
    }
  }]);

  return MesaController;
}(_react2.default.Component);

;

MesaController.propTypes = {
  rows: _propTypes2.default.array.isRequired,
  columns: _propTypes2.default.array.isRequired,
  filteredRows: _propTypes2.default.array,
  options: _propTypes2.default.object,
  actions: _propTypes2.default.arrayOf(_propTypes2.default.shape({
    element: _propTypes2.default.oneOfType([_propTypes2.default.func, _propTypes2.default.node, _propTypes2.default.element]),
    handler: _propTypes2.default.func,
    callback: _propTypes2.default.func
  })),
  uiState: _propTypes2.default.object,
  eventHandlers: _propTypes2.default.objectOf(_propTypes2.default.func)
};

exports.default = MesaController;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9NZXNhQ29udHJvbGxlci5qc3giXSwibmFtZXMiOlsiTWVzYUNvbnRyb2xsZXIiLCJwcm9wcyIsInJlbmRlclRvb2xiYXIiLCJiaW5kIiwicmVuZGVyQWN0aW9uQmFyIiwicmVuZGVyRW1wdHlTdGF0ZSIsInJlbmRlclBhZ2luYXRpb25NZW51IiwidWlTdGF0ZSIsImV2ZW50SGFuZGxlcnMiLCJwYWdpbmF0aW9uIiwiY3VycmVudFBhZ2UiLCJ0b3RhbFBhZ2VzIiwicm93c1BlclBhZ2UiLCJvblBhZ2VDaGFuZ2UiLCJvblJvd3NQZXJQYWdlQ2hhbmdlIiwicm93cyIsIm9wdGlvbnMiLCJjb2x1bW5zIiwiY2hpbGRyZW4iLCJ0b29sYmFyIiwiYWN0aW9ucyIsImxlbmd0aCIsIk9iamVjdCIsImFzc2lnbiIsImVtcHRpbmVzc0N1bHByaXQiLCJmaWx0ZXJlZFJvd3MiLCJCb2R5IiwicmVuZGVyQm9keSIsIlRvb2xiYXIiLCJBY3Rpb25CYXIiLCJQYWdlTmF2IiwiRW1wdHkiLCJDb21wb25lbnQiLCJwcm9wVHlwZXMiLCJhcnJheSIsImlzUmVxdWlyZWQiLCJvYmplY3QiLCJhcnJheU9mIiwic2hhcGUiLCJlbGVtZW50Iiwib25lT2ZUeXBlIiwiZnVuYyIsIm5vZGUiLCJoYW5kbGVyIiwiY2FsbGJhY2siLCJvYmplY3RPZiJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7O0FBRUE7Ozs7QUFDQTs7OztBQUNBOzs7O0FBQ0E7Ozs7QUFDQTs7Ozs7Ozs7Ozs7Ozs7SUFFTUEsYzs7O0FBQ0osMEJBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSxnSUFDWkEsS0FEWTs7QUFFbEIsVUFBS0MsYUFBTCxHQUFxQixNQUFLQSxhQUFMLENBQW1CQyxJQUFuQixPQUFyQjtBQUNBLFVBQUtDLGVBQUwsR0FBdUIsTUFBS0EsZUFBTCxDQUFxQkQsSUFBckIsT0FBdkI7QUFDQSxVQUFLRSxnQkFBTCxHQUF3QixNQUFLQSxnQkFBTCxD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QmhCLFdBQVMsb0JBQVVpQixPQUFWLENBQWtCLG9CQUFVQyxLQUFWLENBQWdCO0FBQ3pDQyxhQUFTLG9CQUFVQyxTQUFWLENBQW9CLENBQUUsb0JBQVVDLElBQVosRUFBa0Isb0JBQVVDLElBQTVCLEVBQWtDLG9CQUFVSCxPQUE1QyxDQUFwQixDQURnQztBQUV6Q0ksYUFBUyxvQkFBVUYsSUFGc0I7QUFHekNHLGNBQVUsb0JBQVVIO0FBSHFCLEdBQWhCLENBQWxCLENBTGdCO0FBVXpCbEMsV0FBUyxvQkFBVTZCLE1BVk07QUFXekI1QixpQkFBZSxvQkFBVXFDLFFBQVYsQ0FBbUIsb0JBQVVKLElBQTdCO0FBWFUsQ0FBM0I7O2tCQWNlekMsYyIsImZpbGUiOiJNZXNhQ29udHJvbGxlci5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5pbXBvcnQgUHJvcFR5cGVzIGZyb20gJ3Byb3AtdHlwZXMnO1xuXG5pbXBvcnQgRGF0YVRhYmxlIGZyb20gJy4uL1VpL0RhdGFUYWJsZSc7XG5pbXBvcnQgVGFibGVUb29sYmFyIGZyb20gJy4uL1VpL1RhYmxlVG9vbGJhcic7XG5pbXBvcnQgQWN0aW9uVG9vbGJhciBmcm9tICcuLi9VaS9BY3Rpb25Ub29sYmFyJztcbmltcG9ydCBQYWdpbmF0aW9uTWVudSBmcm9tICcuLi9VaS9QYWdpbmF0aW9uTWVudSc7XG5pbXBvcnQgRW1wdHlTdGF0ZSBmcm9tICcuLi9VaS9FbXB0eVN0YXRlJztcblxuY2xhc3MgTWVzYUNvbnRyb2xsZXIgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG4gICAgdGhpcy5yZW5kZXJUb29sYmFyID0gdGhpcy5yZW5kZXJUb29sYmFyLmJpbmQodGhpcyk7XG4gICAgdGhpcy5yZW5kZXJBY3Rpb25CYXIgPSB0aGlzLnJlbmRlckFjdGlvbkJhci5iaW5kKHRoaXMpO1xuICAgIHRoaXMucmVuZGVyRW1wdHlTdGF0ZSA9IHRoaXMucmVuZGVyRW1wdHlTdGF0ZS5iaW5kKHRoaXMpO1xuICAgIHRoaXMucmVuZGVyUGFnaW5hdGlvbk1lbnUgPSB0aGlzLnJlbmRlclBhZ2luYXRpb25NZW51LmJpbmQodGhpcyk7XG4gIH1cblxuICByZW5kZXJQYWdpbmF0aW9uTWVudSAoKSB7XG4gICAgY29uc3QgeyB1aVN0YXRlLCBldmVudEhhbmRsZXJzIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgcGFnaW5hdGlvbiB9ID0gdWlTdGF0ZSA/IHVpU3RhdGUgOiB7fTtcbiAgICBjb25zdCB7IGN1cnJlbnRQYWdlLCB0b3RhbFBhZ2VzLCByb3dzUGVyUGFnZSB9ID0gcGFnaW5hdGlvbiA/IHBhZ2luYXRpb24gOiB7fTtcbiAgICBjb25zdCB7IG9uUGFnZUNoYW5nZSwgb25Sb3dzUGVyUGFnZUNoYW5nZSB9ID0gZXZlbnRIYW5kbGVycyA/IGV2ZW50SGFuZGxlcnMgOiB7fTtcblxuICAgIGlmICghb25QYWdlQ2hhbmdlKSByZXR1cm4gbnVsbDtcblxuICAgIGNvbnN0IHByb3BzID0geyBjdXJyZW50UGFnZSwgdG90YWxQYWdlcywgcm93c1BlclBhZ2UsIG9uUGFnZUNoYW5nZSwgb25Sb3dzUGVyUGFnZUNoYW5nZSB9O1xuICAgIHJldHVybiA8UGFnaW5hdGlvbk1lbnUgey4uLnByb3BzfSAvPlxuICB9XG5cbiAgcmVuZGVyVG9vbGJhciAoKSB7XG4gICAgY29uc3QgeyByb3dzLCBvcHRpb25zLCBjb2x1bW5zLCB1aVN0YXRlLCBldmVudEhhbmRsZXJzLCBjaGlsZHJlbiB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBwcm9wcyA9IHsgcm93cywgb3B0aW9ucywgY29sdW1ucywgdWlTdGF0ZSwgZXZlbnRIYW5kbGVycywgY2hpbGRyZW4gfTtcbiAgICBpZiAoIW9wdGlvbnMgfHwgIW9wdGlvbnMudG9vbGJhcikgcmV0dXJuIG51bGw7XG5cbiAgICByZXR1cm4gPFRhYmxlVG9vbGJhciB7Li4ucHJvcHN9IC8+XG4gIH1cblxuICByZW5kZXJBY3Rpb25CYXIgKCkge1xuICAgIGNvbnN0IHsgcm93cywgb3B0aW9ucywgYWN0aW9ucywgZXZlbnRIYW5kbGVycywgY2hpbGRyZW4gfSA9IHRoaXMucHJvcHM7XG4gICAgbGV0IHByb3BzID0geyByb3dzLCBvcHRpb25zLCBhY3Rpb25zLCBldmVudEhhbmRsZXJzIH07XG4gICAgaWYgKCFhY3Rpb25zIHx8ICFhY3Rpb25zLmxlbmd0aCkgcmV0dXJuIG51bGw7XG4gICAgaWYgKCF0aGlzLnJlbmRlclRvb2xiYXIoKSAmJiBjaGlsZHJlbikgcHJvcHMgPSBPYmplY3QuYXNzaWduKHt9LCBwcm9wcywgeyBjaGlsZHJlbiB9KTtcblxuICAgIHJldHVybiA8QWN0aW9uVG9vbGJhciB7Li4ucHJvcHN9IC8+XG4gIH1cblxuICByZW5kZXJFbXB0eVN0YXRlICgpIHtcbiAgICBjb25zdCB7IHVpU3RhdGUsIG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBlbXB0aW5lc3NDdWxwcml0IH0gPSB1aVN0YXRlID8gdWlTdGF0ZSA6IHt9O1xuICAgIGNvbnN0IHsgcmVuZGVyRW1wdHlTdGF0ZSB9ID0gb3B0aW9ucyA/IG9wdGlvbnMgOiB7fTtcblxuICAgIHJldHVybiByZW5kZXJFbXB0eVN0YXRlID8gcmVuZGVyRW1wdHlTdGF0ZSgpIDogPEVtcHR5U3RhdGUgY3VscHJpdD17ZW1wdGluZXNzQ3VscHJpdH0gLz5cbiAgfVxuXG4gIHJlbmRlciAoKSB7XG4gICAgbGV0IHsgcm93cywgZmlsdGVyZWRSb3dzLCBvcHRpb25zLCBjb2x1bW5zLCBhY3Rpb25zLCB1aVN0YXRlLCBldmVudEhhbmRsZXJzIH0gPSB0aGlzLnByb3BzO1xuICAgIGlmICghZmlsdGVyZWRSb3dzKSBmaWx0ZXJlZFJvd3MgPSBbLi4ucm93c107XG4gICAgY29uc3QgcHJvcHMgPSB7IHJvd3MsIGZpbHRlcmVkUm93cywgb3B0aW9ucywgY29sdW1ucywgYWN0aW9ucywgdWlTdGF0ZSwgZXZlbnRIYW5kbGVycyB9O1xuXG4gICAgY29uc3QgQm9keSA9IHRoaXMucmVuZGVyQm9keTtcbiAgICBjb25zdCBUb29sYmFyID0gdGhpcy5yZW5kZXJUb29sYmFyO1xuICAgIGNvbnN0IEFjdGlvbkJhciA9IHRoaXMucmVuZGVyQWN0aW9uQmFyO1xuICAgIGNvbnN0IFBhZ2VOYXYgPSB0aGlzLnJlbmRlclBhZ2luYXRpb25NZW51O1xuICAgIGNvbnN0IEVtcHR5ID0gdGhpcy5yZW5kZXJFbXB0eVN0YXRlO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiTWVzYSBNZXNhQ29tcG9uZW50XCI+XG4gICAgICAgIDxUb29sYmFyIC8+XG4gICAgICAgIDxBY3Rpb25CYXIgLz5cbiAgICAgICAgPFBhZ2VOYXYgLz5cbiAgICAgICAge2ZpbHRlcmVkUm93cy5sZW5ndGhcbiAgICAgICAgICA/IDxEYXRhVGFibGUgey4uLnByb3BzfSAvPlxuICAgICAgICAgIDogPEVtcHR5IC8+XG4gICAgICAgIH1cbiAgICAgICAgPFBhZ2VOYXYgLz5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn07XG5cbk1lc2FDb250cm9sbGVyLnByb3BUeXBlcyA9IHtcbiAgcm93czogUHJvcFR5cGVzLmFycmF5LmlzUmVxdWlyZWQsXG4gIGNvbHVtbnM6IFByb3BUeXBlcy5hcnJheS5pc1JlcXVpcmVkLFxuICBmaWx0ZXJlZFJvd3M6IFByb3BUeXBlcy5hcnJheSxcbiAgb3B0aW9uczogUHJvcFR5cGVzLm9iamVjdCxcbiAgYWN0aW9uczogUHJvcFR5cGVzLmFycmF5T2YoUHJvcFR5cGVzLnNoYXBlKHtcbiAgICBlbGVtZW50OiBQcm9wVHlwZXMub25lT2ZUeXBlKFsgUHJvcFR5cGVzLmZ1bmMsIFByb3BUeXBlcy5ub2RlLCBQcm9wVHlwZXMuZWxlbWVudCBdKSxcbiAgICBoYW5kbGVyOiBQcm9wVHlwZXMuZnVuYyxcbiAgICBjYWxsYmFjazogUHJvcFR5cGVzLmZ1bmNcbiAgfSkpLFxuICB1aVN0YXRlOiBQcm9wVHlwZXMub2JqZWN0LFxuICBldmVudEhhbmRsZXJzOiBQcm9wVHlwZXMub2JqZWN0T2YoUHJvcFR5cGVzLmZ1bmMpXG59O1xuXG5leHBvcnQgZGVmYXVsdCBNZXNhQ29udHJvbGxlcjtcbiJdfQ==