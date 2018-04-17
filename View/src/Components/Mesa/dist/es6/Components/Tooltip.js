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
    _this.renderTooltipBox = _this.renderTooltipBox.bind(_this);
    _this.engageTooltip = _this.engageTooltip.bind(_this);
    _this.getHideDelay = _this.getHideDelay.bind(_this);
    _this.disengageTooltip = _this.disengageTooltip.bind(_this);
    _this.componentDidMount = _this.componentDidMount.bind(_this);
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
      var hideDelay = this.getHideDelay();
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
      var isDisengaged = this.state.isDisengaged;
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL1Rvb2x0aXAuanN4Il0sIm5hbWVzIjpbIlRvb2x0aXAiLCJwcm9wcyIsInN0YXRlIiwiaXNGb2N1cyIsImlzSG92ZXJlZCIsInNob3dUb29sdGlwIiwiYmluZCIsImhpZGVUb29sdGlwIiwicmVuZGVyVG9vbHRpcEJveCIsImVuZ2FnZVRvb2x0aXAiLCJnZXRIaWRlRGVsYXkiLCJkaXNlbmdhZ2VUb29sdGlwIiwiY29tcG9uZW50RGlkTW91bnQiLCJjb250ZXh0IiwiYWRkTW9kYWwiLCJyZW1vdmVNb2RhbCIsIkVycm9yIiwiZWwiLCJjb25zb2xlIiwiZXJyb3IiLCJldmVudHMiLCJ1c2UiLCJmb2N1c0luIiwic2V0U3RhdGUiLCJrZXlwcmVzcyIsImZvY3VzT3V0IiwibW91c2VFbnRlciIsIm1vdXNlTGVhdmUiLCJjbGVhckFsbCIsImhpZGVEZWxheSIsImlkIiwidGV4dEJveCIsInJlbmRlciIsImhpZGVUaW1lb3V0IiwiY2xlYXJUaW1lb3V0Iiwic2hvd0RlbGF5Iiwic2hvd1RpbWVvdXQiLCJzZXRUaW1lb3V0IiwiY29ybmVyIiwibGVuZ3RoIiwic3BsaXQiLCJmaWx0ZXIiLCJzIiwiam9pbiIsImlzRGlzZW5nYWdlZCIsImNvbnRlbnQiLCJwb3NpdGlvbiIsInN0eWxlIiwicmVuZGVySHRtbCIsInRvcCIsImxlZnQiLCJyaWdodCIsImNvcm5lckNsYXNzIiwiZ2V0Q29ybmVyQ2xhc3MiLCJib3hTdHlsZSIsIk9iamVjdCIsImFzc2lnbiIsImRpc3BsYXkiLCJwb2ludGVyRXZlbnRzIiwiekluZGV4Iiwia2V5cyIsIl9faHRtbCIsImNoaWxkcmVuIiwiY2xhc3NOYW1lIiwiZnVsbENsYXNzTmFtZSIsIm5vZGUiLCJnZXRCb3VuZGluZ0NsaWVudFJlY3QiLCJDb21wb25lbnQiLCJwcm9wVHlwZXMiLCJudW1iZXIiLCJzdHJpbmciLCJvYmplY3QiLCJjb250ZXh0VHlwZXMiLCJmdW5jIl0sIm1hcHBpbmdzIjoi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QTZELFFBTEEsV0FLQUEsUUFMQTtBQUFBLFVBS1VDLFNBTFYsV0FLVUEsU0FMVjs7QUFNUixVQUFNQyxnQkFBZ0IsYUFDakJwQixlQUFlLHNCQUFmLEdBQXdDLEVBRHZCLEtBRWpCbUIsWUFBWSxNQUFNQSxTQUFsQixHQUE4QixFQUZiLENBQXRCO0FBR0EsYUFDRTtBQUFBO0FBQUE7QUFDRSxvQkFBVSxDQURaO0FBRUUscUJBQVdDLGFBRmI7QUFHRSxlQUFLLGFBQUMvQyxFQUFEO0FBQUEsbUJBQVEsT0FBS0EsRUFBTCxHQUFVQSxFQUFsQjtBQUFBLFdBSFA7QUFJRzZDO0FBSkgsT0FERjtBQVFEOzs7OEJBdkhpQkcsSSxFQUFNO0FBQ3RCLGFBQU9BLEtBQUtDLHFCQUFMLEVBQVA7QUFDRDs7OztFQXBCbUIsZ0JBQU1DLFM7O0FBMEkzQjs7QUFFRG5FLFFBQVFvRSxTQUFSLEdBQW9CO0FBQ2xCdkMsYUFBVyxvQkFBVXdDLE1BREg7QUFFbEJQLFlBQVUsb0JBQVVHLElBRkY7QUFHbEJGLGFBQVcsb0JBQVVPLE1BSEg7QUFJbEJ6QixXQUFTLG9CQUFVb0IsSUFKRDtBQUtsQjNCLFVBQVEsb0JBQVVnQyxNQUxBO0FBTWxCeEIsWUFBVSxvQkFBVXlCO0FBTkYsQ0FBcEI7O0FBU0F2RSxRQUFRd0UsWUFBUixHQUF1QjtBQUNyQjFELFlBQVUsb0JBQVUyRCxJQURDO0FBRXJCMUQsZUFBYSxvQkFBVTBEO0FBRkYsQ0FBdkI7O2tCQUtlekUsTyIsImZpbGUiOiJUb29sdGlwLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcbmltcG9ydCBQcm9wVHlwZXMgZnJvbSAncHJvcC10eXBlcyc7XG5cbmltcG9ydCB7IEV2ZW50c0ZhY3RvcnkgfSBmcm9tICcuLi9VdGlscy9FdmVudHMnO1xuXG5jbGFzcyBUb29sdGlwIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5zdGF0ZSA9IHtcbiAgICAgIGlzRm9jdXM6IGZhbHNlLFxuICAgICAgaXNIb3ZlcmVkOiBmYWxzZSxcbiAgICB9O1xuXG4gICAgdGhpcy5zaG93VG9vbHRpcCA9IHRoaXMuc2hvd1Rvb2x0aXAuYmluZCh0aGlzKTtcbiAgICB0aGlzLmhpZGVUb29sdGlwID0gdGhpcy5oaWRlVG9vbHRpcC5iaW5kKHRoaXMpO1xuICAgIHRoaXMucmVuZGVyVG9vbHRpcEJveCA9IHRoaXMucmVuZGVyVG9vbHRpcEJveC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuZW5nYWdlVG9vbHRpcCA9IHRoaXMuZW5nYWdlVG9vbHRpcC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuZ2V0SGlkZURlbGF5ID0gdGhpcy5nZXRIaWRlRGVsYXkuYmluZCh0aGlzKTtcbiAgICB0aGlzLmRpc2VuZ2FnZVRvb2x0aXAgPSB0aGlzLmRpc2VuZ2FnZVRvb2x0aXAuYmluZCh0aGlzKTtcbiAgICB0aGlzLmNvbXBvbmVudERpZE1vdW50ID0gdGhpcy5jb21wb25lbnREaWRNb3VudC5iaW5kKHRoaXMpO1xuICB9XG5cbiAgc3RhdGljIGdldE9mZnNldCAobm9kZSkge1xuICAgIHJldHVybiBub2RlLmdldEJvdW5kaW5nQ2xpZW50UmVjdCgpO1xuICB9XG5cbiAgY29tcG9uZW50RGlkTW91bnQgKCkge1xuICAgIGNvbnN0IHsgYWRkTW9kYWwsIHJlbW92ZU1vZGFsIH0gPSB0aGlzLmNvbnRleHQ7XG4gICAgaWYgKHR5cGVvZiBhZGRNb2RhbCAhPT0gJ2Z1bmN0aW9uJyB8fCB0eXBlb2YgcmVtb3ZlTW9kYWwgIT09ICdmdW5jdGlvbicpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcihgXG4gICAgICAgIFRvb2x0aXAgRXJyb3I6IE5vIFwiYWRkTW9kYWxcIiBvciBcInJlbW92ZU1vZGFsXCIgZGV0ZWN0ZWQgaW4gY29udGV4dC5cbiAgICAgICAgUGxlYXNlIHVzZSBhIDxNb2RhbEJvdW5kYXJ5PiBpbiB5b3VyIGVsZW1lbnQgdHJlZSB0byBjYXRjaCBtb2RhbHMuXG4gICAgICBgKTtcbiAgICB9XG4gICAgaWYgKCF0aGlzLmVsKSB7XG4gICAgICBjb25zb2xlLmVycm9yKGBcbiAgICAgICAgVG9vbHRpcCBFcnJvcjogQ2FuJ3Qgc2V0dXAgZm9jdXNJbi9mb2N1c091dCBldmVudHMuXG4gICAgICAgIEVsZW1lbnQgcmVmIGNvdWxkIG5vdCBiZSBmb3VuZDsgd2FzIHJlbmRlciBpbnRlcnJ1cHRlZD9cbiAgICAgIGApO1xuICAgIH0gZWxzZSB7XG4gICAgICB0aGlzLmV2ZW50cyA9IG5ldyBFdmVudHNGYWN0b3J5KHRoaXMuZWwpO1xuICAgICAgdGhpcy5ldmVudHMudXNlKHtcbiAgICAgICAgZm9jdXNJbjogKCkgPT4gdGhpcy5zZXRTdGF0ZSh7IGlzRm9jdXM6IHRydWUgfSksXG4gICAgICAgIGtleXByZXNzOiAoKSA9PiB0aGlzLnNldFN0YXRlKHsgaXNGb2N1czogdHJ1ZSB9KSxcbiAgICAgICAgZm9jdXNPdXQ6ICgpID0+IHRoaXMuc2V0U3RhdGUoeyBpc0ZvY3VzOiBmYWxzZSB9KSxcbiAgICAgICAgbW91c2VFbnRlcjogKCkgPT4gdGhpcy5zZXRTdGF0ZSh7IGlzSG92ZXJlZDogdHJ1ZSB9KSxcbiAgICAgICAgbW91c2VMZWF2ZTogKCkgPT4gdGhpcy5zZXRTdGF0ZSh7IGlzSG92ZXJlZDogZmFsc2UgfSlcbiAgICAgIH0pO1xuICAgIH1cbiAgfVxuXG4gIGNvbXBvbmVudFdpbGxVbm1vdW50ICgpIHtcbiAgICBpZiAodGhpcy5ldmVudHMpIHRoaXMuZXZlbnRzLmNsZWFyQWxsKCk7XG4gIH1cblxuICBnZXRIaWRlRGVsYXkgKCkge1xuICAgIGxldCB7IGhpZGVEZWxheSB9ID0gdGhpcy5wcm9wcztcbiAgICByZXR1cm4gdHlwZW9mIGhpZGVEZWxheSA9PT0gJ251bWJlcidcbiAgICAgID8gaGlkZURlbGF5XG4gICAgICA6IDUwMDtcbiAgfVxuXG4gIHNob3dUb29sdGlwICgpIHtcbiAgICBpZiAodGhpcy5pZCkgcmV0dXJuO1xuICAgIGNvbnN0IHsgYWRkTW9kYWwgfSA9IHRoaXMuY29udGV4dDtcbiAgICBjb25zdCB0ZXh0Qm94ID0geyByZW5kZXI6IHRoaXMucmVuZGVyVG9vbHRpcEJveCB9O1xuICAgIHRoaXMuaWQgPSBhZGRNb2RhbCh0ZXh0Qm94KTtcbiAgICBpZiAodGhpcy5oaWRlVGltZW91dCkgY2xlYXJUaW1lb3V0KHRoaXMuaGlkZVRpbWVvdXQpO1xuICB9XG5cbiAgZW5nYWdlVG9vbHRpcCAoKSB7XG4gICAgbGV0IHsgc2hvd0RlbGF5IH0gPSB0aGlzLnByb3BzO1xuICAgIHNob3dEZWxheSA9IHR5cGVvZiBzaG93RGVsYXkgPT09ICdudW1iZXInID8gc2hvd0RlbGF5IDogMjUwO1xuICAgIHRoaXMuc2hvd1RpbWVvdXQgPSBzZXRUaW1lb3V0KCgpID0+IHtcbiAgICAgIHRoaXMuc2hvd1Rvb2x0aXAoKTtcbiAgICAgIGlmICh0aGlzLmhpZGVUaW1lb3V0KSBjbGVhclRpbWVvdXQodGhpcy5oaWRlVGltZW91dCk7XG4gICAgfSwgc2hvd0RlbGF5KTtcbiAgfVxuXG4gIGRpc2VuZ2FnZVRvb2x0aXAgKCkge1xuICAgIGNvbnN0IGhpZGVEZWxheSA9IHRoaXMuZ2V0SGlkZURlbGF5KCk7XG4gICAgaWYgKHRoaXMuc2hvd1RpbWVvdXQpIGNsZWFyVGltZW91dCh0aGlzLnNob3dUaW1lb3V0KTtcbiAgICB0aGlzLmhpZGVUaW1lb3V0ID0gc2V0VGltZW91dCh0aGlzLmhpZGVUb29sdGlwLCBoaWRlRGVsYXkpO1xuICB9XG5cbiAgaGlkZVRvb2x0aXAgKCkge1xuICAgIGlmICghdGhpcy5pZCB8fCB0aGlzLnN0YXRlLmlzRm9jdXMgfHwgdGhpcy5zdGF0ZS5pc0hvdmVyZWQpIHJldHVybjtcbiAgICBjb25zdCB7IHJlbW92ZU1vZGFsIH0gPSB0aGlzLmNvbnRleHQ7XG4gICAgcmVtb3ZlTW9kYWwodGhpcy5pZCk7XG4gICAgdGhpcy5pZCA9IG51bGw7XG4gIH1cblxuICBnZXRDb3JuZXJDbGFzcyAoKSB7XG4gICAgY29uc3QgeyBjb3JuZXIgfSA9IHRoaXMucHJvcHM7XG4gICAgaWYgKHR5cGVvZiBjb3JuZXIgIT09ICdzdHJpbmcnIHx8ICFjb3JuZXIubGVuZ3RoKSByZXR1cm4gJ25vLWNvcm5lcic7XG4gICAgcmV0dXJuIGNvcm5lci5zcGxpdCgnICcpLmZpbHRlcihzID0+IHMpLmpvaW4oJy0nKTtcbiAgfVxuXG4gIHJlbmRlclRvb2x0aXBCb3ggKCkge1xuICAgIGNvbnN0IHsgaXNEaXNlbmdhZ2VkIH0gPSB0aGlzLnN0YXRlO1xuICAgIGNvbnN0IHsgY29udGVudCwgcG9zaXRpb24sIHN0eWxlLCByZW5kZXJIdG1sIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgdG9wLCBsZWZ0LCByaWdodCB9ID0gcG9zaXRpb24gPyBwb3NpdGlvbiA6IHsgdG9wOiAwLCBsZWZ0OiAwLCByaWdodDogMCB9O1xuICAgIGNvbnN0IGNvcm5lckNsYXNzID0gdGhpcy5nZXRDb3JuZXJDbGFzcygpO1xuICAgIGNvbnN0IGJveFN0eWxlID0gT2JqZWN0LmFzc2lnbih7fSwge1xuICAgICAgdG9wLFxuICAgICAgbGVmdCxcbiAgICAgIHJpZ2h0LFxuICAgICAgZGlzcGxheTogJ2Jsb2NrJyxcbiAgICAgIHBvc2l0aW9uOiAnYWJzb2x1dGUnLFxuICAgICAgcG9pbnRlckV2ZW50czogJ2F1dG8nLFxuICAgICAgekluZGV4OiAxMDAwMDAwXG4gICAgfSwgc3R5bGUgJiYgT2JqZWN0LmtleXMoc3R5bGUpLmxlbmd0aCA/IHN0eWxlIDoge30pO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXZcbiAgICAgICAgc3R5bGU9e2JveFN0eWxlfVxuICAgICAgICBjbGFzc05hbWU9eydUb29sdGlwLUNvbnRlbnQgJyArIGNvcm5lckNsYXNzfVxuICAgICAgICBvbk1vdXNlRW50ZXI9e3RoaXMuZW5nYWdlVG9vbHRpcH1cbiAgICAgICAgb25Nb3VzZUxlYXZlPXt0aGlzLmRpc2VuZ2FnZVRvb2x0aXB9PlxuICAgICAgICB7cmVuZGVySHRtbCA/IDxkaXYgZGFuZ2Vyb3VzbHlTZXRJbm5lckhUTUw9e3sgX19odG1sOiBjb250ZW50IH19IC8+IDogY29udGVudH1cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgaXNGb2N1cywgaXNIb3ZlcmVkLCBpc0Rpc2VuZ2FnZWQgfSA9IHRoaXMuc3RhdGU7XG4gICAgaWYgKHRoaXMuZWwgJiYgKGlzRm9jdXMgfHwgaXNIb3ZlcmVkKSkgdGhpcy5lbmdhZ2VUb29sdGlwKCk7XG4gICAgZWxzZSB0aGlzLmRpc2VuZ2FnZVRvb2x0aXAoKTtcblxuICAgIGNvbnN0IHsgY2hpbGRyZW4sIGNsYXNzTmFtZSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBmdWxsQ2xhc3NOYW1lID0gJ1Rvb2x0aXAnXG4gICAgICArIChpc0Rpc2VuZ2FnZWQgPyAnIFRvb2x0aXAtLURpc2VuZ2FnZWQnIDogJycpXG4gICAgICArIChjbGFzc05hbWUgPyAnICcgKyBjbGFzc05hbWUgOiAnJyk7XG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXZcbiAgICAgICAgdGFiSW5kZXg9ezB9XG4gICAgICAgIGNsYXNzTmFtZT17ZnVsbENsYXNzTmFtZX1cbiAgICAgICAgcmVmPXsoZWwpID0+IHRoaXMuZWwgPSBlbH0+XG4gICAgICAgIHtjaGlsZHJlbn1cbiAgICAgIDwvZGl2PlxuICAgIClcbiAgfVxufTtcblxuVG9vbHRpcC5wcm9wVHlwZXMgPSB7XG4gIGhpZGVEZWxheTogUHJvcFR5cGVzLm51bWJlcixcbiAgY2hpbGRyZW46IFByb3BUeXBlcy5ub2RlLFxuICBjbGFzc05hbWU6IFByb3BUeXBlcy5zdHJpbmcsXG4gIGNvbnRlbnQ6IFByb3BUeXBlcy5ub2RlLFxuICBjb3JuZXI6IFByb3BUeXBlcy5zdHJpbmcsXG4gIHBvc2l0aW9uOiBQcm9wVHlwZXMub2JqZWN0XG59O1xuXG5Ub29sdGlwLmNvbnRleHRUeXBlcyA9IHtcbiAgYWRkTW9kYWw6IFByb3BUeXBlcy5mdW5jLFxuICByZW1vdmVNb2RhbDogUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IFRvb2x0aXA7XG4iXX0=