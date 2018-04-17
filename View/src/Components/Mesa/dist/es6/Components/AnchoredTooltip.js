'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Tooltip = require('./Tooltip');

var _Tooltip2 = _interopRequireDefault(_Tooltip);

var _Events = require('../Utils/Events');

var _Events2 = _interopRequireDefault(_Events);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var AnchoredTooltip = function (_React$Component) {
  _inherits(AnchoredTooltip, _React$Component);

  function AnchoredTooltip(props) {
    _classCallCheck(this, AnchoredTooltip);

    var _this = _possibleConstructorReturn(this, (AnchoredTooltip.__proto__ || Object.getPrototypeOf(AnchoredTooltip)).call(this, props));

    _this.state = { position: {} };
    _this.updateOffset = _this.updateOffset.bind(_this);
    _this.componentDidMount = _this.componentDidMount.bind(_this);
    _this.componentWillUnmount = _this.componentWillUnmount.bind(_this);
    return _this;
  }

  _createClass(AnchoredTooltip, [{
    key: 'componentDidMount',
    value: function componentDidMount() {
      var _this2 = this;

      this.updateOffset();
      this.listeners = {
        scroll: _Events2.default.add('scroll', this.updateOffset),
        resize: _Events2.default.add('resize', this.updateOffset),
        MesaScroll: _Events2.default.add('MesaScroll', this.updateOffset),
        MesaReflow: _Events2.default.add('MesaReflow', this.updateOffset)
      };
      setTimeout(function () {
        _this2.updateOffset();
        console.log('Updated offset after delay');
      }, 100);
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
      if (this.props.debug) console.log('Updating offset...');
      var element = this.element;

      if (!element) {
        if (this.props.debug) console.log('...Returning out, no element');
        return;
      };
      var offset = element.getBoundingClientRect();
      var top = offset.top,
          left = offset.left,
          height = offset.height,
          bottom = offset.bottom;

      var position = { left: left, top: Math.ceil(top) + Math.ceil(height) };
      if (this.props.debug) console.log('Offset is now...', position);
      this.setState({ position: position });
    }
  }, {
    key: 'render',
    value: function render() {
      var _this3 = this;

      var props = this.props;
      var position = this.state.position;

      var ref = function ref(el) {
        return _this3.element = el;
      };
      var children = _react2.default.createElement('div', { ref: ref, style: { display: 'inline-block' }, children: props.children });
      var extractedProps = _extends({}, props, { position: position, children: children });

      return _react2.default.createElement(_Tooltip2.default, _extends({
        corner: 'top-left',
        className: 'AnchoredTooltip'
      }, extractedProps));
    }
  }]);

  return AnchoredTooltip;
}(_react2.default.Component);

;

