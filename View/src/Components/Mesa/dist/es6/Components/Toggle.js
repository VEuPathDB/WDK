'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Toggle = function (_React$Component) {
  _inherits(Toggle, _React$Component);

  function Toggle(props) {
    _classCallCheck(this, Toggle);

    var _this = _possibleConstructorReturn(this, (Toggle.__proto__ || Object.getPrototypeOf(Toggle)).call(this, props));

    _this.handleClick = _this.handleClick.bind(_this);
    return _this;
  }

  _createClass(Toggle, [{
    key: 'handleClick',
    value: function handleClick(e) {
      var _props = this.props,
          enabled = _props.enabled,
          onChange = _props.onChange;

      if (typeof onChange === 'function') onChange(!!enabled);
    }
  }, {
    key: 'render',
    value: function render() {
      var _props2 = this.props,
          enabled = _props2.enabled,
          className = _props2.className,
          disabled = _props2.disabled,
          style = _props2.style;

      className = 'Toggle' + (className ? ' ' + className : '');
      className += ' ' + (enabled ? 'Toggle-On' : 'Toggle-Off');
      className += disabled ? ' Toggle-Disabled' : '';
      var offStyle = {
        fontSize: '1.2rem',
        color: '#989898'
      };
      var onStyle = Object.assign({}, offStyle, {
        color: '#198835'
      });

      return _react2.default.createElement(
        'span',
        {
          style: style,
          className: className,
          onClick: disabled ? null : this.handleClick
        },
        _react2.default.createElement(_Icon2.default, {
          fa: enabled ? 'toggle-on' : 'toggle-off',
          style: enabled ? onStyle : offStyle
        })
      );
    }
  }]);

  return Toggle;
}(_react2.default.Component);

;

