"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var RowCounter = function (_React$PureComponent) {
  _inherits(RowCounter, _React$PureComponent);

  function RowCounter(props) {
    _classCallCheck(this, RowCounter);

    return _possibleConstructorReturn(this, (RowCounter.__proto__ || Object.getPrototypeOf(RowCounter)).call(this, props));
  }

  _createClass(RowCounter, [{
    key: "render",
    value: function render() {
      var _props = this.props,
          count = _props.count,
          noun = _props.noun,
          filtered = _props.filtered,
          start = _props.start,
          end = _props.end;

      var filterString = !filtered ? null : _react2.default.createElement(
        "span",
        { className: "faded" },
        " (",
        filtered,
        " filtered)"
      );
      var countString = _react2.default.createElement(
        "span",
        null,
        _react2.default.createElement(
          "b",
          null,
          count
        ),
        " ",
        noun
      );
      var allResultsShown = !start || !end || start === 1 && end === count;

      if (!allResultsShown) {
        countString = _react2.default.createElement(
          "span",
          null,
          noun,
          " ",
          _react2.default.createElement(
            "b",
            null,
            start
          ),
          " - ",
          _react2.default.createElement(
            "b",
            null,
            end
          ),
          " of ",
          _react2.default.createElement(
            "b",
            null,
            count
          )
        );
      }

      return _react2.default.createElement(
        "div",
        { className: "RowCounter" },
        countString,
        filterString
      );
    }
  }]);

  return RowCounter;
}(_react2.default.PureComponent);

;

exports.default = RowCounter;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9Sb3dDb3VudGVyLmpzeCJdLCJuYW1lcyI6WyJSb3dDb3VudGVyIiwicHJvcHMiLCJjb3VudCIsIm5vdW4iLCJmaWx0ZXJlZCIsInN0YXJ0IiwiZW5kIiwiZmlsdGVyU3RyaW5nIiwiY291bnRTdHJpbmciLCJhbGxSZXN1bHRzU2hvd24iLCJQdXJlQ29tcG9uZW50Il0sIm1hcHBpbmdzIjoiOzs7Ozs7OztBQUFBOzs7Ozs7Ozs7Ozs7SUFFTUEsVTs7O0FBQ0osc0JBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSxtSEFDWkEsS0FEWTtBQUVuQjs7Ozs2QkFFUztBQUFBLG1CQUNvQyxLQUFLQSxLQUR6QztBQUFBLFVBQ0ZDLEtBREUsVUFDRkEsS0FERTtBQUFBLFVBQ0tDLElBREwsVUFDS0EsSUFETDtBQUFBLFVBQ1dDLFFBRFgsVUFDV0EsUUFEWDtBQUFBLFVBQ3FCQyxLQURyQixVQUNxQkEsS0FEckI7QUFBQSxVQUM0QkMsR0FENUIsVUFDNEJBLEdBRDVCOztBQUVSLFVBQUlDLGVBQWUsQ0FBQ0gsUUFBRCxHQUFZLElBQVosR0FBbUI7QUFBQTtBQUFBLFVBQU0sV0FBVSxPQUFoQjtBQUFBO0FBQTJCQSxnQkFBM0I7QUFBQTtBQUFBLE9BQXRDO0FBQ0EsVUFBSUksY0FBZTtBQUFBO0FBQUE7QUFBTTtBQUFBO0FBQUE7QUFBSU47QUFBSixTQUFOO0FBQUE7QUFBc0JDO0FBQXRCLE9BQW5CO0FBQ0EsVUFBSU0sa0JBQW1CLENBQUNKLEtBQUQsSUFBVSxDQUFDQyxHQUFYLElBQW1CRCxVQUFVLENBQVYsSUFBZUMsUUFBUUosS0FBakU7O0FBRUEsVUFBSSxDQUFDTyxlQUFMLEVBQXNCO0FBQ3BCRCxzQkFDRTtBQUFBO0FBQUE7QUFDR0wsY0FESDtBQUFBO0FBQ1M7QUFBQTtBQUFBO0FBQUlFO0FBQUosV0FEVDtBQUFBO0FBQzBCO0FBQUE7QUFBQTtBQUFJQztBQUFKLFdBRDFCO0FBQUE7QUFDMEM7QUFBQTtBQUFBO0FBQUlKO0FBQUo7QUFEMUMsU0FERjtBQUtEOztBQUVELGFBQ0U7QUFBQTtBQUFBLFVBQUssV0FBVSxZQUFmO0FBQ0dNLG1CQURIO0FBRUdEO0FBRkgsT0FERjtBQU1EOzs7O0VBekJzQixnQkFBTUcsYTs7QUEwQjlCOztrQkFFY1YsVSIsImZpbGUiOiJSb3dDb3VudGVyLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcblxuY2xhc3MgUm93Q291bnRlciBleHRlbmRzIFJlYWN0LlB1cmVDb21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGxldCB7IGNvdW50LCBub3VuLCBmaWx0ZXJlZCwgc3RhcnQsIGVuZCB9ID0gdGhpcy5wcm9wcztcbiAgICBsZXQgZmlsdGVyU3RyaW5nID0gIWZpbHRlcmVkID8gbnVsbCA6IDxzcGFuIGNsYXNzTmFtZT1cImZhZGVkXCI+ICh7ZmlsdGVyZWR9IGZpbHRlcmVkKTwvc3Bhbj47XG4gICAgbGV0IGNvdW50U3RyaW5nID0gKDxzcGFuPjxiPntjb3VudH08L2I+IHtub3VufTwvc3Bhbj4pO1xuICAgIGxldCBhbGxSZXN1bHRzU2hvd24gPSAoIXN0YXJ0IHx8ICFlbmQgfHwgKHN0YXJ0ID09PSAxICYmIGVuZCA9PT0gY291bnQpKTtcblxuICAgIGlmICghYWxsUmVzdWx0c1Nob3duKSB7XG4gICAgICBjb3VudFN0cmluZyA9IChcbiAgICAgICAgPHNwYW4+XG4gICAgICAgICAge25vdW59IDxiPntzdGFydH08L2I+IC0gPGI+e2VuZH08L2I+IG9mIDxiPntjb3VudH08L2I+XG4gICAgICAgIDwvc3Bhbj5cbiAgICAgICk7XG4gICAgfVxuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiUm93Q291bnRlclwiPlxuICAgICAgICB7Y291bnRTdHJpbmd9XG4gICAgICAgIHtmaWx0ZXJTdHJpbmd9XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59O1xuXG5leHBvcnQgZGVmYXVsdCBSb3dDb3VudGVyO1xuIl19