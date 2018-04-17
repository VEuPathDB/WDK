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
      var tooltipProps = Object.assign({}, this.props, { position: offsetPosition });
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL0FuY2hvcmVkVG9vbHRpcC5qc3giXSwibmFtZXMiOlsiQW5jaG9yZWRUb29sdGlwIiwicHJvcHMiLCJnZXRPZmZzZXRQb3NpdGlvbiIsImJpbmQiLCJwb3NpdGlvbiIsIm9mZnNldCIsIm91dHB1dCIsImtleSIsImRlZmF1bHRzIiwidG9wIiwibGVmdCIsIndpZHRoIiwicmlnaHQiLCJhbmNob3IiLCJnZXRPZmZzZXQiLCJoZWlnaHQiLCJvZmZzZXRQb3NpdGlvbiIsInRvb2x0aXBQcm9wcyIsIk9iamVjdCIsImFzc2lnbiIsImRpc3BsYXkiLCJmbG9hdCIsImEiLCJDb21wb25lbnQiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7QUFBQTs7OztBQUVBOzs7Ozs7Ozs7Ozs7SUFFTUEsZTs7O0FBQ0osMkJBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSxrSUFDWkEsS0FEWTs7QUFFbEIsVUFBS0MsaUJBQUwsR0FBeUIsTUFBS0EsaUJBQUwsQ0FBdUJDLElBQXZCLE9BQXpCO0FBRmtCO0FBR25COzs7O3NDQUVrQkMsUSxFQUFVQyxNLEVBQVE7QUFDbkMsVUFBSSxTQUFRQSxNQUFSLHlDQUFRQSxNQUFSLE9BQW1CLFFBQW5CLElBQStCLFFBQU9ELFFBQVAseUNBQU9BLFFBQVAsT0FBb0IsUUFBdkQsRUFBaUUsT0FBT0EsUUFBUDtBQUNqRSxVQUFNRSxTQUFTLEVBQWY7QUFDQSxXQUFLLElBQUlDLEdBQVQsSUFBZ0JILFFBQWhCLEVBQTBCO0FBQ3hCRSxlQUFPQyxHQUFQLElBQWNILFNBQVNHLEdBQVQsS0FBaUJBLE9BQU9GLE1BQVAsR0FBZ0JBLE9BQU9FLEdBQVAsQ0FBaEIsR0FBOEIsQ0FBL0MsQ0FBZDtBQUNEO0FBQ0QsYUFBT0QsTUFBUDtBQUNEOzs7NkJBRVM7QUFBQTs7QUFDUixVQUFNRSxXQUFXLEVBQUVDLEtBQUssQ0FBUCxFQUFVQyxNQUFNLENBQWhCLEVBQW1CQyxPQUFPLENBQTFCLEVBQTZCQyxPQUFPLENBQXBDLEVBQWpCO0FBRFEsVUFFQVAsTUFGQSxHQUVXLEtBQUtKLEtBRmhCLENBRUFJLE1BRkE7O0FBQUEsaUJBSXFDLEtBQUtRLE1BQUwsR0FBYyxrQkFBUUMsU0FBUixDQUFrQixLQUFLRCxNQUF2QixDQUFkLEdBQStDTCxRQUpwRjtBQUFBLFVBSUFDLEdBSkEsUUFJQUEsR0FKQTtBQUFBLFVBSUtDLElBSkwsUUFJS0EsSUFKTDtBQUFBLFVBSVdDLEtBSlgsUUFJV0EsS0FKWDtBQUFBLFVBSWtCSSxNQUpsQixRQUlrQkEsTUFKbEI7QUFBQSxVQUkwQkgsS0FKMUIsUUFJMEJBLEtBSjFCOztBQUtSLFVBQU1SLFdBQVcsRUFBRUssS0FBS0EsTUFBTU0sTUFBYixFQUFxQkwsTUFBTUUsUUFBUUQsS0FBbkMsRUFBakI7QUFDQSxVQUFNSyxpQkFBaUIsS0FBS2QsaUJBQUwsQ0FBdUJFLFFBQXZCLEVBQWlDQyxNQUFqQyxDQUF2QjtBQUNBLFVBQU1ZLGVBQWVDLE9BQU9DLE1BQVAsQ0FBYyxFQUFkLEVBQWtCLEtBQUtsQixLQUF2QixFQUE4QixFQUFFRyxVQUFVWSxjQUFaLEVBQTlCLENBQXJCO0FBQ0EsYUFDRTtBQUFBO0FBQUEsVUFBSyxXQUFVLGlCQUFmLEVBQWlDLE9BQU8sRUFBRUksU0FBUyxjQUFYLEVBQXhDO0FBQ0UsZ0RBQU0sV0FBVSx3QkFBaEIsRUFBeUMsT0FBTyxFQUFFQyxPQUFPLE9BQVQsRUFBaEQsRUFBb0UsS0FBSyxhQUFDQyxDQUFEO0FBQUEsbUJBQU8sT0FBS1QsTUFBTCxHQUFjUyxDQUFyQjtBQUFBLFdBQXpFLEdBREY7QUFFRSx5REFBYUwsWUFBYjtBQUZGLE9BREY7QUFNRDs7OztFQTdCMkIsZ0JBQU1NLFM7O0FBOEJuQzs7a0JBRWN2QixlIiwiZmlsZSI6IkFuY2hvcmVkVG9vbHRpcC5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5cbmltcG9ydCBUb29sdGlwIGZyb20gJy4vVG9vbHRpcCc7XG5cbmNsYXNzIEFuY2hvcmVkVG9vbHRpcCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgICB0aGlzLmdldE9mZnNldFBvc2l0aW9uID0gdGhpcy5nZXRPZmZzZXRQb3NpdGlvbi5iaW5kKHRoaXMpO1xuICB9XG5cbiAgZ2V0T2Zmc2V0UG9zaXRpb24gKHBvc2l0aW9uLCBvZmZzZXQpIHtcbiAgICBpZiAoIXR5cGVvZiBvZmZzZXQgIT09ICdvYmplY3QnIHx8IHR5cGVvZiBwb3NpdGlvbiAhPT0gJ29iamVjdCcpIHJldHVybiBwb3NpdGlvbjtcbiAgICBjb25zdCBvdXRwdXQgPSB7fTtcbiAgICBmb3IgKGxldCBrZXkgaW4gcG9zaXRpb24pIHtcbiAgICAgIG91dHB1dFtrZXldID0gcG9zaXRpb25ba2V5XSArIChrZXkgaW4gb2Zmc2V0ID8gb2Zmc2V0W2tleV0gOiAwKTtcbiAgICB9O1xuICAgIHJldHVybiBvdXRwdXQ7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IGRlZmF1bHRzID0geyB0b3A6IDAsIGxlZnQ6IDAsIHdpZHRoOiAwLCByaWdodDogMCB9O1xuICAgIGNvbnN0IHsgb2Zmc2V0IH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3QgeyB0b3AsIGxlZnQsIHdpZHRoLCBoZWlnaHQsIHJpZ2h0IH0gPSAodGhpcy5hbmNob3IgPyBUb29sdGlwLmdldE9mZnNldCh0aGlzLmFuY2hvcikgOiBkZWZhdWx0cyk7XG4gICAgY29uc3QgcG9zaXRpb24gPSB7IHRvcDogdG9wICsgaGVpZ2h0LCBsZWZ0OiByaWdodCArIHdpZHRoIH07XG4gICAgY29uc3Qgb2Zmc2V0UG9zaXRpb24gPSB0aGlzLmdldE9mZnNldFBvc2l0aW9uKHBvc2l0aW9uLCBvZmZzZXQpO1xuICAgIGNvbnN0IHRvb2x0aXBQcm9wcyA9IE9iamVjdC5hc3NpZ24oe30sIHRoaXMucHJvcHMsIHsgcG9zaXRpb246IG9mZnNldFBvc2l0aW9uIH0pO1xuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cIkFuY2hvcmVkVG9vbHRpcFwiIHN0eWxlPXt7IGRpc3BsYXk6ICdpbmxpbmUtYmxvY2snIH19PlxuICAgICAgICA8c3BhbiBjbGFzc05hbWU9XCJBbmNob3JlZFRvb2x0aXAtQW5jaG9yXCIgc3R5bGU9e3sgZmxvYXQ6ICdyaWdodCcgfX0gcmVmPXsoYSkgPT4gdGhpcy5hbmNob3IgPSBhfSAvPlxuICAgICAgICA8VG9vbHRpcCB7Li4udG9vbHRpcFByb3BzfSAvPlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufTtcblxuZXhwb3J0IGRlZmF1bHQgQW5jaG9yZWRUb29sdGlwO1xuIl19