exports.default = Toggle;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL1RvZ2dsZS5qc3giXSwibmFtZXMiOlsiVG9nZ2xlIiwicHJvcHMiLCJoYW5kbGVDbGljayIsImJpbmQiLCJlIiwiZW5hYmxlZCIsIm9uQ2hhbmdlIiwiY2xhc3NOYW1lIiwiZGlzYWJsZWQiLCJzdHlsZSIsIm9mZlN0eWxlIiwiZm9udFNpemUiLCJjb2xvciIsIm9uU3R5bGUiLCJPYmplY3QiLCJhc3NpZ24iLCJDb21wb25lbnQiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7O0FBQUE7Ozs7QUFFQTs7Ozs7Ozs7Ozs7O0lBRU1BLE07OztBQUNKLGtCQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsZ0hBQ1pBLEtBRFk7O0FBRWxCLFVBQUtDLFdBQUwsR0FBbUIsTUFBS0EsV0FBTCxDQUFpQkMsSUFBakIsT0FBbkI7QUFGa0I7QUFHbkI7Ozs7Z0NBRVlDLEMsRUFBRztBQUFBLG1CQUNjLEtBQUtILEtBRG5CO0FBQUEsVUFDUkksT0FEUSxVQUNSQSxPQURRO0FBQUEsVUFDQ0MsUUFERCxVQUNDQSxRQUREOztBQUVkLFVBQUksT0FBT0EsUUFBUCxLQUFvQixVQUF4QixFQUFvQ0EsU0FBUyxDQUFDLENBQUNELE9BQVg7QUFDckM7Ozs2QkFFUztBQUFBLG9CQUNzQyxLQUFLSixLQUQzQztBQUFBLFVBQ0ZJLE9BREUsV0FDRkEsT0FERTtBQUFBLFVBQ09FLFNBRFAsV0FDT0EsU0FEUDtBQUFBLFVBQ2tCQyxRQURsQixXQUNrQkEsUUFEbEI7QUFBQSxVQUM0QkMsS0FENUIsV0FDNEJBLEtBRDVCOztBQUVSRixrQkFBWSxZQUFZQSxZQUFZLE1BQU1BLFNBQWxCLEdBQThCLEVBQTFDLENBQVo7QUFDQUEsbUJBQWEsT0FBT0YsVUFBVSxXQUFWLEdBQXdCLFlBQS9CLENBQWI7QUFDQUUsbUJBQWFDLFdBQVcsa0JBQVgsR0FBZ0MsRUFBN0M7QUFDQSxVQUFJRSxXQUFXO0FBQ2JDLGtCQUFVLFFBREc7QUFFYkMsZUFBTztBQUZNLE9BQWY7QUFJQSxVQUFJQyxVQUFVQyxPQUFPQyxNQUFQLENBQWMsRUFBZCxFQUFrQkwsUUFBbEIsRUFBNEI7QUFDeENFLGVBQU87QUFEaUMsT0FBNUIsQ0FBZDs7QUFJQSxhQUNFO0FBQUE7QUFBQTtBQUNFLGlCQUFPSCxLQURUO0FBRUUscUJBQVdGLFNBRmI7QUFHRSxtQkFBU0MsV0FBVyxJQUFYLEdBQWtCLEtBQUtOO0FBSGxDO0FBS0U7QUFDRSxjQUFJRyxVQUFVLFdBQVYsR0FBd0IsWUFEOUI7QUFFRSxpQkFBT0EsVUFBVVEsT0FBVixHQUFvQkg7QUFGN0I7QUFMRixPQURGO0FBWUQ7Ozs7RUFwQ2tCLGdCQUFNTSxTOztBQXFDMUI7O2tCQUVjaEIsTSIsImZpbGUiOiJUb2dnbGUuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuXG5pbXBvcnQgSWNvbiBmcm9tICcuLi9Db21wb25lbnRzL0ljb24nO1xuXG5jbGFzcyBUb2dnbGUgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG4gICAgdGhpcy5oYW5kbGVDbGljayA9IHRoaXMuaGFuZGxlQ2xpY2suYmluZCh0aGlzKTtcbiAgfVxuXG4gIGhhbmRsZUNsaWNrIChlKSB7XG4gICAgbGV0IHsgZW5hYmxlZCwgb25DaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG4gICAgaWYgKHR5cGVvZiBvbkNoYW5nZSA9PT0gJ2Z1bmN0aW9uJykgb25DaGFuZ2UoISFlbmFibGVkKTtcbiAgfVxuXG4gIHJlbmRlciAoKSB7XG4gICAgbGV0IHsgZW5hYmxlZCwgY2xhc3NOYW1lLCBkaXNhYmxlZCwgc3R5bGUgfSA9IHRoaXMucHJvcHM7XG4gICAgY2xhc3NOYW1lID0gJ1RvZ2dsZScgKyAoY2xhc3NOYW1lID8gJyAnICsgY2xhc3NOYW1lIDogJycpO1xuICAgIGNsYXNzTmFtZSArPSAnICcgKyAoZW5hYmxlZCA/ICdUb2dnbGUtT24nIDogJ1RvZ2dsZS1PZmYnKTtcbiAgICBjbGFzc05hbWUgKz0gZGlzYWJsZWQgPyAnIFRvZ2dsZS1EaXNhYmxlZCcgOiAnJztcbiAgICBsZXQgb2ZmU3R5bGUgPSB7XG4gICAgICBmb250U2l6ZTogJzEuMnJlbScsXG4gICAgICBjb2xvcjogJyM5ODk4OTgnXG4gICAgfTtcbiAgICBsZXQgb25TdHlsZSA9IE9iamVjdC5hc3NpZ24oe30sIG9mZlN0eWxlLCB7XG4gICAgICBjb2xvcjogJyMxOTg4MzUnXG4gICAgfSk7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPHNwYW5cbiAgICAgICAgc3R5bGU9e3N0eWxlfVxuICAgICAgICBjbGFzc05hbWU9e2NsYXNzTmFtZX1cbiAgICAgICAgb25DbGljaz17ZGlzYWJsZWQgPyBudWxsIDogdGhpcy5oYW5kbGVDbGlja31cbiAgICAgID5cbiAgICAgICAgPEljb25cbiAgICAgICAgICBmYT17ZW5hYmxlZCA/ICd0b2dnbGUtb24nIDogJ3RvZ2dsZS1vZmYnfVxuICAgICAgICAgIHN0eWxlPXtlbmFibGVkID8gb25TdHlsZSA6IG9mZlN0eWxlfVxuICAgICAgICAvPlxuICAgICAgPC9zcGFuPlxuICAgICk7XG4gIH1cbn07XG5cbmV4cG9ydCBkZWZhdWx0IFRvZ2dsZTtcbiJdfQ==