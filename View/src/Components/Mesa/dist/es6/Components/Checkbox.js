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

var Checkbox = function (_React$Component) {
  _inherits(Checkbox, _React$Component);

  function Checkbox(props) {
    _classCallCheck(this, Checkbox);

    var _this = _possibleConstructorReturn(this, (Checkbox.__proto__ || Object.getPrototypeOf(Checkbox)).call(this, props));

    _this.handleClick = _this.handleClick.bind(_this);
    return _this;
  }

  _createClass(Checkbox, [{
    key: 'handleClick',
    value: function handleClick(e) {
      var _props = this.props,
          checked = _props.checked,
          onChange = _props.onChange;

      if (typeof onChange === 'function') onChange(!!checked);
    }
  }, {
    key: 'render',
    value: function render() {
      var _props2 = this.props,
          checked = _props2.checked,
          className = _props2.className,
          disabled = _props2.disabled;

      className = 'Checkbox' + (className ? ' ' + className : '');
      className += ' ' + (checked ? 'Checkbox-Checked' : 'Checkbox-Unchecked');
      className += disabled ? ' Checkbox-Disabled' : '';

      return _react2.default.createElement(
        'div',
        { className: className, onClick: disabled ? null : this.handleClick },
        _react2.default.createElement('input', { type: 'checkbox', checked: checked })
      );
    }
  }]);

  return Checkbox;
}(_react2.default.Component);

;

exports.default = Checkbox;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL0NoZWNrYm94LmpzeCJdLCJuYW1lcyI6WyJDaGVja2JveCIsInByb3BzIiwiaGFuZGxlQ2xpY2siLCJiaW5kIiwiZSIsImNoZWNrZWQiLCJvbkNoYW5nZSIsImNsYXNzTmFtZSIsImRpc2FibGVkIiwiQ29tcG9uZW50Il0sIm1hcHBpbmdzIjoiOzs7Ozs7OztBQUFBOzs7O0FBRUE7Ozs7Ozs7Ozs7OztJQUVNQSxROzs7QUFDSixvQkFBYUMsS0FBYixFQUFvQjtBQUFBOztBQUFBLG9IQUNaQSxLQURZOztBQUVsQixVQUFLQyxXQUFMLEdBQW1CLE1BQUtBLFdBQUwsQ0FBaUJDLElBQWpCLE9BQW5CO0FBRmtCO0FBR25COzs7O2dDQUVZQyxDLEVBQUc7QUFBQSxtQkFDYyxLQUFLSCxLQURuQjtBQUFBLFVBQ1JJLE9BRFEsVUFDUkEsT0FEUTtBQUFBLFVBQ0NDLFFBREQsVUFDQ0EsUUFERDs7QUFFZCxVQUFJLE9BQU9BLFFBQVAsS0FBb0IsVUFBeEIsRUFBb0NBLFNBQVMsQ0FBQyxDQUFDRCxPQUFYO0FBQ3JDOzs7NkJBRVM7QUFBQSxvQkFDK0IsS0FBS0osS0FEcEM7QUFBQSxVQUNGSSxPQURFLFdBQ0ZBLE9BREU7QUFBQSxVQUNPRSxTQURQLFdBQ09BLFNBRFA7QUFBQSxVQUNrQkMsUUFEbEIsV0FDa0JBLFFBRGxCOztBQUVSRCxrQkFBWSxjQUFjQSxZQUFZLE1BQU1BLFNBQWxCLEdBQThCLEVBQTVDLENBQVo7QUFDQUEsbUJBQWEsT0FBT0YsVUFBVSxrQkFBVixHQUErQixvQkFBdEMsQ0FBYjtBQUNBRSxtQkFBYUMsV0FBVyxvQkFBWCxHQUFrQyxFQUEvQzs7QUFFQSxhQUNFO0FBQUE7QUFBQSxVQUFLLFdBQVdELFNBQWhCLEVBQTJCLFNBQVNDLFdBQVcsSUFBWCxHQUFrQixLQUFLTixXQUEzRDtBQUNFLGlEQUFPLE1BQUssVUFBWixFQUF1QixTQUFTRyxPQUFoQztBQURGLE9BREY7QUFLRDs7OztFQXRCb0IsZ0JBQU1JLFM7O0FBdUI1Qjs7a0JBRWNULFEiLCJmaWxlIjoiQ2hlY2tib3guanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuXG5pbXBvcnQgSWNvbiBmcm9tICcuLi9Db21wb25lbnRzL0ljb24nO1xuXG5jbGFzcyBDaGVja2JveCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgICB0aGlzLmhhbmRsZUNsaWNrID0gdGhpcy5oYW5kbGVDbGljay5iaW5kKHRoaXMpO1xuICB9XG5cbiAgaGFuZGxlQ2xpY2sgKGUpIHtcbiAgICBsZXQgeyBjaGVja2VkLCBvbkNoYW5nZSB9ID0gdGhpcy5wcm9wcztcbiAgICBpZiAodHlwZW9mIG9uQ2hhbmdlID09PSAnZnVuY3Rpb24nKSBvbkNoYW5nZSghIWNoZWNrZWQpO1xuICB9XG5cbiAgcmVuZGVyICgpIHtcbiAgICBsZXQgeyBjaGVja2VkLCBjbGFzc05hbWUsIGRpc2FibGVkIH0gPSB0aGlzLnByb3BzO1xuICAgIGNsYXNzTmFtZSA9ICdDaGVja2JveCcgKyAoY2xhc3NOYW1lID8gJyAnICsgY2xhc3NOYW1lIDogJycpO1xuICAgIGNsYXNzTmFtZSArPSAnICcgKyAoY2hlY2tlZCA/ICdDaGVja2JveC1DaGVja2VkJyA6ICdDaGVja2JveC1VbmNoZWNrZWQnKTtcbiAgICBjbGFzc05hbWUgKz0gZGlzYWJsZWQgPyAnIENoZWNrYm94LURpc2FibGVkJyA6ICcnO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPXtjbGFzc05hbWV9IG9uQ2xpY2s9e2Rpc2FibGVkID8gbnVsbCA6IHRoaXMuaGFuZGxlQ2xpY2t9PlxuICAgICAgICA8aW5wdXQgdHlwZT1cImNoZWNrYm94XCIgY2hlY2tlZD17Y2hlY2tlZH0gLz5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn07XG5cbmV4cG9ydCBkZWZhdWx0IENoZWNrYm94O1xuIl19