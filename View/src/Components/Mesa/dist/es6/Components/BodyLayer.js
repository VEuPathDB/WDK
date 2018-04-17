'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _reactDom = require('react-dom');

var _reactDom2 = _interopRequireDefault(_reactDom);

var _Utils = require('../Utils/Utils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var BodyLayer = function (_React$Component) {
  _inherits(BodyLayer, _React$Component);

  function BodyLayer(props) {
    _classCallCheck(this, BodyLayer);

    var _this = _possibleConstructorReturn(this, (BodyLayer.__proto__ || Object.getPrototypeOf(BodyLayer)).call(this, props));

    _this.id = '_BodyLayer_' + (0, _Utils.uid)();
    _this.parentElement = null;
    return _this;
  }

  _createClass(BodyLayer, [{
    key: 'render',
    value: function render() {
      return null;
    }
  }, {
    key: 'componentDidMount',
    value: function componentDidMount() {
      var element = document.getElementById(this.id);
      if (!element) {
        element = document.createElement('div');
        element.id = this.id;
        document.body.appendChild(element);
      }
      this.parentElement = element;
      this.componentDidUpdate();
    }
  }, {
    key: 'componentWillUnmount',
    value: function componentWillUnmount() {
      document.body.removeChild(this.parentElement);
    }
  }, {
    key: 'componentDidUpdate',
    value: function componentDidUpdate() {
      var props = this.props;

      _reactDom2.default.render(_react2.default.createElement('div', _extends({ className: 'BodyLayer' }, props)), this.parentElement);
    }
  }]);

  return BodyLayer;
}(_react2.default.Component);

;

exports.default = BodyLayer;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL0JvZHlMYXllci5qc3giXSwibmFtZXMiOlsiQm9keUxheWVyIiwicHJvcHMiLCJpZCIsInBhcmVudEVsZW1lbnQiLCJlbGVtZW50IiwiZG9jdW1lbnQiLCJnZXRFbGVtZW50QnlJZCIsImNyZWF0ZUVsZW1lbnQiLCJib2R5IiwiYXBwZW5kQ2hpbGQiLCJjb21wb25lbnREaWRVcGRhdGUiLCJyZW1vdmVDaGlsZCIsInJlbmRlciIsIkNvbXBvbmVudCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7OztBQUFBOzs7O0FBQ0E7Ozs7QUFFQTs7Ozs7Ozs7OztJQUVNQSxTOzs7QUFDSixxQkFBYUMsS0FBYixFQUFvQjtBQUFBOztBQUFBLHNIQUNaQSxLQURZOztBQUVsQixVQUFLQyxFQUFMLEdBQVUsZ0JBQWdCLGlCQUExQjtBQUNBLFVBQUtDLGFBQUwsR0FBcUIsSUFBckI7QUFIa0I7QUFJbkI7Ozs7NkJBRVM7QUFDUixhQUFPLElBQVA7QUFDRDs7O3dDQUVvQjtBQUNuQixVQUFJQyxVQUFVQyxTQUFTQyxjQUFULENBQXdCLEtBQUtKLEVBQTdCLENBQWQ7QUFDQSxVQUFJLENBQUNFLE9BQUwsRUFBYztBQUNaQSxrQkFBVUMsU0FBU0UsYUFBVCxDQUF1QixLQUF2QixDQUFWO0FBQ0FILGdCQUFRRixFQUFSLEdBQWEsS0FBS0EsRUFBbEI7QUFDQUcsaUJBQVNHLElBQVQsQ0FBY0MsV0FBZCxDQUEwQkwsT0FBMUI7QUFDRDtBQUNELFdBQUtELGFBQUwsR0FBcUJDLE9BQXJCO0FBQ0EsV0FBS00sa0JBQUw7QUFDRDs7OzJDQUV1QjtBQUN0QkwsZUFBU0csSUFBVCxDQUFjRyxXQUFkLENBQTBCLEtBQUtSLGFBQS9CO0FBQ0Q7Ozt5Q0FFcUI7QUFBQSxVQUNaRixLQURZLEdBQ0YsSUFERSxDQUNaQSxLQURZOztBQUVwQix5QkFBU1csTUFBVCxDQUFnQixnREFBSyxXQUFVLFdBQWYsSUFBK0JYLEtBQS9CLEVBQWhCLEVBQTBELEtBQUtFLGFBQS9EO0FBQ0Q7Ozs7RUE3QnFCLGdCQUFNVSxTOztBQThCN0I7O2tCQUVjYixTIiwiZmlsZSI6IkJvZHlMYXllci5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5pbXBvcnQgUmVhY3RET00gZnJvbSAncmVhY3QtZG9tJztcblxuaW1wb3J0IHsgdWlkIH0gZnJvbSAnLi4vVXRpbHMvVXRpbHMnO1xuXG5jbGFzcyBCb2R5TGF5ZXIgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG4gICAgdGhpcy5pZCA9ICdfQm9keUxheWVyXycgKyB1aWQoKTtcbiAgICB0aGlzLnBhcmVudEVsZW1lbnQgPSBudWxsO1xuICB9XG5cbiAgcmVuZGVyICgpIHtcbiAgICByZXR1cm4gbnVsbDtcbiAgfVxuXG4gIGNvbXBvbmVudERpZE1vdW50ICgpIHtcbiAgICBsZXQgZWxlbWVudCA9IGRvY3VtZW50LmdldEVsZW1lbnRCeUlkKHRoaXMuaWQpO1xuICAgIGlmICghZWxlbWVudCkge1xuICAgICAgZWxlbWVudCA9IGRvY3VtZW50LmNyZWF0ZUVsZW1lbnQoJ2RpdicpO1xuICAgICAgZWxlbWVudC5pZCA9IHRoaXMuaWQ7XG4gICAgICBkb2N1bWVudC5ib2R5LmFwcGVuZENoaWxkKGVsZW1lbnQpO1xuICAgIH1cbiAgICB0aGlzLnBhcmVudEVsZW1lbnQgPSBlbGVtZW50O1xuICAgIHRoaXMuY29tcG9uZW50RGlkVXBkYXRlKCk7XG4gIH1cblxuICBjb21wb25lbnRXaWxsVW5tb3VudCAoKSB7XG4gICAgZG9jdW1lbnQuYm9keS5yZW1vdmVDaGlsZCh0aGlzLnBhcmVudEVsZW1lbnQpO1xuICB9XG5cbiAgY29tcG9uZW50RGlkVXBkYXRlICgpIHtcbiAgICBjb25zdCB7IHByb3BzIH0gPSB0aGlzO1xuICAgIFJlYWN0RE9NLnJlbmRlcig8ZGl2IGNsYXNzTmFtZT1cIkJvZHlMYXllclwiIHsuLi5wcm9wc30gLz4sIHRoaXMucGFyZW50RWxlbWVudCk7XG4gIH1cbn07XG5cbmV4cG9ydCBkZWZhdWx0IEJvZHlMYXllcjtcbiJdfQ==