exports.default = AnchoredTooltip;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL0FuY2hvcmVkVG9vbHRpcC5qc3giXSwibmFtZXMiOlsiQW5jaG9yZWRUb29sdGlwIiwicHJvcHMiLCJzdGF0ZSIsInBvc2l0aW9uIiwidXBkYXRlT2Zmc2V0IiwiYmluZCIsImNvbXBvbmVudERpZE1vdW50IiwiY29tcG9uZW50V2lsbFVubW91bnQiLCJsaXN0ZW5lcnMiLCJzY3JvbGwiLCJhZGQiLCJyZXNpemUiLCJNZXNhU2Nyb2xsIiwiTWVzYVJlZmxvdyIsInNldFRpbWVvdXQiLCJjb25zb2xlIiwibG9nIiwiT2JqZWN0IiwidmFsdWVzIiwiZm9yRWFjaCIsInJlbW92ZSIsImxpc3RlbmVySWQiLCJkZWJ1ZyIsImVsZW1lbnQiLCJvZmZzZXQiLCJnZXRCb3VuZGluZ0NsaWVudFJlY3QiLCJ0b3AiLCJsZWZ0IiwiaGVpZ2h0IiwiYm90dG9tIiwiTWF0aCIsImNlaWwiLCJzZXRTdGF0ZSIsInJlZiIsImVsIiwiY2hpbGRyZW4iLCJkaXNwbGF5IiwiZXh0cmFjdGVkUHJvcHMiLCJDb21wb25lbnQiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7QUFBQTs7OztBQUVBOzs7O0FBQ0E7Ozs7Ozs7Ozs7OztJQUVNQSxlOzs7QUFDSiwyQkFBYUMsS0FBYixFQUFvQjtBQUFBOztBQUFBLGtJQUNaQSxLQURZOztBQUVsQixVQUFLQyxLQUFMLEdBQWEsRUFBRUMsVUFBVSxFQUFaLEVBQWI7QUFDQSxVQUFLQyxZQUFMLEdBQW9CLE1BQUtBLFlBQUwsQ0FBa0JDLElBQWxCLE9BQXBCO0FBQ0EsVUFBS0MsaUJBQUwsR0FBeUIsTUFBS0EsaUJBQUwsQ0FBdUJELElBQXZCLE9BQXpCO0FBQ0EsVUFBS0Usb0JBQUwsR0FBNEIsTUFBS0Esb0JBQUwsQ0FBMEJGLElBQTFCLE9BQTVCO0FBTGtCO0FBTW5COzs7O3dDQUVvQjtBQUFBOztBQUNuQixXQUFLRCxZQUFMO0FBQ0EsV0FBS0ksU0FBTCxHQUFpQjtBQUNmQyxnQkFBUSxpQkFBT0MsR0FBUCxDQUFXLFFBQVgsRUFBcUIsS0FBS04sWUFBMUIsQ0FETztBQUVmTyxnQkFBUSxpQkFBT0QsR0FBUCxDQUFXLFFBQVgsRUFBcUIsS0FBS04sWUFBMUIsQ0FGTztBQUdmUSxvQkFBWSxpQkFBT0YsR0FBUCxDQUFXLFlBQVgsRUFBeUIsS0FBS04sWUFBOUIsQ0FIRztBQUlmUyxvQkFBWSxpQkFBT0gsR0FBUCxDQUFXLFlBQVgsRUFBeUIsS0FBS04sWUFBOUI7QUFKRyxPQUFqQjtBQU1BVSxpQkFBVyxZQUFNO0FBQ2YsZUFBS1YsWUFBTDtBQUNBVyxnQkFBUUMsR0FBUixDQUFZLDRCQUFaO0FBQ0QsT0FIRCxFQUdHLEdBSEg7QUFJRDs7OzJDQUV1QjtBQUN0QkMsYUFBT0MsTUFBUCxDQUFjLEtBQUtWLFNBQW5CLEVBQThCVyxPQUE5QixDQUFzQztBQUFBLGVBQWMsaUJBQU9DLE1BQVAsQ0FBY0MsVUFBZCxDQUFkO0FBQUEsT0FBdEM7QUFDRDs7O21DQUVlO0FBQ2QsVUFBSSxLQUFLcEIsS0FBTCxDQUFXcUIsS0FBZixFQUFzQlAsUUFBUUMsR0FBUixDQUFZLG9CQUFaO0FBRFIsVUFFTk8sT0FGTSxHQUVNLElBRk4sQ0FFTkEsT0FGTTs7QUFHZCxVQUFJLENBQUNBLE9BQUwsRUFBYztBQUNaLFlBQUksS0FBS3RCLEtBQUwsQ0FBV3FCLEtBQWYsRUFBc0JQLFFBQVFDLEdBQVIsQ0FBWSw4QkFBWjtBQUN0QjtBQUNEO0FBQ0QsVUFBTVEsU0FBU0QsUUFBUUUscUJBQVIsRUFBZjtBQVBjLFVBUU5DLEdBUk0sR0FRd0JGLE1BUnhCLENBUU5FLEdBUk07QUFBQSxVQVFEQyxJQVJDLEdBUXdCSCxNQVJ4QixDQVFERyxJQVJDO0FBQUEsVUFRS0MsTUFSTCxHQVF3QkosTUFSeEIsQ0FRS0ksTUFSTDtBQUFBLFVBUWFDLE1BUmIsR0FRd0JMLE1BUnhCLENBUWFLLE1BUmI7O0FBU2QsVUFBTTFCLFdBQVcsRUFBRXdCLFVBQUYsRUFBUUQsS0FBS0ksS0FBS0MsSUFBTCxDQUFVTCxHQUFWLElBQWlCSSxLQUFLQyxJQUFMLENBQVVILE1BQVYsQ0FBOUIsRUFBakI7QUFDQSxVQUFJLEtBQUszQixLQUFMLENBQVdxQixLQUFmLEVBQXNCUCxRQUFRQyxHQUFSLENBQVksa0JBQVosRUFBZ0NiLFFBQWhDO0FBQ3RCLFdBQUs2QixRQUFMLENBQWMsRUFBRTdCLGtCQUFGLEVBQWQ7QUFDRDs7OzZCQUVTO0FBQUE7O0FBQUEsVUFDQUYsS0FEQSxHQUNVLElBRFYsQ0FDQUEsS0FEQTtBQUFBLFVBRUFFLFFBRkEsR0FFYSxLQUFLRCxLQUZsQixDQUVBQyxRQUZBOztBQUdSLFVBQU04QixNQUFNLFNBQU5BLEdBQU0sQ0FBQ0MsRUFBRDtBQUFBLGVBQVEsT0FBS1gsT0FBTCxHQUFlVyxFQUF2QjtBQUFBLE9BQVo7QUFDQSxVQUFNQyxXQUFZLHVDQUFLLEtBQUtGLEdBQVYsRUFBZSxPQUFPLEVBQUVHLFNBQVMsY0FBWCxFQUF0QixFQUFtRCxVQUFVbkMsTUFBTWtDLFFBQW5FLEdBQWxCO0FBQ0EsVUFBTUUsOEJBQXNCcEMsS0FBdEIsSUFBNkJFLGtCQUE3QixFQUF1Q2dDLGtCQUF2QyxHQUFOOztBQUVBLGFBQ0U7QUFDRSxnQkFBTyxVQURUO0FBRUUsbUJBQVU7QUFGWixTQUdNRSxjQUhOLEVBREY7QUFPRDs7OztFQXZEMkIsZ0JBQU1DLFM7O0FBd0RuQzs7a0JBRWN0QyxlIiwiZmlsZSI6IkFuY2hvcmVkVG9vbHRpcC5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5cbmltcG9ydCBUb29sdGlwIGZyb20gJy4vVG9vbHRpcCc7XG5pbXBvcnQgRXZlbnRzIGZyb20gJy4uL1V0aWxzL0V2ZW50cyc7XG5cbmNsYXNzIEFuY2hvcmVkVG9vbHRpcCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgICB0aGlzLnN0YXRlID0geyBwb3NpdGlvbjoge30gfTtcbiAgICB0aGlzLnVwZGF0ZU9mZnNldCA9IHRoaXMudXBkYXRlT2Zmc2V0LmJpbmQodGhpcyk7XG4gICAgdGhpcy5jb21wb25lbnREaWRNb3VudCA9IHRoaXMuY29tcG9uZW50RGlkTW91bnQuYmluZCh0aGlzKTtcbiAgICB0aGlzLmNvbXBvbmVudFdpbGxVbm1vdW50ID0gdGhpcy5jb21wb25lbnRXaWxsVW5tb3VudC5iaW5kKHRoaXMpO1xuICB9XG5cbiAgY29tcG9uZW50RGlkTW91bnQgKCkge1xuICAgIHRoaXMudXBkYXRlT2Zmc2V0KCk7XG4gICAgdGhpcy5saXN0ZW5lcnMgPSB7XG4gICAgICBzY3JvbGw6IEV2ZW50cy5hZGQoJ3Njcm9sbCcsIHRoaXMudXBkYXRlT2Zmc2V0KSxcbiAgICAgIHJlc2l6ZTogRXZlbnRzLmFkZCgncmVzaXplJywgdGhpcy51cGRhdGVPZmZzZXQpLFxuICAgICAgTWVzYVNjcm9sbDogRXZlbnRzLmFkZCgnTWVzYVNjcm9sbCcsIHRoaXMudXBkYXRlT2Zmc2V0KSxcbiAgICAgIE1lc2FSZWZsb3c6IEV2ZW50cy5hZGQoJ01lc2FSZWZsb3cnLCB0aGlzLnVwZGF0ZU9mZnNldClcbiAgICB9O1xuICAgIHNldFRpbWVvdXQoKCkgPT4ge1xuICAgICAgdGhpcy51cGRhdGVPZmZzZXQoKVxuICAgICAgY29uc29sZS5sb2coJ1VwZGF0ZWQgb2Zmc2V0IGFmdGVyIGRlbGF5JylcbiAgICB9LCAxMDApO1xuICB9XG5cbiAgY29tcG9uZW50V2lsbFVubW91bnQgKCkge1xuICAgIE9iamVjdC52YWx1ZXModGhpcy5saXN0ZW5lcnMpLmZvckVhY2gobGlzdGVuZXJJZCA9PiBFdmVudHMucmVtb3ZlKGxpc3RlbmVySWQpKTtcbiAgfVxuXG4gIHVwZGF0ZU9mZnNldCAoKSB7XG4gICAgaWYgKHRoaXMucHJvcHMuZGVidWcpIGNvbnNvbGUubG9nKCdVcGRhdGluZyBvZmZzZXQuLi4nKTtcbiAgICBjb25zdCB7IGVsZW1lbnQgfSA9IHRoaXM7XG4gICAgaWYgKCFlbGVtZW50KSB7XG4gICAgICBpZiAodGhpcy5wcm9wcy5kZWJ1ZykgY29uc29sZS5sb2coJy4uLlJldHVybmluZyBvdXQsIG5vIGVsZW1lbnQnKTtcbiAgICAgIHJldHVybjtcbiAgICB9O1xuICAgIGNvbnN0IG9mZnNldCA9IGVsZW1lbnQuZ2V0Qm91bmRpbmdDbGllbnRSZWN0KCk7XG4gICAgY29uc3QgeyB0b3AsIGxlZnQsIGhlaWdodCwgYm90dG9tIH0gPSBvZmZzZXQ7XG4gICAgY29uc3QgcG9zaXRpb24gPSB7IGxlZnQsIHRvcDogTWF0aC5jZWlsKHRvcCkgKyBNYXRoLmNlaWwoaGVpZ2h0KSB9O1xuICAgIGlmICh0aGlzLnByb3BzLmRlYnVnKSBjb25zb2xlLmxvZygnT2Zmc2V0IGlzIG5vdy4uLicsIHBvc2l0aW9uKTtcbiAgICB0aGlzLnNldFN0YXRlKHsgcG9zaXRpb24gfSk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgcHJvcHMgfSA9IHRoaXM7XG4gICAgY29uc3QgeyBwb3NpdGlvbiB9ID0gdGhpcy5zdGF0ZTtcbiAgICBjb25zdCByZWYgPSAoZWwpID0+IHRoaXMuZWxlbWVudCA9IGVsO1xuICAgIGNvbnN0IGNoaWxkcmVuID0gKDxkaXYgcmVmPXtyZWZ9IHN0eWxlPXt7IGRpc3BsYXk6ICdpbmxpbmUtYmxvY2snIH19IGNoaWxkcmVuPXtwcm9wcy5jaGlsZHJlbn0gLz4pO1xuICAgIGNvbnN0IGV4dHJhY3RlZFByb3BzID0geyAuLi5wcm9wcywgcG9zaXRpb24sIGNoaWxkcmVuIH07XG5cbiAgICByZXR1cm4gKFxuICAgICAgPFRvb2x0aXBcbiAgICAgICAgY29ybmVyPVwidG9wLWxlZnRcIlxuICAgICAgICBjbGFzc05hbWU9XCJBbmNob3JlZFRvb2x0aXBcIlxuICAgICAgICB7Li4uZXh0cmFjdGVkUHJvcHN9XG4gICAgICAvPlxuICAgICk7XG4gIH1cbn07XG5cbmV4cG9ydCBkZWZhdWx0IEFuY2hvcmVkVG9vbHRpcDtcbiJdfQ==