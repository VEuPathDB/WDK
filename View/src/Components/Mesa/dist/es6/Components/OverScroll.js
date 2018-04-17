'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Defaults = require('../Defaults');

var _Defaults2 = _interopRequireDefault(_Defaults);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var OverScroll = function (_React$Component) {
  _inherits(OverScroll, _React$Component);

  function OverScroll(props) {
    _classCallCheck(this, OverScroll);

    return _possibleConstructorReturn(this, (OverScroll.__proto__ || Object.getPrototypeOf(OverScroll)).call(this, props));
  }

  _createClass(OverScroll, [{
    key: 'render',
    value: function render() {
      var _props = this.props,
          className = _props.className,
          height = _props.height;

      className = 'OverScroll' + (className ? ' ' + className : '');
      height = typeof height === 'number' ? height + 'px' : 'none';

      var style = {
        maxHeight: height,
        overflowY: 'auto'
      };

      return _react2.default.createElement(
        'div',
        { className: className },
        _react2.default.createElement(
          'div',
          { className: 'OverScroll-Inner', style: style },
          this.props.children
        )
      );
    }
  }]);

  return OverScroll;
}(_react2.default.Component);

;

exports.default = OverScroll;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL092ZXJTY3JvbGwuanN4Il0sIm5hbWVzIjpbIk92ZXJTY3JvbGwiLCJwcm9wcyIsImNsYXNzTmFtZSIsImhlaWdodCIsInN0eWxlIiwibWF4SGVpZ2h0Iiwib3ZlcmZsb3dZIiwiY2hpbGRyZW4iLCJDb21wb25lbnQiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7O0FBQUE7Ozs7QUFFQTs7Ozs7Ozs7Ozs7O0lBRU1BLFU7OztBQUNKLHNCQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsbUhBQ1pBLEtBRFk7QUFFbkI7Ozs7NkJBRVM7QUFBQSxtQkFDb0IsS0FBS0EsS0FEekI7QUFBQSxVQUNGQyxTQURFLFVBQ0ZBLFNBREU7QUFBQSxVQUNTQyxNQURULFVBQ1NBLE1BRFQ7O0FBRVJELGtCQUFZLGdCQUFnQkEsWUFBWSxNQUFNQSxTQUFsQixHQUE4QixFQUE5QyxDQUFaO0FBQ0FDLGVBQVMsT0FBT0EsTUFBUCxLQUFrQixRQUFsQixHQUE2QkEsU0FBUyxJQUF0QyxHQUE2QyxNQUF0RDs7QUFFQSxVQUFNQyxRQUFRO0FBQ1pDLG1CQUFXRixNQURDO0FBRVpHLG1CQUFXO0FBRkMsT0FBZDs7QUFLQSxhQUNFO0FBQUE7QUFBQSxVQUFLLFdBQVdKLFNBQWhCO0FBQ0U7QUFBQTtBQUFBLFlBQUssV0FBVSxrQkFBZixFQUFrQyxPQUFPRSxLQUF6QztBQUNHLGVBQUtILEtBQUwsQ0FBV007QUFEZDtBQURGLE9BREY7QUFPRDs7OztFQXRCc0IsZ0JBQU1DLFM7O0FBdUI5Qjs7a0JBRWNSLFUiLCJmaWxlIjoiT3ZlclNjcm9sbC5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5cbmltcG9ydCBEZWZhdWx0cyBmcm9tICcuLi9EZWZhdWx0cyc7XG5cbmNsYXNzIE92ZXJTY3JvbGwgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGxldCB7IGNsYXNzTmFtZSwgaGVpZ2h0IH0gPSB0aGlzLnByb3BzO1xuICAgIGNsYXNzTmFtZSA9ICdPdmVyU2Nyb2xsJyArIChjbGFzc05hbWUgPyAnICcgKyBjbGFzc05hbWUgOiAnJyk7XG4gICAgaGVpZ2h0ID0gdHlwZW9mIGhlaWdodCA9PT0gJ251bWJlcicgPyBoZWlnaHQgKyAncHgnIDogJ25vbmUnO1xuXG4gICAgY29uc3Qgc3R5bGUgPSB7XG4gICAgICBtYXhIZWlnaHQ6IGhlaWdodCxcbiAgICAgIG92ZXJmbG93WTogJ2F1dG8nXG4gICAgfTtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT17Y2xhc3NOYW1lfT5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJPdmVyU2Nyb2xsLUlubmVyXCIgc3R5bGU9e3N0eWxlfT5cbiAgICAgICAgICB7dGhpcy5wcm9wcy5jaGlsZHJlbn1cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59O1xuXG5leHBvcnQgZGVmYXVsdCBPdmVyU2Nyb2xsO1xuIl19