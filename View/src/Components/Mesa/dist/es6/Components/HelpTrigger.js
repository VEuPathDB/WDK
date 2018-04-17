'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Icon = require('./Icon');

var _Icon2 = _interopRequireDefault(_Icon);

var _Tooltip = require('./Tooltip');

var _Tooltip2 = _interopRequireDefault(_Tooltip);

var _Events = require('../Utils/Events');

var _Events2 = _interopRequireDefault(_Events);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var HelpTrigger = function (_React$Component) {
  _inherits(HelpTrigger, _React$Component);

  function HelpTrigger(props) {
    _classCallCheck(this, HelpTrigger);

    var _this = _possibleConstructorReturn(this, (HelpTrigger.__proto__ || Object.getPrototypeOf(HelpTrigger)).call(this, props));

    _this.state = { position: {} };
    _this.updateOffset = _this.updateOffset.bind(_this);
    _this.componentDidMount = _this.componentDidMount.bind(_this);
    _this.componentWillUnmount = _this.componentWillUnmount.bind(_this);
    return _this;
  }

  _createClass(HelpTrigger, [{
    key: 'componentDidMount',
    value: function componentDidMount() {
      this.updateOffset();
      this.listeners = {
        scroll: _Events2.default.add('scroll', this.updateOffset),
        resize: _Events2.default.add('resize', this.updateOffset),
        MesaScroll: _Events2.default.add('MesaScroll', this.updateOffset),
        MesaReflow: _Events2.default.add('MesaReflow', this.updateOffset)
      };
    }
  }, {
    key: 'componentWillUnmount',
    value: function componentWillUnmount() {
      Object.values(this.listeners).forEach(function (listenerId) {
        return _Events2.default.remove(listenerId);
      });
    }
  }, {
    key: 'updateOffset',
    value: function updateOffset() {
      var element = this.element;

      if (!element) return;
      var offset = _Tooltip2.default.getOffset(element);
      var top = offset.top,
          left = offset.left,
          height = offset.height;

      var position = { left: left, top: top + height };
      this.setState({ position: position });
    }
  }, {
    key: 'render',
    value: function render() {
      var _this2 = this;

      var position = this.state.position;
      var children = this.props.children;


      return _react2.default.createElement(
        _Tooltip2.default,
        {
          corner: 'top-left',
          position: position,
          className: 'Trigger HelpTrigger',
          content: children },
        _react2.default.createElement(
          'div',
          { ref: function ref(el) {
              return _this2.element = el;
            } },
          _react2.default.createElement(_Icon2.default, { fa: 'question-circle' })
        )
      );
    }
  }]);

  return HelpTrigger;
}(_react2.default.Component);

;

