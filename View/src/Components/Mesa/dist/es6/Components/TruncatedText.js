'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Utils = require('../Utils/Utils');

var _Utils2 = _interopRequireDefault(_Utils);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var TruncatedText = function (_React$Component) {
  _inherits(TruncatedText, _React$Component);

  function TruncatedText(props) {
    _classCallCheck(this, TruncatedText);

    var _this = _possibleConstructorReturn(this, (TruncatedText.__proto__ || Object.getPrototypeOf(TruncatedText)).call(this, props));

    _this.state = { expanded: false };
    _this.toggleExpansion = _this.toggleExpansion.bind(_this);
    return _this;
  }

  _createClass(TruncatedText, [{
    key: 'toggleExpansion',
    value: function toggleExpansion() {
      var expanded = this.state.expanded;

      this.setState({ expanded: !expanded });
    }
  }, {
    key: 'render',
    value: function render() {
      var expanded = this.state.expanded;
      var _props = this.props,
          className = _props.className,
          cutoff = _props.cutoff,
          text = _props.text;

      cutoff = typeof cutoff === 'number' ? cutoff : 100;
      var expandable = _Utils2.default.wordCount(text) > cutoff;

      className = 'TruncatedText' + (className ? ' ' + className : '');
      text = expanded ? text : _Utils2.default.truncate(text, cutoff);

      return _react2.default.createElement(
        'div',
        { className: className },
        text,
        expandable && _react2.default.createElement(
          'button',
          { className: 'TruncatedText-Toggle', onClick: this.toggleExpansion },
          expanded ? 'Show Less' : 'Show More',
          _react2.default.createElement(_Icon2.default, { fa: expanded ? 'angle-double-up' : 'angle-double-down' })
        )
      );
    }
  }]);

  return TruncatedText;
}(_react2.default.Component);

;

exports.default = TruncatedText;