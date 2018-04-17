'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var SelectBox = function (_React$PureComponent) {
  _inherits(SelectBox, _React$PureComponent);

  function SelectBox(props) {
    _classCallCheck(this, SelectBox);

    var _this = _possibleConstructorReturn(this, (SelectBox.__proto__ || Object.getPrototypeOf(SelectBox)).call(this, props));

    _this.handleChange = _this.handleChange.bind(_this);
    return _this;
  }

  _createClass(SelectBox, [{
    key: 'handleChange',
    value: function handleChange(e) {
      var onChange = this.props.onChange;

      var value = e.target.value;
      if (onChange) onChange(value);
    }
  }, {
    key: 'getOptions',
    value: function getOptions() {
      var options = this.props.options;

      if (!Array.isArray(options)) return [];
      if (!options.every(function (opt) {
        return (typeof opt === 'undefined' ? 'undefined' : _typeof(opt)) === 'object';
      })) options = options.map(function (option) {
        return { name: option.toString(), value: option };
      });
      return options;
    }
  }, {
    key: 'render',
    value: function render() {
      var _props = this.props,
          name = _props.name,
          className = _props.className,
          selected = _props.selected;

      var options = this.getOptions();

      return _react2.default.createElement(
        'select',
        {
          name: name,
          className: className,
          onChange: this.handleChange,
          value: selected
        },
        options.map(function (_ref) {
          var value = _ref.value,
              name = _ref.name;
          return _react2.default.createElement(
            'option',
            { key: value, value: value },
            name
          );
        })
      );
    }
  }]);

  return SelectBox;
}(_react2.default.PureComponent);

;

exports.default = SelectBox;