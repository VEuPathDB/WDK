'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _DataCell = require('../Ui/DataCell');

var _DataCell2 = _interopRequireDefault(_DataCell);

var _SelectionCell = require('../Ui/SelectionCell');

var _SelectionCell2 = _interopRequireDefault(_SelectionCell);

var _Utils = require('../Utils/Utils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var dataRowClass = (0, _Utils.makeClassifier)('DataRow');

var DataRow = function (_React$PureComponent) {
  _inherits(DataRow, _React$PureComponent);

  function DataRow(props) {
    _classCallCheck(this, DataRow);

    var _this = _possibleConstructorReturn(this, (DataRow.__proto__ || Object.getPrototypeOf(DataRow)).call(this, props));

    _this.state = { expanded: false };
    _this.handleRowClick = _this.handleRowClick.bind(_this);
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
    key: 'handleRowClick',
    value: function handleRowClick() {
      var _props = this.props,
          row = _props.row,
          rowIndex = _props.rowIndex,
          options = _props.options;
      var inline = options.inline,
          onRowClick = options.onRowClick;

      if (!inline && !onRowClick) return;

      if (inline) this.setState({ expanded: !this.state.expanded });
      if (typeof onRowClick === 'function') onRowClick(row, rowIndex);
    }
  }, {
    key: 'render',
    value: function render() {
      var _props2 = this.props,
          row = _props2.row,
          rowIndex = _props2.rowIndex,
          columns = _props2.columns,
          options = _props2.options,
          actions = _props2.actions,
          eventHandlers = _props2.eventHandlers;
      var expanded = this.state.expanded;

      var inline = options.inline ? !expanded : false;

      var hasSelectionColumn = typeof options.isRowSelected === 'function' && typeof eventHandlers.onRowSelect === 'function' && typeof eventHandlers.onRowDeselect === 'function';

      var rowStyle = !inline ? {} : { whiteSpace: 'nowrap', textOverflow: 'ellipsis' };
      var className = dataRowClass(null, inline ? 'inline' : '');

      var deriveRowClassName = options.deriveRowClassName;

      if (typeof deriveRowClassName === 'function') {
        var derivedClassName = deriveRowClassName(row);
        className += typeof derivedClassName === 'string' ? ' ' + derivedClassName : '';
      };

      var cellProps = { row: row, inline: inline, options: options, rowIndex: rowIndex };

      return _react2.default.createElement(
        'tr',
        { className: className, style: rowStyle, onClick: this.handleRowClick },
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

DataRow.propTypes = {
  row: _propTypes2.default.object.isRequired,
  rowIndex: _propTypes2.default.number.isRequired,
  columns: _propTypes2.default.array.isRequired,

  options: _propTypes2.default.object,
  actions: _propTypes2.default.array,
  eventHandlers: _propTypes2.default.object
};

exports.default = DataRow;