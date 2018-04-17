'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _HeadingCell = require('../Ui/HeadingCell');

var _HeadingCell2 = _interopRequireDefault(_HeadingCell);

var _SelectionCell = require('../Ui/SelectionCell');

var _SelectionCell2 = _interopRequireDefault(_SelectionCell);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var HeadingRow = function (_React$PureComponent) {
  _inherits(HeadingRow, _React$PureComponent);

  function HeadingRow(props) {
    _classCallCheck(this, HeadingRow);

    return _possibleConstructorReturn(this, (HeadingRow.__proto__ || Object.getPrototypeOf(HeadingRow)).call(this, props));
  }

  _createClass(HeadingRow, [{
    key: 'render',
    value: function render() {
      var _props = this.props,
          state = _props.state,
          dispatch = _props.dispatch,
          filteredRows = _props.filteredRows;
      var columns = state.columns,
          actions = state.actions;


      return _react2.default.createElement(
        'tr',
        { className: 'HeadingRow' },
        actions.length ? _react2.default.createElement(_SelectionCell2.default, {
          heading: true,
          state: state,
          dispatch: dispatch,
          filteredRows: filteredRows
        }) : null,
        columns.map(function (column) {
          return _react2.default.createElement(_HeadingCell2.default, {
            key: column.key,
            column: column,
            state: state,
            dispatch: dispatch
          });
        })
      );
    }
  }]);

  return HeadingRow;
}(_react2.default.PureComponent);

;

exports.default = HeadingRow;