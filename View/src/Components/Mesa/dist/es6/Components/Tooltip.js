'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _BodyLayer = require('./BodyLayer');

var _BodyLayer2 = _interopRequireDefault(_BodyLayer);

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
      isShown: false,
      isFocus: false,
      isHovered: false,
      isDisengaged: true
    };

    _this.componentDidMount = _this.componentDidMount.bind(_this);
    _this.componentWillUnmount = _this.componentWillUnmount.bind(_this);
    _this.getHideDelay = _this.getHideDelay.bind(_this);
    _this.getShowDelay = _this.getShowDelay.bind(_this);
    _this.getCornerClass = _this.getCornerClass.bind(_this);
    _this.showTooltip = _this.showTooltip.bind(_this);
    _this.hideTooltip = _this.hideTooltip.bind(_this);
    _this.engageTooltip = _this.engageTooltip.bind(_this);
    _this.disengageTooltip = _this.disengageTooltip.bind(_this);
    _this.renderTooltipContent = _this.renderTooltipContent.bind(_this);
    return _this;
  }

  /* -=-=-=-=-=-=-=-=-=-=-=-= Lifecycle -=-=-=-=-=-=-=-=-=-=-=-= */

  _createClass(Tooltip, [{
    key: 'componentDidMount',
    value: function componentDidMount() {
      if (!this.el) {
        console.error('\n        Tooltip Error: Can\'t setup focusIn/focusOut events.\n        Element ref could not be found; was render interrupted?\n      ');
      } else {
        this.events = new _Events.EventsFactory(this.el);
        this.events.use({
          focusIn: this.engageTooltip,
          keypress: this.engageTooltip,
          mouseEnter: this.engageTooltip,

          focusOut: this.disengageTooltip,
          mouseLeave: this.disengageTooltip
        });
      }
    }
  }, {
    key: 'componentWillUnmount',
    value: function componentWillUnmount() {
      if (this.events) this.events.clearAll();
    }

    /* -=-=-=-=-=-=-=-=-=-=-=-= Utilities -=-=-=-=-=-=-=-=-=-=-=-= */

  }, {
    key: 'getShowDelay',
    value: function getShowDelay() {
      var showDelay = this.props.showDelay;

      return typeof showDelay === 'number' ? showDelay : 250;
    }
  }, {
    key: 'getHideDelay',
    value: function getHideDelay() {
      var hideDelay = this.props.hideDelay;

      return typeof hideDelay === 'number' ? hideDelay : 500;
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

    /* -=-=-=-=-=-=-=-=-=-=-=-= Show/Hide -=-=-=-=-=-=-=-=-=-=-=-= */

  }, {
    key: 'showTooltip',
    value: function showTooltip() {
      this.setState({ isShown: true });
      if (this.hideTimeout) clearTimeout(this.hideTimeout);
    }
  }, {
    key: 'hideTooltip',
    value: function hideTooltip() {
      if (!this.state.isDisengaged) return;
      this.setState({ isShown: false });
    }

    /* -=-=-=-=-=-=-=-=-=-=-=-= Engage/Disengage -=-=-=-=-=-=-=-=-=-=-=-= */

  }, {
    key: 'engageTooltip',
    value: function engageTooltip() {
      var _this2 = this;

      this.setState({ isDisengaged: false });
      this.showTimeout = setTimeout(function () {
        _this2.showTooltip();
        if (_this2.hideTimeout) clearTimeout(_this2.hideTimeout);
      }, this.getShowDelay());
    }
  }, {
    key: 'disengageTooltip',
    value: function disengageTooltip() {
      this.setState({ isDisengaged: true });
      if (this.showTimeout) clearTimeout(this.showTimeout);
      this.hideTimeout = setTimeout(this.hideTooltip, this.getHideDelay());
    }

    /* -=-=-=-=-=-=-=-=-=-=-=-= Renderers -=-=-=-=-=-=-=-=-=-=-=-= */

  }, {
    key: 'renderTooltipContent',
    value: function renderTooltipContent() {
      var isDisengaged = this.state.isDisengaged;
      var _props = this.props,
          content = _props.content,
          position = _props.position,
          style = _props.style,
          renderHtml = _props.renderHtml;


      var opacity = isDisengaged ? 0.01 : 1;

      var _Object$assign = Object.assign({ top: 0, left: 0 }, position),
          top = _Object$assign.top,
          left = _Object$assign.left;

      var existingStyle = style && Object.keys(style).length ? style : {};
      var contentStyle = Object.assign({}, { top: top, left: left, opacity: opacity }, existingStyle);

      var cornerClass = this.getCornerClass();
      var disengagedClass = isDisengaged ? ' Tooltip-Content--Disengaged' : '';
      var className = ['Tooltip-Content', cornerClass, disengagedClass].join(' ');

      return _react2.default.createElement(
        'div',
        {
          style: contentStyle,
          className: className,
          onMouseEnter: this.engageTooltip,
          onMouseLeave: this.disengageTooltip },
        renderHtml ? _react2.default.createElement('div', { dangerouslySetInnerHTML: { __html: content } }) : content
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var _this3 = this;

      var isShown = this.state.isShown;

      var TooltipContent = this.renderTooltipContent;

      var _props2 = this.props,
          children = _props2.children,
          className = _props2.className;

      return _react2.default.createElement(
        'div',
        { className: 'Tooltip' + (className ? ' ' + className : ''), ref: function ref(el) {
            return _this3.el = el;
          } },
        !isShown ? null : _react2.default.createElement(
          _BodyLayer2.default,
          { className: 'Tooltip-Wrapper' },
          _react2.default.createElement(TooltipContent, null)
        ),
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

exports.default = Tooltip;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL1Rvb2x0aXAuanN4Il0sIm5hbWVzIjpbIlRvb2x0aXAiLCJwcm9wcyIsInN0YXRlIiwiaXNTaG93biIsImlzRm9jdXMiLCJpc0hvdmVyZWQiLCJpc0Rpc2VuZ2FnZWQiLCJjb21wb25lbnREaWRNb3VudCIsImJpbmQiLCJjb21wb25lbnRXaWxsVW5tb3VudCIsImdldEhpZGVEZWxheSIsImdldFNob3dEZWxheSIsImdldENvcm5lckNsYXNzIiwic2hvd1Rvb2x0aXAiLCJoaWRlVG9vbHRpcCIsImVuZ2FnZVRvb2x0aXAiLCJkaXNlbmdhZ2VUb29sdGlwIiwicmVuZGVyVG9vbHRpcENvbnRlbnQiLCJlbCIsImNvbnNvbGUiLCJlcnJvciIsImV2ZW50cyIsInVzZSIsImZvY3VzSW4iLCJrZXlwcmVzcyIsIm1vdXNlRW50ZXIiLCJmb2N1c091dCIsIm1vdXNlTGVhdmUiLCJjbGVhckFsbCIsInNob3dEZWxheSIsImhpZGVEZWxheSIsImNvcm5lciIsImxlbmd0aCIsInNwbGl0IiwiZmlsdGVyIiwicyIsImpvaW4iLCJzZXRTdGF0ZSIsImhpZGVUaW1lb3V0IiwiY2xlYXJUaW1lb3V0Iiwic2hvd1RpbWVvdXQiLCJzZXRUaW1lb3V0IiwiY29udGVudCIsInBvc2l0aW9uIiwic3R5bGUiLCJyZW5kZXJIdG1sIiwib3BhY2l0eSIsIk9iamVjdCIsImFzc2lnbiIsInRvcCIsImxlZnQiLCJleGlzdGluZ1N0eWxlIiwia2V5cyIsImNvbnRlbnRTdHlsZSIsImNvcm5lckNsYXNzIiwiZGlzZW5nYWdlZENsYXNzIiwiY2xhc3NOYW1lIiwiX19odG1sIiwiVG9vbHRpcENvbnRlbnQiLCJjaGlsZHJlbiIsIm5vZGUiLCJnZXRCb3VuZGluZ0NsaWVudFJlY3QiLCJDb21wb25lbnQiLCJwcm9wVHlwZXMiLCJudW1iZXIiLCJzdHJpbmciLCJmYWRlT3V0IiwiYm9vbCIsIm9iamVjdCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7QUFBQTs7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Q2xCakMsYUFBVyxvQkFBVWtDLE1BREg7QUFFbEJMLFlBQVUsb0JBQVVDLElBRkY7QUFHbEJKLGFBQVcsb0JBQVVTLE1BSEg7QUFJbEJ2QixXQUFTLG9CQUFVa0IsSUFKRDtBQUtsQjdCLFVBQVEsb0JBQVVrQyxNQUxBO0FBTWxCQyxXQUFTLG9CQUFVQyxJQU5EO0FBT2xCeEIsWUFBVSxvQkFBVXlCO0FBUEYsQ0FBcEI7O2tCQVVlcEUsTyIsImZpbGUiOiJUb29sdGlwLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcbmltcG9ydCBQcm9wVHlwZXMgZnJvbSAncHJvcC10eXBlcyc7XG5cbmltcG9ydCBCb2R5TGF5ZXIgZnJvbSAnLi9Cb2R5TGF5ZXInO1xuaW1wb3J0IHsgRXZlbnRzRmFjdG9yeSB9IGZyb20gJy4uL1V0aWxzL0V2ZW50cyc7XG5cbmNsYXNzIFRvb2x0aXAgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG5cbiAgICB0aGlzLnN0YXRlID0ge1xuICAgICAgaXNTaG93bjogZmFsc2UsXG4gICAgICBpc0ZvY3VzOiBmYWxzZSxcbiAgICAgIGlzSG92ZXJlZDogZmFsc2UsXG4gICAgICBpc0Rpc2VuZ2FnZWQ6IHRydWVcbiAgICB9O1xuXG4gICAgdGhpcy5jb21wb25lbnREaWRNb3VudCA9IHRoaXMuY29tcG9uZW50RGlkTW91bnQuYmluZCh0aGlzKTtcbiAgICB0aGlzLmNvbXBvbmVudFdpbGxVbm1vdW50ID0gdGhpcy5jb21wb25lbnRXaWxsVW5tb3VudC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuZ2V0SGlkZURlbGF5ID0gdGhpcy5nZXRIaWRlRGVsYXkuYmluZCh0aGlzKTtcbiAgICB0aGlzLmdldFNob3dEZWxheSA9IHRoaXMuZ2V0U2hvd0RlbGF5LmJpbmQodGhpcyk7XG4gICAgdGhpcy5nZXRDb3JuZXJDbGFzcyA9IHRoaXMuZ2V0Q29ybmVyQ2xhc3MuYmluZCh0aGlzKTtcbiAgICB0aGlzLnNob3dUb29sdGlwID0gdGhpcy5zaG93VG9vbHRpcC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuaGlkZVRvb2x0aXAgPSB0aGlzLmhpZGVUb29sdGlwLmJpbmQodGhpcyk7XG4gICAgdGhpcy5lbmdhZ2VUb29sdGlwID0gdGhpcy5lbmdhZ2VUb29sdGlwLmJpbmQodGhpcyk7XG4gICAgdGhpcy5kaXNlbmdhZ2VUb29sdGlwID0gdGhpcy5kaXNlbmdhZ2VUb29sdGlwLmJpbmQodGhpcyk7XG4gICAgdGhpcy5yZW5kZXJUb29sdGlwQ29udGVudCA9IHRoaXMucmVuZGVyVG9vbHRpcENvbnRlbnQuYmluZCh0aGlzKTtcbiAgfVxuXG4gIC8qIC09LT0tPS09LT0tPS09LT0tPS09LT0tPSBMaWZlY3ljbGUgLT0tPS09LT0tPS09LT0tPS09LT0tPS09ICovXG5cbiAgY29tcG9uZW50RGlkTW91bnQgKCkge1xuICAgIGlmICghdGhpcy5lbCkge1xuICAgICAgY29uc29sZS5lcnJvcihgXG4gICAgICAgIFRvb2x0aXAgRXJyb3I6IENhbid0IHNldHVwIGZvY3VzSW4vZm9jdXNPdXQgZXZlbnRzLlxuICAgICAgICBFbGVtZW50IHJlZiBjb3VsZCBub3QgYmUgZm91bmQ7IHdhcyByZW5kZXIgaW50ZXJydXB0ZWQ/XG4gICAgICBgKTtcbiAgICB9IGVsc2Uge1xuICAgICAgdGhpcy5ldmVudHMgPSBuZXcgRXZlbnRzRmFjdG9yeSh0aGlzLmVsKTtcbiAgICAgIHRoaXMuZXZlbnRzLnVzZSh7XG4gICAgICAgIGZvY3VzSW46IHRoaXMuZW5nYWdlVG9vbHRpcCxcbiAgICAgICAga2V5cHJlc3M6IHRoaXMuZW5nYWdlVG9vbHRpcCxcbiAgICAgICAgbW91c2VFbnRlcjogdGhpcy5lbmdhZ2VUb29sdGlwLFxuXG4gICAgICAgIGZvY3VzT3V0OiB0aGlzLmRpc2VuZ2FnZVRvb2x0aXAsXG4gICAgICAgIG1vdXNlTGVhdmU6IHRoaXMuZGlzZW5nYWdlVG9vbHRpcCxcbiAgICAgIH0pO1xuICAgIH1cbiAgfVxuXG4gIGNvbXBvbmVudFdpbGxVbm1vdW50ICgpIHtcbiAgICBpZiAodGhpcy5ldmVudHMpIHRoaXMuZXZlbnRzLmNsZWFyQWxsKCk7XG4gIH1cblxuICAvKiAtPS09LT0tPS09LT0tPS09LT0tPS09LT0gVXRpbGl0aWVzIC09LT0tPS09LT0tPS09LT0tPS09LT0tPSAqL1xuXG4gIHN0YXRpYyBnZXRPZmZzZXQgKG5vZGUpIHtcbiAgICByZXR1cm4gbm9kZS5nZXRCb3VuZGluZ0NsaWVudFJlY3QoKTtcbiAgfVxuXG4gIGdldFNob3dEZWxheSAoKSB7XG4gICAgY29uc3QgeyBzaG93RGVsYXkgfSA9IHRoaXMucHJvcHM7XG4gICAgcmV0dXJuIHR5cGVvZiBzaG93RGVsYXkgPT09ICdudW1iZXInXG4gICAgICA/IHNob3dEZWxheVxuICAgICAgOiAyNTA7XG4gIH1cbiAgZ2V0SGlkZURlbGF5ICgpIHtcbiAgICBsZXQgeyBoaWRlRGVsYXkgfSA9IHRoaXMucHJvcHM7XG4gICAgcmV0dXJuIHR5cGVvZiBoaWRlRGVsYXkgPT09ICdudW1iZXInXG4gICAgICA/IGhpZGVEZWxheVxuICAgICAgOiA1MDA7XG4gIH1cblxuICBnZXRDb3JuZXJDbGFzcyAoKSB7XG4gICAgY29uc3QgeyBjb3JuZXIgfSA9IHRoaXMucHJvcHM7XG4gICAgaWYgKHR5cGVvZiBjb3JuZXIgIT09ICdzdHJpbmcnIHx8ICFjb3JuZXIubGVuZ3RoKSByZXR1cm4gJ25vLWNvcm5lcic7XG4gICAgcmV0dXJuIGNvcm5lci5zcGxpdCgnICcpLmZpbHRlcihzID0+IHMpLmpvaW4oJy0nKTtcbiAgfVxuXG4gIC8qIC09LT0tPS09LT0tPS09LT0tPS09LT0tPSBTaG93L0hpZGUgLT0tPS09LT0tPS09LT0tPS09LT0tPS09ICovXG5cbiAgc2hvd1Rvb2x0aXAgKCkge1xuICAgIHRoaXMuc2V0U3RhdGUoeyBpc1Nob3duOiB0cnVlIH0pO1xuICAgIGlmICh0aGlzLmhpZGVUaW1lb3V0KSBjbGVhclRpbWVvdXQodGhpcy5oaWRlVGltZW91dCk7XG4gIH1cblxuICBoaWRlVG9vbHRpcCAoKSB7XG4gICAgaWYgKCF0aGlzLnN0YXRlLmlzRGlzZW5nYWdlZCkgcmV0dXJuO1xuICAgIHRoaXMuc2V0U3RhdGUoeyBpc1Nob3duOiBmYWxzZSB9KTtcbiAgfVxuXG4gIC8qIC09LT0tPS09LT0tPS09LT0tPS09LT0tPSBFbmdhZ2UvRGlzZW5nYWdlIC09LT0tPS09LT0tPS09LT0tPS09LT0tPSAqL1xuXG4gIGVuZ2FnZVRvb2x0aXAgKCkge1xuICAgIHRoaXMuc2V0U3RhdGUoeyBpc0Rpc2VuZ2FnZWQ6IGZhbHNlIH0pO1xuICAgIHRoaXMuc2hvd1RpbWVvdXQgPSBzZXRUaW1lb3V0KCgpID0+IHtcbiAgICAgIHRoaXMuc2hvd1Rvb2x0aXAoKTtcbiAgICAgIGlmICh0aGlzLmhpZGVUaW1lb3V0KSBjbGVhclRpbWVvdXQodGhpcy5oaWRlVGltZW91dCk7XG4gICAgfSwgdGhpcy5nZXRTaG93RGVsYXkoKSk7XG4gIH1cblxuICBkaXNlbmdhZ2VUb29sdGlwICgpIHtcbiAgICB0aGlzLnNldFN0YXRlKHsgaXNEaXNlbmdhZ2VkOiB0cnVlIH0pO1xuICAgIGlmICh0aGlzLnNob3dUaW1lb3V0KSBjbGVhclRpbWVvdXQodGhpcy5zaG93VGltZW91dCk7XG4gICAgdGhpcy5oaWRlVGltZW91dCA9IHNldFRpbWVvdXQodGhpcy5oaWRlVG9vbHRpcCwgdGhpcy5nZXRIaWRlRGVsYXkoKSk7XG4gIH1cblxuICAvKiAtPS09LT0tPS09LT0tPS09LT0tPS09LT0gUmVuZGVyZXJzIC09LT0tPS09LT0tPS09LT0tPS09LT0tPSAqL1xuXG4gIHJlbmRlclRvb2x0aXBDb250ZW50ICgpIHtcbiAgICBjb25zdCB7IGlzRGlzZW5nYWdlZCB9ID0gdGhpcy5zdGF0ZTtcbiAgICBjb25zdCB7IGNvbnRlbnQsIHBvc2l0aW9uLCBzdHlsZSwgcmVuZGVySHRtbCB9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IG9wYWNpdHkgPSBpc0Rpc2VuZ2FnZWQgPyAwLjAxIDogMTtcbiAgICBjb25zdCB7IHRvcCwgbGVmdCB9ID0gT2JqZWN0LmFzc2lnbih7IHRvcDogMCwgbGVmdDogMCB9LCBwb3NpdGlvbik7XG4gICAgY29uc3QgZXhpc3RpbmdTdHlsZSA9IHN0eWxlICYmIE9iamVjdC5rZXlzKHN0eWxlKS5sZW5ndGggPyBzdHlsZSA6IHt9O1xuICAgIGNvbnN0IGNvbnRlbnRTdHlsZSA9IE9iamVjdC5hc3NpZ24oe30sIHsgdG9wLCBsZWZ0LCBvcGFjaXR5IH0sIGV4aXN0aW5nU3R5bGUpO1xuXG4gICAgY29uc3QgY29ybmVyQ2xhc3MgPSB0aGlzLmdldENvcm5lckNsYXNzKCk7XG4gICAgY29uc3QgZGlzZW5nYWdlZENsYXNzID0gaXNEaXNlbmdhZ2VkID8gJyBUb29sdGlwLUNvbnRlbnQtLURpc2VuZ2FnZWQnIDogJyc7XG4gICAgY29uc3QgY2xhc3NOYW1lID0gWydUb29sdGlwLUNvbnRlbnQnLCBjb3JuZXJDbGFzcywgZGlzZW5nYWdlZENsYXNzXS5qb2luKCcgJyk7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdlxuICAgICAgICBzdHlsZT17Y29udGVudFN0eWxlfVxuICAgICAgICBjbGFzc05hbWU9e2NsYXNzTmFtZX1cbiAgICAgICAgb25Nb3VzZUVudGVyPXt0aGlzLmVuZ2FnZVRvb2x0aXB9XG4gICAgICAgIG9uTW91c2VMZWF2ZT17dGhpcy5kaXNlbmdhZ2VUb29sdGlwfT5cbiAgICAgICAge3JlbmRlckh0bWxcbiAgICAgICAgICA/IDxkaXYgZGFuZ2Vyb3VzbHlTZXRJbm5lckhUTUw9e3sgX19odG1sOiBjb250ZW50IH19IC8+XG4gICAgICAgICAgOiBjb250ZW50XG4gICAgICAgIH1cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgaXNTaG93biB9ID0gdGhpcy5zdGF0ZTtcbiAgICBjb25zdCBUb29sdGlwQ29udGVudCA9IHRoaXMucmVuZGVyVG9vbHRpcENvbnRlbnQ7XG5cbiAgICBjb25zdCB7IGNoaWxkcmVuLCBjbGFzc05hbWUgfSA9IHRoaXMucHJvcHM7XG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPXsnVG9vbHRpcCcgKyAoY2xhc3NOYW1lID8gJyAnICsgY2xhc3NOYW1lIDogJycpfSByZWY9eyhlbCkgPT4gdGhpcy5lbCA9IGVsfT5cbiAgICAgICAgeyFpc1Nob3duID8gbnVsbCA6IChcbiAgICAgICAgICA8Qm9keUxheWVyIGNsYXNzTmFtZT1cIlRvb2x0aXAtV3JhcHBlclwiPlxuICAgICAgICAgICAgPFRvb2x0aXBDb250ZW50IC8+XG4gICAgICAgICAgPC9Cb2R5TGF5ZXI+XG4gICAgICAgICl9XG4gICAgICAgIHtjaGlsZHJlbn1cbiAgICAgIDwvZGl2PlxuICAgIClcbiAgfVxufTtcblxuVG9vbHRpcC5wcm9wVHlwZXMgPSB7XG4gIGhpZGVEZWxheTogUHJvcFR5cGVzLm51bWJlcixcbiAgY2hpbGRyZW46IFByb3BUeXBlcy5ub2RlLFxuICBjbGFzc05hbWU6IFByb3BUeXBlcy5zdHJpbmcsXG4gIGNvbnRlbnQ6IFByb3BUeXBlcy5ub2RlLFxuICBjb3JuZXI6IFByb3BUeXBlcy5zdHJpbmcsXG4gIGZhZGVPdXQ6IFByb3BUeXBlcy5ib29sLFxuICBwb3NpdGlvbjogUHJvcFR5cGVzLm9iamVjdFxufTtcblxuZXhwb3J0IGRlZmF1bHQgVG9vbHRpcDtcbiJdfQ==