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

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Mesa = function (_React$Component) {
  _inherits(Mesa, _React$Component);

  function Mesa(props) {
    _classCallCheck(this, Mesa);

    var _this = _possibleConstructorReturn(this, (Mesa.__proto__ || Object.getPrototypeOf(Mesa)).call(this, props));

    _this.renderToolbar = _this.renderToolbar.bind(_this);
    _this.renderActionBar = _this.renderActionBar.bind(_this);
    _this.renderPaginationMenu = _this.renderPaginationMenu.bind(_this);
    _this.renderEmptyState = _this.renderEmptyState.bind(_this);
    _this.renderBody = _this.renderBody.bind(_this);
    return _this;
  }

  _createClass(Mesa, [{
    key: 'renderPaginationMenu',
    value: function renderPaginationMenu() {
      var _props = this.props,
          uiState = _props.uiState,
          eventHandlers = _props.eventHandlers;

      var _ref = uiState ? uiState.pagination : {},
          currentPage = _ref.currentPage,
          totalPages = _ref.totalPages,
          rowsPerPage = _ref.rowsPerPage;

      var _ref2 = eventHandlers ? eventHandlers : {},
          onPageChange = _ref2.onPageChange,
          onRowsPerPageChange = _ref2.onRowsPerPageChange;

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
      if (!options.toolbar) return _react2.default.createElement(
        'div',
        null,
        children
      );

      return _react2.default.createElement(_TableToolbar2.default, props);
    }
  }, {
    key: 'renderActionBar',
    value: function renderActionBar() {
      var _props3 = this.props,
          rows = _props3.rows,
          options = _props3.options,
          actions = _props3.actions,
          eventHandlers = _props3.eventHandlers;

      var props = { rows: rows, options: options, actions: actions, eventHandlers: eventHandlers };
      if (!actions.length) return null;

      return _react2.default.createElement(_ActionToolbar2.default, props);
    }
  }, {
    key: 'renderEmptyState',
    value: function renderEmptyState() {
      var uiState = this.props.uiState;

      var _ref3 = uiState ? uiState : {},
          emptinessCulprit = _ref3.emptinessCulprit;

      return _react2.default.createElement(_EmptyState2.default, { culprit: emptinessCulprit });
    }
  }, {
    key: 'renderBody',
    value: function renderBody() {
      var _props4 = this.props,
          rows = _props4.rows,
          options = _props4.options,
          columns = _props4.columns,
          actions = _props4.actions,
          uiState = _props4.uiState,
          eventHandlers = _props4.eventHandlers;

      var props = { rows: rows, options: options, columns: columns, actions: actions, uiState: uiState, eventHandlers: eventHandlers };
      var Empty = this.renderEmptyState;

      return rows.length ? _react2.default.createElement(_DataTable2.default, props) : _react2.default.createElement(Empty, null);
    }
  }, {
    key: 'render',
    value: function render() {
      var _props5 = this.props,
          rows = _props5.rows,
          options = _props5.options,
          columns = _props5.columns,
          actions = _props5.actions,
          uiState = _props5.uiState,
          eventHandlers = _props5.eventHandlers;

      var props = { rows: rows, options: options, columns: columns, actions: actions, uiState: uiState, eventHandlers: eventHandlers };

      var Body = this.renderBody;
      var Toolbar = this.renderToolbar;
      var ActionBar = this.renderActionBar;
      var PageNav = this.renderPaginationMenu;

      return _react2.default.createElement(
        'div',
        { className: 'Mesa' },
        _react2.default.createElement(Toolbar, null),
        _react2.default.createElement(ActionBar, null),
        _react2.default.createElement(PageNav, null),
        _react2.default.createElement(Body, null),
        _react2.default.createElement(PageNav, null)
      );
    }
  }]);

  return Mesa;
}(_react2.default.Component);

;

_DataTable2.default.propTypes = {
  rows: _propTypes2.default.array,
  columns: _propTypes2.default.array,
  options: _propTypes2.default.object,
  actions: _propTypes2.default.arrayOf(_propTypes2.default.shape({
    element: _propTypes2.default.oneOfType([_propTypes2.default.func, _propTypes2.default.node, _propTypes2.default.element]),
    handler: _propTypes2.default.func,
    callback: _propTypes2.default.func
  })),
  uiState: _propTypes2.default.object,
  eventHandlers: _propTypes2.default.objectOf(_propTypes2.default.func)
};

exports.default = Mesa;