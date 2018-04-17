'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var SelectBox = function (_React$PureComponent) {
  _inherits(SelectBox, _React$PureComponent);

  function SelectBox(props) {
    _classCallCheck(this, SelectBox);

    var _this = _possibleConstructorReturn(this, (SelectBox.__proto__ || Object.getPrototypeOf(SelectBox)).call(this, props));

    _this.handleChange = _this.handleChange.bind(_this);
    return _this;
  }

  _createClass(SelectBox, [{
    key: 'handleChange',
    value: function handleChange(e) {
      var onChange = this.props.onChange;

      var value = e.target.value;
      if (onChange) onChange(value);
    }
  }, {
    key: 'getOptions',
    value: function getOptions() {
      var options = this.props.options;

      if (!Array.isArray(options)) return [];
      options = options.map(function (option) {
        return (typeof option === 'undefined' ? 'undefined' : _typeof(option)) === 'object' && 'name' in option && 'value' in option ? option : { name: option.toString(), value: option };
      });
      return options;
    }
  }, {
    key: 'render',
    value: function render() {
      var _props = this.props,
          name = _props.name,
          className = _props.className,
          selected = _props.selected;

      var options = this.getOptions();

      return _react2.default.createElement(
        'select',
        {
          name: name,
          className: className,
          onChange: this.handleChange,
          value: selected
        },
        options.map(function (_ref) {
          var value = _ref.value,
              name = _ref.name;
          return _react2.default.createElement(
            'option',
            { key: value, value: value },
            name
          );
        })
      );
    }
  }]);

  return SelectBox;
}(_react2.default.PureComponent);

;

