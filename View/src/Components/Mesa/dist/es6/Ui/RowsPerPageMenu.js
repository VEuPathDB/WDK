'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _SelectBox = require('../Components/SelectBox');

var _SelectBox2 = _interopRequireDefault(_SelectBox);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var RowsPerPageMenu = function (_React$PureComponent) {
  _inherits(RowsPerPageMenu, _React$PureComponent);

  function RowsPerPageMenu(props) {
    _classCallCheck(this, RowsPerPageMenu);

    var _this = _possibleConstructorReturn(this, (RowsPerPageMenu.__proto__ || Object.getPrototypeOf(RowsPerPageMenu)).call(this, props));

    _this.handleChange = _this.handleChange.bind(_this);
    return _this;
  }

  _createClass(RowsPerPageMenu, [{
    key: 'handleChange',
    value: function handleChange(itemsPerPage) {
      var onRowsPerPageChange = this.props.onRowsPerPageChange;

      itemsPerPage = parseInt(itemsPerPage);
      if (onRowsPerPageChange) onRowsPerPageChange(itemsPerPage);
    }
  }, {
    key: 'render',
    value: function render() {
      var _props = this.props,
          rowsPerPage = _props.rowsPerPage,
          rowsPerPageOptions = _props.rowsPerPageOptions;

      if (!rowsPerPageOptions) rowsPerPageOptions = [5, 10, 20, 50, 100, { value: 500, name: '500 (slow)' }, { value: 1000, name: '1000 (very slow)' }];

      return _react2.default.createElement(
        'div',
        { className: 'PaginationEditor' },
        _react2.default.createElement(
          'span',
          null,
          'Rows per page: '
        ),
        _react2.default.createElement(_SelectBox2.default, {
          selected: rowsPerPage,
          options: rowsPerPageOptions,
          onChange: this.handleChange
        })
      );
    }
  }]);

  return RowsPerPageMenu;
}(_react2.default.PureComponent);

;

exports.default = RowsPerPageMenu;