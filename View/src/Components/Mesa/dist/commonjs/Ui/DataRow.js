'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _DataCell = require('../Ui/DataCell');

var _DataCell2 = _interopRequireDefault(_DataCell);

var _SelectionCell = require('../Ui/SelectionCell');

var _SelectionCell2 = _interopRequireDefault(_SelectionCell);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var DataRow = function (_React$PureComponent) {
  _inherits(DataRow, _React$PureComponent);

  function DataRow(props) {
    _classCallCheck(this, DataRow);

    var _this = _possibleConstructorReturn(this, (DataRow.__proto__ || Object.getPrototypeOf(DataRow)).call(this, props));

    _this.state = { expanded: false };
    _this.toggleRow = _this.toggleRow.bind(_this);
    _this.expandRow = _this.expandRow.bind(_this);
    _this.collapseRow = _this.collapseRow.bind(_this);
    _this.componentWillReceiveProps = _this.componentWillReceiveProps.bind(_this);
    return _this;
  }

  _createClass(DataRow, [{
    key: 'componentWillReceiveProps',
    value: function componentWillReceiveProps(newProps) {
      var row = this.props.row;

      if (newProps.row !== row) this.collapseRow();
    }
  }, {
    key: 'expandRow',
    value: function expandRow() {
      var options = this.props.options;

      if (!options.inline) return;
      this.setState({ expanded: true });
    }
  }, {
    key: 'collapseRow',
    value: function collapseRow() {
      var options = this.props.options;

      if (!options.inline) return;
      this.setState({ expanded: false });
    }
  }, {
    key: 'toggleRow',
    value: function toggleRow() {
      var options = this.props.options;

      if (!options.inline) return;

      var expanded = this.state.expanded;

      this.setState({ expanded: !expanded });
    }
  }, {
    key: 'render',
    value: function render() {
      var _props = this.props,
          row = _props.row,
          rowIndex = _props.rowIndex,
          columns = _props.columns,
          options = _props.options,
          actions = _props.actions,
          eventHandlers = _props.eventHandlers;
      var expanded = this.state.expanded;

      var inline = options.inline ? !expanded : false;

      var hasSelectionColumn = typeof options.isRowSelected === 'function' && typeof eventHandlers.onRowSelect === 'function' && typeof eventHandlers.onRowDeselect === 'function';

      var rowStyle = !inline ? {} : { whiteSpace: 'nowrap', textOverflow: 'ellipsis' };
      var className = 'Row DataRow' + (inline ? ' DataRow-Inline' : '');

      var cellProps = { row: row, inline: inline, options: options, rowIndex: rowIndex };

      return _react2.default.createElement(
        'tr',
        { className: className, style: rowStyle, onClick: this.toggleRow },
        !hasSelectionColumn ? null : _react2.default.createElement(_SelectionCell2.default, {
          row: row,
          eventHandlers: eventHandlers,
          isRowSelected: options.isRowSelected
        }),
        columns.map(function (column, columnIndex) {
          return _react2.default.createElement(_DataCell2.default, _extends({ key: column.key, column: column, columnIndex: columnIndex }, cellProps));
        })
      );
    }
  }]);

  return DataRow;
}(_react2.default.PureComponent);

;

exports.default = DataRow;