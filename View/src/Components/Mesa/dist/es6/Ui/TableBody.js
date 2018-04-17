'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _DataRow = require('../Ui/DataRow');

var _DataRow2 = _interopRequireDefault(_DataRow);

var _RowUtils = require('../Utils/RowUtils');

var _RowUtils2 = _interopRequireDefault(_RowUtils);

var _HeadingRow = require('../Ui/HeadingRow');

var _HeadingRow2 = _interopRequireDefault(_HeadingRow);

var _EmptyState = require('../Ui/EmptyState');

var _EmptyState2 = _interopRequireDefault(_EmptyState);

var _PaginatedList = require('../Ui/PaginatedList');

var _PaginatedList2 = _interopRequireDefault(_PaginatedList);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var TableBody = function (_React$Component) {
  _inherits(TableBody, _React$Component);

  function TableBody(props) {
    _classCallCheck(this, TableBody);

    var _this = _possibleConstructorReturn(this, (TableBody.__proto__ || Object.getPrototypeOf(TableBody)).call(this, props));

    _this.renderDataRow = _this.renderDataRow.bind(_this);
    return _this;
  }

  _createClass(TableBody, [{
    key: 'renderDataRow',
    value: function renderDataRow(row, idx) {
      var _props = this.props,
          state = _props.state,
          dispatch = _props.dispatch;

      return _react2.default.createElement(_DataRow2.default, {
        key: row.__id,
        row: row,
        state: state,
        dispatch: dispatch
      });
    }
  }, {
    key: 'render',
    value: function render() {
      var _props2 = this.props,
          dispatch = _props2.dispatch,
          state = _props2.state,
          filteredRows = _props2.filteredRows;
      var columns = state.columns,
          options = state.options,
          uiState = state.uiState;
      var paginationState = uiState.paginationState;


      var content = void 0;
      if (!filteredRows.length) content = _react2.default.createElement(
        'tbody',
        null,
        _react2.default.createElement(_EmptyState2.default, { state: state, dispatch: dispatch })
      );else if (!options.paginate) content = _react2.default.createElement(
        'tbody',
        null,
        filteredRows.map(this.renderDataRow)
      );else content = _react2.default.createElement(_PaginatedList2.default, {
        container: 'tbody',
        list: filteredRows,
        paginationState: paginationState,
        renderItem: this.renderDataRow
      });

      return _react2.default.createElement(
        'div',
        { className: 'TableBody' },
        _react2.default.createElement(
          'table',
          { cellSpacing: '0', cellPadding: '0' },
          _react2.default.createElement(
            'tbody',
            null,
            _react2.default.createElement(_HeadingRow2.default, {
              dispatch: dispatch,
              state: state,
              filteredRows: filteredRows
            })
          ),
          content
        )
      );
    }
  }]);

  return TableBody;
}(_react2.default.Component);

;

exports.default = TableBody;