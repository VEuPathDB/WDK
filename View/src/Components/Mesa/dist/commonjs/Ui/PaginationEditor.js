'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _SelectBox = require('../Components/SelectBox');

var _SelectBox2 = _interopRequireDefault(_SelectBox);

var _Actions = require('../State/Actions');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var PaginationEditor = function (_React$PureComponent) {
  _inherits(PaginationEditor, _React$PureComponent);

  function PaginationEditor(props) {
    _classCallCheck(this, PaginationEditor);

    var _this = _possibleConstructorReturn(this, (PaginationEditor.__proto__ || Object.getPrototypeOf(PaginationEditor)).call(this, props));

    _this.handleItemsPerPageChange = _this.handleItemsPerPageChange.bind(_this);
    return _this;
  }

  _createClass(PaginationEditor, [{
    key: 'handleItemsPerPageChange',
    value: function handleItemsPerPageChange(itemsPerPage) {
      var dispatch = this.props.dispatch;

      itemsPerPage = parseInt(itemsPerPage);
      dispatch((0, _Actions.setPaginatedItemsPerPage)(itemsPerPage));
    }
  }, {
    key: 'render',
    value: function render() {
      var pagination = this.props.pagination;

      var options = [5, 10, 20, 35, 50, 100];

      return _react2.default.createElement(
        'div',
        { className: 'PaginationEditor' },
        _react2.default.createElement(
          'span',
          null,
          'Rows per page: '
        ),
        _react2.default.createElement(_SelectBox2.default, {
          options: options,
          selected: pagination.itemsPerPage,
          onChange: this.handleItemsPerPageChange
        })
      );
    }
  }]);

  return PaginationEditor;
}(_react2.default.PureComponent);

;

exports.default = PaginationEditor;