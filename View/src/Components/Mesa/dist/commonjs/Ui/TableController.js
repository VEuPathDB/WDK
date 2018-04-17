'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _TableBody = require('../Ui/TableBody');

var _TableBody2 = _interopRequireDefault(_TableBody);

var _RowUtils = require('../Utils/RowUtils');

var _RowUtils2 = _interopRequireDefault(_RowUtils);

var _TableToolbar = require('../Ui/TableToolbar');

var _TableToolbar2 = _interopRequireDefault(_TableToolbar);

var _ActionToolbar = require('../Ui/ActionToolbar');

var _ActionToolbar2 = _interopRequireDefault(_ActionToolbar);

var _PaginationMenu = require('../Ui/PaginationMenu');

var _PaginationMenu2 = _interopRequireDefault(_PaginationMenu);

var _Actions = require('../State/Actions');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var TableController = function (_React$Component) {
  _inherits(TableController, _React$Component);

  function TableController(props) {
    _classCallCheck(this, TableController);

    var _this = _possibleConstructorReturn(this, (TableController.__proto__ || Object.getPrototypeOf(TableController)).call(this, props));

    _this.getFilteredRows = _this.getFilteredRows.bind(_this);
    return _this;
  }

  _createClass(TableController, [{
    key: 'getFilteredRows',
    value: function getFilteredRows() {
      var _props = this.props,
          state = _props.state,
          dispatch = _props.dispatch;
      var rows = state.rows,
          ui = state.ui,
          columns = state.columns;
      var searchQuery = ui.searchQuery,
          sort = ui.sort,
          emptinessCulprit = ui.emptinessCulprit;


      if (!rows.length) {
        if (emptinessCulprit && emptinessCulprit !== 'nodata') dispatch((0, _Actions.setEmptinessCulprit)('nodata'));
        return [];
      }

      if (searchQuery && searchQuery.length) rows = _RowUtils2.default.searchRowsForQuery(rows, columns, searchQuery);
      if (!rows.length) {
        if (emptinessCulprit !== 'search') dispatch((0, _Actions.setEmptinessCulprit)('search'));
        return [];
      }

      rows = _RowUtils2.default.filterRowsByColumns(rows, columns);
      if (!rows.length) {
        if (emptinessCulprit !== 'filters') dispatch((0, _Actions.setEmptinessCulprit)('filters'));
        return [];
      }

      if (sort.byColumn) rows = _RowUtils2.default.sortRowsByColumn(rows, sort.byColumn, sort.ascending);

      return rows;
    }
  }, {
    key: 'render',
    value: function render() {
      var _props2 = this.props,
          state = _props2.state,
          dispatch = _props2.dispatch,
          children = _props2.children;
      var ui = state.ui,
          options = state.options,
          actions = state.actions;
      var pagination = ui.pagination;


      var filteredRows = this.getFilteredRows();

      var PageNav = function PageNav() {
        return !options.paginate ? null : _react2.default.createElement(_PaginationMenu2.default, {
          dispatch: dispatch,
          list: filteredRows,
          pagination: pagination
        });
      };

      return _react2.default.createElement(
        'div',
        { className: 'TableController' },
        !options.toolbar ? _react2.default.createElement(
          'div',
          null,
          children
        ) : _react2.default.createElement(
          _TableToolbar2.default,
          {
            state: state,
            dispatch: dispatch,
            filteredRows: filteredRows },
          children
        ),
        !actions.length ? null : _react2.default.createElement(_ActionToolbar2.default, {
          state: state,
          dispatch: dispatch,
          filteredRows: filteredRows
        }),
        _react2.default.createElement(PageNav, null),
        _react2.default.createElement(_TableBody2.default, {
          state: state,
          dispatch: dispatch,
          filteredRows: filteredRows
        }),
        _react2.default.createElement(PageNav, null)
      );
    }
  }]);

  return TableController;
}(_react2.default.Component);

;

exports.default = TableController;