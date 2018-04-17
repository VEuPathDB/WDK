'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

var _TableSearch = require('../Ui/TableSearch');

var _TableSearch2 = _interopRequireDefault(_TableSearch);

var _ColumnEditor = require('../Ui/ColumnEditor');

var _ColumnEditor2 = _interopRequireDefault(_ColumnEditor);

var _RowCounter = require('../Ui/RowCounter');

var _RowCounter2 = _interopRequireDefault(_RowCounter);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var TableToolbar = function (_React$PureComponent) {
  _inherits(TableToolbar, _React$PureComponent);

  function TableToolbar(props) {
    _classCallCheck(this, TableToolbar);

    var _this = _possibleConstructorReturn(this, (TableToolbar.__proto__ || Object.getPrototypeOf(TableToolbar)).call(this, props));

    _this.renderTitle = _this.renderTitle.bind(_this);
    _this.renderSearch = _this.renderSearch.bind(_this);
    _this.renderCounter = _this.renderCounter.bind(_this);
    _this.renderChildren = _this.renderChildren.bind(_this);
    _this.renderAddRemoveColumns = _this.renderAddRemoveColumns.bind(_this);
    return _this;
  }

  _createClass(TableToolbar, [{
    key: 'renderTitle',
    value: function renderTitle() {
      var options = this.props.options;
      var title = options.title;


      if (!title) return null;
      return _react2.default.createElement(
        'h1',
        { className: 'TableToolbar-Title' },
        title
      );
    }
  }, {
    key: 'renderSearch',
    value: function renderSearch() {
      var _props = this.props,
          options = _props.options,
          uiState = _props.uiState,
          eventHandlers = _props.eventHandlers;
      var onSearch = eventHandlers.onSearch;
      var searchQuery = uiState.searchQuery;


      if (!onSearch) return;
      return _react2.default.createElement(_TableSearch2.default, {
        query: searchQuery,
        onSearch: onSearch
      });
    }
  }, {
    key: 'renderCounter',
    value: function renderCounter() {
      var _props2 = this.props,
          rows = _props2.rows,
          options = _props2.options,
          uiState = _props2.uiState,
          eventHandlers = _props2.eventHandlers;
      var pagination = uiState.pagination,
          filteredRowCount = uiState.filteredRowCount;
      var totalRows = pagination.totalRows,
          rowsPerPage = pagination.rowsPerPage;
      var showCount = options.showCount;

      if (!showCount) return null;

      var isPaginated = 'onPageChange' in eventHandlers;
      var isSearching = uiState.searchQuery && uiState.searchQuery.length;

      var count = totalRows ? totalRows : rows.length;
      var noun = (isSearching ? 'result' : 'row') + (count % rowsPerPage === 1 ? '' : 's');
      var start = !isPaginated ? null : (pagination.currentPage - 1) * rowsPerPage + 1;
      var end = !isPaginated ? null : start + rowsPerPage > count ? count : start - 1 + rowsPerPage;

      var props = { count: count, noun: noun, start: start, end: end, filteredRowCount: filteredRowCount };

      return _react2.default.createElement(
        'div',
        { className: 'TableToolbar-Info' },
        _react2.default.createElement(_RowCounter2.default, props)
      );
    }
  }, {
    key: 'renderChildren',
    value: function renderChildren() {
      var children = this.props.children;

      if (!children) return null;

      return _react2.default.createElement(
        'div',
        { className: 'TableToolbar-Children' },
        children
      );
    }
  }, {
    key: 'renderAddRemoveColumns',
    value: function renderAddRemoveColumns() {
      var _props3 = this.props,
          options = _props3.options,
          columns = _props3.columns,
          eventHandlers = _props3.eventHandlers;
      var editableColumns = options.editableColumns;

      var columnsAreHideable = columns.some(function (column) {
        return column.hideable;
      });
      if (!editableColumns || !columnsAreHideable) return null;

      var onShowColumn = eventHandlers.onShowColumn,
          onHideColumn = eventHandlers.onHideColumn;

      return _react2.default.createElement(
        _ColumnEditor2.default,
        {
          columns: columns,
          onShowColumn: onShowColumn,
          onHideColumn: onHideColumn
        },
        _react2.default.createElement(
          'button',
          null,
          _react2.default.createElement(_Icon2.default, { fa: 'columns' }),
          _react2.default.createElement(
            'span',
            null,
            'Add/Remove Columns'
          )
        )
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var Title = this.renderTitle;
      var Search = this.renderSearch;
      var Counter = this.renderCounter;
      var Children = this.renderChildren;
      var AddRemove = this.renderAddRemoveColumns;

      return _react2.default.createElement(
        'div',
        { className: 'Toolbar TableToolbar' },
        _react2.default.createElement(Title, null),
        _react2.default.createElement(Search, null),
        _react2.default.createElement(Counter, null),
        _react2.default.createElement(Children, null),
        _react2.default.createElement(AddRemove, null)
      );
    }
  }]);

  return TableToolbar;
}(_react2.default.PureComponent);

;

exports.default = TableToolbar;