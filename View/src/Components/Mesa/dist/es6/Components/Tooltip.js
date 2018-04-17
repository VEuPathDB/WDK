'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _Events = require('../Utils/Events');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Tooltip = function (_React$Component) {
  _inherits(Tooltip, _React$Component);

  function Tooltip(props) {
    _classCallCheck(this, Tooltip);

    var _this = _possibleConstructorReturn(this, (Tooltip.__proto__ || Object.getPrototypeOf(Tooltip)).call(this, props));

    _this.showTooltip = _this.showTooltip.bind(_this);
    _this.hideTooltip = _this.hideTooltip.bind(_this);
    _this.renderTooltipBox = _this.renderTooltipBox.bind(_this);
    _this.engageTooltip = _this.engageTooltip.bind(_this);
    _this.disengageTooltip = _this.disengageTooltip.bind(_this);
    _this.componentDidMount = _this.componentDidMount.bind(_this);
    _this.state = { isFocus: false, isHovered: false };
    return _this;
  }

  _createClass(Tooltip, [{
    key: 'componentDidMount',
    value: function componentDidMount() {
      var _this2 = this;

      var _context = this.context,
          addModal = _context.addModal,
          removeModal = _context.removeModal;

      if (typeof addModal !== 'function' || typeof removeModal !== 'function') {
        throw new Error('\n        Tooltip Error: No "addModal" or "removeModal" detected in context.\n        Please use a <ModalBoundary> in your element tree to catch modals.\n      ');
      }
      if (!this.el) {
        console.error('\n        Tooltip Error: Can\'t setup focusIn/focusOut events.\n        Element ref could not be found; was render interrupted?\n      ');
      } else {
        this.events = new _Events.EventsFactory(this.el);
        this.events.use({
          focusIn: function focusIn() {
            console.log('focus in!');_this2.setState({ isFocus: true });
          },
          keypress: function keypress() {
            console.log('keypress!');_this2.setState({ isFocus: true });
          },
          focusOut: function focusOut() {
            console.log('focus out!!');_this2.setState({ isFocus: false });
          },
          mouseEnter: function mouseEnter() {
            return _this2.setState({ isHovered: true });
          },
          mouseLeave: function mouseLeave() {
            return _this2.setState({ isHovered: false });
          }
        });
      }
    }
  }, {
    key: 'componentWillUnmount',
    value: function componentWillUnmount() {
      if (this.events) this.events.clearAll();
    }
  }, {
    key: 'showTooltip',
    value: function showTooltip() {
      if (this.id) return;
      var addModal = this.context.addModal;

      var textBox = { render: this.renderTooltipBox };
      this.id = addModal(textBox);
      if (this.hideTimeout) clearTimeout(this.hideTimeout);
    }
  }, {
    key: 'engageTooltip',
    value: function engageTooltip() {
      var _this3 = this;

      var showDelay = this.props.showDelay;

      showDelay = typeof showDelay === 'number' ? showDelay : 250;
      this.showTimeout = setTimeout(function () {
        _this3.showTooltip();
        if (_this3.hideTimeout) clearTimeout(_this3.hideTimeout);
      }, showDelay);
    }
  }, {
    key: 'disengageTooltip',
    value: function disengageTooltip() {
      var hideDelay = this.props.hideDelay;

      hideDelay = typeof hideDelay === 'number' ? hideDelay : 500;
      if (this.showTimeout) clearTimeout(this.showTimeout);
      this.hideTimeout = setTimeout(this.hideTooltip, hideDelay);
    }
  }, {
    key: 'hideTooltip',
    value: function hideTooltip() {
      if (!this.id || this.state.isFocus || this.state.isHovered) return;
      var removeModal = this.context.removeModal;

      removeModal(this.id);
      this.id = null;
    }
  }, {
    key: 'getCornerClass',
    value: function getCornerClass() {
      var corner = this.props.corner;

      if (typeof corner !== 'string' || !corner.length) return 'no-corner';
      return corner.split(' ').filter(function (s) {
        return s;
      }).join('-');
    }
  }, {
    key: 'renderTooltipBox',
    value: function renderTooltipBox() {
      var _props = this.props,
          content = _props.content,
          position = _props.position,
          style = _props.style,
          renderHtml = _props.renderHtml;

      var _ref = position ? position : { top: 0, left: 0, right: 0 },
          top = _ref.top,
          left = _ref.left,
          right = _ref.right;

      var cornerClass = this.getCornerClass();
      var boxStyle = Object.assign({}, {
        top: top,
        left: left,
        right: right,
        display: 'block',
        position: 'absolute',
        pointerEvents: 'auto',
        zIndex: 1000000
      }, style && Object.keys(style).length ? style : {});

      return _react2.default.createElement(
        'div',
        {
          style: boxStyle,
          className: 'Tooltip-Content ' + cornerClass,
          onMouseEnter: this.engageTooltip,
          onMouseLeave: this.disengageTooltip },
        renderHtml ? _react2.default.createElement('div', { dangerouslySetInnerHTML: { __html: content } }) : content
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var _this4 = this;

      var _state = this.state,
          isFocus = _state.isFocus,
          isHovered = _state.isHovered;

      if (this.el && (isFocus || isHovered)) this.engageTooltip();else this.disengageTooltip();

      var _props2 = this.props,
          children = _props2.children,
          className = _props2.className;

      var fullClassName = 'Tooltip' + (className ? ' ' + className : '');
      return _react2.default.createElement(
        'div',
        {
          tabIndex: 0,
          className: fullClassName,
          ref: function ref(el) {
            return _this4.el = el;
          } },
        children
      );
    }
  }], [{
    key: 'getOffset',
    value: function getOffset(node) {
      return node.getBoundingClientRect();
    }
  }]);

  return Tooltip;
}(_react2.default.Component);

;

Tooltip.propTypes = {
  hideDelay: _propTypes2.default.number,
  children: _propTypes2.default.node,
  className: _propTypes2.default.string,
  content: _propTypes2.default.node,
  corner: _propTypes2.default.string,
  position: _propTypes2.default.object
};

Tooltip.contextTypes = {
  addModal: _propTypes2.default.func,
  removeModal: _propTypes2.default.func
};

exports.default = Tooltip;