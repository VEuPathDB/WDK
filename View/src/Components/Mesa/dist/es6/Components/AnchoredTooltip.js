'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Tooltip = require('./Tooltip');

var _Tooltip2 = _interopRequireDefault(_Tooltip);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var AnchoredTooltip = function (_React$Component) {
  _inherits(AnchoredTooltip, _React$Component);

  function AnchoredTooltip(props) {
    _classCallCheck(this, AnchoredTooltip);

    var _this = _possibleConstructorReturn(this, (AnchoredTooltip.__proto__ || Object.getPrototypeOf(AnchoredTooltip)).call(this, props));

    _this.getOffsetPosition = _this.getOffsetPosition.bind(_this);
    return _this;
  }

  _createClass(AnchoredTooltip, [{
    key: 'getOffsetPosition',
    value: function getOffsetPosition(position, offset) {
      if (!(typeof offset === 'undefined' ? 'undefined' : _typeof(offset)) !== 'object' || (typeof position === 'undefined' ? 'undefined' : _typeof(position)) !== 'object') return position;
      var output = {};
      for (var key in position) {
        output[key] = position[key] + (key in offset ? offset[key] : 0);
      };
      return output;
    }
  }, {
    key: 'render',
    value: function render() {
      var _this2 = this;

      var corner = 'left-middle';
      var defaults = { top: 0, left: 0, width: 0, right: 0 };
      var offset = this.props.offset;

      var _ref = this.anchor ? _Tooltip2.default.getOffset(this.anchor) : defaults,
          top = _ref.top,
          left = _ref.left,
          width = _ref.width,
          height = _ref.height,
          right = _ref.right;

      var position = { top: top + height, left: right + width };
      var offsetPosition = this.getOffsetPosition(position, offset);

      var tooltipProps = Object.assign({ corner: corner }, this.props, { position: offsetPosition });
      return _react2.default.createElement(
        'div',
        { className: 'AnchoredTooltip', style: { display: 'inline-block' } },
        _react2.default.createElement('span', { className: 'AnchoredTooltip-Anchor', style: { float: 'right' }, ref: function ref(a) {
            return _this2.anchor = a;
          } }),
        _react2.default.createElement(_Tooltip2.default, tooltipProps)
      );
    }
  }]);

  return AnchoredTooltip;
}(_react2.default.Component);

;

