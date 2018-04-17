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
        rows.length ? _react2.default.createElement(_DataTable2.default, props) : _react2.default.createElement(Empty, null),
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9NZXNhQ29udHJvbGxlci5qc3giXSwibmFtZXMiOlsiTWVzYUNvbnRyb2xsZXIiLCJwcm9wcyIsInJlbmRlclRvb2xiYXIiLCJiaW5kIiwicmVuZGVyQWN0aW9uQmFyIiwicmVuZGVyRW1wdHlTdGF0ZSIsInJlbmRlclBhZ2luYXRpb25NZW51IiwidWlTdGF0ZSIsImV2ZW50SGFuZGxlcnMiLCJwYWdpbmF0aW9uIiwiY3VycmVudFBhZ2UiLCJ0b3RhbFBhZ2VzIiwicm93c1BlclBhZ2UiLCJvblBhZ2VDaGFuZ2UiLCJvblJvd3NQZXJQYWdlQ2hhbmdlIiwicm93cyIsIm9wdGlvbnMiLCJjb2x1bW5zIiwiY2hpbGRyZW4iLCJ0b29sYmFyIiwiYWN0aW9ucyIsImxlbmd0aCIsIk9iamVjdCIsImFzc2lnbiIsImVtcHRpbmVzc0N1bHByaXQiLCJmaWx0ZXJlZFJvd3MiLCJCb2R5IiwicmVuZGVyQm9keSIsIlRvb2xiYXIiLCJBY3Rpb25CYXIiLCJQYWdlTmF2IiwiRW1wdHkiLCJDb21wb25lbnQiLCJwcm9wVHlwZXMiLCJhcnJheSIsImlzUmVxdWlyZWQiLCJvYmplY3QiLCJhcnJheU9mIiwic2hhcGUiLCJlbGVtZW50Iiwib25lT2ZUeXBlIiwiZnVuYyIsIm5vZGUiLCJoYW5kbGVyIiwiY2FsbGJhY2siLCJvYmplY3RPZiJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7O0FBRUE7Ozs7QUFDQTs7OztBQUNBOzs7O0FBQ0E7Ozs7QUFDQTs7Ozs7Ozs7Ozs7Ozs7SUFFTUEsYzs7O0FBQ0osMEJBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSxnSUFDWkEsS0FEWTs7QUFFbEIsVUFBS0MsYUFBTCxHQUFxQixNQUFLQSxhQUFMLENBQW1CQyxJQUFuQixPQUFyQjtBQUNBLFVBQUtDLGVBQUwsR0FBdUIsTUFBS0EsZUFBTCxDQUFxQkQsSUFBckIsT0FBdkI7QUFDQSxVQUFLRSxnQkFBTCxHQUF3QixNQUFLQSxnQkFBTCxD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QixXQUFTLG9CQUFVaUIsT0FBVixDQUFrQixvQkFBVUMsS0FBVixDQUFnQjtBQUN6Q0MsYUFBUyxvQkFBVUMsU0FBVixDQUFvQixDQUFFLG9CQUFVQyxJQUFaLEVBQWtCLG9CQUFVQyxJQUE1QixFQUFrQyxvQkFBVUgsT0FBNUMsQ0FBcEIsQ0FEZ0M7QUFFekNJLGFBQVMsb0JBQVVGLElBRnNCO0FBR3pDRyxjQUFVLG9CQUFVSDtBQUhxQixHQUFoQixDQUFsQixDQUxnQjtBQVV6QmxDLFdBQVMsb0JBQVU2QixNQVZNO0FBV3pCNUIsaUJBQWUsb0JBQVVxQyxRQUFWLENBQW1CLG9CQUFVSixJQUE3QjtBQVhVLENBQTNCOztrQkFjZXpDLGMiLCJmaWxlIjoiTWVzYUNvbnRyb2xsZXIuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IFByb3BUeXBlcyBmcm9tICdwcm9wLXR5cGVzJztcblxuaW1wb3J0IERhdGFUYWJsZSBmcm9tICcuLi9VaS9EYXRhVGFibGUnO1xuaW1wb3J0IFRhYmxlVG9vbGJhciBmcm9tICcuLi9VaS9UYWJsZVRvb2xiYXInO1xuaW1wb3J0IEFjdGlvblRvb2xiYXIgZnJvbSAnLi4vVWkvQWN0aW9uVG9vbGJhcic7XG5pbXBvcnQgUGFnaW5hdGlvbk1lbnUgZnJvbSAnLi4vVWkvUGFnaW5hdGlvbk1lbnUnO1xuaW1wb3J0IEVtcHR5U3RhdGUgZnJvbSAnLi4vVWkvRW1wdHlTdGF0ZSc7XG5cbmNsYXNzIE1lc2FDb250cm9sbGVyIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuICAgIHRoaXMucmVuZGVyVG9vbGJhciA9IHRoaXMucmVuZGVyVG9vbGJhci5iaW5kKHRoaXMpO1xuICAgIHRoaXMucmVuZGVyQWN0aW9uQmFyID0gdGhpcy5yZW5kZXJBY3Rpb25CYXIuYmluZCh0aGlzKTtcbiAgICB0aGlzLnJlbmRlckVtcHR5U3RhdGUgPSB0aGlzLnJlbmRlckVtcHR5U3RhdGUuYmluZCh0aGlzKTtcbiAgICB0aGlzLnJlbmRlclBhZ2luYXRpb25NZW51ID0gdGhpcy5yZW5kZXJQYWdpbmF0aW9uTWVudS5iaW5kKHRoaXMpO1xuICB9XG5cbiAgcmVuZGVyUGFnaW5hdGlvbk1lbnUgKCkge1xuICAgIGNvbnN0IHsgdWlTdGF0ZSwgZXZlbnRIYW5kbGVycyB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IHBhZ2luYXRpb24gfSA9IHVpU3RhdGUgPyB1aVN0YXRlIDoge307XG4gICAgY29uc3QgeyBjdXJyZW50UGFnZSwgdG90YWxQYWdlcywgcm93c1BlclBhZ2UgfSA9IHBhZ2luYXRpb24gPyBwYWdpbmF0aW9uIDoge307XG4gICAgY29uc3QgeyBvblBhZ2VDaGFuZ2UsIG9uUm93c1BlclBhZ2VDaGFuZ2UgfSA9IGV2ZW50SGFuZGxlcnMgPyBldmVudEhhbmRsZXJzIDoge307XG5cbiAgICBpZiAoIW9uUGFnZUNoYW5nZSkgcmV0dXJuIG51bGw7XG5cbiAgICBjb25zdCBwcm9wcyA9IHsgY3VycmVudFBhZ2UsIHRvdGFsUGFnZXMsIHJvd3NQZXJQYWdlLCBvblBhZ2VDaGFuZ2UsIG9uUm93c1BlclBhZ2VDaGFuZ2UgfTtcbiAgICByZXR1cm4gPFBhZ2luYXRpb25NZW51IHsuLi5wcm9wc30gLz5cbiAgfVxuXG4gIHJlbmRlclRvb2xiYXIgKCkge1xuICAgIGNvbnN0IHsgcm93cywgb3B0aW9ucywgY29sdW1ucywgdWlTdGF0ZSwgZXZlbnRIYW5kbGVycywgY2hpbGRyZW4gfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgcHJvcHMgPSB7IHJvd3MsIG9wdGlvbnMsIGNvbHVtbnMsIHVpU3RhdGUsIGV2ZW50SGFuZGxlcnMsIGNoaWxkcmVuIH07XG4gICAgaWYgKCFvcHRpb25zIHx8ICFvcHRpb25zLnRvb2xiYXIpIHJldHVybiBudWxsO1xuXG4gICAgcmV0dXJuIDxUYWJsZVRvb2xiYXIgey4uLnByb3BzfSAvPlxuICB9XG5cbiAgcmVuZGVyQWN0aW9uQmFyICgpIHtcbiAgICBjb25zdCB7IHJvd3MsIG9wdGlvbnMsIGFjdGlvbnMsIGV2ZW50SGFuZGxlcnMsIGNoaWxkcmVuIH0gPSB0aGlzLnByb3BzO1xuICAgIGxldCBwcm9wcyA9IHsgcm93cywgb3B0aW9ucywgYWN0aW9ucywgZXZlbnRIYW5kbGVycyB9O1xuICAgIGlmICghYWN0aW9ucyB8fCAhYWN0aW9ucy5sZW5ndGgpIHJldHVybiBudWxsO1xuICAgIGlmICghdGhpcy5yZW5kZXJUb29sYmFyKCkgJiYgY2hpbGRyZW4pIHByb3BzID0gT2JqZWN0LmFzc2lnbih7fSwgcHJvcHMsIHsgY2hpbGRyZW4gfSk7XG5cbiAgICByZXR1cm4gPEFjdGlvblRvb2xiYXIgey4uLnByb3BzfSAvPlxuICB9XG5cbiAgcmVuZGVyRW1wdHlTdGF0ZSAoKSB7XG4gICAgY29uc3QgeyB1aVN0YXRlLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgZW1wdGluZXNzQ3VscHJpdCB9ID0gdWlTdGF0ZSA/IHVpU3RhdGUgOiB7fTtcbiAgICBjb25zdCB7IHJlbmRlckVtcHR5U3RhdGUgfSA9IG9wdGlvbnMgPyBvcHRpb25zIDoge307XG5cbiAgICByZXR1cm4gcmVuZGVyRW1wdHlTdGF0ZSA/IHJlbmRlckVtcHR5U3RhdGUoKSA6IDxFbXB0eVN0YXRlIGN1bHByaXQ9e2VtcHRpbmVzc0N1bHByaXR9IC8+XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGxldCB7IHJvd3MsIGZpbHRlcmVkUm93cywgb3B0aW9ucywgY29sdW1ucywgYWN0aW9ucywgdWlTdGF0ZSwgZXZlbnRIYW5kbGVycyB9ID0gdGhpcy5wcm9wcztcbiAgICBpZiAoIWZpbHRlcmVkUm93cykgZmlsdGVyZWRSb3dzID0gWy4uLnJvd3NdO1xuICAgIGNvbnN0IHByb3BzID0geyByb3dzLCBmaWx0ZXJlZFJvd3MsIG9wdGlvbnMsIGNvbHVtbnMsIGFjdGlvbnMsIHVpU3RhdGUsIGV2ZW50SGFuZGxlcnMgfTtcblxuICAgIGNvbnN0IEJvZHkgPSB0aGlzLnJlbmRlckJvZHk7XG4gICAgY29uc3QgVG9vbGJhciA9IHRoaXMucmVuZGVyVG9vbGJhcjtcbiAgICBjb25zdCBBY3Rpb25CYXIgPSB0aGlzLnJlbmRlckFjdGlvbkJhcjtcbiAgICBjb25zdCBQYWdlTmF2ID0gdGhpcy5yZW5kZXJQYWdpbmF0aW9uTWVudTtcbiAgICBjb25zdCBFbXB0eSA9IHRoaXMucmVuZGVyRW1wdHlTdGF0ZTtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cIk1lc2EgTWVzYUNvbXBvbmVudFwiPlxuICAgICAgICA8VG9vbGJhciAvPlxuICAgICAgICA8QWN0aW9uQmFyIC8+XG4gICAgICAgIDxQYWdlTmF2IC8+XG4gICAgICAgIHtyb3dzLmxlbmd0aFxuICAgICAgICAgID8gPERhdGFUYWJsZSB7Li4ucHJvcHN9IC8+XG4gICAgICAgICAgOiA8RW1wdHkgLz5cbiAgICAgICAgfVxuICAgICAgICA8UGFnZU5hdiAvPlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufTtcblxuTWVzYUNvbnRyb2xsZXIucHJvcFR5cGVzID0ge1xuICByb3dzOiBQcm9wVHlwZXMuYXJyYXkuaXNSZXF1aXJlZCxcbiAgY29sdW1uczogUHJvcFR5cGVzLmFycmF5LmlzUmVxdWlyZWQsXG4gIGZpbHRlcmVkUm93czogUHJvcFR5cGVzLmFycmF5LFxuICBvcHRpb25zOiBQcm9wVHlwZXMub2JqZWN0LFxuICBhY3Rpb25zOiBQcm9wVHlwZXMuYXJyYXlPZihQcm9wVHlwZXMuc2hhcGUoe1xuICAgIGVsZW1lbnQ6IFByb3BUeXBlcy5vbmVPZlR5cGUoWyBQcm9wVHlwZXMuZnVuYywgUHJvcFR5cGVzLm5vZGUsIFByb3BUeXBlcy5lbGVtZW50IF0pLFxuICAgIGhhbmRsZXI6IFByb3BUeXBlcy5mdW5jLFxuICAgIGNhbGxiYWNrOiBQcm9wVHlwZXMuZnVuY1xuICB9KSksXG4gIHVpU3RhdGU6IFByb3BUeXBlcy5vYmplY3QsXG4gIGV2ZW50SGFuZGxlcnM6IFByb3BUeXBlcy5vYmplY3RPZihQcm9wVHlwZXMuZnVuYylcbn07XG5cbmV4cG9ydCBkZWZhdWx0IE1lc2FDb250cm9sbGVyO1xuIl19