exports.default = HelpTrigger;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL0hlbHBUcmlnZ2VyLmpzeCJdLCJuYW1lcyI6WyJIZWxwVHJpZ2dlciIsInByb3BzIiwic3RhdGUiLCJwb3NpdGlvbiIsInVwZGF0ZU9mZnNldCIsImJpbmQiLCJjb21wb25lbnREaWRNb3VudCIsImNvbXBvbmVudFdpbGxVbm1vdW50IiwibGlzdGVuZXJzIiwic2Nyb2xsIiwiYWRkIiwicmVzaXplIiwiTWVzYVNjcm9sbCIsIk1lc2FSZWZsb3ciLCJPYmplY3QiLCJ2YWx1ZXMiLCJmb3JFYWNoIiwicmVtb3ZlIiwibGlzdGVuZXJJZCIsImVsZW1lbnQiLCJvZmZzZXQiLCJnZXRPZmZzZXQiLCJ0b3AiLCJsZWZ0IiwiaGVpZ2h0Iiwic2V0U3RhdGUiLCJjaGlsZHJlbiIsImVsIiwiQ29tcG9uZW50Il0sIm1hcHBpbmdzIjoiOzs7Ozs7OztBQUFBOzs7O0FBQ0E7Ozs7QUFDQTs7OztBQUNBOzs7Ozs7Ozs7Ozs7SUFFTUEsVzs7O0FBQ0osdUJBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSwwSEFDWkEsS0FEWTs7QUFFbEIsVUFBS0MsS0FBTCxHQUFhLEVBQUVDLFVBQVUsRUFBWixFQUFiO0FBQ0EsVUFBS0MsWUFBTCxHQUFvQixNQUFLQSxZQUFMLENBQWtCQyxJQUFsQixPQUFwQjtBQUNBLFVBQUtDLGlCQUFMLEdBQXlCLE1BQUtBLGlCQUFMLENBQXVCRCxJQUF2QixPQUF6QjtBQUNBLFVBQUtFLG9CQUFMLEdBQTRCLE1BQUtBLG9CQUFMLENBQTBCRixJQUExQixPQUE1QjtBQUxrQjtBQU1uQjs7Ozt3Q0FFb0I7QUFDbkIsV0FBS0QsWUFBTDtBQUNBLFdBQUtJLFNBQUwsR0FBaUI7QUFDZkMsZ0JBQVEsaUJBQU9DLEdBQVAsQ0FBVyxRQUFYLEVBQXFCLEtBQUtOLFlBQTFCLENBRE87QUFFZk8sZ0JBQVEsaUJBQU9ELEdBQVAsQ0FBVyxRQUFYLEVBQXFCLEtBQUtOLFlBQTFCLENBRk87QUFHZlEsb0JBQVksaUJBQU9GLEdBQVAsQ0FBVyxZQUFYLEVBQXlCLEtBQUtOLFlBQTlCLENBSEc7QUFJZlMsb0JBQVksaUJBQU9ILEdBQVAsQ0FBVyxZQUFYLEVBQXlCLEtBQUtOLFlBQTlCO0FBSkcsT0FBakI7QUFNRDs7OzJDQUV1QjtBQUN0QlUsYUFBT0MsTUFBUCxDQUFjLEtBQUtQLFNBQW5CLEVBQThCUSxPQUE5QixDQUFzQztBQUFBLGVBQWMsaUJBQU9DLE1BQVAsQ0FBY0MsVUFBZCxDQUFkO0FBQUEsT0FBdEM7QUFDRDs7O21DQUVlO0FBQUEsVUFDTkMsT0FETSxHQUNNLElBRE4sQ0FDTkEsT0FETTs7QUFFZCxVQUFJLENBQUNBLE9BQUwsRUFBYztBQUNkLFVBQU1DLFNBQVMsa0JBQVFDLFNBQVIsQ0FBa0JGLE9BQWxCLENBQWY7QUFIYyxVQUlORyxHQUpNLEdBSWdCRixNQUpoQixDQUlORSxHQUpNO0FBQUEsVUFJREMsSUFKQyxHQUlnQkgsTUFKaEIsQ0FJREcsSUFKQztBQUFBLFVBSUtDLE1BSkwsR0FJZ0JKLE1BSmhCLENBSUtJLE1BSkw7O0FBS2QsVUFBTXJCLFdBQVcsRUFBRW9CLFVBQUYsRUFBUUQsS0FBS0EsTUFBTUUsTUFBbkIsRUFBakI7QUFDQSxXQUFLQyxRQUFMLENBQWMsRUFBRXRCLGtCQUFGLEVBQWQ7QUFDRDs7OzZCQUVTO0FBQUE7O0FBQUEsVUFDQUEsUUFEQSxHQUNhLEtBQUtELEtBRGxCLENBQ0FDLFFBREE7QUFBQSxVQUVBdUIsUUFGQSxHQUVhLEtBQUt6QixLQUZsQixDQUVBeUIsUUFGQTs7O0FBSVIsYUFDRTtBQUFBO0FBQUE7QUFDRSxrQkFBTyxVQURUO0FBRUUsb0JBQVV2QixRQUZaO0FBR0UscUJBQVUscUJBSFo7QUFJRSxtQkFBU3VCLFFBSlg7QUFLRTtBQUFBO0FBQUEsWUFBSyxLQUFLLGFBQUNDLEVBQUQ7QUFBQSxxQkFBUSxPQUFLUixPQUFMLEdBQWVRLEVBQXZCO0FBQUEsYUFBVjtBQUNFLDBEQUFNLElBQUcsaUJBQVQ7QUFERjtBQUxGLE9BREY7QUFXRDs7OztFQS9DdUIsZ0JBQU1DLFM7O0FBZ0QvQjs7a0JBRWM1QixXIiwiZmlsZSI6IkhlbHBUcmlnZ2VyLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcbmltcG9ydCBJY29uIGZyb20gJy4vSWNvbic7XG5pbXBvcnQgVG9vbHRpcCBmcm9tICcuL1Rvb2x0aXAnO1xuaW1wb3J0IEV2ZW50cyBmcm9tICcuLi9VdGlscy9FdmVudHMnO1xuXG5jbGFzcyBIZWxwVHJpZ2dlciBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgICB0aGlzLnN0YXRlID0geyBwb3NpdGlvbjoge30gfTtcbiAgICB0aGlzLnVwZGF0ZU9mZnNldCA9IHRoaXMudXBkYXRlT2Zmc2V0LmJpbmQodGhpcyk7XG4gICAgdGhpcy5jb21wb25lbnREaWRNb3VudCA9IHRoaXMuY29tcG9uZW50RGlkTW91bnQuYmluZCh0aGlzKTtcbiAgICB0aGlzLmNvbXBvbmVudFdpbGxVbm1vdW50ID0gdGhpcy5jb21wb25lbnRXaWxsVW5tb3VudC5iaW5kKHRoaXMpO1xuICB9XG5cbiAgY29tcG9uZW50RGlkTW91bnQgKCkge1xuICAgIHRoaXMudXBkYXRlT2Zmc2V0KCk7XG4gICAgdGhpcy5saXN0ZW5lcnMgPSB7XG4gICAgICBzY3JvbGw6IEV2ZW50cy5hZGQoJ3Njcm9sbCcsIHRoaXMudXBkYXRlT2Zmc2V0KSxcbiAgICAgIHJlc2l6ZTogRXZlbnRzLmFkZCgncmVzaXplJywgdGhpcy51cGRhdGVPZmZzZXQpLFxuICAgICAgTWVzYVNjcm9sbDogRXZlbnRzLmFkZCgnTWVzYVNjcm9sbCcsIHRoaXMudXBkYXRlT2Zmc2V0KSxcbiAgICAgIE1lc2FSZWZsb3c6IEV2ZW50cy5hZGQoJ01lc2FSZWZsb3cnLCB0aGlzLnVwZGF0ZU9mZnNldClcbiAgICB9O1xuICB9XG5cbiAgY29tcG9uZW50V2lsbFVubW91bnQgKCkge1xuICAgIE9iamVjdC52YWx1ZXModGhpcy5saXN0ZW5lcnMpLmZvckVhY2gobGlzdGVuZXJJZCA9PiBFdmVudHMucmVtb3ZlKGxpc3RlbmVySWQpKTtcbiAgfVxuXG4gIHVwZGF0ZU9mZnNldCAoKSB7XG4gICAgY29uc3QgeyBlbGVtZW50IH0gPSB0aGlzO1xuICAgIGlmICghZWxlbWVudCkgcmV0dXJuO1xuICAgIGNvbnN0IG9mZnNldCA9IFRvb2x0aXAuZ2V0T2Zmc2V0KGVsZW1lbnQpO1xuICAgIGNvbnN0IHsgdG9wLCBsZWZ0LCBoZWlnaHQgfSA9IG9mZnNldDtcbiAgICBjb25zdCBwb3NpdGlvbiA9IHsgbGVmdCwgdG9wOiB0b3AgKyBoZWlnaHQgfTtcbiAgICB0aGlzLnNldFN0YXRlKHsgcG9zaXRpb24gfSk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgcG9zaXRpb24gfSA9IHRoaXMuc3RhdGU7XG4gICAgY29uc3QgeyBjaGlsZHJlbiB9ID0gdGhpcy5wcm9wcztcblxuICAgIHJldHVybiAoXG4gICAgICA8VG9vbHRpcFxuICAgICAgICBjb3JuZXI9XCJ0b3AtbGVmdFwiXG4gICAgICAgIHBvc2l0aW9uPXtwb3NpdGlvbn1cbiAgICAgICAgY2xhc3NOYW1lPVwiVHJpZ2dlciBIZWxwVHJpZ2dlclwiXG4gICAgICAgIGNvbnRlbnQ9e2NoaWxkcmVufT5cbiAgICAgICAgPGRpdiByZWY9eyhlbCkgPT4gdGhpcy5lbGVtZW50ID0gZWx9PlxuICAgICAgICAgIDxJY29uIGZhPVwicXVlc3Rpb24tY2lyY2xlXCIgLz5cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L1Rvb2x0aXA+XG4gICAgKTtcbiAgfVxufTtcblxuZXhwb3J0IGRlZmF1bHQgSGVscFRyaWdnZXI7XG4iXX0=