exports.default = SelectBox;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL1NlbGVjdEJveC5qc3giXSwibmFtZXMiOlsiU2VsZWN0Qm94IiwicHJvcHMiLCJoYW5kbGVDaGFuZ2UiLCJiaW5kIiwiZSIsIm9uQ2hhbmdlIiwidmFsdWUiLCJ0YXJnZXQiLCJvcHRpb25zIiwiQXJyYXkiLCJpc0FycmF5IiwibWFwIiwib3B0aW9uIiwibmFtZSIsInRvU3RyaW5nIiwiY2xhc3NOYW1lIiwic2VsZWN0ZWQiLCJnZXRPcHRpb25zIiwiUHVyZUNvbXBvbmVudCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7OztBQUFBOzs7Ozs7Ozs7Ozs7SUFFTUEsUzs7O0FBQ0oscUJBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSxzSEFDWkEsS0FEWTs7QUFFbEIsVUFBS0MsWUFBTCxHQUFvQixNQUFLQSxZQUFMLENBQWtCQyxJQUFsQixPQUFwQjtBQUZrQjtBQUduQjs7OztpQ0FFYUMsQyxFQUFHO0FBQUEsVUFDUEMsUUFETyxHQUNNLEtBQUtKLEtBRFgsQ0FDUEksUUFETzs7QUFFZixVQUFNQyxRQUFRRixFQUFFRyxNQUFGLENBQVNELEtBQXZCO0FBQ0EsVUFBSUQsUUFBSixFQUFjQSxTQUFTQyxLQUFUO0FBQ2Y7OztpQ0FFYTtBQUFBLFVBQ05FLE9BRE0sR0FDTSxLQUFLUCxLQURYLENBQ05PLE9BRE07O0FBRVosVUFBSSxDQUFDQyxNQUFNQyxPQUFOLENBQWNGLE9BQWQsQ0FBTCxFQUE2QixPQUFPLEVBQVA7QUFDN0JBLGdCQUFVQSxRQUFRRyxHQUFSLENBQVksa0JBQVU7QUFDOUIsZUFBUSxRQUFPQyxNQUFQLHlDQUFPQSxNQUFQLE9BQWtCLFFBQWxCLElBQThCLFVBQVVBLE1BQXhDLElBQWtELFdBQVdBLE1BQTlELEdBQ0hBLE1BREcsR0FFSCxFQUFFQyxNQUFNRCxPQUFPRSxRQUFQLEVBQVIsRUFBMkJSLE9BQU9NLE1BQWxDLEVBRko7QUFHRCxPQUpTLENBQVY7QUFLQSxhQUFPSixPQUFQO0FBQ0Q7Ozs2QkFFUztBQUFBLG1CQUM4QixLQUFLUCxLQURuQztBQUFBLFVBQ0FZLElBREEsVUFDQUEsSUFEQTtBQUFBLFVBQ01FLFNBRE4sVUFDTUEsU0FETjtBQUFBLFVBQ2lCQyxRQURqQixVQUNpQkEsUUFEakI7O0FBRVIsVUFBSVIsVUFBVSxLQUFLUyxVQUFMLEVBQWQ7O0FBRUEsYUFDRTtBQUFBO0FBQUE7QUFDRSxnQkFBTUosSUFEUjtBQUVFLHFCQUFXRSxTQUZiO0FBR0Usb0JBQVUsS0FBS2IsWUFIakI7QUFJRSxpQkFBT2M7QUFKVDtBQU1HUixnQkFBUUcsR0FBUixDQUFZO0FBQUEsY0FBR0wsS0FBSCxRQUFHQSxLQUFIO0FBQUEsY0FBVU8sSUFBVixRQUFVQSxJQUFWO0FBQUEsaUJBQ1g7QUFBQTtBQUFBLGNBQVEsS0FBS1AsS0FBYixFQUFvQixPQUFPQSxLQUEzQjtBQUNHTztBQURILFdBRFc7QUFBQSxTQUFaO0FBTkgsT0FERjtBQWNEOzs7O0VBekNxQixnQkFBTUssYTs7QUEwQzdCOztrQkFFY2xCLFMiLCJmaWxlIjoiU2VsZWN0Qm94LmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcblxuY2xhc3MgU2VsZWN0Qm94IGV4dGVuZHMgUmVhY3QuUHVyZUNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgICB0aGlzLmhhbmRsZUNoYW5nZSA9IHRoaXMuaGFuZGxlQ2hhbmdlLmJpbmQodGhpcyk7XG4gIH1cblxuICBoYW5kbGVDaGFuZ2UgKGUpIHtcbiAgICBjb25zdCB7IG9uQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHZhbHVlID0gZS50YXJnZXQudmFsdWU7XG4gICAgaWYgKG9uQ2hhbmdlKSBvbkNoYW5nZSh2YWx1ZSk7XG4gIH1cblxuICBnZXRPcHRpb25zICgpIHtcbiAgICBsZXQgeyBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuICAgIGlmICghQXJyYXkuaXNBcnJheShvcHRpb25zKSkgcmV0dXJuIFtdO1xuICAgIG9wdGlvbnMgPSBvcHRpb25zLm1hcChvcHRpb24gPT4ge1xuICAgICAgcmV0dXJuICh0eXBlb2Ygb3B0aW9uID09PSAnb2JqZWN0JyAmJiAnbmFtZScgaW4gb3B0aW9uICYmICd2YWx1ZScgaW4gb3B0aW9uKVxuICAgICAgICA/IG9wdGlvblxuICAgICAgICA6IHsgbmFtZTogb3B0aW9uLnRvU3RyaW5nKCksIHZhbHVlOiBvcHRpb24gfTtcbiAgICB9KTtcbiAgICByZXR1cm4gb3B0aW9ucztcbiAgfVxuXG4gIHJlbmRlciAoKSB7XG4gICAgY29uc3QgeyBuYW1lLCBjbGFzc05hbWUsIHNlbGVjdGVkIH0gPSB0aGlzLnByb3BzO1xuICAgIGxldCBvcHRpb25zID0gdGhpcy5nZXRPcHRpb25zKCk7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPHNlbGVjdFxuICAgICAgICBuYW1lPXtuYW1lfVxuICAgICAgICBjbGFzc05hbWU9e2NsYXNzTmFtZX1cbiAgICAgICAgb25DaGFuZ2U9e3RoaXMuaGFuZGxlQ2hhbmdlfVxuICAgICAgICB2YWx1ZT17c2VsZWN0ZWR9XG4gICAgICA+XG4gICAgICAgIHtvcHRpb25zLm1hcCgoeyB2YWx1ZSwgbmFtZSB9KSA9PiAoXG4gICAgICAgICAgPG9wdGlvbiBrZXk9e3ZhbHVlfSB2YWx1ZT17dmFsdWV9PlxuICAgICAgICAgICAge25hbWV9XG4gICAgICAgICAgPC9vcHRpb24+XG4gICAgICAgICkpfVxuICAgICAgPC9zZWxlY3Q+XG4gICAgKTtcbiAgfVxufTtcblxuZXhwb3J0IGRlZmF1bHQgU2VsZWN0Qm94O1xuIl19