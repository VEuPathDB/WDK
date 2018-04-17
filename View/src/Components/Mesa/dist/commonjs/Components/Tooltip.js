'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Tooltip = function (_React$Component) {
  _inherits(Tooltip, _React$Component);

  function Tooltip(props) {
    _classCallCheck(this, Tooltip);

    var _this = _possibleConstructorReturn(this, (Tooltip.__proto__ || Object.getPrototypeOf(Tooltip)).call(this, props));

    _this.state = { showText: false };
    _this.showTooltip = _this.showTooltip.bind(_this);
    _this.hideTooltip = _this.hideTooltip.bind(_this);
    _this.renderTextBox = _this.renderTextBox.bind(_this);
    _this.componentDidMount = _this.componentDidMount.bind(_this);
    return _this;
  }

  _createClass(Tooltip, [{
    key: 'componentDidMount',
    value: function componentDidMount() {}
  }, {
    key: 'showTooltip',
    value: function showTooltip() {
      var showText = true;
      this.setState({ showText: showText });
    }
  }, {
    key: 'hideTooltip',
    value: function hideTooltip() {
      var showText = false;
      this.setState({ showText: showText });
    }
  }, {
    key: 'renderTextBox',
    value: function renderTextBox() {
      var _props = this.props,
          text = _props.text,
          position = _props.position;
      var showText = this.state.showText;

      var _ref = position ? position : { top: 0, left: 0 },
          top = _ref.top,
          left = _ref.left;

      var textStyle = {
        top: top,
        left: left,
        position: 'absolute',
        zIndex: 1000000
      };

      return !showText ? null : _react2.default.createElement(
        'div',
        {
          style: textStyle,
          className: 'Tooltip-Text',
          onMouseEnter: this.showTooltip,
          onMouseLeave: this.hideTooltip },
        text
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var children = this.props.children;

      var className = 'Tooltip' + (this.props.className ? ' ' + this.props.className : '');
      var TextBox = this.renderTextBox;
      return _react2.default.createElement(
        'div',
        {
          className: className,
          onMouseEnter: this.showTooltip,
          onMouseLeave: this.hideTooltip },
        children,
        _react2.default.createElement(TextBox, null)
      );
    }
  }], [{
    key: 'getOffset',
    value: function getOffset(node) {
      var top = 0;
      var left = 0;
      var height = node.offsetHeight;
      var width = node.offsetWidth;

      do {
        top += node.offsetTop || 0;
        left += node.offsetLeft || 0;
        node = node.offsetParent;
      } while (node);
      return { top: top, left: left, height: height, width: width };
    }
  }]);

  return Tooltip;
}(_react2.default.Component);

;

exports.default = Tooltip;