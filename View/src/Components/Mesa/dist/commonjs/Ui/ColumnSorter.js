'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ColumnSorter = function (_React$PureComponent) {
  _inherits(ColumnSorter, _React$PureComponent);

  function ColumnSorter(props) {
    _classCallCheck(this, ColumnSorter);

    return _possibleConstructorReturn(this, (ColumnSorter.__proto__ || Object.getPrototypeOf(ColumnSorter)).call(this, props));
  }

  _createClass(ColumnSorter, [{
    key: 'render',
    value: function render() {
      var _props = this.props,
          column = _props.column,
          state = _props.state,
          dispatch = _props.dispatch;
      var sort = state.ui.sort;

      var currentlySorting = sort.byColumn === column;
      var sortIcon = !currentlySorting ? 'sort-amount-asc inactive' : sort.ascending ? 'sort-amount-asc active' : 'sort-amount-desc active';

      return _react2.default.createElement(_Icon2.default, { fa: sortIcon + ' Trigger SortTrigger' });
    }
  }]);

  return ColumnSorter;
}(_react2.default.PureComponent);

;

exports.default = ColumnSorter;