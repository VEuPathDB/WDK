'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _MesaController = require('../Ui/MesaController');

var _MesaController2 = _interopRequireDefault(_MesaController);

var _MesaState = require('../Utils/MesaState');

var _MesaState2 = _interopRequireDefault(_MesaState);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Mesa = function (_React$Component) {
  _inherits(Mesa, _React$Component);

  function Mesa(props) {
    _classCallCheck(this, Mesa);

    return _possibleConstructorReturn(this, (Mesa.__proto__ || Object.getPrototypeOf(Mesa)).call(this, props));
  }

  _createClass(Mesa, [{
    key: 'render',
    value: function render() {
      var _props = this.props,
          state = _props.state,
          children = _props.children;

      return _react2.default.createElement(
        _MesaController2.default,
        state,
        children
      );
    }
  }]);

  return Mesa;
}(_react2.default.Component);

;

exports.default = Mesa;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9NZXNhLmpzeCJdLCJuYW1lcyI6WyJNZXNhIiwicHJvcHMiLCJzdGF0ZSIsImNoaWxkcmVuIiwiQ29tcG9uZW50Il0sIm1hcHBpbmdzIjoiOzs7Ozs7OztBQUFBOzs7O0FBRUE7Ozs7QUFDQTs7Ozs7Ozs7Ozs7O0lBRU1BLEk7OztBQUNKLGdCQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsdUdBQ1pBLEtBRFk7QUFFbkI7Ozs7NkJBRVM7QUFBQSxtQkFDb0IsS0FBS0EsS0FEekI7QUFBQSxVQUNBQyxLQURBLFVBQ0FBLEtBREE7QUFBQSxVQUNPQyxRQURQLFVBQ09BLFFBRFA7O0FBRVIsYUFBTztBQUFBO0FBQW9CRCxhQUFwQjtBQUE0QkM7QUFBNUIsT0FBUDtBQUNEOzs7O0VBUmdCLGdCQUFNQyxTOztBQVN4Qjs7a0JBRWNKLEkiLCJmaWxlIjoiTWVzYS5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5cbmltcG9ydCBNZXNhQ29udHJvbGxlciBmcm9tICcuLi9VaS9NZXNhQ29udHJvbGxlcic7XG5pbXBvcnQgTWVzYVN0YXRlIGZyb20gJy4uL1V0aWxzL01lc2FTdGF0ZSc7XG5cbmNsYXNzIE1lc2EgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgc3RhdGUsIGNoaWxkcmVuIH0gPSB0aGlzLnByb3BzO1xuICAgIHJldHVybiA8TWVzYUNvbnRyb2xsZXIgey4uLnN0YXRlfT57Y2hpbGRyZW59PC9NZXNhQ29udHJvbGxlcj5cbiAgfVxufTtcblxuZXhwb3J0IGRlZmF1bHQgTWVzYTtcbiJdfQ==