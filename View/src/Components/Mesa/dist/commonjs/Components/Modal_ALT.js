'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Events = require('../Utils/Events');

var _Events2 = _interopRequireDefault(_Events);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Modal = function (_React$Component) {
  _inherits(Modal, _React$Component);

  function Modal(props) {
    _classCallCheck(this, Modal);

    var _this = _possibleConstructorReturn(this, (Modal.__proto__ || Object.getPrototypeOf(Modal)).call(this, props));

    _this.handleClose = _this.handleClose.bind(_this);
    return _this;
  }

  _createClass(Modal, [{
    key: 'componentWillReceiveProps',
    value: function componentWillReceiveProps(_ref) {
      var open = _ref.open;

      if (!!open === !!this.props.open) return;
      if (!!open && !this.props.open) return this.closeListener = _Events2.default.onKey('esc', this.handleClose);else _Events2.default.remove(this.closeListener);
    }
  }, {
    key: 'handleClose',
    value: function handleClose() {
      var onClose = this.props.onClose;

      return onClose && onClose();
    }
  }, {
    key: 'diffuseClick',
    value: function diffuseClick(event) {
      return event.stopPropagation();
    }
  }, {
    key: 'render',
    value: function render() {
      var _props = this.props,
          open = _props.open,
          children = _props.children,
          className = _props.className;

      var _className = 'Modal ' + (open ? 'Modal-Open' : 'Modal-Closed') + ' ' + (className || '');

      return _react2.default.createElement(
        'div',
        { className: 'Modal-Wrapper' },
        _react2.default.createElement(
          'div',
          { className: _className, onClick: this.handleClose },
          _react2.default.createElement(
            'div',
            { className: 'Modal-Body', onClick: this.diffuseClick },
            children
          )
        )
      );
    }
  }]);

  return Modal;
}(_react2.default.Component);

;

exports.default = Modal;