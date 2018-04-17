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
      console.log('Updating offset...');
      var element = this.element;

      if (!element) return console.log('...Returning out, no element');
      var offset = element.getBoundingClientRect();
      var top = offset.top,
          left = offset.left,
          height = offset.height;

      var position = { left: left, top: top + height };
      console.log('Offset is now...', position);
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL0FuY2hvcmVkVG9vbHRpcC5qc3giXSwibmFtZXMiOlsiQW5jaG9yZWRUb29sdGlwIiwicHJvcHMiLCJzdGF0ZSIsInBvc2l0aW9uIiwidXBkYXRlT2Zmc2V0IiwiYmluZCIsImNvbXBvbmVudERpZE1vdW50IiwiY29tcG9uZW50V2lsbFVubW91bnQiLCJsaXN0ZW5lcnMiLCJzY3JvbGwiLCJhZGQiLCJyZXNpemUiLCJNZXNhU2Nyb2xsIiwiTWVzYVJlZmxvdyIsIk9iamVjdCIsInZhbHVlcyIsImZvckVhY2giLCJyZW1vdmUiLCJsaXN0ZW5lcklkIiwiY29uc29sZSIsImxvZyIsImVsZW1lbnQiLCJvZmZzZXQiLCJnZXRCb3VuZGluZ0NsaWVudFJlY3QiLCJ0b3AiLCJsZWZ0IiwiaGVpZ2h0Iiwic2V0U3RhdGUiLCJyZWYiLCJlbCIsImNoaWxkcmVuIiwiZGlzcGxheSIsImV4dHJhY3RlZFByb3BzIiwiQ29tcG9uZW50Il0sIm1hcHBpbmdzIjoiOzs7Ozs7Ozs7O0FBQUE7Ozs7QUFFQTs7OztBQUNBOzs7Ozs7Ozs7Ozs7SUFFTUEsZTs7O0FBQ0osMkJBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSxrSUFDWkEsS0FEWTs7QUFFbEIsVUFBS0MsS0FBTCxHQUFhLEVBQUVDLFVBQVUsRUFBWixFQUFiO0FBQ0EsVUFBS0MsWUFBTCxHQUFvQixNQUFLQSxZQUFMLENBQWtCQyxJQUFsQixPQUFwQjtBQUNBLFVBQUtDLGlCQUFMLEdBQXlCLE1BQUtBLGlCQUFMLENBQXVCRCxJQUF2QixPQUF6QjtBQUNBLFVBQUtFLG9CQUFMLEdBQTRCLE1BQUtBLG9CQUFMLENBQTBCRixJQUExQixPQUE1QjtBQUxrQjtBQU1uQjs7Ozt3Q0FFb0I7QUFDbkIsV0FBS0QsWUFBTDtBQUNBLFdBQUtJLFNBQUwsR0FBaUI7QUFDZkMsZ0JBQVEsaUJBQU9DLEdBQVAsQ0FBVyxRQUFYLEVBQXFCLEtBQUtOLFlBQTFCLENBRE87QUFFZk8sZ0JBQVEsaUJBQU9ELEdBQVAsQ0FBVyxRQUFYLEVBQXFCLEtBQUtOLFlBQTFCLENBRk87QUFHZlEsb0JBQVksaUJBQU9GLEdBQVAsQ0FBVyxZQUFYLEVBQXlCLEtBQUtOLFlBQTlCLENBSEc7QUFJZlMsb0JBQVksaUJBQU9ILEdBQVAsQ0FBVyxZQUFYLEVBQXlCLEtBQUtOLFlBQTlCO0FBSkcsT0FBakI7QUFNRDs7OzJDQUV1QjtBQUN0QlUsYUFBT0MsTUFBUCxDQUFjLEtBQUtQLFNBQW5CLEVBQThCUSxPQUE5QixDQUFzQztBQUFBLGVBQWMsaUJBQU9DLE1BQVAsQ0FBY0MsVUFBZCxDQUFkO0FBQUEsT0FBdEM7QUFDRDs7O21DQUVlO0FBQ2RDLGNBQVFDLEdBQVIsQ0FBWSxvQkFBWjtBQURjLFVBRU5DLE9BRk0sR0FFTSxJQUZOLENBRU5BLE9BRk07O0FBR2QsVUFBSSxDQUFDQSxPQUFMLEVBQWMsT0FBT0YsUUFBUUMsR0FBUixDQUFZLDhCQUFaLENBQVA7QUFDZCxVQUFNRSxTQUFTRCxRQUFRRSxxQkFBUixFQUFmO0FBSmMsVUFLTkMsR0FMTSxHQUtnQkYsTUFMaEIsQ0FLTkUsR0FMTTtBQUFBLFVBS0RDLElBTEMsR0FLZ0JILE1BTGhCLENBS0RHLElBTEM7QUFBQSxVQUtLQyxNQUxMLEdBS2dCSixNQUxoQixDQUtLSSxNQUxMOztBQU1kLFVBQU12QixXQUFXLEVBQUVzQixVQUFGLEVBQVFELEtBQUtBLE1BQU1FLE1BQW5CLEVBQWpCO0FBQ0FQLGNBQVFDLEdBQVIsQ0FBWSxrQkFBWixFQUFnQ2pCLFFBQWhDO0FBQ0EsV0FBS3dCLFFBQUwsQ0FBYyxFQUFFeEIsa0JBQUYsRUFBZDtBQUNEOzs7NkJBRVM7QUFBQTs7QUFBQSxVQUNBRixLQURBLEdBQ1UsSUFEVixDQUNBQSxLQURBO0FBQUEsVUFFQUUsUUFGQSxHQUVhLEtBQUtELEtBRmxCLENBRUFDLFFBRkE7O0FBR1IsVUFBTXlCLE1BQU0sU0FBTkEsR0FBTSxDQUFDQyxFQUFEO0FBQUEsZUFBUSxPQUFLUixPQUFMLEdBQWVRLEVBQXZCO0FBQUEsT0FBWjtBQUNBLFVBQU1DLFdBQVksdUNBQUssS0FBS0YsR0FBVixFQUFlLE9BQU8sRUFBRUcsU0FBUyxjQUFYLEVBQXRCLEVBQW1ELFVBQVU5QixNQUFNNkIsUUFBbkUsR0FBbEI7QUFDQSxVQUFNRSw4QkFBc0IvQixLQUF0QixJQUE2QkUsa0JBQTdCLEVBQXVDMkIsa0JBQXZDLEdBQU47O0FBRUEsYUFDRTtBQUNFLGdCQUFPLFVBRFQ7QUFFRSxtQkFBVTtBQUZaLFNBR01FLGNBSE4sRUFERjtBQU9EOzs7O0VBaEQyQixnQkFBTUMsUzs7QUFpRG5DOztrQkFFY2pDLGUiLCJmaWxlIjoiQW5jaG9yZWRUb29sdGlwLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcblxuaW1wb3J0IFRvb2x0aXAgZnJvbSAnLi9Ub29sdGlwJztcbmltcG9ydCBFdmVudHMgZnJvbSAnLi4vVXRpbHMvRXZlbnRzJztcblxuY2xhc3MgQW5jaG9yZWRUb29sdGlwIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuICAgIHRoaXMuc3RhdGUgPSB7IHBvc2l0aW9uOiB7fSB9O1xuICAgIHRoaXMudXBkYXRlT2Zmc2V0ID0gdGhpcy51cGRhdGVPZmZzZXQuYmluZCh0aGlzKTtcbiAgICB0aGlzLmNvbXBvbmVudERpZE1vdW50ID0gdGhpcy5jb21wb25lbnREaWRNb3VudC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuY29tcG9uZW50V2lsbFVubW91bnQgPSB0aGlzLmNvbXBvbmVudFdpbGxVbm1vdW50LmJpbmQodGhpcyk7XG4gIH1cblxuICBjb21wb25lbnREaWRNb3VudCAoKSB7XG4gICAgdGhpcy51cGRhdGVPZmZzZXQoKTtcbiAgICB0aGlzLmxpc3RlbmVycyA9IHtcbiAgICAgIHNjcm9sbDogRXZlbnRzLmFkZCgnc2Nyb2xsJywgdGhpcy51cGRhdGVPZmZzZXQpLFxuICAgICAgcmVzaXplOiBFdmVudHMuYWRkKCdyZXNpemUnLCB0aGlzLnVwZGF0ZU9mZnNldCksXG4gICAgICBNZXNhU2Nyb2xsOiBFdmVudHMuYWRkKCdNZXNhU2Nyb2xsJywgdGhpcy51cGRhdGVPZmZzZXQpLFxuICAgICAgTWVzYVJlZmxvdzogRXZlbnRzLmFkZCgnTWVzYVJlZmxvdycsIHRoaXMudXBkYXRlT2Zmc2V0KVxuICAgIH07XG4gIH1cblxuICBjb21wb25lbnRXaWxsVW5tb3VudCAoKSB7XG4gICAgT2JqZWN0LnZhbHVlcyh0aGlzLmxpc3RlbmVycykuZm9yRWFjaChsaXN0ZW5lcklkID0+IEV2ZW50cy5yZW1vdmUobGlzdGVuZXJJZCkpO1xuICB9XG5cbiAgdXBkYXRlT2Zmc2V0ICgpIHtcbiAgICBjb25zb2xlLmxvZygnVXBkYXRpbmcgb2Zmc2V0Li4uJyk7XG4gICAgY29uc3QgeyBlbGVtZW50IH0gPSB0aGlzO1xuICAgIGlmICghZWxlbWVudCkgcmV0dXJuIGNvbnNvbGUubG9nKCcuLi5SZXR1cm5pbmcgb3V0LCBubyBlbGVtZW50Jyk7XG4gICAgY29uc3Qgb2Zmc2V0ID0gZWxlbWVudC5nZXRCb3VuZGluZ0NsaWVudFJlY3QoKTtcbiAgICBjb25zdCB7IHRvcCwgbGVmdCwgaGVpZ2h0IH0gPSBvZmZzZXQ7XG4gICAgY29uc3QgcG9zaXRpb24gPSB7IGxlZnQsIHRvcDogdG9wICsgaGVpZ2h0IH07XG4gICAgY29uc29sZS5sb2coJ09mZnNldCBpcyBub3cuLi4nLCBwb3NpdGlvbik7XG4gICAgdGhpcy5zZXRTdGF0ZSh7IHBvc2l0aW9uIH0pO1xuICB9XG5cbiAgcmVuZGVyICgpIHtcbiAgICBjb25zdCB7IHByb3BzIH0gPSB0aGlzO1xuICAgIGNvbnN0IHsgcG9zaXRpb24gfSA9IHRoaXMuc3RhdGU7XG4gICAgY29uc3QgcmVmID0gKGVsKSA9PiB0aGlzLmVsZW1lbnQgPSBlbDtcbiAgICBjb25zdCBjaGlsZHJlbiA9ICg8ZGl2IHJlZj17cmVmfSBzdHlsZT17eyBkaXNwbGF5OiAnaW5saW5lLWJsb2NrJyB9fSBjaGlsZHJlbj17cHJvcHMuY2hpbGRyZW59IC8+KTtcbiAgICBjb25zdCBleHRyYWN0ZWRQcm9wcyA9IHsgLi4ucHJvcHMsIHBvc2l0aW9uLCBjaGlsZHJlbiB9O1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxUb29sdGlwXG4gICAgICAgIGNvcm5lcj1cInRvcC1sZWZ0XCJcbiAgICAgICAgY2xhc3NOYW1lPVwiQW5jaG9yZWRUb29sdGlwXCJcbiAgICAgICAgey4uLmV4dHJhY3RlZFByb3BzfVxuICAgICAgLz5cbiAgICApO1xuICB9XG59O1xuXG5leHBvcnQgZGVmYXVsdCBBbmNob3JlZFRvb2x0aXA7XG4iXX0=