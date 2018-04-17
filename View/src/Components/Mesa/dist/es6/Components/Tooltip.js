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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL1Rvb2x0aXAuanN4Il0sIm5hbWVzIjpbIlRvb2x0aXAiLCJwcm9wcyIsInNob3dUb29sdGlwIiwiYmluZCIsImhpZGVUb29sdGlwIiwicmVuZGVyVG9vbHRpcEJveCIsImVuZ2FnZVRvb2x0aXAiLCJkaXNlbmdhZ2VUb29sdGlwIiwiY29tcG9uZW50RGlkTW91bnQiLCJzdGF0ZSIsImlzRm9jdXMiLCJpc0hvdmVyZWQiLCJjb250ZXh0IiwiYWRkTW9kYWwiLCJyZW1vdmVNb2RhbCIsIkVycm9yIiwiZWwiLCJjb25zb2xlIiwiZXJyb3IiLCJldmVudHMiLCJ1c2UiLCJmb2N1c0luIiwic2V0U3RhdGUiLCJrZXlwcmVzcyIsImZvY3VzT3V0IiwibW91c2VFbnRlciIsIm1vdXNlTGVhdmUiLCJjbGVhckFsbCIsImlkIiwidGV4dEJveCIsInJlbmRlciIsImhpZGVUaW1lb3V0IiwiY2xlYXJUaW1lb3V0Iiwic2hvd0RlbGF5Iiwic2hvd1RpbWVvdXQiLCJzZXRUaW1lb3V0IiwiaGlkZURlbGF5IiwiY29ybmVyIiwibGVuZ3RoIiwic3BsaXQiLCJmaWx0ZXIiLCJzIiwiam9pbiIsImNvbnRlbnQiLCJwb3NpdGlvbiIsInN0eWxlIiwicmVuZGVySHRtbCIsInRvcCIsImxlZnQiLCJyaWdodCIsImNvcm5lckNsYXNzIiwiZ2V0Q29ybmVyQ2xhc3MiLCJib3hTdHlsZSIsIk9iamVjdCIsImFzc2lnbiIsImRpc3BsYXkiLCJwb2ludGVyRXZlbnRzIiwiekluZGV4Iiwia2V5cyIsIl9faHRtbCIsImNoaWxkcmVuIiwiY2xhc3NOYW1lIiwiZnVsbENsYXNzTmFtZSIsIm5vZGUiLCJnZXRCb3VuZGluZ0NsaWVudFJlY3QiLCJDb21wb25lbnQiLCJwcm9wVHlwZXMiLCJudW1iZXIiLCJzdHJpbmciLCJvYmplY3QiLCJjb250ZXh0VHlwZXMiLCJmdW5jIl0sIm1hcHBpbmdzIjoiOzs7Ozs7OztBQUFBOzs7O0FBQ0E7Ozs7QUFFQTs7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QjtBQUFBLFdBSFA7QUFJRzRDO0FBSkgsT0FERjtBQVFEOzs7OEJBOUdpQkcsSSxFQUFNO0FBQ3RCLGFBQU9BLEtBQUtDLHFCQUFMLEVBQVA7QUFDRDs7OztFQWRtQixnQkFBTUMsUzs7QUEySDNCOztBQUVEakUsUUFBUWtFLFNBQVIsR0FBb0I7QUFDbEI5QixhQUFXLG9CQUFVK0IsTUFESDtBQUVsQlAsWUFBVSxvQkFBVUcsSUFGRjtBQUdsQkYsYUFBVyxvQkFBVU8sTUFISDtBQUlsQnpCLFdBQVMsb0JBQVVvQixJQUpEO0FBS2xCMUIsVUFBUSxvQkFBVStCLE1BTEE7QUFNbEJ4QixZQUFVLG9CQUFVeUI7QUFORixDQUFwQjs7QUFTQXJFLFFBQVFzRSxZQUFSLEdBQXVCO0FBQ3JCekQsWUFBVSxvQkFBVTBELElBREM7QUFFckJ6RCxlQUFhLG9CQUFVeUQ7QUFGRixDQUF2Qjs7a0JBS2V2RSxPIiwiZmlsZSI6IlRvb2x0aXAuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IFByb3BUeXBlcyBmcm9tICdwcm9wLXR5cGVzJztcblxuaW1wb3J0IHsgRXZlbnRzRmFjdG9yeSB9IGZyb20gJy4uL1V0aWxzL0V2ZW50cyc7XG5cbmNsYXNzIFRvb2x0aXAgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG4gICAgdGhpcy5zaG93VG9vbHRpcCA9IHRoaXMuc2hvd1Rvb2x0aXAuYmluZCh0aGlzKTtcbiAgICB0aGlzLmhpZGVUb29sdGlwID0gdGhpcy5oaWRlVG9vbHRpcC5iaW5kKHRoaXMpO1xuICAgIHRoaXMucmVuZGVyVG9vbHRpcEJveCA9IHRoaXMucmVuZGVyVG9vbHRpcEJveC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuZW5nYWdlVG9vbHRpcCA9IHRoaXMuZW5nYWdlVG9vbHRpcC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuZGlzZW5nYWdlVG9vbHRpcCA9IHRoaXMuZGlzZW5nYWdlVG9vbHRpcC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuY29tcG9uZW50RGlkTW91bnQgPSB0aGlzLmNvbXBvbmVudERpZE1vdW50LmJpbmQodGhpcyk7XG4gICAgdGhpcy5zdGF0ZSA9IHsgaXNGb2N1czogZmFsc2UsIGlzSG92ZXJlZDogZmFsc2UgfTtcbiAgfVxuXG4gIHN0YXRpYyBnZXRPZmZzZXQgKG5vZGUpIHtcbiAgICByZXR1cm4gbm9kZS5nZXRCb3VuZGluZ0NsaWVudFJlY3QoKTtcbiAgfVxuXG4gIGNvbXBvbmVudERpZE1vdW50ICgpIHtcbiAgICBjb25zdCB7IGFkZE1vZGFsLCByZW1vdmVNb2RhbCB9ID0gdGhpcy5jb250ZXh0O1xuICAgIGlmICh0eXBlb2YgYWRkTW9kYWwgIT09ICdmdW5jdGlvbicgfHwgdHlwZW9mIHJlbW92ZU1vZGFsICE9PSAnZnVuY3Rpb24nKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoYFxuICAgICAgICBUb29sdGlwIEVycm9yOiBObyBcImFkZE1vZGFsXCIgb3IgXCJyZW1vdmVNb2RhbFwiIGRldGVjdGVkIGluIGNvbnRleHQuXG4gICAgICAgIFBsZWFzZSB1c2UgYSA8TW9kYWxCb3VuZGFyeT4gaW4geW91ciBlbGVtZW50IHRyZWUgdG8gY2F0Y2ggbW9kYWxzLlxuICAgICAgYCk7XG4gICAgfVxuICAgIGlmICghdGhpcy5lbCkge1xuICAgICAgY29uc29sZS5lcnJvcihgXG4gICAgICAgIFRvb2x0aXAgRXJyb3I6IENhbid0IHNldHVwIGZvY3VzSW4vZm9jdXNPdXQgZXZlbnRzLlxuICAgICAgICBFbGVtZW50IHJlZiBjb3VsZCBub3QgYmUgZm91bmQ7IHdhcyByZW5kZXIgaW50ZXJydXB0ZWQ/XG4gICAgICBgKTtcbiAgICB9IGVsc2Uge1xuICAgICAgdGhpcy5ldmVudHMgPSBuZXcgRXZlbnRzRmFjdG9yeSh0aGlzLmVsKTtcbiAgICAgIHRoaXMuZXZlbnRzLnVzZSh7XG4gICAgICAgIGZvY3VzSW46ICgpID0+IHRoaXMuc2V0U3RhdGUoeyBpc0ZvY3VzOiB0cnVlIH0pLFxuICAgICAgICBrZXlwcmVzczogKCkgPT4gdGhpcy5zZXRTdGF0ZSh7IGlzRm9jdXM6IHRydWUgfSksXG4gICAgICAgIGZvY3VzT3V0OiAoKSA9PiB0aGlzLnNldFN0YXRlKHsgaXNGb2N1czogZmFsc2UgfSksXG4gICAgICAgIG1vdXNlRW50ZXI6ICgpID0+IHRoaXMuc2V0U3RhdGUoeyBpc0hvdmVyZWQ6IHRydWUgfSksXG4gICAgICAgIG1vdXNlTGVhdmU6ICgpID0+IHRoaXMuc2V0U3RhdGUoeyBpc0hvdmVyZWQ6IGZhbHNlIH0pXG4gICAgICB9KTtcbiAgICB9XG4gIH1cblxuICBjb21wb25lbnRXaWxsVW5tb3VudCAoKSB7XG4gICAgaWYgKHRoaXMuZXZlbnRzKSB0aGlzLmV2ZW50cy5jbGVhckFsbCgpO1xuICB9XG5cbiAgc2hvd1Rvb2x0aXAgKCkge1xuICAgIGlmICh0aGlzLmlkKSByZXR1cm47XG4gICAgY29uc3QgeyBhZGRNb2RhbCB9ID0gdGhpcy5jb250ZXh0O1xuICAgIGNvbnN0IHRleHRCb3ggPSB7IHJlbmRlcjogdGhpcy5yZW5kZXJUb29sdGlwQm94IH07XG4gICAgdGhpcy5pZCA9IGFkZE1vZGFsKHRleHRCb3gpO1xuICAgIGlmICh0aGlzLmhpZGVUaW1lb3V0KSBjbGVhclRpbWVvdXQodGhpcy5oaWRlVGltZW91dCk7XG4gIH1cblxuICBlbmdhZ2VUb29sdGlwICgpIHtcbiAgICBsZXQgeyBzaG93RGVsYXkgfSA9IHRoaXMucHJvcHM7XG4gICAgc2hvd0RlbGF5ID0gdHlwZW9mIHNob3dEZWxheSA9PT0gJ251bWJlcicgPyBzaG93RGVsYXkgOiAyNTA7XG4gICAgdGhpcy5zaG93VGltZW91dCA9IHNldFRpbWVvdXQoKCkgPT4ge1xuICAgICAgdGhpcy5zaG93VG9vbHRpcCgpO1xuICAgICAgaWYgKHRoaXMuaGlkZVRpbWVvdXQpIGNsZWFyVGltZW91dCh0aGlzLmhpZGVUaW1lb3V0KTtcbiAgICB9LCBzaG93RGVsYXkpO1xuICB9XG5cbiAgZGlzZW5nYWdlVG9vbHRpcCAoKSB7XG4gICAgbGV0IHsgaGlkZURlbGF5IH0gPSB0aGlzLnByb3BzO1xuICAgIGhpZGVEZWxheSA9IHR5cGVvZiBoaWRlRGVsYXkgPT09ICdudW1iZXInID8gaGlkZURlbGF5IDogNTAwO1xuICAgIGlmICh0aGlzLnNob3dUaW1lb3V0KSBjbGVhclRpbWVvdXQodGhpcy5zaG93VGltZW91dCk7XG4gICAgdGhpcy5oaWRlVGltZW91dCA9IHNldFRpbWVvdXQodGhpcy5oaWRlVG9vbHRpcCwgaGlkZURlbGF5KTtcbiAgfVxuXG4gIGhpZGVUb29sdGlwICgpIHtcbiAgICBpZiAoIXRoaXMuaWQgfHwgdGhpcy5zdGF0ZS5pc0ZvY3VzIHx8IHRoaXMuc3RhdGUuaXNIb3ZlcmVkKSByZXR1cm47XG4gICAgY29uc3QgeyByZW1vdmVNb2RhbCB9ID0gdGhpcy5jb250ZXh0O1xuICAgIHJlbW92ZU1vZGFsKHRoaXMuaWQpO1xuICAgIHRoaXMuaWQgPSBudWxsO1xuICB9XG5cbiAgZ2V0Q29ybmVyQ2xhc3MgKCkge1xuICAgIGNvbnN0IHsgY29ybmVyIH0gPSB0aGlzLnByb3BzO1xuICAgIGlmICh0eXBlb2YgY29ybmVyICE9PSAnc3RyaW5nJyB8fCAhY29ybmVyLmxlbmd0aCkgcmV0dXJuICduby1jb3JuZXInO1xuICAgIHJldHVybiBjb3JuZXIuc3BsaXQoJyAnKS5maWx0ZXIocyA9PiBzKS5qb2luKCctJyk7XG4gIH1cblxuICByZW5kZXJUb29sdGlwQm94ICgpIHtcbiAgICBjb25zdCB7IGNvbnRlbnQsIHBvc2l0aW9uLCBzdHlsZSwgcmVuZGVySHRtbCB9ID0gdGhpcy5wcm9wcztcbiAgICBsZXQgeyB0b3AsIGxlZnQsIHJpZ2h0IH0gPSBwb3NpdGlvbiA/IHBvc2l0aW9uIDogeyB0b3A6IDAsIGxlZnQ6IDAsIHJpZ2h0OiAwIH07XG4gICAgY29uc3QgY29ybmVyQ2xhc3MgPSB0aGlzLmdldENvcm5lckNsYXNzKCk7XG4gICAgY29uc3QgYm94U3R5bGUgPSBPYmplY3QuYXNzaWduKHt9LCB7XG4gICAgICB0b3AsXG4gICAgICBsZWZ0LFxuICAgICAgcmlnaHQsXG4gICAgICBkaXNwbGF5OiAnYmxvY2snLFxuICAgICAgcG9zaXRpb246ICdhYnNvbHV0ZScsXG4gICAgICBwb2ludGVyRXZlbnRzOiAnYXV0bycsXG4gICAgICB6SW5kZXg6IDEwMDAwMDBcbiAgICB9LCBzdHlsZSAmJiBPYmplY3Qua2V5cyhzdHlsZSkubGVuZ3RoID8gc3R5bGUgOiB7fSk7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdlxuICAgICAgICBzdHlsZT17Ym94U3R5bGV9XG4gICAgICAgIGNsYXNzTmFtZT17J1Rvb2x0aXAtQ29udGVudCAnICsgY29ybmVyQ2xhc3N9XG4gICAgICAgIG9uTW91c2VFbnRlcj17dGhpcy5lbmdhZ2VUb29sdGlwfVxuICAgICAgICBvbk1vdXNlTGVhdmU9e3RoaXMuZGlzZW5nYWdlVG9vbHRpcH0+XG4gICAgICAgIHtyZW5kZXJIdG1sID8gPGRpdiBkYW5nZXJvdXNseVNldElubmVySFRNTD17eyBfX2h0bWw6IGNvbnRlbnQgfX0gLz4gOiBjb250ZW50fVxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxuXG4gIHJlbmRlciAoKSB7XG4gICAgY29uc3QgeyBpc0ZvY3VzLCBpc0hvdmVyZWQgfSA9IHRoaXMuc3RhdGU7XG4gICAgaWYgKHRoaXMuZWwgJiYgKGlzRm9jdXMgfHwgaXNIb3ZlcmVkKSkgdGhpcy5lbmdhZ2VUb29sdGlwKCk7XG4gICAgZWxzZSB0aGlzLmRpc2VuZ2FnZVRvb2x0aXAoKTtcblxuICAgIGNvbnN0IHsgY2hpbGRyZW4sIGNsYXNzTmFtZSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBmdWxsQ2xhc3NOYW1lID0gJ1Rvb2x0aXAnICsgKGNsYXNzTmFtZSA/ICcgJyArIGNsYXNzTmFtZSA6ICcnKTtcbiAgICByZXR1cm4gKFxuICAgICAgPGRpdlxuICAgICAgICB0YWJJbmRleD17MH1cbiAgICAgICAgY2xhc3NOYW1lPXtmdWxsQ2xhc3NOYW1lfVxuICAgICAgICByZWY9eyhlbCkgPT4gdGhpcy5lbCA9IGVsfT5cbiAgICAgICAge2NoaWxkcmVufVxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG59O1xuXG5Ub29sdGlwLnByb3BUeXBlcyA9IHtcbiAgaGlkZURlbGF5OiBQcm9wVHlwZXMubnVtYmVyLFxuICBjaGlsZHJlbjogUHJvcFR5cGVzLm5vZGUsXG4gIGNsYXNzTmFtZTogUHJvcFR5cGVzLnN0cmluZyxcbiAgY29udGVudDogUHJvcFR5cGVzLm5vZGUsXG4gIGNvcm5lcjogUHJvcFR5cGVzLnN0cmluZyxcbiAgcG9zaXRpb246IFByb3BUeXBlcy5vYmplY3Rcbn07XG5cblRvb2x0aXAuY29udGV4dFR5cGVzID0ge1xuICBhZGRNb2RhbDogUHJvcFR5cGVzLmZ1bmMsXG4gIHJlbW92ZU1vZGFsOiBQcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgVG9vbHRpcDtcbiJdfQ==