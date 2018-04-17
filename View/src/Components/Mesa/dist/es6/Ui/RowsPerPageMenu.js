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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9Sb3dzUGVyUGFnZU1lbnUuanN4Il0sIm5hbWVzIjpbIlJvd3NQZXJQYWdlTWVudSIsInByb3BzIiwiaGFuZGxlQ2hhbmdlIiwiYmluZCIsIml0ZW1zUGVyUGFnZSIsIm9uUm93c1BlclBhZ2VDaGFuZ2UiLCJwYXJzZUludCIsInJvd3NQZXJQYWdlIiwicm93c1BlclBhZ2VPcHRpb25zIiwidmFsdWUiLCJuYW1lIiwiUHVyZUNvbXBvbmVudCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7QUFBQTs7OztBQUVBOzs7Ozs7Ozs7Ozs7SUFFTUEsZTs7O0FBQ0osMkJBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSxrSUFDWkEsS0FEWTs7QUFFbEIsVUFBS0MsWUFBTCxHQUFvQixNQUFLQSxZQUFMLENBQWtCQyxJQUFsQixPQUFwQjtBQUZrQjtBQUduQjs7OztpQ0FFYUMsWSxFQUFjO0FBQUEsVUFDbEJDLG1CQURrQixHQUNNLEtBQUtKLEtBRFgsQ0FDbEJJLG1CQURrQjs7QUFFMUJELHFCQUFlRSxTQUFTRixZQUFULENBQWY7QUFDQSxVQUFJQyxtQkFBSixFQUF5QkEsb0JBQW9CRCxZQUFwQjtBQUMxQjs7OzZCQUVTO0FBQUEsbUJBQ2tDLEtBQUtILEtBRHZDO0FBQUEsVUFDRk0sV0FERSxVQUNGQSxXQURFO0FBQUEsVUFDV0Msa0JBRFgsVUFDV0Esa0JBRFg7O0FBRVIsVUFBSSxDQUFDQSxrQkFBTCxFQUF5QkEscUJBQXFCLENBQzVDLENBRDRDLEVBQ3pDLEVBRHlDLEVBQ3JDLEVBRHFDLEVBQ2pDLEVBRGlDLEVBQzdCLEdBRDZCLEVBRTVDLEVBQUVDLE9BQU8sR0FBVCxFQUFjQyxNQUFNLFlBQXBCLEVBRjRDLEVBRzVDLEVBQUVELE9BQU8sSUFBVCxFQUFlQyxNQUFNLGtCQUFyQixFQUg0QyxDQUFyQjs7QUFNekIsYUFDRTtBQUFBO0FBQUEsVUFBSyxXQUFVLGtCQUFmO0FBQ0U7QUFBQTtBQUFBO0FBQUE7QUFBQSxTQURGO0FBRUU7QUFDRSxvQkFBVUgsV0FEWjtBQUVFLG1CQUFTQyxrQkFGWDtBQUdFLG9CQUFVLEtBQUtOO0FBSGpCO0FBRkYsT0FERjtBQVVEOzs7O0VBOUIyQixnQkFBTVMsYTs7QUErQm5DOztrQkFFY1gsZSIsImZpbGUiOiJSb3dzUGVyUGFnZU1lbnUuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuXG5pbXBvcnQgU2VsZWN0Qm94IGZyb20gJy4uL0NvbXBvbmVudHMvU2VsZWN0Qm94JztcblxuY2xhc3MgUm93c1BlclBhZ2VNZW51IGV4dGVuZHMgUmVhY3QuUHVyZUNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgICB0aGlzLmhhbmRsZUNoYW5nZSA9IHRoaXMuaGFuZGxlQ2hhbmdlLmJpbmQodGhpcyk7XG4gIH1cblxuICBoYW5kbGVDaGFuZ2UgKGl0ZW1zUGVyUGFnZSkge1xuICAgIGNvbnN0IHsgb25Sb3dzUGVyUGFnZUNoYW5nZSB9ID0gdGhpcy5wcm9wcztcbiAgICBpdGVtc1BlclBhZ2UgPSBwYXJzZUludChpdGVtc1BlclBhZ2UpO1xuICAgIGlmIChvblJvd3NQZXJQYWdlQ2hhbmdlKSBvblJvd3NQZXJQYWdlQ2hhbmdlKGl0ZW1zUGVyUGFnZSk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGxldCB7IHJvd3NQZXJQYWdlLCByb3dzUGVyUGFnZU9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG4gICAgaWYgKCFyb3dzUGVyUGFnZU9wdGlvbnMpIHJvd3NQZXJQYWdlT3B0aW9ucyA9IFtcbiAgICAgIDUsIDEwLCAyMCwgNTAsIDEwMCxcbiAgICAgIHsgdmFsdWU6IDUwMCwgbmFtZTogJzUwMCAoc2xvdyknfSxcbiAgICAgIHsgdmFsdWU6IDEwMDAsIG5hbWU6ICcxMDAwICh2ZXJ5IHNsb3cpJyB9XG4gICAgXTtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cIlBhZ2luYXRpb25FZGl0b3JcIj5cbiAgICAgICAgPHNwYW4+Um93cyBwZXIgcGFnZTogPC9zcGFuPlxuICAgICAgICA8U2VsZWN0Qm94XG4gICAgICAgICAgc2VsZWN0ZWQ9e3Jvd3NQZXJQYWdlfVxuICAgICAgICAgIG9wdGlvbnM9e3Jvd3NQZXJQYWdlT3B0aW9uc31cbiAgICAgICAgICBvbkNoYW5nZT17dGhpcy5oYW5kbGVDaGFuZ2V9XG4gICAgICAgIC8+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59O1xuXG5leHBvcnQgZGVmYXVsdCBSb3dzUGVyUGFnZU1lbnU7XG4iXX0=