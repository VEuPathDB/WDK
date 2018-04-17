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
        _react2.default.createElement(_Icon2.default, { fa: checked ? 'check-square' : 'square' })
      );
    }
  }]);

  return Checkbox;
}(_react2.default.Component);

;

exports.default = Checkbox;