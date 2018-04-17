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

var _RowUtils = require('../Utils/RowUtils');

var _RowUtils2 = _interopRequireDefault(_RowUtils);

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

    return _possibleConstructorReturn(this, (TableToolbar.__proto__ || Object.getPrototypeOf(TableToolbar)).call(this, props));
  }

  _createClass(TableToolbar, [{
    key: 'render',
    value: function render() {
      var _props = this.props,
          dispatch = _props.dispatch,
          state = _props.state,
          filteredRows = _props.filteredRows,
          children = _props.children;
      var rows = state.rows,
          columns = state.columns,
          options = state.options,
          uiState = state.uiState;
      var paginationState = uiState.paginationState;


      var hiddenRowCount = rows.length - filteredRows.length;
      var columnsAreHideable = columns.some(function (column) {
        return column.hideable;
      });

      var first = 1,
          last = 2,
          total = 3;


      return _react2.default.createElement(
        'div',
        { className: 'Toolbar TableToolbar' },
        options.title && _react2.default.createElement(
          'h1',
          { className: 'TableToolbar-Title' },
          options.title
        ),
        options.search && _react2.default.createElement(_TableSearch2.default, {
          state: state,
          dispatch: dispatch
        }),
        _react2.default.createElement(
          'div',
          { className: 'TableToolbar-Info' },
          _react2.default.createElement(_RowCounter2.default, { state: state, filteredRows: filteredRows })
        ),
        children && _react2.default.createElement(
          'div',
          { className: 'TableToolbar-Children' },
          children
        ),
        options.editableColumns && columnsAreHideable && _react2.default.createElement(
          _ColumnEditor2.default,
          { state: state, dispatch: dispatch },
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
        )
      );
    }
  }]);

  return TableToolbar;
}(_react2.default.PureComponent);

;

exports.default = TableToolbar;