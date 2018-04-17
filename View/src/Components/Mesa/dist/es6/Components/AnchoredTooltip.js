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
      this.updateOffset();
      this.listeners = {
        scroll: _Events2.default.add('scroll', this.updateOffset),
        resize: _Events2.default.add('resize', this.updateOffset),
        MesaScroll: _Events2.default.add('MesaScroll', this.updateOffset),
        MesaReflow: _Events2.default.add('MesaReflow', this.updateOffset)
      };
      setTimeout(this.updateOffset, 100);
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
          height = offset.height;

      var position = { left: left, top: top + height };
      if (this.props.debug) console.log('Offset is now...', position);
      this.setState({ position: position });
    }
  }, {
    key: 'render',
    value: function render() {
      var _this2 = this;

      var props = this.props;
      var position = this.state.position;

      var ref = function ref(el) {
        return _this2.element = el;
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL0FuY2hvcmVkVG9vbHRpcC5qc3giXSwibmFtZXMiOlsiQW5jaG9yZWRUb29sdGlwIiwicHJvcHMiLCJzdGF0ZSIsInBvc2l0aW9uIiwidXBkYXRlT2Zmc2V0IiwiYmluZCIsImNvbXBvbmVudERpZE1vdW50IiwiY29tcG9uZW50V2lsbFVubW91bnQiLCJsaXN0ZW5lcnMiLCJzY3JvbGwiLCJhZGQiLCJyZXNpemUiLCJNZXNhU2Nyb2xsIiwiTWVzYVJlZmxvdyIsInNldFRpbWVvdXQiLCJPYmplY3QiLCJ2YWx1ZXMiLCJmb3JFYWNoIiwicmVtb3ZlIiwibGlzdGVuZXJJZCIsImRlYnVnIiwiY29uc29sZSIsImxvZyIsImVsZW1lbnQiLCJvZmZzZXQiLCJnZXRCb3VuZGluZ0NsaWVudFJlY3QiLCJ0b3AiLCJsZWZ0IiwiaGVpZ2h0Iiwic2V0U3RhdGUiLCJyZWYiLCJlbCIsImNoaWxkcmVuIiwiZGlzcGxheSIsImV4dHJhY3RlZFByb3BzIiwiQ29tcG9uZW50Il0sIm1hcHBpbmdzIjoiOzs7Ozs7Ozs7O0FBQUE7Ozs7QUFFQTs7OztBQUNBOzs7Ozs7Ozs7Ozs7SUFFTUEsZTs7O0FBQ0osMkJBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSxrSUFDWkEsS0FEWTs7QUFFbEIsVUFBS0MsS0FBTCxHQUFhLEVBQUVDLFVBQVUsRUFBWixFQUFiO0FBQ0EsVUFBS0MsWUFBTCxHQUFvQixNQUFLQSxZQUFMLENBQWtCQyxJQUFsQixPQUFwQjtBQUNBLFVBQUtDLGlCQUFMLEdBQXlCLE1BQUtBLGlCQUFMLENBQXVCRCxJQUF2QixPQUF6QjtBQUNBLFVBQUtFLG9CQUFMLEdBQTRCLE1BQUtBLG9CQUFMLENBQTBCRixJQUExQixPQUE1QjtBQUxrQjtBQU1uQjs7Ozt3Q0FFb0I7QUFDbkIsV0FBS0QsWUFBTDtBQUNBLFdBQUtJLFNBQUwsR0FBaUI7QUFDZkMsZ0JBQVEsaUJBQU9DLEdBQVAsQ0FBVyxRQUFYLEVBQXFCLEtBQUtOLFlBQTFCLENBRE87QUFFZk8sZ0JBQVEsaUJBQU9ELEdBQVAsQ0FBVyxRQUFYLEVBQXFCLEtBQUtOLFlBQTFCLENBRk87QUFHZlEsb0JBQVksaUJBQU9GLEdBQVAsQ0FBVyxZQUFYLEVBQXlCLEtBQUtOLFlBQTlCLENBSEc7QUFJZlMsb0JBQVksaUJBQU9ILEdBQVAsQ0FBVyxZQUFYLEVBQXlCLEtBQUtOLFlBQTlCO0FBSkcsT0FBakI7QUFNQVUsaUJBQVcsS0FBS1YsWUFBaEIsRUFBOEIsR0FBOUI7QUFDRDs7OzJDQUV1QjtBQUN0QlcsYUFBT0MsTUFBUCxDQUFjLEtBQUtSLFNBQW5CLEVBQThCUyxPQUE5QixDQUFzQztBQUFBLGVBQWMsaUJBQU9DLE1BQVAsQ0FBY0MsVUFBZCxDQUFkO0FBQUEsT0FBdEM7QUFDRDs7O21DQUVlO0FBQ2QsVUFBSSxLQUFLbEIsS0FBTCxDQUFXbUIsS0FBZixFQUFzQkMsUUFBUUMsR0FBUixDQUFZLG9CQUFaO0FBRFIsVUFFTkMsT0FGTSxHQUVNLElBRk4sQ0FFTkEsT0FGTTs7QUFHZCxVQUFJLENBQUNBLE9BQUwsRUFBYztBQUNaLFlBQUksS0FBS3RCLEtBQUwsQ0FBV21CLEtBQWYsRUFBc0JDLFFBQVFDLEdBQVIsQ0FBWSw4QkFBWjtBQUN0QjtBQUNEO0FBQ0QsVUFBTUUsU0FBU0QsUUFBUUUscUJBQVIsRUFBZjtBQVBjLFVBUU5DLEdBUk0sR0FRZ0JGLE1BUmhCLENBUU5FLEdBUk07QUFBQSxVQVFEQyxJQVJDLEdBUWdCSCxNQVJoQixDQVFERyxJQVJDO0FBQUEsVUFRS0MsTUFSTCxHQVFnQkosTUFSaEIsQ0FRS0ksTUFSTDs7QUFTZCxVQUFNekIsV0FBVyxFQUFFd0IsVUFBRixFQUFRRCxLQUFLQSxNQUFNRSxNQUFuQixFQUFqQjtBQUNBLFVBQUksS0FBSzNCLEtBQUwsQ0FBV21CLEtBQWYsRUFBc0JDLFFBQVFDLEdBQVIsQ0FBWSxrQkFBWixFQUFnQ25CLFFBQWhDO0FBQ3RCLFdBQUswQixRQUFMLENBQWMsRUFBRTFCLGtCQUFGLEVBQWQ7QUFDRDs7OzZCQUVTO0FBQUE7O0FBQUEsVUFDQUYsS0FEQSxHQUNVLElBRFYsQ0FDQUEsS0FEQTtBQUFBLFVBRUFFLFFBRkEsR0FFYSxLQUFLRCxLQUZsQixDQUVBQyxRQUZBOztBQUdSLFVBQU0yQixNQUFNLFNBQU5BLEdBQU0sQ0FBQ0MsRUFBRDtBQUFBLGVBQVEsT0FBS1IsT0FBTCxHQUFlUSxFQUF2QjtBQUFBLE9BQVo7QUFDQSxVQUFNQyxXQUFZLHVDQUFLLEtBQUtGLEdBQVYsRUFBZSxPQUFPLEVBQUVHLFNBQVMsY0FBWCxFQUF0QixFQUFtRCxVQUFVaEMsTUFBTStCLFFBQW5FLEdBQWxCO0FBQ0EsVUFBTUUsOEJBQXNCakMsS0FBdEIsSUFBNkJFLGtCQUE3QixFQUF1QzZCLGtCQUF2QyxHQUFOOztBQUVBLGFBQ0U7QUFDRSxnQkFBTyxVQURUO0FBRUUsbUJBQVU7QUFGWixTQUdNRSxjQUhOLEVBREY7QUFPRDs7OztFQXBEMkIsZ0JBQU1DLFM7O0FBcURuQzs7a0JBRWNuQyxlIiwiZmlsZSI6IkFuY2hvcmVkVG9vbHRpcC5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5cbmltcG9ydCBUb29sdGlwIGZyb20gJy4vVG9vbHRpcCc7XG5pbXBvcnQgRXZlbnRzIGZyb20gJy4uL1V0aWxzL0V2ZW50cyc7XG5cbmNsYXNzIEFuY2hvcmVkVG9vbHRpcCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgICB0aGlzLnN0YXRlID0geyBwb3NpdGlvbjoge30gfTtcbiAgICB0aGlzLnVwZGF0ZU9mZnNldCA9IHRoaXMudXBkYXRlT2Zmc2V0LmJpbmQodGhpcyk7XG4gICAgdGhpcy5jb21wb25lbnREaWRNb3VudCA9IHRoaXMuY29tcG9uZW50RGlkTW91bnQuYmluZCh0aGlzKTtcbiAgICB0aGlzLmNvbXBvbmVudFdpbGxVbm1vdW50ID0gdGhpcy5jb21wb25lbnRXaWxsVW5tb3VudC5iaW5kKHRoaXMpO1xuICB9XG5cbiAgY29tcG9uZW50RGlkTW91bnQgKCkge1xuICAgIHRoaXMudXBkYXRlT2Zmc2V0KCk7XG4gICAgdGhpcy5saXN0ZW5lcnMgPSB7XG4gICAgICBzY3JvbGw6IEV2ZW50cy5hZGQoJ3Njcm9sbCcsIHRoaXMudXBkYXRlT2Zmc2V0KSxcbiAgICAgIHJlc2l6ZTogRXZlbnRzLmFkZCgncmVzaXplJywgdGhpcy51cGRhdGVPZmZzZXQpLFxuICAgICAgTWVzYVNjcm9sbDogRXZlbnRzLmFkZCgnTWVzYVNjcm9sbCcsIHRoaXMudXBkYXRlT2Zmc2V0KSxcbiAgICAgIE1lc2FSZWZsb3c6IEV2ZW50cy5hZGQoJ01lc2FSZWZsb3cnLCB0aGlzLnVwZGF0ZU9mZnNldClcbiAgICB9O1xuICAgIHNldFRpbWVvdXQodGhpcy51cGRhdGVPZmZzZXQsIDEwMCk7XG4gIH1cblxuICBjb21wb25lbnRXaWxsVW5tb3VudCAoKSB7XG4gICAgT2JqZWN0LnZhbHVlcyh0aGlzLmxpc3RlbmVycykuZm9yRWFjaChsaXN0ZW5lcklkID0+IEV2ZW50cy5yZW1vdmUobGlzdGVuZXJJZCkpO1xuICB9XG5cbiAgdXBkYXRlT2Zmc2V0ICgpIHtcbiAgICBpZiAodGhpcy5wcm9wcy5kZWJ1ZykgY29uc29sZS5sb2coJ1VwZGF0aW5nIG9mZnNldC4uLicpO1xuICAgIGNvbnN0IHsgZWxlbWVudCB9ID0gdGhpcztcbiAgICBpZiAoIWVsZW1lbnQpIHtcbiAgICAgIGlmICh0aGlzLnByb3BzLmRlYnVnKSBjb25zb2xlLmxvZygnLi4uUmV0dXJuaW5nIG91dCwgbm8gZWxlbWVudCcpO1xuICAgICAgcmV0dXJuO1xuICAgIH07XG4gICAgY29uc3Qgb2Zmc2V0ID0gZWxlbWVudC5nZXRCb3VuZGluZ0NsaWVudFJlY3QoKTtcbiAgICBjb25zdCB7IHRvcCwgbGVmdCwgaGVpZ2h0IH0gPSBvZmZzZXQ7XG4gICAgY29uc3QgcG9zaXRpb24gPSB7IGxlZnQsIHRvcDogdG9wICsgaGVpZ2h0IH07XG4gICAgaWYgKHRoaXMucHJvcHMuZGVidWcpIGNvbnNvbGUubG9nKCdPZmZzZXQgaXMgbm93Li4uJywgcG9zaXRpb24pO1xuICAgIHRoaXMuc2V0U3RhdGUoeyBwb3NpdGlvbiB9KTtcbiAgfVxuXG4gIHJlbmRlciAoKSB7XG4gICAgY29uc3QgeyBwcm9wcyB9ID0gdGhpcztcbiAgICBjb25zdCB7IHBvc2l0aW9uIH0gPSB0aGlzLnN0YXRlO1xuICAgIGNvbnN0IHJlZiA9IChlbCkgPT4gdGhpcy5lbGVtZW50ID0gZWw7XG4gICAgY29uc3QgY2hpbGRyZW4gPSAoPGRpdiByZWY9e3JlZn0gc3R5bGU9e3sgZGlzcGxheTogJ2lubGluZS1ibG9jaycgfX0gY2hpbGRyZW49e3Byb3BzLmNoaWxkcmVufSAvPik7XG4gICAgY29uc3QgZXh0cmFjdGVkUHJvcHMgPSB7IC4uLnByb3BzLCBwb3NpdGlvbiwgY2hpbGRyZW4gfTtcblxuICAgIHJldHVybiAoXG4gICAgICA8VG9vbHRpcFxuICAgICAgICBjb3JuZXI9XCJ0b3AtbGVmdFwiXG4gICAgICAgIGNsYXNzTmFtZT1cIkFuY2hvcmVkVG9vbHRpcFwiXG4gICAgICAgIHsuLi5leHRyYWN0ZWRQcm9wc31cbiAgICAgIC8+XG4gICAgKTtcbiAgfVxufTtcblxuZXhwb3J0IGRlZmF1bHQgQW5jaG9yZWRUb29sdGlwO1xuIl19