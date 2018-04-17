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