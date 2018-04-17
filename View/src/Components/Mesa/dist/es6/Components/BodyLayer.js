'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

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
        element.className = '_BodyLayer';
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

      _reactDom2.default.render(_react2.default.createElement('div', props), this.parentElement);
    }
  }]);

  return BodyLayer;
}(_react2.default.Component);

;

exports.default = BodyLayer;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL0JvZHlMYXllci5qc3giXSwibmFtZXMiOlsiQm9keUxheWVyIiwicHJvcHMiLCJpZCIsInBhcmVudEVsZW1lbnQiLCJlbGVtZW50IiwiZG9jdW1lbnQiLCJnZXRFbGVtZW50QnlJZCIsImNyZWF0ZUVsZW1lbnQiLCJjbGFzc05hbWUiLCJib2R5IiwiYXBwZW5kQ2hpbGQiLCJjb21wb25lbnREaWRVcGRhdGUiLCJyZW1vdmVDaGlsZCIsInJlbmRlciIsIkNvbXBvbmVudCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7O0FBRUE7Ozs7Ozs7Ozs7SUFFTUEsUzs7O0FBQ0oscUJBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSxzSEFDWkEsS0FEWTs7QUFFbEIsVUFBS0MsRUFBTCxHQUFVLGdCQUFnQixpQkFBMUI7QUFDQSxVQUFLQyxhQUFMLEdBQXFCLElBQXJCO0FBSGtCO0FBSW5COzs7OzZCQUVTO0FBQ1IsYUFBTyxJQUFQO0FBQ0Q7Ozt3Q0FFb0I7QUFDbkIsVUFBSUMsVUFBVUMsU0FBU0MsY0FBVCxDQUF3QixLQUFLSixFQUE3QixDQUFkO0FBQ0EsVUFBSSxDQUFDRSxPQUFMLEVBQWM7QUFDWkEsa0JBQVVDLFNBQVNFLGFBQVQsQ0FBdUIsS0FBdkIsQ0FBVjtBQUNBSCxnQkFBUUYsRUFBUixHQUFhLEtBQUtBLEVBQWxCO0FBQ0FFLGdCQUFRSSxTQUFSLEdBQW9CLFlBQXBCO0FBQ0FILGlCQUFTSSxJQUFULENBQWNDLFdBQWQsQ0FBMEJOLE9BQTFCO0FBQ0Q7QUFDRCxXQUFLRCxhQUFMLEdBQXFCQyxPQUFyQjtBQUNBLFdBQUtPLGtCQUFMO0FBQ0Q7OzsyQ0FFdUI7QUFDdEJOLGVBQVNJLElBQVQsQ0FBY0csV0FBZCxDQUEwQixLQUFLVCxhQUEvQjtBQUNEOzs7eUNBRXFCO0FBQUEsVUFDWkYsS0FEWSxHQUNGLElBREUsQ0FDWkEsS0FEWTs7QUFFcEIseUJBQVNZLE1BQVQsQ0FBZ0IscUNBQVNaLEtBQVQsQ0FBaEIsRUFBb0MsS0FBS0UsYUFBekM7QUFDRDs7OztFQTlCcUIsZ0JBQU1XLFM7O0FBK0I3Qjs7a0JBRWNkLFMiLCJmaWxlIjoiQm9keUxheWVyLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcbmltcG9ydCBSZWFjdERPTSBmcm9tICdyZWFjdC1kb20nO1xuXG5pbXBvcnQgeyB1aWQgfSBmcm9tICcuLi9VdGlscy9VdGlscyc7XG5cbmNsYXNzIEJvZHlMYXllciBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgICB0aGlzLmlkID0gJ19Cb2R5TGF5ZXJfJyArIHVpZCgpO1xuICAgIHRoaXMucGFyZW50RWxlbWVudCA9IG51bGw7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIHJldHVybiBudWxsO1xuICB9XG5cbiAgY29tcG9uZW50RGlkTW91bnQgKCkge1xuICAgIGxldCBlbGVtZW50ID0gZG9jdW1lbnQuZ2V0RWxlbWVudEJ5SWQodGhpcy5pZCk7XG4gICAgaWYgKCFlbGVtZW50KSB7XG4gICAgICBlbGVtZW50ID0gZG9jdW1lbnQuY3JlYXRlRWxlbWVudCgnZGl2Jyk7XG4gICAgICBlbGVtZW50LmlkID0gdGhpcy5pZDtcbiAgICAgIGVsZW1lbnQuY2xhc3NOYW1lID0gJ19Cb2R5TGF5ZXInO1xuICAgICAgZG9jdW1lbnQuYm9keS5hcHBlbmRDaGlsZChlbGVtZW50KTtcbiAgICB9XG4gICAgdGhpcy5wYXJlbnRFbGVtZW50ID0gZWxlbWVudDtcbiAgICB0aGlzLmNvbXBvbmVudERpZFVwZGF0ZSgpO1xuICB9XG5cbiAgY29tcG9uZW50V2lsbFVubW91bnQgKCkge1xuICAgIGRvY3VtZW50LmJvZHkucmVtb3ZlQ2hpbGQodGhpcy5wYXJlbnRFbGVtZW50KTtcbiAgfVxuXG4gIGNvbXBvbmVudERpZFVwZGF0ZSAoKSB7XG4gICAgY29uc3QgeyBwcm9wcyB9ID0gdGhpcztcbiAgICBSZWFjdERPTS5yZW5kZXIoPGRpdiB7Li4ucHJvcHN9IC8+LCB0aGlzLnBhcmVudEVsZW1lbnQpO1xuICB9XG59O1xuXG5leHBvcnQgZGVmYXVsdCBCb2R5TGF5ZXI7XG4iXX0=