'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Icon = function (_React$PureComponent) {
  _inherits(Icon, _React$PureComponent);

  function Icon() {
    _classCallCheck(this, Icon);

    return _possibleConstructorReturn(this, (Icon.__proto__ || Object.getPrototypeOf(Icon)).apply(this, arguments));
  }

  _createClass(Icon, [{
    key: 'render',
    value: function render() {
      var _props = this.props,
          fa = _props.fa,
          className = _props.className,
          onClick = _props.onClick,
          style = _props.style;

      className = 'icon fa fa-' + fa + ' ' + (className || '');
      return _react2.default.createElement(
        'i',
        { onClick: onClick, style: style, className: className },
        ' '
      );
    }
  }]);

  return Icon;
}(_react2.default.PureComponent);

;

exports.default = Icon;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL0ljb24uanN4Il0sIm5hbWVzIjpbIkljb24iLCJwcm9wcyIsImZhIiwiY2xhc3NOYW1lIiwib25DbGljayIsInN0eWxlIiwiUHVyZUNvbXBvbmVudCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7QUFBQTs7Ozs7Ozs7Ozs7O0lBRU1BLEk7Ozs7Ozs7Ozs7OzZCQUNNO0FBQUEsbUJBQ2dDLEtBQUtDLEtBRHJDO0FBQUEsVUFDRkMsRUFERSxVQUNGQSxFQURFO0FBQUEsVUFDRUMsU0FERixVQUNFQSxTQURGO0FBQUEsVUFDYUMsT0FEYixVQUNhQSxPQURiO0FBQUEsVUFDc0JDLEtBRHRCLFVBQ3NCQSxLQUR0Qjs7QUFFWkYsa0NBQTBCRCxFQUExQixVQUFpQ0MsYUFBYSxFQUE5QztBQUNJLGFBQU87QUFBQTtBQUFBLFVBQUcsU0FBU0MsT0FBWixFQUFxQixPQUFPQyxLQUE1QixFQUFtQyxXQUFXRixTQUE5QztBQUFBO0FBQUEsT0FBUDtBQUNEOzs7O0VBTGdCLGdCQUFNRyxhOztBQU14Qjs7a0JBRWNOLEkiLCJmaWxlIjoiSWNvbi5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5cbmNsYXNzIEljb24gZXh0ZW5kcyBSZWFjdC5QdXJlQ29tcG9uZW50IHtcbiAgcmVuZGVyICgpIHtcbiAgICBsZXQgeyBmYSwgY2xhc3NOYW1lLCBvbkNsaWNrLCBzdHlsZSB9ID0gdGhpcy5wcm9wcztcbmNsYXNzTmFtZSA9IGBpY29uIGZhIGZhLSR7ZmF9ICR7KGNsYXNzTmFtZSB8fCAnJyl9YDtcbiAgICByZXR1cm4gPGkgb25DbGljaz17b25DbGlja30gc3R5bGU9e3N0eWxlfSBjbGFzc05hbWU9e2NsYXNzTmFtZX0+IDwvaT5cbiAgfVxufTtcblxuZXhwb3J0IGRlZmF1bHQgSWNvbjtcbiJdfQ==