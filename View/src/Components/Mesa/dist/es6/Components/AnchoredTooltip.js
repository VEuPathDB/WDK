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
          right = _ref.right;

      var position = { top: top, left: window.innerWidth - right };
      var offsetPosition = this.getOffsetPosition(position, offset);
      var tooltipProps = Object.assign({}, this.props, { position: offsetPosition });
      return _react2.default.createElement(
        'div',
        { className: 'AnchoredTooltip', style: { display: 'inline-block' } },
        _react2.default.createElement(_Tooltip2.default, tooltipProps),
        _react2.default.createElement('span', { className: 'AnchoredTooltip-Anchor', ref: function ref(a) {
            return _this2.anchor = a;
          } })
      );
    }
  }]);

  return AnchoredTooltip;
}(_react2.default.Component);

;

exports.default = AnchoredTooltip;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL0FuY2hvcmVkVG9vbHRpcC5qc3giXSwibmFtZXMiOlsiQW5jaG9yZWRUb29sdGlwIiwicHJvcHMiLCJnZXRPZmZzZXRQb3NpdGlvbiIsImJpbmQiLCJwb3NpdGlvbiIsIm9mZnNldCIsIm91dHB1dCIsImtleSIsImRlZmF1bHRzIiwidG9wIiwibGVmdCIsIndpZHRoIiwicmlnaHQiLCJhbmNob3IiLCJnZXRPZmZzZXQiLCJ3aW5kb3ciLCJpbm5lcldpZHRoIiwib2Zmc2V0UG9zaXRpb24iLCJ0b29sdGlwUHJvcHMiLCJPYmplY3QiLCJhc3NpZ24iLCJkaXNwbGF5IiwiYSIsIkNvbXBvbmVudCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7OztBQUFBOzs7O0FBRUE7Ozs7Ozs7Ozs7OztJQUVNQSxlOzs7QUFDSiwyQkFBYUMsS0FBYixFQUFvQjtBQUFBOztBQUFBLGtJQUNaQSxLQURZOztBQUVsQixVQUFLQyxpQkFBTCxHQUF5QixNQUFLQSxpQkFBTCxDQUF1QkMsSUFBdkIsT0FBekI7QUFGa0I7QUFHbkI7Ozs7c0NBRWtCQyxRLEVBQVVDLE0sRUFBUTtBQUNuQyxVQUFJLFNBQVFBLE1BQVIseUNBQVFBLE1BQVIsT0FBbUIsUUFBbkIsSUFBK0IsUUFBT0QsUUFBUCx5Q0FBT0EsUUFBUCxPQUFvQixRQUF2RCxFQUFpRSxPQUFPQSxRQUFQO0FBQ2pFLFVBQU1FLFNBQVMsRUFBZjtBQUNBLFdBQUssSUFBSUMsR0FBVCxJQUFnQkgsUUFBaEIsRUFBMEI7QUFDeEJFLGVBQU9DLEdBQVAsSUFBY0gsU0FBU0csR0FBVCxLQUFpQkEsT0FBT0YsTUFBUCxHQUFnQkEsT0FBT0UsR0FBUCxDQUFoQixHQUE4QixDQUEvQyxDQUFkO0FBQ0Q7QUFDRCxhQUFPRCxNQUFQO0FBQ0Q7Ozs2QkFFUztBQUFBOztBQUNSLFVBQU1FLFdBQVcsRUFBRUMsS0FBSyxDQUFQLEVBQVVDLE1BQU0sQ0FBaEIsRUFBbUJDLE9BQU8sQ0FBMUIsRUFBNkJDLE9BQU8sQ0FBcEMsRUFBakI7QUFEUSxVQUVBUCxNQUZBLEdBRVcsS0FBS0osS0FGaEIsQ0FFQUksTUFGQTs7QUFBQSxpQkFJNkIsS0FBS1EsTUFBTCxHQUFjLGtCQUFRQyxTQUFSLENBQWtCLEtBQUtELE1BQXZCLENBQWQsR0FBK0NMLFFBSjVFO0FBQUEsVUFJQUMsR0FKQSxRQUlBQSxHQUpBO0FBQUEsVUFJS0MsSUFKTCxRQUlLQSxJQUpMO0FBQUEsVUFJV0MsS0FKWCxRQUlXQSxLQUpYO0FBQUEsVUFJa0JDLEtBSmxCLFFBSWtCQSxLQUpsQjs7QUFLUixVQUFNUixXQUFXLEVBQUVLLFFBQUYsRUFBT0MsTUFBTUssT0FBT0MsVUFBUCxHQUFvQkosS0FBakMsRUFBakI7QUFDQSxVQUFNSyxpQkFBaUIsS0FBS2YsaUJBQUwsQ0FBdUJFLFFBQXZCLEVBQWlDQyxNQUFqQyxDQUF2QjtBQUNBLFVBQU1hLGVBQWVDLE9BQU9DLE1BQVAsQ0FBYyxFQUFkLEVBQWtCLEtBQUtuQixLQUF2QixFQUE4QixFQUFFRyxVQUFVYSxjQUFaLEVBQTlCLENBQXJCO0FBQ0EsYUFDRTtBQUFBO0FBQUEsVUFBSyxXQUFVLGlCQUFmLEVBQWlDLE9BQU8sRUFBRUksU0FBUyxjQUFYLEVBQXhDO0FBQ0UseURBQWFILFlBQWIsQ0FERjtBQUVFLGdEQUFNLFdBQVUsd0JBQWhCLEVBQXlDLEtBQUssYUFBQ0ksQ0FBRDtBQUFBLG1CQUFPLE9BQUtULE1BQUwsR0FBY1MsQ0FBckI7QUFBQSxXQUE5QztBQUZGLE9BREY7QUFNRDs7OztFQTdCMkIsZ0JBQU1DLFM7O0FBOEJuQzs7a0JBRWN2QixlIiwiZmlsZSI6IkFuY2hvcmVkVG9vbHRpcC5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5cbmltcG9ydCBUb29sdGlwIGZyb20gJy4vVG9vbHRpcCc7XG5cbmNsYXNzIEFuY2hvcmVkVG9vbHRpcCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgICB0aGlzLmdldE9mZnNldFBvc2l0aW9uID0gdGhpcy5nZXRPZmZzZXRQb3NpdGlvbi5iaW5kKHRoaXMpO1xuICB9XG5cbiAgZ2V0T2Zmc2V0UG9zaXRpb24gKHBvc2l0aW9uLCBvZmZzZXQpIHtcbiAgICBpZiAoIXR5cGVvZiBvZmZzZXQgIT09ICdvYmplY3QnIHx8IHR5cGVvZiBwb3NpdGlvbiAhPT0gJ29iamVjdCcpIHJldHVybiBwb3NpdGlvbjtcbiAgICBjb25zdCBvdXRwdXQgPSB7fTtcbiAgICBmb3IgKGxldCBrZXkgaW4gcG9zaXRpb24pIHtcbiAgICAgIG91dHB1dFtrZXldID0gcG9zaXRpb25ba2V5XSArIChrZXkgaW4gb2Zmc2V0ID8gb2Zmc2V0W2tleV0gOiAwKTtcbiAgICB9O1xuICAgIHJldHVybiBvdXRwdXQ7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IGRlZmF1bHRzID0geyB0b3A6IDAsIGxlZnQ6IDAsIHdpZHRoOiAwLCByaWdodDogMCB9O1xuICAgIGNvbnN0IHsgb2Zmc2V0IH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3QgeyB0b3AsIGxlZnQsIHdpZHRoLCByaWdodCB9ID0gKHRoaXMuYW5jaG9yID8gVG9vbHRpcC5nZXRPZmZzZXQodGhpcy5hbmNob3IpIDogZGVmYXVsdHMpO1xuICAgIGNvbnN0IHBvc2l0aW9uID0geyB0b3AsIGxlZnQ6IHdpbmRvdy5pbm5lcldpZHRoIC0gcmlnaHQgfTtcbiAgICBjb25zdCBvZmZzZXRQb3NpdGlvbiA9IHRoaXMuZ2V0T2Zmc2V0UG9zaXRpb24ocG9zaXRpb24sIG9mZnNldCk7XG4gICAgY29uc3QgdG9vbHRpcFByb3BzID0gT2JqZWN0LmFzc2lnbih7fSwgdGhpcy5wcm9wcywgeyBwb3NpdGlvbjogb2Zmc2V0UG9zaXRpb24gfSk7XG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiQW5jaG9yZWRUb29sdGlwXCIgc3R5bGU9e3sgZGlzcGxheTogJ2lubGluZS1ibG9jaycgfX0+XG4gICAgICAgIDxUb29sdGlwIHsuLi50b29sdGlwUHJvcHN9IC8+XG4gICAgICAgIDxzcGFuIGNsYXNzTmFtZT1cIkFuY2hvcmVkVG9vbHRpcC1BbmNob3JcIiByZWY9eyhhKSA9PiB0aGlzLmFuY2hvciA9IGF9IC8+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59O1xuXG5leHBvcnQgZGVmYXVsdCBBbmNob3JlZFRvb2x0aXA7XG4iXX0=