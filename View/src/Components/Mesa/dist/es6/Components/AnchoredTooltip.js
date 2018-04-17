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
          height = offset.height;

      var position = { left: left, top: top + height };
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL0FuY2hvcmVkVG9vbHRpcC5qc3giXSwibmFtZXMiOlsiQW5jaG9yZWRUb29sdGlwIiwicHJvcHMiLCJzdGF0ZSIsInBvc2l0aW9uIiwidXBkYXRlT2Zmc2V0IiwiYmluZCIsImNvbXBvbmVudERpZE1vdW50IiwiY29tcG9uZW50V2lsbFVubW91bnQiLCJsaXN0ZW5lcnMiLCJzY3JvbGwiLCJhZGQiLCJyZXNpemUiLCJNZXNhU2Nyb2xsIiwiTWVzYVJlZmxvdyIsInNldFRpbWVvdXQiLCJjb25zb2xlIiwibG9nIiwiT2JqZWN0IiwidmFsdWVzIiwiZm9yRWFjaCIsInJlbW92ZSIsImxpc3RlbmVySWQiLCJkZWJ1ZyIsImVsZW1lbnQiLCJvZmZzZXQiLCJnZXRCb3VuZGluZ0NsaWVudFJlY3QiLCJ0b3AiLCJsZWZ0IiwiaGVpZ2h0Iiwic2V0U3RhdGUiLCJyZWYiLCJlbCIsImNoaWxkcmVuIiwiZGlzcGxheSIsImV4dHJhY3RlZFByb3BzIiwiQ29tcG9uZW50Il0sIm1hcHBpbmdzIjoiOzs7Ozs7Ozs7O0FBQUE7Ozs7QUFFQTs7OztBQUNBOzs7Ozs7Ozs7Ozs7SUFFTUEsZTs7O0FBQ0osMkJBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSxrSUFDWkEsS0FEWTs7QUFFbEIsVUFBS0MsS0FBTCxHQUFhLEVBQUVDLFVBQVUsRUFBWixFQUFiO0FBQ0EsVUFBS0MsWUFBTCxHQUFvQixNQUFLQSxZQUFMLENBQWtCQyxJQUFsQixPQUFwQjtBQUNBLFVBQUtDLGlCQUFMLEdBQXlCLE1BQUtBLGlCQUFMLENBQXVCRCxJQUF2QixPQUF6QjtBQUNBLFVBQUtFLG9CQUFMLEdBQTRCLE1BQUtBLG9CQUFMLENBQTBCRixJQUExQixPQUE1QjtBQUxrQjtBQU1uQjs7Ozt3Q0FFb0I7QUFBQTs7QUFDbkIsV0FBS0QsWUFBTDtBQUNBLFdBQUtJLFNBQUwsR0FBaUI7QUFDZkMsZ0JBQVEsaUJBQU9DLEdBQVAsQ0FBVyxRQUFYLEVBQXFCLEtBQUtOLFlBQTFCLENBRE87QUFFZk8sZ0JBQVEsaUJBQU9ELEdBQVAsQ0FBVyxRQUFYLEVBQXFCLEtBQUtOLFlBQTFCLENBRk87QUFHZlEsb0JBQVksaUJBQU9GLEdBQVAsQ0FBVyxZQUFYLEVBQXlCLEtBQUtOLFlBQTlCLENBSEc7QUFJZlMsb0JBQVksaUJBQU9ILEdBQVAsQ0FBVyxZQUFYLEVBQXlCLEtBQUtOLFlBQTlCO0FBSkcsT0FBakI7QUFNQVUsaUJBQVcsWUFBTTtBQUNmLGVBQUtWLFlBQUw7QUFDQVcsZ0JBQVFDLEdBQVIsQ0FBWSw0QkFBWjtBQUNELE9BSEQsRUFHRyxHQUhIO0FBSUQ7OzsyQ0FFdUI7QUFDdEJDLGFBQU9DLE1BQVAsQ0FBYyxLQUFLVixTQUFuQixFQUE4QlcsT0FBOUIsQ0FBc0M7QUFBQSxlQUFjLGlCQUFPQyxNQUFQLENBQWNDLFVBQWQsQ0FBZDtBQUFBLE9BQXRDO0FBQ0Q7OzttQ0FFZTtBQUNkLFVBQUksS0FBS3BCLEtBQUwsQ0FBV3FCLEtBQWYsRUFBc0JQLFFBQVFDLEdBQVIsQ0FBWSxvQkFBWjtBQURSLFVBRU5PLE9BRk0sR0FFTSxJQUZOLENBRU5BLE9BRk07O0FBR2QsVUFBSSxDQUFDQSxPQUFMLEVBQWM7QUFDWixZQUFJLEtBQUt0QixLQUFMLENBQVdxQixLQUFmLEVBQXNCUCxRQUFRQyxHQUFSLENBQVksOEJBQVo7QUFDdEI7QUFDRDtBQUNELFVBQU1RLFNBQVNELFFBQVFFLHFCQUFSLEVBQWY7QUFQYyxVQVFOQyxHQVJNLEdBUWdCRixNQVJoQixDQVFORSxHQVJNO0FBQUEsVUFRREMsSUFSQyxHQVFnQkgsTUFSaEIsQ0FRREcsSUFSQztBQUFBLFVBUUtDLE1BUkwsR0FRZ0JKLE1BUmhCLENBUUtJLE1BUkw7O0FBU2QsVUFBTXpCLFdBQVcsRUFBRXdCLFVBQUYsRUFBUUQsS0FBS0EsTUFBTUUsTUFBbkIsRUFBakI7QUFDQSxVQUFJLEtBQUszQixLQUFMLENBQVdxQixLQUFmLEVBQXNCUCxRQUFRQyxHQUFSLENBQVksa0JBQVosRUFBZ0NiLFFBQWhDO0FBQ3RCLFdBQUswQixRQUFMLENBQWMsRUFBRTFCLGtCQUFGLEVBQWQ7QUFDRDs7OzZCQUVTO0FBQUE7O0FBQUEsVUFDQUYsS0FEQSxHQUNVLElBRFYsQ0FDQUEsS0FEQTtBQUFBLFVBRUFFLFFBRkEsR0FFYSxLQUFLRCxLQUZsQixDQUVBQyxRQUZBOztBQUdSLFVBQU0yQixNQUFNLFNBQU5BLEdBQU0sQ0FBQ0MsRUFBRDtBQUFBLGVBQVEsT0FBS1IsT0FBTCxHQUFlUSxFQUF2QjtBQUFBLE9BQVo7QUFDQSxVQUFNQyxXQUFZLHVDQUFLLEtBQUtGLEdBQVYsRUFBZSxPQUFPLEVBQUVHLFNBQVMsY0FBWCxFQUF0QixFQUFtRCxVQUFVaEMsTUFBTStCLFFBQW5FLEdBQWxCO0FBQ0EsVUFBTUUsOEJBQXNCakMsS0FBdEIsSUFBNkJFLGtCQUE3QixFQUF1QzZCLGtCQUF2QyxHQUFOOztBQUVBLGFBQ0U7QUFDRSxnQkFBTyxVQURUO0FBRUUsbUJBQVU7QUFGWixTQUdNRSxjQUhOLEVBREY7QUFPRDs7OztFQXZEMkIsZ0JBQU1DLFM7O0FBd0RuQzs7a0JBRWNuQyxlIiwiZmlsZSI6IkFuY2hvcmVkVG9vbHRpcC5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5cbmltcG9ydCBUb29sdGlwIGZyb20gJy4vVG9vbHRpcCc7XG5pbXBvcnQgRXZlbnRzIGZyb20gJy4uL1V0aWxzL0V2ZW50cyc7XG5cbmNsYXNzIEFuY2hvcmVkVG9vbHRpcCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgICB0aGlzLnN0YXRlID0geyBwb3NpdGlvbjoge30gfTtcbiAgICB0aGlzLnVwZGF0ZU9mZnNldCA9IHRoaXMudXBkYXRlT2Zmc2V0LmJpbmQodGhpcyk7XG4gICAgdGhpcy5jb21wb25lbnREaWRNb3VudCA9IHRoaXMuY29tcG9uZW50RGlkTW91bnQuYmluZCh0aGlzKTtcbiAgICB0aGlzLmNvbXBvbmVudFdpbGxVbm1vdW50ID0gdGhpcy5jb21wb25lbnRXaWxsVW5tb3VudC5iaW5kKHRoaXMpO1xuICB9XG5cbiAgY29tcG9uZW50RGlkTW91bnQgKCkge1xuICAgIHRoaXMudXBkYXRlT2Zmc2V0KCk7XG4gICAgdGhpcy5saXN0ZW5lcnMgPSB7XG4gICAgICBzY3JvbGw6IEV2ZW50cy5hZGQoJ3Njcm9sbCcsIHRoaXMudXBkYXRlT2Zmc2V0KSxcbiAgICAgIHJlc2l6ZTogRXZlbnRzLmFkZCgncmVzaXplJywgdGhpcy51cGRhdGVPZmZzZXQpLFxuICAgICAgTWVzYVNjcm9sbDogRXZlbnRzLmFkZCgnTWVzYVNjcm9sbCcsIHRoaXMudXBkYXRlT2Zmc2V0KSxcbiAgICAgIE1lc2FSZWZsb3c6IEV2ZW50cy5hZGQoJ01lc2FSZWZsb3cnLCB0aGlzLnVwZGF0ZU9mZnNldClcbiAgICB9O1xuICAgIHNldFRpbWVvdXQoKCkgPT4ge1xuICAgICAgdGhpcy51cGRhdGVPZmZzZXQoKVxuICAgICAgY29uc29sZS5sb2coJ1VwZGF0ZWQgb2Zmc2V0IGFmdGVyIGRlbGF5JylcbiAgICB9LCAxMDApO1xuICB9XG5cbiAgY29tcG9uZW50V2lsbFVubW91bnQgKCkge1xuICAgIE9iamVjdC52YWx1ZXModGhpcy5saXN0ZW5lcnMpLmZvckVhY2gobGlzdGVuZXJJZCA9PiBFdmVudHMucmVtb3ZlKGxpc3RlbmVySWQpKTtcbiAgfVxuXG4gIHVwZGF0ZU9mZnNldCAoKSB7XG4gICAgaWYgKHRoaXMucHJvcHMuZGVidWcpIGNvbnNvbGUubG9nKCdVcGRhdGluZyBvZmZzZXQuLi4nKTtcbiAgICBjb25zdCB7IGVsZW1lbnQgfSA9IHRoaXM7XG4gICAgaWYgKCFlbGVtZW50KSB7XG4gICAgICBpZiAodGhpcy5wcm9wcy5kZWJ1ZykgY29uc29sZS5sb2coJy4uLlJldHVybmluZyBvdXQsIG5vIGVsZW1lbnQnKTtcbiAgICAgIHJldHVybjtcbiAgICB9O1xuICAgIGNvbnN0IG9mZnNldCA9IGVsZW1lbnQuZ2V0Qm91bmRpbmdDbGllbnRSZWN0KCk7XG4gICAgY29uc3QgeyB0b3AsIGxlZnQsIGhlaWdodCB9ID0gb2Zmc2V0O1xuICAgIGNvbnN0IHBvc2l0aW9uID0geyBsZWZ0LCB0b3A6IHRvcCArIGhlaWdodCB9O1xuICAgIGlmICh0aGlzLnByb3BzLmRlYnVnKSBjb25zb2xlLmxvZygnT2Zmc2V0IGlzIG5vdy4uLicsIHBvc2l0aW9uKTtcbiAgICB0aGlzLnNldFN0YXRlKHsgcG9zaXRpb24gfSk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgcHJvcHMgfSA9IHRoaXM7XG4gICAgY29uc3QgeyBwb3NpdGlvbiB9ID0gdGhpcy5zdGF0ZTtcbiAgICBjb25zdCByZWYgPSAoZWwpID0+IHRoaXMuZWxlbWVudCA9IGVsO1xuICAgIGNvbnN0IGNoaWxkcmVuID0gKDxkaXYgcmVmPXtyZWZ9IHN0eWxlPXt7IGRpc3BsYXk6ICdpbmxpbmUtYmxvY2snIH19IGNoaWxkcmVuPXtwcm9wcy5jaGlsZHJlbn0gLz4pO1xuICAgIGNvbnN0IGV4dHJhY3RlZFByb3BzID0geyAuLi5wcm9wcywgcG9zaXRpb24sIGNoaWxkcmVuIH07XG5cbiAgICByZXR1cm4gKFxuICAgICAgPFRvb2x0aXBcbiAgICAgICAgY29ybmVyPVwidG9wLWxlZnRcIlxuICAgICAgICBjbGFzc05hbWU9XCJBbmNob3JlZFRvb2x0aXBcIlxuICAgICAgICB7Li4uZXh0cmFjdGVkUHJvcHN9XG4gICAgICAvPlxuICAgICk7XG4gIH1cbn07XG5cbmV4cG9ydCBkZWZhdWx0IEFuY2hvcmVkVG9vbHRpcDtcbiJdfQ==