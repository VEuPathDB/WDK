'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _HeadingRow = require('../Ui/HeadingRow');

var _HeadingRow2 = _interopRequireDefault(_HeadingRow);

var _DataRowList = require('../Ui/DataRowList');

var _DataRowList2 = _interopRequireDefault(_DataRowList);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var DataTable = function (_React$PureComponent) {
  _inherits(DataTable, _React$PureComponent);

  function DataTable(props) {
    _classCallCheck(this, DataTable);

    return _possibleConstructorReturn(this, (DataTable.__proto__ || Object.getPrototypeOf(DataTable)).call(this, props));
  }

  _createClass(DataTable, [{
    key: 'render',
    value: function render() {
      var _props = this.props,
          rows = _props.rows,
          options = _props.options,
          columns = _props.columns,
          actions = _props.actions,
          uiState = _props.uiState,
          eventHandlers = _props.eventHandlers;

      var props = { rows: rows, options: options, columns: columns, actions: actions, uiState: uiState, eventHandlers: eventHandlers };

      return _react2.default.createElement(
        'div',
        { className: 'Mesa' },
        _react2.default.createElement(
          'div',
          { className: 'DataTable' },
          _react2.default.createElement(
            'table',
            { cellSpacing: '0', cellPadding: '0' },
            _react2.default.createElement(
              'thead',
              null,
              _react2.default.createElement(_HeadingRow2.default, props)
            ),
            _react2.default.createElement(_DataRowList2.default, props)
          )
        )
      );
    }
  }]);

  return DataTable;
}(_react2.default.PureComponent);

;

DataTable.propTypes = {
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

exports.default = DataTable;