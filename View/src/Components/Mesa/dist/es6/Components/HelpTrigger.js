'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Icon = require('./Icon');

var _Icon2 = _interopRequireDefault(_Icon);

var _Tooltip = require('./Tooltip');

var _Tooltip2 = _interopRequireDefault(_Tooltip);

var _Events = require('../Utils/Events');

var _Events2 = _interopRequireDefault(_Events);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var HelpTrigger = function (_React$Component) {
  _inherits(HelpTrigger, _React$Component);

  function HelpTrigger(props) {
    _classCallCheck(this, HelpTrigger);

    var _this = _possibleConstructorReturn(this, (HelpTrigger.__proto__ || Object.getPrototypeOf(HelpTrigger)).call(this, props));

    _this.state = { position: {} };
    _this.updateOffset = _this.updateOffset.bind(_this);
    _this.componentDidMount = _this.componentDidMount.bind(_this);
    _this.componentWillUnmount = _this.componentWillUnmount.bind(_this);
    return _this;
  }

  _createClass(HelpTrigger, [{
    key: 'componentDidMount',
    value: function componentDidMount() {
      this.updateOffset();
      this.listeners = {
        scroll: _Events2.default.add('scroll', this.updateOffset),
        resize: _Events2.default.add('resize', this.updateOffset),
        MesaScroll: _Events2.default.add('MesaScroll', this.updateOffset),
        MesaReflow: _Events2.default.add('MesaReflow', this.updateOffset)
      };
    }
  }, {
    key: 'componentWillUnmount',
    value: function componentWillUnmount() {
      Object.values(this.listeners).forEach(function (listenerId) {
        return _Events2.default.remove(listenerId);
      });
    }
  }, {
    key: 'updateOffset',
    value: function updateOffset() {
      var element = this.element;

      if (!element) return;
      var offset = _Tooltip2.default.getOffset(element);
      var top = offset.top,
          left = offset.left,
          height = offset.height;

      var position = { left: left, top: top + height };
      this.setState({ position: position });
    }
  }, {
    key: 'render',
    value: function render() {
      var _this2 = this;

      var position = this.state.position;
      var children = this.props.children;


      return _react2.default.createElement(
        _Tooltip2.default,
        {
          corner: 'top-left',
          position: position,
          className: 'Trigger HelpTrigger',
          content: children },
        _react2.default.createElement(
          'div',
          { ref: function ref(el) {
              return _this2.element = el;
            } },
          _react2.default.createElement(_Icon2.default, { fa: 'question-circle' })
        )
      );
    }
  }]);

  return HelpTrigger;
}(_react2.default.Component);

;

exports.default = HelpTrigger;