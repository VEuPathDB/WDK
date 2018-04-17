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
  fadeOut: _propTypes2.default.bool,
  position: _propTypes2.default.object
};

Tooltip.contextTypes = {
  addModal: _propTypes2.default.func,
  removeModal: _propTypes2.default.func,
  triggerModalRefresh: _propTypes2.default.func
};

exports.default = Tooltip;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL1Rvb2x0aXAuanN4Il0sIm5hbWVzIjpbIlRvb2x0aXAiLCJwcm9wcyIsInN0YXRlIiwiaXNGb2N1cyIsImlzSG92ZXJlZCIsInNob3dUb29sdGlwIiwiYmluZCIsImhpZGVUb29sdGlwIiwiZ2V0SGlkZURlbGF5IiwiZ2V0U2hvd0RlbGF5IiwiZW5nYWdlVG9vbHRpcCIsImRpc2VuZ2FnZVRvb2x0aXAiLCJyZW5kZXJUb29sdGlwQm94IiwiY29tcG9uZW50RGlkTW91bnQiLCJzaG93RGVsYXkiLCJjb250ZXh0IiwiYWRkTW9kYWwiLCJyZW1vdmVNb2RhbCIsIkVycm9yIiwiZWwiLCJjb25zb2xlIiwiZXJyb3IiLCJldmVudHMiLCJ1c2UiLCJmb2N1c0luIiwic2V0U3RhdGUiLCJrZXlwcmVzcyIsImZvY3VzT3V0IiwibW91c2VFbnRlciIsIm1vdXNlTGVhdmUiLCJjbGVhckFsbCIsImhpZGVEZWxheSIsImlkIiwicmVuZGVyIiwiaGlkZVRpbWVvdXQiLCJjbGVhclRpbWVvdXQiLCJmYWRlT3V0IiwiaXNEaXNlbmdhZ2VkIiwic2hvd1RpbWVvdXQiLCJzZXRUaW1lb3V0IiwidHJpZ2dlck1vZGFsUmVmcmVzaCIsImNvcm5lciIsImxlbmd0aCIsInNwbGl0IiwiZmlsdGVyIiwicyIsImpvaW4iLCJjb250ZW50IiwicG9zaXRpb24iLCJzdHlsZSIsInJlbmRlckh0bWwiLCJ0b3AiLCJsZWZ0IiwicmlnaHQiLCJjb3JuZXJDbGFzcyIsImdldENvcm5lckNsYXNzIiwib3BhY2l0eSIsImJveFN0eWxlIiwiT2JqZWN0IiwiYXNzaWduIiwiekluZGV4IiwiZGlzcGxheSIsInBvaW50ZXJFdmVudHMiLCJ0cmFuc2l0aW9uIiwia2V5cyIsIl9faHRtbCIsImNoaWxkcmVuIiwiY2xhc3NOYW1lIiwiZnVsbENsYXNzTmFtZSIsIm5vZGUiLCJnZXRCb3VuZGluZ0NsaWVudFJlY3QiLCJDb21wb25lbnQiLCJwcm9wVHlwZXMiLCJu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QUFQO0FBQ0Q7Ozs7RUF4Qm1CLGdCQUFNQyxTOztBQXVLM0I7O0FBRUR2RSxRQUFRd0UsU0FBUixHQUFvQjtBQUNsQnpDLGFBQVcsb0JBQVUwQyxNQURIO0FBRWxCUCxZQUFVLG9CQUFVRyxJQUZGO0FBR2xCRixhQUFXLG9CQUFVTyxNQUhIO0FBSWxCM0IsV0FBUyxvQkFBVXNCLElBSkQ7QUFLbEI1QixVQUFRLG9CQUFVaUMsTUFMQTtBQU1sQnRDLFdBQVMsb0JBQVV1QyxJQU5EO0FBT2xCM0IsWUFBVSxvQkFBVTRCO0FBUEYsQ0FBcEI7O0FBVUE1RSxRQUFRNkUsWUFBUixHQUF1QjtBQUNyQjdELFlBQVUsb0JBQVU4RCxJQURDO0FBRXJCN0QsZUFBYSxvQkFBVTZELElBRkY7QUFHckJ0Qyx1QkFBcUIsb0JBQVVzQztBQUhWLENBQXZCOztrQkFNZTlFLE8iLCJmaWxlIjoiVG9vbHRpcC5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5pbXBvcnQgUHJvcFR5cGVzIGZyb20gJ3Byb3AtdHlwZXMnO1xuXG5pbXBvcnQgeyBFdmVudHNGYWN0b3J5IH0gZnJvbSAnLi4vVXRpbHMvRXZlbnRzJztcblxuY2xhc3MgVG9vbHRpcCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcblxuICAgIHRoaXMuc3RhdGUgPSB7XG4gICAgICBpc0ZvY3VzOiBmYWxzZSxcbiAgICAgIGlzSG92ZXJlZDogZmFsc2UsXG4gICAgfTtcblxuICAgIHRoaXMuc2hvd1Rvb2x0aXAgPSB0aGlzLnNob3dUb29sdGlwLmJpbmQodGhpcyk7XG4gICAgdGhpcy5oaWRlVG9vbHRpcCA9IHRoaXMuaGlkZVRvb2x0aXAuYmluZCh0aGlzKTtcblxuICAgIHRoaXMuZ2V0SGlkZURlbGF5ID0gdGhpcy5nZXRIaWRlRGVsYXkuYmluZCh0aGlzKTtcbiAgICB0aGlzLmdldFNob3dEZWxheSA9IHRoaXMuZ2V0U2hvd0RlbGF5LmJpbmQodGhpcyk7XG5cbiAgICB0aGlzLmVuZ2FnZVRvb2x0aXAgPSB0aGlzLmVuZ2FnZVRvb2x0aXAuYmluZCh0aGlzKTtcbiAgICB0aGlzLmRpc2VuZ2FnZVRvb2x0aXAgPSB0aGlzLmRpc2VuZ2FnZVRvb2x0aXAuYmluZCh0aGlzKTtcblxuICAgIHRoaXMucmVuZGVyVG9vbHRpcEJveCA9IHRoaXMucmVuZGVyVG9vbHRpcEJveC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuY29tcG9uZW50RGlkTW91bnQgPSB0aGlzLmNvbXBvbmVudERpZE1vdW50LmJpbmQodGhpcyk7XG4gIH1cblxuICBzdGF0aWMgZ2V0T2Zmc2V0IChub2RlKSB7XG4gICAgcmV0dXJuIG5vZGUuZ2V0Qm91bmRpbmdDbGllbnRSZWN0KCk7XG4gIH1cblxuICBnZXRTaG93RGVsYXkgKCkge1xuICAgIGNvbnN0IHsgc2hvd0RlbGF5IH0gPSB0aGlzLnByb3BzO1xuICAgIHJldHVybiB0eXBlb2Ygc2hvd0RlbGF5ID09PSAnbnVtYmVyJ1xuICAgICAgPyBzaG93RGVsYXlcbiAgICAgIDogMjUwO1xuICB9XG5cbiAgY29tcG9uZW50RGlkTW91bnQgKCkge1xuICAgIGNvbnN0IHsgYWRkTW9kYWwsIHJlbW92ZU1vZGFsIH0gPSB0aGlzLmNvbnRleHQ7XG4gICAgaWYgKFxuICAgICAgdHlwZW9mIGFkZE1vZGFsICE9PSAnZnVuY3Rpb24nXG4gICAgICB8fCB0eXBlb2YgcmVtb3ZlTW9kYWwgIT09ICdmdW5jdGlvbidcbiAgICApIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcihgXG4gICAgICAgIFRvb2x0aXAgRXJyb3I6IE5vIFwiYWRkTW9kYWxcIiBvciBcInJlbW92ZU1vZGFsXCIgZGV0ZWN0ZWQgaW4gY29udGV4dC5cbiAgICAgICAgUGxlYXNlIHVzZSBhIDxNb2RhbEJvdW5kYXJ5PiBpbiB5b3VyIGVsZW1lbnQgdHJlZSB0byBjYXRjaCBtb2RhbHMuXG4gICAgICBgKTtcbiAgICB9XG4gICAgaWYgKCF0aGlzLmVsKSB7XG4gICAgICBjb25zb2xlLmVycm9yKGBcbiAgICAgICAgVG9vbHRpcCBFcnJvcjogQ2FuJ3Qgc2V0dXAgZm9jdXNJbi9mb2N1c091dCBldmVudHMuXG4gICAgICAgIEVsZW1lbnQgcmVmIGNvdWxkIG5vdCBiZSBmb3VuZDsgd2FzIHJlbmRlciBpbnRlcnJ1cHRlZD9cbiAgICAgIGApO1xuICAgIH0gZWxzZSB7XG4gICAgICB0aGlzLmV2ZW50cyA9IG5ldyBFdmVudHNGYWN0b3J5KHRoaXMuZWwpO1xuICAgICAgdGhpcy5ldmVudHMudXNlKHtcbiAgICAgICAgZm9jdXNJbjogKCkgPT4gdGhpcy5zZXRTdGF0ZSh7IGlzRm9jdXM6IHRydWUgfSksXG4gICAgICAgIGtleXByZXNzOiAoKSA9PiB0aGlzLnNldFN0YXRlKHsgaXNGb2N1czogdHJ1ZSB9KSxcbiAgICAgICAgZm9jdXNPdXQ6ICgpID0+IHRoaXMuc2V0U3RhdGUoeyBpc0ZvY3VzOiBmYWxzZSB9KSxcbiAgICAgICAgbW91c2VFbnRlcjogKCkgPT4gdGhpcy5zZXRTdGF0ZSh7IGlzSG92ZXJlZDogdHJ1ZSB9KSxcbiAgICAgICAgbW91c2VMZWF2ZTogKCkgPT4gdGhpcy5zZXRTdGF0ZSh7IGlzSG92ZXJlZDogZmFsc2UgfSlcbiAgICAgIH0pO1xuICAgIH1cbiAgfVxuXG4gIGNvbXBvbmVudFdpbGxVbm1vdW50ICgpIHtcbiAgICBpZiAodGhpcy5ldmVudHMpIHRoaXMuZXZlbnRzLmNsZWFyQWxsKCk7XG4gIH1cblxuICBnZXRIaWRlRGVsYXkgKCkge1xuICAgIGxldCB7IGhpZGVEZWxheSB9ID0gdGhpcy5wcm9wcztcbiAgICByZXR1cm4gdHlwZW9mIGhpZGVEZWxheSA9PT0gJ251bWJlcidcbiAgICAgID8gaGlkZURlbGF5XG4gICAgICA6IDUwMDtcbiAgfVxuXG4gIHNob3dUb29sdGlwICgpIHtcbiAgICBpZiAodGhpcy5pZCkgcmV0dXJuO1xuICAgIGNvbnN0IHsgYWRkTW9kYWwgfSA9IHRoaXMuY29udGV4dDtcbiAgICB0aGlzLmlkID0gYWRkTW9kYWwoeyByZW5kZXI6ICgpID0+IHRoaXMucmVuZGVyVG9vbHRpcEJveCgpIH0pO1xuICAgIGlmICh0aGlzLmhpZGVUaW1lb3V0KSBjbGVhclRpbWVvdXQodGhpcy5oaWRlVGltZW91dCk7XG4gIH1cblxuICBlbmdhZ2VUb29sdGlwICgpIHtcbiAgICBjb25zdCB7IGZhZGVPdXQgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3Qgc2hvd0RlbGF5ID0gdGhpcy5nZXRTaG93RGVsYXkoKTtcblxuICAgIGlmICh0aGlzLmlzRGlzZW5nYWdlZCAmJiBmYWRlT3V0KSB7XG4gICAgICB0aGlzLmlzRGlzZW5nYWdlZCA9IGZhbHNlO1xuICAgIH1cblxuICAgIHRoaXMuc2hvd1RpbWVvdXQgPSBzZXRUaW1lb3V0KCgpID0+IHtcbiAgICAgIHRoaXMuc2hvd1Rvb2x0aXAoKTtcbiAgICAgIGlmICh0aGlzLmhpZGVUaW1lb3V0KSBjbGVhclRpbWVvdXQodGhpcy5oaWRlVGltZW91dCk7XG4gICAgfSwgc2hvd0RlbGF5KTtcbiAgfVxuXG4gIGRpc2VuZ2FnZVRvb2x0aXAgKCkge1xuICAgIGNvbnN0IHsgZmFkZU91dCB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IHRyaWdnZXJNb2RhbFJlZnJlc2ggfSA9IHRoaXMuY29udGV4dDtcbiAgICBjb25zdCBoaWRlRGVsYXkgPSB0aGlzLmdldEhpZGVEZWxheSgpO1xuXG4gICAgaWYgKCF0aGlzLmlzRGlzZW5nYWdlZCAmJiBmYWRlT3V0KSB7XG4gICAgICB0aGlzLmlzRGlzZW5nYWdlZCA9IHRydWU7XG4gICAgICB0cmlnZ2VyTW9kYWxSZWZyZXNoKCk7XG4gICAgfVxuXG4gICAgaWYgKHRoaXMuc2hvd1RpbWVvdXQpIGNsZWFyVGltZW91dCh0aGlzLnNob3dUaW1lb3V0KTtcbiAgICB0aGlzLmhpZGVUaW1lb3V0ID0gc2V0VGltZW91dCh0aGlzLmhpZGVUb29sdGlwLCBoaWRlRGVsYXkpO1xuICB9XG5cbiAgaGlkZVRvb2x0aXAgKCkge1xuICAgIGlmICghdGhpcy5pZCB8fCB0aGlzLnN0YXRlLmlzRm9jdXMgfHwgdGhpcy5zdGF0ZS5pc0hvdmVyZWQpIHJldHVybjtcbiAgICBjb25zdCB7IHJlbW92ZU1vZGFsIH0gPSB0aGlzLmNvbnRleHQ7XG4gICAgcmVtb3ZlTW9kYWwodGhpcy5pZCk7XG4gICAgdGhpcy5pZCA9IG51bGw7XG4gIH1cblxuICBnZXRDb3JuZXJDbGFzcyAoKSB7XG4gICAgY29uc3QgeyBjb3JuZXIgfSA9IHRoaXMucHJvcHM7XG4gICAgaWYgKHR5cGVvZiBjb3JuZXIgIT09ICdzdHJpbmcnIHx8ICFjb3JuZXIubGVuZ3RoKSByZXR1cm4gJ25vLWNvcm5lcic7XG4gICAgcmV0dXJuIGNvcm5lci5zcGxpdCgnICcpLmZpbHRlcihzID0+IHMpLmpvaW4oJy0nKTtcbiAgfVxuXG4gIHJlbmRlclRvb2x0aXBCb3ggKCkge1xuICAgIGNvbnN0IHsgaXNEaXNlbmdhZ2VkIH0gPSB0aGlzO1xuICAgIGNvbnN0IHsgY29udGVudCwgcG9zaXRpb24sIHN0eWxlLCByZW5kZXJIdG1sIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgdG9wLCBsZWZ0LCByaWdodCB9ID0gcG9zaXRpb24gPyBwb3NpdGlvbiA6IHsgdG9wOiAwLCBsZWZ0OiAwLCByaWdodDogMCB9O1xuICAgIGNvbnN0IGNvcm5lckNsYXNzID0gdGhpcy5nZXRDb3JuZXJDbGFzcygpO1xuICAgIGNvbnN0IG9wYWNpdHkgPSBpc0Rpc2VuZ2FnZWQgPyAwLjA1IDogMTtcbiAgICBjb25zdCBib3hTdHlsZSA9IE9iamVjdC5hc3NpZ24oe30sIHtcbiAgICAgIHRvcCxcbiAgICAgIGxlZnQsXG4gICAgICByaWdodCxcbiAgICAgIHpJbmRleDogMTAwMDAwMCxcbiAgICAgIGRpc3BsYXk6ICdibG9jaycsXG4gICAgICBwb3NpdGlvbjogJ2Fic29sdXRlJyxcbiAgICAgIHBvaW50ZXJFdmVudHM6ICdhdXRvJyxcbiAgICAgIHRyYW5zaXRpb246ICdvcGFjaXR5IDAuN3MnLFxuICAgICAgb3BhY2l0eSxcbiAgICB9LCBzdHlsZSAmJiBPYmplY3Qua2V5cyhzdHlsZSkubGVuZ3RoID8gc3R5bGUgOiB7fSk7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdlxuICAgICAgICBzdHlsZT17Ym94U3R5bGV9XG4gICAgICAgIGNsYXNzTmFtZT17J1Rvb2x0aXAtQ29udGVudCAnICsgY29ybmVyQ2xhc3MgKyAoaXNEaXNlbmdhZ2VkID8gJyBUb29sdGlwLUNvbnRlbnQtLURpc2VuZ2FnZWQnIDogJycpfVxuICAgICAgICBvbk1vdXNlRW50ZXI9e3RoaXMuZW5nYWdlVG9vbHRpcH1cbiAgICAgICAgb25Nb3VzZUxlYXZlPXt0aGlzLmRpc2VuZ2FnZVRvb2x0aXB9PlxuICAgICAgICB7cmVuZGVySHRtbCA/IDxkaXYgZGFuZ2Vyb3VzbHlTZXRJbm5lckhUTUw9e3sgX19odG1sOiBjb250ZW50IH19IC8+IDogY29udGVudH1cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgaXNGb2N1cywgaXNIb3ZlcmVkLCBpc0Rpc2VuZ2FnZWQgfSA9IHRoaXMuc3RhdGU7XG4gICAgaWYgKHRoaXMuZWwgJiYgKGlzRm9jdXMgfHwgaXNIb3ZlcmVkKSkgdGhpcy5lbmdhZ2VUb29sdGlwKCk7XG4gICAgZWxzZSB0aGlzLmRpc2VuZ2FnZVRvb2x0aXAoKTtcblxuICAgIGNvbnN0IHsgY2hpbGRyZW4sIGNsYXNzTmFtZSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBmdWxsQ2xhc3NOYW1lID0gJ1Rvb2x0aXAnXG4gICAgICArIChpc0Rpc2VuZ2FnZWQgPyAnIFRvb2x0aXAtLURpc2VuZ2FnZWQnIDogJycpXG4gICAgICArIChjbGFzc05hbWUgPyAnICcgKyBjbGFzc05hbWUgOiAnJyk7XG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXZcbiAgICAgICAgdGFiSW5kZXg9ezB9XG4gICAgICAgIGNsYXNzTmFtZT17ZnVsbENsYXNzTmFtZX1cbiAgICAgICAgcmVmPXsoZWwpID0+IHRoaXMuZWwgPSBlbH0+XG4gICAgICAgIHtjaGlsZHJlbn1cbiAgICAgIDwvZGl2PlxuICAgIClcbiAgfVxufTtcblxuVG9vbHRpcC5wcm9wVHlwZXMgPSB7XG4gIGhpZGVEZWxheTogUHJvcFR5cGVzLm51bWJlcixcbiAgY2hpbGRyZW46IFByb3BUeXBlcy5ub2RlLFxuICBjbGFzc05hbWU6IFByb3BUeXBlcy5zdHJpbmcsXG4gIGNvbnRlbnQ6IFByb3BUeXBlcy5ub2RlLFxuICBjb3JuZXI6IFByb3BUeXBlcy5zdHJpbmcsXG4gIGZhZGVPdXQ6IFByb3BUeXBlcy5ib29sLFxuICBwb3NpdGlvbjogUHJvcFR5cGVzLm9iamVjdFxufTtcblxuVG9vbHRpcC5jb250ZXh0VHlwZXMgPSB7XG4gIGFkZE1vZGFsOiBQcm9wVHlwZXMuZnVuYyxcbiAgcmVtb3ZlTW9kYWw6IFByb3BUeXBlcy5mdW5jLFxuICB0cmlnZ2VyTW9kYWxSZWZyZXNoOiBQcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgVG9vbHRpcDtcbiJdfQ==