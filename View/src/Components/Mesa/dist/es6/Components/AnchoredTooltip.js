'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

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

      var defaults = { top: 0, left: 0 };
      var _props = this.props,
          children = _props.children,
          content = _props.content,
          style = _props.style,
          offset = _props.offset;

      var _ref = this.anchor ? _Tooltip2.default.getOffset(this.anchor) : defaults,
          top = _ref.top,
          width = _ref.width,
          right = _ref.right,
          left = _ref.left;

      var position = { top: top, left: left + width };
      var offsetPosition = offset ? this.getOffsetPosition(position, offset) : position;
      var tooltipProps = { content: content, style: style, children: children, position: offsetPosition };
      return _react2.default.createElement(
        'div',
        { className: 'AnchoredTooltip', style: { display: 'inline-block' } },
        _react2.default.createElement(_Tooltip2.default, tooltipProps),
        _react2.default.createElement('span', { ref: function ref(a) {
            return _this2.anchor = a;
          } })
      );
    }
  }]);

  return AnchoredTooltip;
}(_react2.default.Component);

;

exports.default = AnchoredTooltip;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL0FuY2hvcmVkVG9vbHRpcC5qc3giXSwibmFtZXMiOlsiQW5jaG9yZWRUb29sdGlwIiwicHJvcHMiLCJnZXRPZmZzZXRQb3NpdGlvbiIsImJpbmQiLCJwb3NpdGlvbiIsIm9mZnNldCIsIm91dHB1dCIsImtleSIsImRlZmF1bHRzIiwidG9wIiwibGVmdCIsImNoaWxkcmVuIiwiY29udGVudCIsInN0eWxlIiwiYW5jaG9yIiwiZ2V0T2Zmc2V0Iiwid2lkdGgiLCJyaWdodCIsIm9mZnNldFBvc2l0aW9uIiwidG9vbHRpcFByb3BzIiwiZGlzcGxheSIsImEiLCJDb21wb25lbnQiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7O0FBQUE7Ozs7QUFFQTs7Ozs7Ozs7Ozs7O0lBRU1BLGU7OztBQUNKLDJCQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsa0lBQ1pBLEtBRFk7O0FBRWxCLFVBQUtDLGlCQUFMLEdBQXlCLE1BQUtBLGlCQUFMLENBQXVCQyxJQUF2QixPQUF6QjtBQUZrQjtBQUduQjs7OztzQ0FFa0JDLFEsRUFBVUMsTSxFQUFRO0FBQ25DLFVBQU1DLFNBQVMsRUFBZjtBQUNBLFdBQUssSUFBSUMsR0FBVCxJQUFnQkgsUUFBaEIsRUFBMEI7QUFDeEJFLGVBQU9DLEdBQVAsSUFBY0gsU0FBU0csR0FBVCxLQUFpQkEsT0FBT0YsTUFBUCxHQUFnQkEsT0FBT0UsR0FBUCxDQUFoQixHQUE4QixDQUEvQyxDQUFkO0FBQ0Q7QUFDRCxhQUFPRCxNQUFQO0FBQ0Q7Ozs2QkFFUztBQUFBOztBQUNSLFVBQU1FLFdBQVcsRUFBRUMsS0FBSyxDQUFQLEVBQVVDLE1BQU0sQ0FBaEIsRUFBakI7QUFEUSxtQkFFcUMsS0FBS1QsS0FGMUM7QUFBQSxVQUVBVSxRQUZBLFVBRUFBLFFBRkE7QUFBQSxVQUVVQyxPQUZWLFVBRVVBLE9BRlY7QUFBQSxVQUVtQkMsS0FGbkIsVUFFbUJBLEtBRm5CO0FBQUEsVUFFMEJSLE1BRjFCLFVBRTBCQSxNQUYxQjs7QUFBQSxpQkFJNEIsS0FBS1MsTUFBTCxHQUFjLGtCQUFRQyxTQUFSLENBQWtCLEtBQUtELE1BQXZCLENBQWQsR0FBK0NOLFFBSjNFO0FBQUEsVUFJQUMsR0FKQSxRQUlBQSxHQUpBO0FBQUEsVUFJS08sS0FKTCxRQUlLQSxLQUpMO0FBQUEsVUFJWUMsS0FKWixRQUlZQSxLQUpaO0FBQUEsVUFJbUJQLElBSm5CLFFBSW1CQSxJQUpuQjs7QUFLUixVQUFNTixXQUFXLEVBQUVLLFFBQUYsRUFBT0MsTUFBTUEsT0FBUU0sS0FBckIsRUFBakI7QUFDQSxVQUFNRSxpQkFBaUJiLFNBQVMsS0FBS0gsaUJBQUwsQ0FBdUJFLFFBQXZCLEVBQWlDQyxNQUFqQyxDQUFULEdBQW9ERCxRQUEzRTtBQUNBLFVBQU1lLGVBQWUsRUFBRVAsZ0JBQUYsRUFBV0MsWUFBWCxFQUFrQkYsa0JBQWxCLEVBQTRCUCxVQUFVYyxjQUF0QyxFQUFyQjtBQUNBLGFBQ0U7QUFBQTtBQUFBLFVBQUssV0FBVSxpQkFBZixFQUFpQyxPQUFPLEVBQUVFLFNBQVMsY0FBWCxFQUF4QztBQUNFLHlEQUFhRCxZQUFiLENBREY7QUFFRSxnREFBTSxLQUFLLGFBQUNFLENBQUQ7QUFBQSxtQkFBTyxPQUFLUCxNQUFMLEdBQWNPLENBQXJCO0FBQUEsV0FBWDtBQUZGLE9BREY7QUFNRDs7OztFQTVCMkIsZ0JBQU1DLFM7O0FBNkJuQzs7a0JBRWN0QixlIiwiZmlsZSI6IkFuY2hvcmVkVG9vbHRpcC5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5cbmltcG9ydCBUb29sdGlwIGZyb20gJy4vVG9vbHRpcCc7XG5cbmNsYXNzIEFuY2hvcmVkVG9vbHRpcCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgICB0aGlzLmdldE9mZnNldFBvc2l0aW9uID0gdGhpcy5nZXRPZmZzZXRQb3NpdGlvbi5iaW5kKHRoaXMpO1xuICB9XG5cbiAgZ2V0T2Zmc2V0UG9zaXRpb24gKHBvc2l0aW9uLCBvZmZzZXQpIHtcbiAgICBjb25zdCBvdXRwdXQgPSB7fTtcbiAgICBmb3IgKGxldCBrZXkgaW4gcG9zaXRpb24pIHtcbiAgICAgIG91dHB1dFtrZXldID0gcG9zaXRpb25ba2V5XSArIChrZXkgaW4gb2Zmc2V0ID8gb2Zmc2V0W2tleV0gOiAwKTtcbiAgICB9O1xuICAgIHJldHVybiBvdXRwdXQ7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IGRlZmF1bHRzID0geyB0b3A6IDAsIGxlZnQ6IDAgfTtcbiAgICBjb25zdCB7IGNoaWxkcmVuLCBjb250ZW50LCBzdHlsZSwgb2Zmc2V0IH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3QgeyB0b3AsIHdpZHRoLCByaWdodCwgbGVmdCB9ID0gdGhpcy5hbmNob3IgPyBUb29sdGlwLmdldE9mZnNldCh0aGlzLmFuY2hvcikgOiBkZWZhdWx0cztcbiAgICBjb25zdCBwb3NpdGlvbiA9IHsgdG9wLCBsZWZ0OiBsZWZ0ICsgKHdpZHRoKSAgfTtcbiAgICBjb25zdCBvZmZzZXRQb3NpdGlvbiA9IG9mZnNldCA/IHRoaXMuZ2V0T2Zmc2V0UG9zaXRpb24ocG9zaXRpb24sIG9mZnNldCkgOiBwb3NpdGlvbjtcbiAgICBjb25zdCB0b29sdGlwUHJvcHMgPSB7IGNvbnRlbnQsIHN0eWxlLCBjaGlsZHJlbiwgcG9zaXRpb246IG9mZnNldFBvc2l0aW9uIH07XG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiQW5jaG9yZWRUb29sdGlwXCIgc3R5bGU9e3sgZGlzcGxheTogJ2lubGluZS1ibG9jaycgfX0+XG4gICAgICAgIDxUb29sdGlwIHsuLi50b29sdGlwUHJvcHN9IC8+XG4gICAgICAgIDxzcGFuIHJlZj17KGEpID0+IHRoaXMuYW5jaG9yID0gYX0gLz5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn07XG5cbmV4cG9ydCBkZWZhdWx0IEFuY2hvcmVkVG9vbHRpcDtcbiJdfQ==