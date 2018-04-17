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

    _this.state = {
      isFocus: false,
      isHovered: false
    };

    _this.showTooltip = _this.showTooltip.bind(_this);
    _this.hideTooltip = _this.hideTooltip.bind(_this);

    _this.getHideDelay = _this.getHideDelay.bind(_this);
    _this.getShowDelay = _this.getShowDelay.bind(_this);

    _this.engageTooltip = _this.engageTooltip.bind(_this);
    _this.disengageTooltip = _this.disengageTooltip.bind(_this);

    _this.renderTooltipBox = _this.renderTooltipBox.bind(_this);
    _this.componentDidMount = _this.componentDidMount.bind(_this);
    return _this;
  }

  _createClass(Tooltip, [{
    key: 'getShowDelay',
    value: function getShowDelay() {
      var showDelay = this.props.showDelay;

      return typeof showDelay === 'number' ? showDelay : 250;
    }
  }, {
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
            return _this2.setState({ isFocus: true });
          },
          keypress: function keypress() {
            return _this2.setState({ isFocus: true });
          },
          focusOut: function focusOut() {
            return _this2.setState({ isFocus: false });
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
    key: 'getHideDelay',
    value: function getHideDelay() {
      var hideDelay = this.props.hideDelay;

      return typeof hideDelay === 'number' ? hideDelay : 500;
    }
  }, {
    key: 'showTooltip',
    value: function showTooltip() {
      var _this3 = this;

      if (this.id) return;
      var addModal = this.context.addModal;

      this.id = addModal({ render: function render() {
          return _this3.renderTooltipBox();
        } });
      if (this.hideTimeout) clearTimeout(this.hideTimeout);
    }
  }, {
    key: 'engageTooltip',
    value: function engageTooltip() {
      var _this4 = this;

      var fadeOut = this.props.fadeOut;

      var showDelay = this.getShowDelay();

      if (this.isDisengaged && fadeOut) {
        this.isDisengaged = false;
      }

      this.showTimeout = setTimeout(function () {
        _this4.showTooltip();
        if (_this4.hideTimeout) clearTimeout(_this4.hideTimeout);
      }, showDelay);
    }
  }, {
    key: 'disengageTooltip',
    value: function disengageTooltip() {
      var fadeOut = this.props.fadeOut;
      var triggerModalRefresh = this.context.triggerModalRefresh;

      var hideDelay = this.getHideDelay();

      if (!this.isDisengaged && fadeOut) {
        this.isDisengaged = true;
        triggerModalRefresh();
      }

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
      var isDisengaged = this.isDisengaged;
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
      var opacity = isDisengaged ? 0.05 : 1;
      var boxStyle = Object.assign({}, {
        top: top,
        left: left,
        right: right,
        zIndex: 1000000,
        display: 'block',
        position: 'absolute',
        pointerEvents: 'auto',
        transition: 'opacity 0.7s',
        opacity: opacity
      }, style && Object.keys(style).length ? style : {});

      return _react2.default.createElement(
        'div',
        {
          style: boxStyle,
          className: 'Tooltip-Content ' + cornerClass + (isDisengaged ? ' Tooltip-Content--Disengaged' : ''),
          onMouseEnter: this.engageTooltip,
          onMouseLeave: this.disengageTooltip },
        renderHtml ? _react2.default.createElement('div', { dangerouslySetInnerHTML: { __html: content } }) : content
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var _this5 = this;

      var _state = this.state,
          isFocus = _state.isFocus,
          isHovered = _state.isHovered,
          isDisengaged = _state.isDisengaged;

      if (this.el && (isFocus || isHovered)) this.engageTooltip();else this.disengageTooltip();

      var _props2 = this.props,
          children = _props2.children,
          className = _props2.className;

      var fullClassName = 'Tooltip' + (isDisengaged ? ' Tooltip--Disengaged' : '') + (className ? ' ' + className : '');
      return _react2.default.createElement(
        'div',
        {
          tabIndex: 0,
          className: fullClassName,
          ref: function ref(el) {
            return _this5.el = el;
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
  removeModal: _propTypes2.default.func,
  triggerModalRefresh: _propTypes2.default.func
};

exports.default = Tooltip;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL1Rvb2x0aXAuanN4Il0sIm5hbWVzIjpbIlRvb2x0aXAiLCJwcm9wcyIsInN0YXRlIiwiaXNGb2N1cyIsImlzSG92ZXJlZCIsInNob3dUb29sdGlwIiwiYmluZCIsImhpZGVUb29sdGlwIiwiZ2V0SGlkZURlbGF5IiwiZ2V0U2hvd0RlbGF5IiwiZW5nYWdlVG9vbHRpcCIsImRpc2VuZ2FnZVRvb2x0aXAiLCJyZW5kZXJUb29sdGlwQm94IiwiY29tcG9uZW50RGlkTW91bnQiLCJzaG93RGVsYXkiLCJjb250ZXh0IiwiYWRkTW9kYWwiLCJyZW1vdmVNb2RhbCIsIkVycm9yIiwiZWwiLCJjb25zb2xlIiwiZXJyb3IiLCJldmVudHMiLCJ1c2UiLCJmb2N1c0luIiwic2V0U3RhdGUiLCJrZXlwcmVzcyIsImZvY3VzT3V0IiwibW91c2VFbnRlciIsIm1vdXNlTGVhdmUiLCJjbGVhckFsbCIsImhpZGVEZWxheSIsImlkIiwicmVuZGVyIiwiaGlkZVRpbWVvdXQiLCJjbGVhclRpbWVvdXQiLCJmYWRlT3V0IiwiaXNEaXNlbmdhZ2VkIiwic2hvd1RpbWVvdXQiLCJzZXRUaW1lb3V0IiwidHJpZ2dlck1vZGFsUmVmcmVzaCIsImNvcm5lciIsImxlbmd0aCIsInNwbGl0IiwiZmlsdGVyIiwicyIsImpvaW4iLCJjb250ZW50IiwicG9zaXRpb24iLCJzdHlsZSIsInJlbmRlckh0bWwiLCJ0b3AiLCJsZWZ0IiwicmlnaHQiLCJjb3JuZXJDbGFzcyIsImdldENvcm5lckNsYXNzIiwib3BhY2l0eSIsImJveFN0eWxlIiwiT2JqZWN0IiwiYXNzaWduIiwiekluZGV4IiwiZGlzcGxheSIsInBvaW50ZXJFdmVudHMiLCJ0cmFuc2l0aW9uIiwia2V5cyIsIl9faHRtbCIsImNoaWxkcmVuIiwiY2xhc3NOYW1lIiwiZnVsbENsYXNzTmFtZSIsIm5vZGUiLCJnZXRCb3VuZGluZ0NsaWVudFJlY3QiLCJDb21wb25lbnQiLCJwcm9wVHlwZXMiLCJu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RDs7OztFQXhCbUIsZ0JBQU1DLFM7O0FBdUszQjs7QUFFRHZFLFFBQVF3RSxTQUFSLEdBQW9CO0FBQ2xCekMsYUFBVyxvQkFBVTBDLE1BREg7QUFFbEJQLFlBQVUsb0JBQVVHLElBRkY7QUFHbEJGLGFBQVcsb0JBQVVPLE1BSEg7QUFJbEIzQixXQUFTLG9CQUFVc0IsSUFKRDtBQUtsQjVCLFVBQVEsb0JBQVVpQyxNQUxBO0FBTWxCMUIsWUFBVSxvQkFBVTJCO0FBTkYsQ0FBcEI7O0FBU0EzRSxRQUFRNEUsWUFBUixHQUF1QjtBQUNyQjVELFlBQVUsb0JBQVU2RCxJQURDO0FBRXJCNUQsZUFBYSxvQkFBVTRELElBRkY7QUFHckJyQyx1QkFBcUIsb0JBQVVxQztBQUhWLENBQXZCOztrQkFNZTdFLE8iLCJmaWxlIjoiVG9vbHRpcC5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5pbXBvcnQgUHJvcFR5cGVzIGZyb20gJ3Byb3AtdHlwZXMnO1xuXG5pbXBvcnQgeyBFdmVudHNGYWN0b3J5IH0gZnJvbSAnLi4vVXRpbHMvRXZlbnRzJztcblxuY2xhc3MgVG9vbHRpcCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcblxuICAgIHRoaXMuc3RhdGUgPSB7XG4gICAgICBpc0ZvY3VzOiBmYWxzZSxcbiAgICAgIGlzSG92ZXJlZDogZmFsc2UsXG4gICAgfTtcblxuICAgIHRoaXMuc2hvd1Rvb2x0aXAgPSB0aGlzLnNob3dUb29sdGlwLmJpbmQodGhpcyk7XG4gICAgdGhpcy5oaWRlVG9vbHRpcCA9IHRoaXMuaGlkZVRvb2x0aXAuYmluZCh0aGlzKTtcblxuICAgIHRoaXMuZ2V0SGlkZURlbGF5ID0gdGhpcy5nZXRIaWRlRGVsYXkuYmluZCh0aGlzKTtcbiAgICB0aGlzLmdldFNob3dEZWxheSA9IHRoaXMuZ2V0U2hvd0RlbGF5LmJpbmQodGhpcyk7XG5cbiAgICB0aGlzLmVuZ2FnZVRvb2x0aXAgPSB0aGlzLmVuZ2FnZVRvb2x0aXAuYmluZCh0aGlzKTtcbiAgICB0aGlzLmRpc2VuZ2FnZVRvb2x0aXAgPSB0aGlzLmRpc2VuZ2FnZVRvb2x0aXAuYmluZCh0aGlzKTtcblxuICAgIHRoaXMucmVuZGVyVG9vbHRpcEJveCA9IHRoaXMucmVuZGVyVG9vbHRpcEJveC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuY29tcG9uZW50RGlkTW91bnQgPSB0aGlzLmNvbXBvbmVudERpZE1vdW50LmJpbmQodGhpcyk7XG4gIH1cblxuICBzdGF0aWMgZ2V0T2Zmc2V0IChub2RlKSB7XG4gICAgcmV0dXJuIG5vZGUuZ2V0Qm91bmRpbmdDbGllbnRSZWN0KCk7XG4gIH1cblxuICBnZXRTaG93RGVsYXkgKCkge1xuICAgIGNvbnN0IHsgc2hvd0RlbGF5IH0gPSB0aGlzLnByb3BzO1xuICAgIHJldHVybiB0eXBlb2Ygc2hvd0RlbGF5ID09PSAnbnVtYmVyJ1xuICAgICAgPyBzaG93RGVsYXlcbiAgICAgIDogMjUwO1xuICB9XG5cbiAgY29tcG9uZW50RGlkTW91bnQgKCkge1xuICAgIGNvbnN0IHsgYWRkTW9kYWwsIHJlbW92ZU1vZGFsIH0gPSB0aGlzLmNvbnRleHQ7XG4gICAgaWYgKFxuICAgICAgdHlwZW9mIGFkZE1vZGFsICE9PSAnZnVuY3Rpb24nXG4gICAgICB8fCB0eXBlb2YgcmVtb3ZlTW9kYWwgIT09ICdmdW5jdGlvbidcbiAgICApIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcihgXG4gICAgICAgIFRvb2x0aXAgRXJyb3I6IE5vIFwiYWRkTW9kYWxcIiBvciBcInJlbW92ZU1vZGFsXCIgZGV0ZWN0ZWQgaW4gY29udGV4dC5cbiAgICAgICAgUGxlYXNlIHVzZSBhIDxNb2RhbEJvdW5kYXJ5PiBpbiB5b3VyIGVsZW1lbnQgdHJlZSB0byBjYXRjaCBtb2RhbHMuXG4gICAgICBgKTtcbiAgICB9XG4gICAgaWYgKCF0aGlzLmVsKSB7XG4gICAgICBjb25zb2xlLmVycm9yKGBcbiAgICAgICAgVG9vbHRpcCBFcnJvcjogQ2FuJ3Qgc2V0dXAgZm9jdXNJbi9mb2N1c091dCBldmVudHMuXG4gICAgICAgIEVsZW1lbnQgcmVmIGNvdWxkIG5vdCBiZSBmb3VuZDsgd2FzIHJlbmRlciBpbnRlcnJ1cHRlZD9cbiAgICAgIGApO1xuICAgIH0gZWxzZSB7XG4gICAgICB0aGlzLmV2ZW50cyA9IG5ldyBFdmVudHNGYWN0b3J5KHRoaXMuZWwpO1xuICAgICAgdGhpcy5ldmVudHMudXNlKHtcbiAgICAgICAgZm9jdXNJbjogKCkgPT4gdGhpcy5zZXRTdGF0ZSh7IGlzRm9jdXM6IHRydWUgfSksXG4gICAgICAgIGtleXByZXNzOiAoKSA9PiB0aGlzLnNldFN0YXRlKHsgaXNGb2N1czogdHJ1ZSB9KSxcbiAgICAgICAgZm9jdXNPdXQ6ICgpID0+IHRoaXMuc2V0U3RhdGUoeyBpc0ZvY3VzOiBmYWxzZSB9KSxcbiAgICAgICAgbW91c2VFbnRlcjogKCkgPT4gdGhpcy5zZXRTdGF0ZSh7IGlzSG92ZXJlZDogdHJ1ZSB9KSxcbiAgICAgICAgbW91c2VMZWF2ZTogKCkgPT4gdGhpcy5zZXRTdGF0ZSh7IGlzSG92ZXJlZDogZmFsc2UgfSlcbiAgICAgIH0pO1xuICAgIH1cbiAgfVxuXG4gIGNvbXBvbmVudFdpbGxVbm1vdW50ICgpIHtcbiAgICBpZiAodGhpcy5ldmVudHMpIHRoaXMuZXZlbnRzLmNsZWFyQWxsKCk7XG4gIH1cblxuICBnZXRIaWRlRGVsYXkgKCkge1xuICAgIGxldCB7IGhpZGVEZWxheSB9ID0gdGhpcy5wcm9wcztcbiAgICByZXR1cm4gdHlwZW9mIGhpZGVEZWxheSA9PT0gJ251bWJlcidcbiAgICAgID8gaGlkZURlbGF5XG4gICAgICA6IDUwMDtcbiAgfVxuXG4gIHNob3dUb29sdGlwICgpIHtcbiAgICBpZiAodGhpcy5pZCkgcmV0dXJuO1xuICAgIGNvbnN0IHsgYWRkTW9kYWwgfSA9IHRoaXMuY29udGV4dDtcbiAgICB0aGlzLmlkID0gYWRkTW9kYWwoeyByZW5kZXI6ICgpID0+IHRoaXMucmVuZGVyVG9vbHRpcEJveCgpIH0pO1xuICAgIGlmICh0aGlzLmhpZGVUaW1lb3V0KSBjbGVhclRpbWVvdXQodGhpcy5oaWRlVGltZW91dCk7XG4gIH1cblxuICBlbmdhZ2VUb29sdGlwICgpIHtcbiAgICBjb25zdCB7IGZhZGVPdXQgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3Qgc2hvd0RlbGF5ID0gdGhpcy5nZXRTaG93RGVsYXkoKTtcblxuICAgIGlmICh0aGlzLmlzRGlzZW5nYWdlZCAmJiBmYWRlT3V0KSB7XG4gICAgICB0aGlzLmlzRGlzZW5nYWdlZCA9IGZhbHNlO1xuICAgIH1cblxuICAgIHRoaXMuc2hvd1RpbWVvdXQgPSBzZXRUaW1lb3V0KCgpID0+IHtcbiAgICAgIHRoaXMuc2hvd1Rvb2x0aXAoKTtcbiAgICAgIGlmICh0aGlzLmhpZGVUaW1lb3V0KSBjbGVhclRpbWVvdXQodGhpcy5oaWRlVGltZW91dCk7XG4gICAgfSwgc2hvd0RlbGF5KTtcbiAgfVxuXG4gIGRpc2VuZ2FnZVRvb2x0aXAgKCkge1xuICAgIGNvbnN0IHsgZmFkZU91dCB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IHRyaWdnZXJNb2RhbFJlZnJlc2ggfSA9IHRoaXMuY29udGV4dDtcbiAgICBjb25zdCBoaWRlRGVsYXkgPSB0aGlzLmdldEhpZGVEZWxheSgpO1xuXG4gICAgaWYgKCF0aGlzLmlzRGlzZW5nYWdlZCAmJiBmYWRlT3V0KSB7XG4gICAgICB0aGlzLmlzRGlzZW5nYWdlZCA9IHRydWU7XG4gICAgICB0cmlnZ2VyTW9kYWxSZWZyZXNoKCk7XG4gICAgfVxuXG4gICAgaWYgKHRoaXMuc2hvd1RpbWVvdXQpIGNsZWFyVGltZW91dCh0aGlzLnNob3dUaW1lb3V0KTtcbiAgICB0aGlzLmhpZGVUaW1lb3V0ID0gc2V0VGltZW91dCh0aGlzLmhpZGVUb29sdGlwLCBoaWRlRGVsYXkpO1xuICB9XG5cbiAgaGlkZVRvb2x0aXAgKCkge1xuICAgIGlmICghdGhpcy5pZCB8fCB0aGlzLnN0YXRlLmlzRm9jdXMgfHwgdGhpcy5zdGF0ZS5pc0hvdmVyZWQpIHJldHVybjtcbiAgICBjb25zdCB7IHJlbW92ZU1vZGFsIH0gPSB0aGlzLmNvbnRleHQ7XG4gICAgcmVtb3ZlTW9kYWwodGhpcy5pZCk7XG4gICAgdGhpcy5pZCA9IG51bGw7XG4gIH1cblxuICBnZXRDb3JuZXJDbGFzcyAoKSB7XG4gICAgY29uc3QgeyBjb3JuZXIgfSA9IHRoaXMucHJvcHM7XG4gICAgaWYgKHR5cGVvZiBjb3JuZXIgIT09ICdzdHJpbmcnIHx8ICFjb3JuZXIubGVuZ3RoKSByZXR1cm4gJ25vLWNvcm5lcic7XG4gICAgcmV0dXJuIGNvcm5lci5zcGxpdCgnICcpLmZpbHRlcihzID0+IHMpLmpvaW4oJy0nKTtcbiAgfVxuXG4gIHJlbmRlclRvb2x0aXBCb3ggKCkge1xuICAgIGNvbnN0IHsgaXNEaXNlbmdhZ2VkIH0gPSB0aGlzO1xuICAgIGNvbnN0IHsgY29udGVudCwgcG9zaXRpb24sIHN0eWxlLCByZW5kZXJIdG1sIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgdG9wLCBsZWZ0LCByaWdodCB9ID0gcG9zaXRpb24gPyBwb3NpdGlvbiA6IHsgdG9wOiAwLCBsZWZ0OiAwLCByaWdodDogMCB9O1xuICAgIGNvbnN0IGNvcm5lckNsYXNzID0gdGhpcy5nZXRDb3JuZXJDbGFzcygpO1xuICAgIGNvbnN0IG9wYWNpdHkgPSBpc0Rpc2VuZ2FnZWQgPyAwLjA1IDogMTtcbiAgICBjb25zdCBib3hTdHlsZSA9IE9iamVjdC5hc3NpZ24oe30sIHtcbiAgICAgIHRvcCxcbiAgICAgIGxlZnQsXG4gICAgICByaWdodCxcbiAgICAgIHpJbmRleDogMTAwMDAwMCxcbiAgICAgIGRpc3BsYXk6ICdibG9jaycsXG4gICAgICBwb3NpdGlvbjogJ2Fic29sdXRlJyxcbiAgICAgIHBvaW50ZXJFdmVudHM6ICdhdXRvJyxcbiAgICAgIHRyYW5zaXRpb246ICdvcGFjaXR5IDAuN3MnLFxuICAgICAgb3BhY2l0eSxcbiAgICB9LCBzdHlsZSAmJiBPYmplY3Qua2V5cyhzdHlsZSkubGVuZ3RoID8gc3R5bGUgOiB7fSk7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdlxuICAgICAgICBzdHlsZT17Ym94U3R5bGV9XG4gICAgICAgIGNsYXNzTmFtZT17J1Rvb2x0aXAtQ29udGVudCAnICsgY29ybmVyQ2xhc3MgKyAoaXNEaXNlbmdhZ2VkID8gJyBUb29sdGlwLUNvbnRlbnQtLURpc2VuZ2FnZWQnIDogJycpfVxuICAgICAgICBvbk1vdXNlRW50ZXI9e3RoaXMuZW5nYWdlVG9vbHRpcH1cbiAgICAgICAgb25Nb3VzZUxlYXZlPXt0aGlzLmRpc2VuZ2FnZVRvb2x0aXB9PlxuICAgICAgICB7cmVuZGVySHRtbCA/IDxkaXYgZGFuZ2Vyb3VzbHlTZXRJbm5lckhUTUw9e3sgX19odG1sOiBjb250ZW50IH19IC8+IDogY29udGVudH1cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgaXNGb2N1cywgaXNIb3ZlcmVkLCBpc0Rpc2VuZ2FnZWQgfSA9IHRoaXMuc3RhdGU7XG4gICAgaWYgKHRoaXMuZWwgJiYgKGlzRm9jdXMgfHwgaXNIb3ZlcmVkKSkgdGhpcy5lbmdhZ2VUb29sdGlwKCk7XG4gICAgZWxzZSB0aGlzLmRpc2VuZ2FnZVRvb2x0aXAoKTtcblxuICAgIGNvbnN0IHsgY2hpbGRyZW4sIGNsYXNzTmFtZSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBmdWxsQ2xhc3NOYW1lID0gJ1Rvb2x0aXAnXG4gICAgICArIChpc0Rpc2VuZ2FnZWQgPyAnIFRvb2x0aXAtLURpc2VuZ2FnZWQnIDogJycpXG4gICAgICArIChjbGFzc05hbWUgPyAnICcgKyBjbGFzc05hbWUgOiAnJyk7XG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXZcbiAgICAgICAgdGFiSW5kZXg9ezB9XG4gICAgICAgIGNsYXNzTmFtZT17ZnVsbENsYXNzTmFtZX1cbiAgICAgICAgcmVmPXsoZWwpID0+IHRoaXMuZWwgPSBlbH0+XG4gICAgICAgIHtjaGlsZHJlbn1cbiAgICAgIDwvZGl2PlxuICAgIClcbiAgfVxufTtcblxuVG9vbHRpcC5wcm9wVHlwZXMgPSB7XG4gIGhpZGVEZWxheTogUHJvcFR5cGVzLm51bWJlcixcbiAgY2hpbGRyZW46IFByb3BUeXBlcy5ub2RlLFxuICBjbGFzc05hbWU6IFByb3BUeXBlcy5zdHJpbmcsXG4gIGNvbnRlbnQ6IFByb3BUeXBlcy5ub2RlLFxuICBjb3JuZXI6IFByb3BUeXBlcy5zdHJpbmcsXG4gIHBvc2l0aW9uOiBQcm9wVHlwZXMub2JqZWN0XG59O1xuXG5Ub29sdGlwLmNvbnRleHRUeXBlcyA9IHtcbiAgYWRkTW9kYWw6IFByb3BUeXBlcy5mdW5jLFxuICByZW1vdmVNb2RhbDogUHJvcFR5cGVzLmZ1bmMsXG4gIHRyaWdnZXJNb2RhbFJlZnJlc2g6IFByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBUb29sdGlwO1xuIl19