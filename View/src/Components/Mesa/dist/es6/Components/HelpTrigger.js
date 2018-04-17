'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Icon = require('./Icon');

var _Icon2 = _interopRequireDefault(_Icon);

var _AnchoredTooltip = require('./AnchoredTooltip');

var _AnchoredTooltip2 = _interopRequireDefault(_AnchoredTooltip);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var HelpTrigger = function (_React$Component) {
  _inherits(HelpTrigger, _React$Component);

  function HelpTrigger(props) {
    _classCallCheck(this, HelpTrigger);

    return _possibleConstructorReturn(this, (HelpTrigger.__proto__ || Object.getPrototypeOf(HelpTrigger)).call(this, props));
  }

  _createClass(HelpTrigger, [{
    key: 'render',
    value: function render() {
      var props = this.props;

      var content = props.children;
      var className = 'Trigger HelpTrigger' + (props.className ? ' ' + props.className : '');
      var children = _react2.default.createElement(_Icon2.default, { fa: 'question-circle' });
      var newProps = _extends({}, props, { content: content, children: children, className: className });
      return _react2.default.createElement(_AnchoredTooltip2.default, newProps);
    }
  }]);

  return HelpTrigger;
}(_react2.default.Component);

;

exports.default = HelpTrigger;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL0hlbHBUcmlnZ2VyLmpzeCJdLCJuYW1lcyI6WyJIZWxwVHJpZ2dlciIsInByb3BzIiwiY29udGVudCIsImNoaWxkcmVuIiwiY2xhc3NOYW1lIiwibmV3UHJvcHMiLCJDb21wb25lbnQiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7O0FBQ0E7Ozs7Ozs7Ozs7OztJQUVNQSxXOzs7QUFDSix1QkFBYUMsS0FBYixFQUFvQjtBQUFBOztBQUFBLHFIQUNaQSxLQURZO0FBRW5COzs7OzZCQUVTO0FBQUEsVUFDQUEsS0FEQSxHQUNVLElBRFYsQ0FDQUEsS0FEQTs7QUFFUixVQUFNQyxVQUFVRCxNQUFNRSxRQUF0QjtBQUNBLFVBQU1DLFlBQVkseUJBQXlCSCxNQUFNRyxTQUFOLEdBQWtCLE1BQU1ILE1BQU1HLFNBQTlCLEdBQTBDLEVBQW5FLENBQWxCO0FBQ0EsVUFBTUQsV0FBVyxnREFBTSxJQUFHLGlCQUFULEdBQWpCO0FBQ0EsVUFBTUUsd0JBQWdCSixLQUFoQixJQUF1QkMsZ0JBQXZCLEVBQWdDQyxrQkFBaEMsRUFBMENDLG9CQUExQyxHQUFOO0FBQ0EsYUFBTyx5REFBcUJDLFFBQXJCLENBQVA7QUFDRDs7OztFQVp1QixnQkFBTUMsUzs7QUFhL0I7O2tCQUVjTixXIiwiZmlsZSI6IkhlbHBUcmlnZ2VyLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcbmltcG9ydCBJY29uIGZyb20gJy4vSWNvbic7XG5pbXBvcnQgQW5jaG9yZWRUb29sdGlwIGZyb20gJy4vQW5jaG9yZWRUb29sdGlwJztcblxuY2xhc3MgSGVscFRyaWdnZXIgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgcHJvcHMgfSA9IHRoaXM7XG4gICAgY29uc3QgY29udGVudCA9IHByb3BzLmNoaWxkcmVuO1xuICAgIGNvbnN0IGNsYXNzTmFtZSA9ICdUcmlnZ2VyIEhlbHBUcmlnZ2VyJyArIChwcm9wcy5jbGFzc05hbWUgPyAnICcgKyBwcm9wcy5jbGFzc05hbWUgOiAnJyk7XG4gICAgY29uc3QgY2hpbGRyZW4gPSA8SWNvbiBmYT1cInF1ZXN0aW9uLWNpcmNsZVwiIC8+O1xuICAgIGNvbnN0IG5ld1Byb3BzID0geyAuLi5wcm9wcywgY29udGVudCwgY2hpbGRyZW4sIGNsYXNzTmFtZSB9O1xuICAgIHJldHVybiA8QW5jaG9yZWRUb29sdGlwIHsuLi5uZXdQcm9wc30gLz47XG4gIH1cbn07XG5cbmV4cG9ydCBkZWZhdWx0IEhlbHBUcmlnZ2VyO1xuIl19