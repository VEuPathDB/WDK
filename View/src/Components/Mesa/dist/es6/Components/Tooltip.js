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
      isHovered: false,
      isDisengaged: false
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
          removeModal = _context.removeModal,
          updateModal = _context.updateModal;

      if (typeof addModal !== 'function' || typeof removeModal !== 'function' || typeof updateModal !== 'function') {
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

      var updateModal = this.context.updateModal;

      var showDelay = this.getShowDelay();

      updateModal(this.id, { render: function render() {
          return _this4.renderTooltipBox();
        } });

      this.showTimeout = setTimeout(function () {
        _this4.showTooltip();
        if (_this4.hideTimeout) clearTimeout(_this4.hideTimeout);
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
      var disengaged = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : false;
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
          className: 'Tooltip-Content ' + cornerClass + (disengaged ? ' Tooltip-Content--Disengaged' : ''),
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
  updateModal: _propTypes2.default.func,
  removeModal: _propTypes2.default.func
};

exports.default = Tooltip;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL1Rvb2x0aXAuanN4Il0sIm5hbWVzIjpbIlRvb2x0aXAiLCJwcm9wcyIsInN0YXRlIiwiaXNGb2N1cyIsImlzSG92ZXJlZCIsImlzRGlzZW5nYWdlZCIsInNob3dUb29sdGlwIiwiYmluZCIsImhpZGVUb29sdGlwIiwiZ2V0SGlkZURlbGF5IiwiZ2V0U2hvd0RlbGF5IiwiZW5nYWdlVG9vbHRpcCIsImRpc2VuZ2FnZVRvb2x0aXAiLCJyZW5kZXJUb29sdGlwQm94IiwiY29tcG9uZW50RGlkTW91bnQiLCJzaG93RGVsYXkiLCJjb250ZXh0IiwiYWRkTW9kYWwiLCJyZW1vdmVNb2RhbCIsInVwZGF0ZU1vZGFsIiwiRXJyb3IiLCJlbCIsImNvbnNvbGUiLCJlcnJvciIsImV2ZW50cyIsInVzZSIsImZvY3VzSW4iLCJzZXRTdGF0ZSIsImtleXByZXNzIiwiZm9jdXNPdXQiLCJtb3VzZUVudGVyIiwibW91c2VMZWF2ZSIsImNsZWFyQWxsIiwiaGlkZURlbGF5IiwiaWQiLCJyZW5kZXIiLCJoaWRlVGltZW91dCIsImNsZWFyVGltZW91dCIsInNob3dUaW1lb3V0Iiwic2V0VGltZW91dCIsImNvcm5lciIsImxlbmd0aCIsInNwbGl0IiwiZmlsdGVyIiwicyIsImpvaW4iLCJkaXNlbmdhZ2VkIiwiY29udGVudCIsInBvc2l0aW9uIiwic3R5bGUiLCJyZW5kZXJIdG1sIiwidG9wIiwibGVmdCIsInJpZ2h0IiwiY29ybmVyQ2xhc3MiLCJnZXRDb3JuZXJDbGFzcyIsImJveFN0eWxlIiwiT2JqZWN0IiwiYXNzaWduIiwiZGlzcGxheSIsInBvaW50ZXJFdmVudHMiLCJ6SW5kZXgiLCJrZXlzIiwiX19odG1sIiwiY2hpbGRyZW4iLCJjbGFzc05hbWUiLCJmdWxsQ2xhc3NOYW1lIiwibm9kZSIsImdldEJvdW5kaW5nQ2xpZW50UmVjdCIsIkNvbXBvbmVudCIsInByb3BUeXBlcyIsIm51bWJlciIsInN0cmluZyIsIm9iamVjdCIsImNv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b0JBQVV5QjtBQU5GLENBQXBCOztBQVNBekUsUUFBUTBFLFlBQVIsR0FBdUI7QUFDckJ6RCxZQUFVLG9CQUFVMEQsSUFEQztBQUVyQnhELGVBQWEsb0JBQVV3RCxJQUZGO0FBR3JCekQsZUFBYSxvQkFBVXlEO0FBSEYsQ0FBdkI7O2tCQU1lM0UsTyIsImZpbGUiOiJUb29sdGlwLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcbmltcG9ydCBQcm9wVHlwZXMgZnJvbSAncHJvcC10eXBlcyc7XG5cbmltcG9ydCB7IEV2ZW50c0ZhY3RvcnkgfSBmcm9tICcuLi9VdGlscy9FdmVudHMnO1xuXG5jbGFzcyBUb29sdGlwIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5zdGF0ZSA9IHtcbiAgICAgIGlzRm9jdXM6IGZhbHNlLFxuICAgICAgaXNIb3ZlcmVkOiBmYWxzZSxcbiAgICAgIGlzRGlzZW5nYWdlZDogZmFsc2VcbiAgICB9O1xuXG4gICAgdGhpcy5zaG93VG9vbHRpcCA9IHRoaXMuc2hvd1Rvb2x0aXAuYmluZCh0aGlzKTtcbiAgICB0aGlzLmhpZGVUb29sdGlwID0gdGhpcy5oaWRlVG9vbHRpcC5iaW5kKHRoaXMpO1xuXG4gICAgdGhpcy5nZXRIaWRlRGVsYXkgPSB0aGlzLmdldEhpZGVEZWxheS5iaW5kKHRoaXMpO1xuICAgIHRoaXMuZ2V0U2hvd0RlbGF5ID0gdGhpcy5nZXRTaG93RGVsYXkuYmluZCh0aGlzKTtcblxuICAgIHRoaXMuZW5nYWdlVG9vbHRpcCA9IHRoaXMuZW5nYWdlVG9vbHRpcC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuZGlzZW5nYWdlVG9vbHRpcCA9IHRoaXMuZGlzZW5nYWdlVG9vbHRpcC5iaW5kKHRoaXMpO1xuXG4gICAgdGhpcy5yZW5kZXJUb29sdGlwQm94ID0gdGhpcy5yZW5kZXJUb29sdGlwQm94LmJpbmQodGhpcyk7XG4gICAgdGhpcy5jb21wb25lbnREaWRNb3VudCA9IHRoaXMuY29tcG9uZW50RGlkTW91bnQuYmluZCh0aGlzKTtcbiAgfVxuXG4gIHN0YXRpYyBnZXRPZmZzZXQgKG5vZGUpIHtcbiAgICByZXR1cm4gbm9kZS5nZXRCb3VuZGluZ0NsaWVudFJlY3QoKTtcbiAgfVxuXG4gIGdldFNob3dEZWxheSAoKSB7XG4gICAgY29uc3QgeyBzaG93RGVsYXkgfSA9IHRoaXMucHJvcHM7XG4gICAgcmV0dXJuIHR5cGVvZiBzaG93RGVsYXkgPT09ICdudW1iZXInXG4gICAgICA/IHNob3dEZWxheVxuICAgICAgOiAyNTA7XG4gIH1cblxuICBjb21wb25lbnREaWRNb3VudCAoKSB7XG4gICAgY29uc3QgeyBhZGRNb2RhbCwgcmVtb3ZlTW9kYWwsIHVwZGF0ZU1vZGFsIH0gPSB0aGlzLmNvbnRleHQ7XG4gICAgaWYgKFxuICAgICAgdHlwZW9mIGFkZE1vZGFsICE9PSAnZnVuY3Rpb24nXG4gICAgICB8fCB0eXBlb2YgcmVtb3ZlTW9kYWwgIT09ICdmdW5jdGlvbidcbiAgICAgIHx8IHR5cGVvZiB1cGRhdGVNb2RhbCAhPT0gJ2Z1bmN0aW9uJ1xuICAgICkge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKGBcbiAgICAgICAgVG9vbHRpcCBFcnJvcjogTm8gXCJhZGRNb2RhbFwiIG9yIFwicmVtb3ZlTW9kYWxcIiBkZXRlY3RlZCBpbiBjb250ZXh0LlxuICAgICAgICBQbGVhc2UgdXNlIGEgPE1vZGFsQm91bmRhcnk+IGluIHlvdXIgZWxlbWVudCB0cmVlIHRvIGNhdGNoIG1vZGFscy5cbiAgICAgIGApO1xuICAgIH1cbiAgICBpZiAoIXRoaXMuZWwpIHtcbiAgICAgIGNvbnNvbGUuZXJyb3IoYFxuICAgICAgICBUb29sdGlwIEVycm9yOiBDYW4ndCBzZXR1cCBmb2N1c0luL2ZvY3VzT3V0IGV2ZW50cy5cbiAgICAgICAgRWxlbWVudCByZWYgY291bGQgbm90IGJlIGZvdW5kOyB3YXMgcmVuZGVyIGludGVycnVwdGVkP1xuICAgICAgYCk7XG4gICAgfSBlbHNlIHtcbiAgICAgIHRoaXMuZXZlbnRzID0gbmV3IEV2ZW50c0ZhY3RvcnkodGhpcy5lbCk7XG4gICAgICB0aGlzLmV2ZW50cy51c2Uoe1xuICAgICAgICBmb2N1c0luOiAoKSA9PiB0aGlzLnNldFN0YXRlKHsgaXNGb2N1czogdHJ1ZSB9KSxcbiAgICAgICAga2V5cHJlc3M6ICgpID0+IHRoaXMuc2V0U3RhdGUoeyBpc0ZvY3VzOiB0cnVlIH0pLFxuICAgICAgICBmb2N1c091dDogKCkgPT4gdGhpcy5zZXRTdGF0ZSh7IGlzRm9jdXM6IGZhbHNlIH0pLFxuICAgICAgICBtb3VzZUVudGVyOiAoKSA9PiB0aGlzLnNldFN0YXRlKHsgaXNIb3ZlcmVkOiB0cnVlIH0pLFxuICAgICAgICBtb3VzZUxlYXZlOiAoKSA9PiB0aGlzLnNldFN0YXRlKHsgaXNIb3ZlcmVkOiBmYWxzZSB9KVxuICAgICAgfSk7XG4gICAgfVxuICB9XG5cbiAgY29tcG9uZW50V2lsbFVubW91bnQgKCkge1xuICAgIGlmICh0aGlzLmV2ZW50cykgdGhpcy5ldmVudHMuY2xlYXJBbGwoKTtcbiAgfVxuXG4gIGdldEhpZGVEZWxheSAoKSB7XG4gICAgbGV0IHsgaGlkZURlbGF5IH0gPSB0aGlzLnByb3BzO1xuICAgIHJldHVybiB0eXBlb2YgaGlkZURlbGF5ID09PSAnbnVtYmVyJ1xuICAgICAgPyBoaWRlRGVsYXlcbiAgICAgIDogNTAwO1xuICB9XG5cbiAgc2hvd1Rvb2x0aXAgKCkge1xuICAgIGlmICh0aGlzLmlkKSByZXR1cm47XG4gICAgY29uc3QgeyBhZGRNb2RhbCB9ID0gdGhpcy5jb250ZXh0O1xuICAgIHRoaXMuaWQgPSBhZGRNb2RhbCh7IHJlbmRlcjogKCkgPT4gdGhpcy5yZW5kZXJUb29sdGlwQm94KCkgfSk7XG4gICAgaWYgKHRoaXMuaGlkZVRpbWVvdXQpIGNsZWFyVGltZW91dCh0aGlzLmhpZGVUaW1lb3V0KTtcbiAgfVxuXG4gIGVuZ2FnZVRvb2x0aXAgKCkge1xuICAgIGNvbnN0IHsgdXBkYXRlTW9kYWwgfSA9IHRoaXMuY29udGV4dDtcbiAgICBjb25zdCBzaG93RGVsYXkgPSB0aGlzLmdldFNob3dEZWxheSgpO1xuXG4gICAgdXBkYXRlTW9kYWwodGhpcy5pZCwgeyByZW5kZXI6ICgpID0+IHRoaXMucmVuZGVyVG9vbHRpcEJveCgpIH0pO1xuXG4gICAgdGhpcy5zaG93VGltZW91dCA9IHNldFRpbWVvdXQoKCkgPT4ge1xuICAgICAgdGhpcy5zaG93VG9vbHRpcCgpO1xuICAgICAgaWYgKHRoaXMuaGlkZVRpbWVvdXQpIGNsZWFyVGltZW91dCh0aGlzLmhpZGVUaW1lb3V0KTtcbiAgICB9LCBzaG93RGVsYXkpO1xuICB9XG5cbiAgZGlzZW5nYWdlVG9vbHRpcCAoKSB7XG4gICAgY29uc3QgaGlkZURlbGF5ID0gdGhpcy5nZXRIaWRlRGVsYXkoKTtcbiAgICBpZiAodGhpcy5zaG93VGltZW91dCkgY2xlYXJUaW1lb3V0KHRoaXMuc2hvd1RpbWVvdXQpO1xuICAgIHRoaXMuaGlkZVRpbWVvdXQgPSBzZXRUaW1lb3V0KHRoaXMuaGlkZVRvb2x0aXAsIGhpZGVEZWxheSk7XG4gIH1cblxuICBoaWRlVG9vbHRpcCAoKSB7XG4gICAgaWYgKCF0aGlzLmlkIHx8IHRoaXMuc3RhdGUuaXNGb2N1cyB8fCB0aGlzLnN0YXRlLmlzSG92ZXJlZCkgcmV0dXJuO1xuICAgIGNvbnN0IHsgcmVtb3ZlTW9kYWwgfSA9IHRoaXMuY29udGV4dDtcbiAgICByZW1vdmVNb2RhbCh0aGlzLmlkKTtcbiAgICB0aGlzLmlkID0gbnVsbDtcbiAgfVxuXG4gIGdldENvcm5lckNsYXNzICgpIHtcbiAgICBjb25zdCB7IGNvcm5lciB9ID0gdGhpcy5wcm9wcztcbiAgICBpZiAodHlwZW9mIGNvcm5lciAhPT0gJ3N0cmluZycgfHwgIWNvcm5lci5sZW5ndGgpIHJldHVybiAnbm8tY29ybmVyJztcbiAgICByZXR1cm4gY29ybmVyLnNwbGl0KCcgJykuZmlsdGVyKHMgPT4gcykuam9pbignLScpO1xuICB9XG5cbiAgcmVuZGVyVG9vbHRpcEJveCAoZGlzZW5nYWdlZCA9IGZhbHNlKSB7XG4gICAgY29uc3QgeyBjb250ZW50LCBwb3NpdGlvbiwgc3R5bGUsIHJlbmRlckh0bWwgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyB0b3AsIGxlZnQsIHJpZ2h0IH0gPSBwb3NpdGlvbiA/IHBvc2l0aW9uIDogeyB0b3A6IDAsIGxlZnQ6IDAsIHJpZ2h0OiAwIH07XG4gICAgY29uc3QgY29ybmVyQ2xhc3MgPSB0aGlzLmdldENvcm5lckNsYXNzKCk7XG4gICAgY29uc3QgYm94U3R5bGUgPSBPYmplY3QuYXNzaWduKHt9LCB7XG4gICAgICB0b3AsXG4gICAgICBsZWZ0LFxuICAgICAgcmlnaHQsXG4gICAgICBkaXNwbGF5OiAnYmxvY2snLFxuICAgICAgcG9zaXRpb246ICdhYnNvbHV0ZScsXG4gICAgICBwb2ludGVyRXZlbnRzOiAnYXV0bycsXG4gICAgICB6SW5kZXg6IDEwMDAwMDBcbiAgICB9LCBzdHlsZSAmJiBPYmplY3Qua2V5cyhzdHlsZSkubGVuZ3RoID8gc3R5bGUgOiB7fSk7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdlxuICAgICAgICBzdHlsZT17Ym94U3R5bGV9XG4gICAgICAgIGNsYXNzTmFtZT17J1Rvb2x0aXAtQ29udGVudCAnICsgY29ybmVyQ2xhc3MgKyAoZGlzZW5nYWdlZCA/ICcgVG9vbHRpcC1Db250ZW50LS1EaXNlbmdhZ2VkJyA6ICcnKX1cbiAgICAgICAgb25Nb3VzZUVudGVyPXt0aGlzLmVuZ2FnZVRvb2x0aXB9XG4gICAgICAgIG9uTW91c2VMZWF2ZT17dGhpcy5kaXNlbmdhZ2VUb29sdGlwfT5cbiAgICAgICAge3JlbmRlckh0bWwgPyA8ZGl2IGRhbmdlcm91c2x5U2V0SW5uZXJIVE1MPXt7IF9faHRtbDogY29udGVudCB9fSAvPiA6IGNvbnRlbnR9XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG5cbiAgcmVuZGVyICgpIHtcbiAgICBjb25zdCB7IGlzRm9jdXMsIGlzSG92ZXJlZCwgaXNEaXNlbmdhZ2VkIH0gPSB0aGlzLnN0YXRlO1xuICAgIGlmICh0aGlzLmVsICYmIChpc0ZvY3VzIHx8IGlzSG92ZXJlZCkpIHRoaXMuZW5nYWdlVG9vbHRpcCgpO1xuICAgIGVsc2UgdGhpcy5kaXNlbmdhZ2VUb29sdGlwKCk7XG5cbiAgICBjb25zdCB7IGNoaWxkcmVuLCBjbGFzc05hbWUgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgZnVsbENsYXNzTmFtZSA9ICdUb29sdGlwJ1xuICAgICAgKyAoaXNEaXNlbmdhZ2VkID8gJyBUb29sdGlwLS1EaXNlbmdhZ2VkJyA6ICcnKVxuICAgICAgKyAoY2xhc3NOYW1lID8gJyAnICsgY2xhc3NOYW1lIDogJycpO1xuICAgIHJldHVybiAoXG4gICAgICA8ZGl2XG4gICAgICAgIHRhYkluZGV4PXswfVxuICAgICAgICBjbGFzc05hbWU9e2Z1bGxDbGFzc05hbWV9XG4gICAgICAgIHJlZj17KGVsKSA9PiB0aGlzLmVsID0gZWx9PlxuICAgICAgICB7Y2hpbGRyZW59XG4gICAgICA8L2Rpdj5cbiAgICApXG4gIH1cbn07XG5cblRvb2x0aXAucHJvcFR5cGVzID0ge1xuICBoaWRlRGVsYXk6IFByb3BUeXBlcy5udW1iZXIsXG4gIGNoaWxkcmVuOiBQcm9wVHlwZXMubm9kZSxcbiAgY2xhc3NOYW1lOiBQcm9wVHlwZXMuc3RyaW5nLFxuICBjb250ZW50OiBQcm9wVHlwZXMubm9kZSxcbiAgY29ybmVyOiBQcm9wVHlwZXMuc3RyaW5nLFxuICBwb3NpdGlvbjogUHJvcFR5cGVzLm9iamVjdFxufTtcblxuVG9vbHRpcC5jb250ZXh0VHlwZXMgPSB7XG4gIGFkZE1vZGFsOiBQcm9wVHlwZXMuZnVuYyxcbiAgdXBkYXRlTW9kYWw6IFByb3BUeXBlcy5mdW5jLFxuICByZW1vdmVNb2RhbDogUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IFRvb2x0aXA7XG4iXX0=