exports.default = AnchoredTooltip;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL0FuY2hvcmVkVG9vbHRpcC5qc3giXSwibmFtZXMiOlsiQW5jaG9yZWRUb29sdGlwIiwicHJvcHMiLCJnZXRPZmZzZXRQb3NpdGlvbiIsImJpbmQiLCJwb3NpdGlvbiIsIm9mZnNldCIsIm91dHB1dCIsImtleSIsImNvcm5lciIsImRlZmF1bHRzIiwidG9wIiwibGVmdCIsIndpZHRoIiwicmlnaHQiLCJhbmNob3IiLCJnZXRPZmZzZXQiLCJoZWlnaHQiLCJvZmZzZXRQb3NpdGlvbiIsInRvb2x0aXBQcm9wcyIsIk9iamVjdCIsImFzc2lnbiIsImRpc3BsYXkiLCJmbG9hdCIsImEiLCJDb21wb25lbnQiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7QUFBQTs7OztBQUVBOzs7Ozs7Ozs7Ozs7SUFFTUEsZTs7O0FBQ0osMkJBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSxrSUFDWkEsS0FEWTs7QUFFbEIsVUFBS0MsaUJBQUwsR0FBeUIsTUFBS0EsaUJBQUwsQ0FBdUJDLElBQXZCLE9BQXpCO0FBRmtCO0FBR25COzs7O3NDQUVrQkMsUSxFQUFVQyxNLEVBQVE7QUFDbkMsVUFBSSxTQUFRQSxNQUFSLHlDQUFRQSxNQUFSLE9BQW1CLFFBQW5CLElBQStCLFFBQU9ELFFBQVAseUNBQU9BLFFBQVAsT0FBb0IsUUFBdkQsRUFBaUUsT0FBT0EsUUFBUDtBQUNqRSxVQUFNRSxTQUFTLEVBQWY7QUFDQSxXQUFLLElBQUlDLEdBQVQsSUFBZ0JILFFBQWhCLEVBQTBCO0FBQ3hCRSxlQUFPQyxHQUFQLElBQWNILFNBQVNHLEdBQVQsS0FBaUJBLE9BQU9GLE1BQVAsR0FBZ0JBLE9BQU9FLEdBQVAsQ0FBaEIsR0FBOEIsQ0FBL0MsQ0FBZDtBQUNEO0FBQ0QsYUFBT0QsTUFBUDtBQUNEOzs7NkJBRVM7QUFBQTs7QUFDUixVQUFNRSxTQUFTLGFBQWY7QUFDQSxVQUFNQyxXQUFXLEVBQUVDLEtBQUssQ0FBUCxFQUFVQyxNQUFNLENBQWhCLEVBQW1CQyxPQUFPLENBQTFCLEVBQTZCQyxPQUFPLENBQXBDLEVBQWpCO0FBRlEsVUFHQVIsTUFIQSxHQUdXLEtBQUtKLEtBSGhCLENBR0FJLE1BSEE7O0FBQUEsaUJBS3FDLEtBQUtTLE1BQUwsR0FBYyxrQkFBUUMsU0FBUixDQUFrQixLQUFLRCxNQUF2QixDQUFkLEdBQStDTCxRQUxwRjtBQUFBLFVBS0FDLEdBTEEsUUFLQUEsR0FMQTtBQUFBLFVBS0tDLElBTEwsUUFLS0EsSUFMTDtBQUFBLFVBS1dDLEtBTFgsUUFLV0EsS0FMWDtBQUFBLFVBS2tCSSxNQUxsQixRQUtrQkEsTUFMbEI7QUFBQSxVQUswQkgsS0FMMUIsUUFLMEJBLEtBTDFCOztBQU1SLFVBQU1ULFdBQVcsRUFBRU0sS0FBS0EsTUFBTU0sTUFBYixFQUFxQkwsTUFBTUUsUUFBUUQsS0FBbkMsRUFBakI7QUFDQSxVQUFNSyxpQkFBaUIsS0FBS2YsaUJBQUwsQ0FBdUJFLFFBQXZCLEVBQWlDQyxNQUFqQyxDQUF2Qjs7QUFFQSxVQUFNYSxlQUFlQyxPQUFPQyxNQUFQLENBQWMsRUFBRVosY0FBRixFQUFkLEVBQTBCLEtBQUtQLEtBQS9CLEVBQXNDLEVBQUVHLFVBQVVhLGNBQVosRUFBdEMsQ0FBckI7QUFDQSxhQUNFO0FBQUE7QUFBQSxVQUFLLFdBQVUsaUJBQWYsRUFBaUMsT0FBTyxFQUFFSSxTQUFTLGNBQVgsRUFBeEM7QUFDRSxnREFBTSxXQUFVLHdCQUFoQixFQUF5QyxPQUFPLEVBQUVDLE9BQU8sT0FBVCxFQUFoRCxFQUFvRSxLQUFLLGFBQUNDLENBQUQ7QUFBQSxtQkFBTyxPQUFLVCxNQUFMLEdBQWNTLENBQXJCO0FBQUEsV0FBekUsR0FERjtBQUVFLHlEQUFhTCxZQUFiO0FBRkYsT0FERjtBQU1EOzs7O0VBL0IyQixnQkFBTU0sUzs7QUFnQ25DOztrQkFFY3hCLGUiLCJmaWxlIjoiQW5jaG9yZWRUb29sdGlwLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcblxuaW1wb3J0IFRvb2x0aXAgZnJvbSAnLi9Ub29sdGlwJztcblxuY2xhc3MgQW5jaG9yZWRUb29sdGlwIGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuICAgIHRoaXMuZ2V0T2Zmc2V0UG9zaXRpb24gPSB0aGlzLmdldE9mZnNldFBvc2l0aW9uLmJpbmQodGhpcyk7XG4gIH1cblxuICBnZXRPZmZzZXRQb3NpdGlvbiAocG9zaXRpb24sIG9mZnNldCkge1xuICAgIGlmICghdHlwZW9mIG9mZnNldCAhPT0gJ29iamVjdCcgfHwgdHlwZW9mIHBvc2l0aW9uICE9PSAnb2JqZWN0JykgcmV0dXJuIHBvc2l0aW9uO1xuICAgIGNvbnN0IG91dHB1dCA9IHt9O1xuICAgIGZvciAobGV0IGtleSBpbiBwb3NpdGlvbikge1xuICAgICAgb3V0cHV0W2tleV0gPSBwb3NpdGlvbltrZXldICsgKGtleSBpbiBvZmZzZXQgPyBvZmZzZXRba2V5XSA6IDApO1xuICAgIH07XG4gICAgcmV0dXJuIG91dHB1dDtcbiAgfVxuXG4gIHJlbmRlciAoKSB7XG4gICAgY29uc3QgY29ybmVyID0gJ2xlZnQtbWlkZGxlJztcbiAgICBjb25zdCBkZWZhdWx0cyA9IHsgdG9wOiAwLCBsZWZ0OiAwLCB3aWR0aDogMCwgcmlnaHQ6IDAgfTtcbiAgICBjb25zdCB7IG9mZnNldCB9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IHsgdG9wLCBsZWZ0LCB3aWR0aCwgaGVpZ2h0LCByaWdodCB9ID0gKHRoaXMuYW5jaG9yID8gVG9vbHRpcC5nZXRPZmZzZXQodGhpcy5hbmNob3IpIDogZGVmYXVsdHMpO1xuICAgIGNvbnN0IHBvc2l0aW9uID0geyB0b3A6IHRvcCArIGhlaWdodCwgbGVmdDogcmlnaHQgKyB3aWR0aCB9O1xuICAgIGNvbnN0IG9mZnNldFBvc2l0aW9uID0gdGhpcy5nZXRPZmZzZXRQb3NpdGlvbihwb3NpdGlvbiwgb2Zmc2V0KTtcblxuICAgIGNvbnN0IHRvb2x0aXBQcm9wcyA9IE9iamVjdC5hc3NpZ24oeyBjb3JuZXIgfSwgdGhpcy5wcm9wcywgeyBwb3NpdGlvbjogb2Zmc2V0UG9zaXRpb24gfSk7XG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiQW5jaG9yZWRUb29sdGlwXCIgc3R5bGU9e3sgZGlzcGxheTogJ2lubGluZS1ibG9jaycgfX0+XG4gICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cIkFuY2hvcmVkVG9vbHRpcC1BbmNob3JcIiBzdHlsZT17eyBmbG9hdDogJ3JpZ2h0JyB9fSByZWY9eyhhKSA9PiB0aGlzLmFuY2hvciA9IGF9IC8+XG4gICAgICAgIDxUb29sdGlwIHsuLi50b29sdGlwUHJvcHN9IC8+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59O1xuXG5leHBvcnQgZGVmYXVsdCBBbmNob3JlZFRvb2x0aXA7XG4iXX0=