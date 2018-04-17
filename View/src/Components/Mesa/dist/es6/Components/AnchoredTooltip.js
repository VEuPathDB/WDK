'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Tooltip = require('./Tooltip');

var _Tooltip2 = _interopRequireDefault(_Tooltip);

var _Events = require('../Utils/Events');

var _Events2 = _interopRequireDefault(_Events);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var AnchoredTooltip = function (_React$Component) {
  _inherits(AnchoredTooltip, _React$Component);

  function AnchoredTooltip(props) {
    _classCallCheck(this, AnchoredTooltip);

    var _this = _possibleConstructorReturn(this, (AnchoredTooltip.__proto__ || Object.getPrototypeOf(AnchoredTooltip)).call(this, props));

    _this.state = { position: {} };
    _this.updateOffset = _this.updateOffset.bind(_this);
    _this.componentDidMount = _this.componentDidMount.bind(_this);
    _this.componentWillUnmount = _this.componentWillUnmount.bind(_this);
    return _this;
  }

  _createClass(AnchoredTooltip, [{
    key: 'componentDidMount',
    value: function componentDidMount() {
      var _this2 = this;

      this.updateOffset();
      this.listeners = {
        scroll: _Events2.default.add('scroll', this.updateOffset),
        resize: _Events2.default.add('resize', this.updateOffset),
        MesaScroll: _Events2.default.add('MesaScroll', this.updateOffset),
        MesaReflow: _Events2.default.add('MesaReflow', this.updateOffset)
      };
      setTimeout(function () {
        return _this2.updateOffset();
      }, 300);
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
      if (this.props.debug) console.log('Updating offset...');
      var element = this.element;

      if (!element) {
        if (this.props.debug) console.log('...Returning out, no element');
        return;
      };
      var offset = element.getBoundingClientRect();
      var top = offset.top,
          left = offset.left;

      var position = { left: left, top: Math.ceil(top) + Math.ceil(element.offsetHeight) };
      if (this.props.debug) console.log('Offset is now...', position);
      this.setState({ position: position });
    }
  }, {
    key: 'render',
    value: function render() {
      var _this3 = this;

      var props = this.props;
      var position = this.state.position;

      var ref = function ref(el) {
        return _this3.element = el;
      };
      var children = _react2.default.createElement('div', { ref: ref, style: { display: 'inline-block' }, children: props.children });
      var extractedProps = _extends({}, props, { position: position, children: children });

      return _react2.default.createElement(_Tooltip2.default, _extends({
        corner: 'top-left',
        className: 'AnchoredTooltip'
      }, extractedProps));
    }
  }]);

  return AnchoredTooltip;
}(_react2.default.Component);

;

exports.default = AnchoredTooltip;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL0FuY2hvcmVkVG9vbHRpcC5qc3giXSwibmFtZXMiOlsiQW5jaG9yZWRUb29sdGlwIiwicHJvcHMiLCJzdGF0ZSIsInBvc2l0aW9uIiwidXBkYXRlT2Zmc2V0IiwiYmluZCIsImNvbXBvbmVudERpZE1vdW50IiwiY29tcG9uZW50V2lsbFVubW91bnQiLCJsaXN0ZW5lcnMiLCJzY3JvbGwiLCJhZGQiLCJyZXNpemUiLCJNZXNhU2Nyb2xsIiwiTWVzYVJlZmxvdyIsInNldFRpbWVvdXQiLCJPYmplY3QiLCJ2YWx1ZXMiLCJmb3JFYWNoIiwicmVtb3ZlIiwibGlzdGVuZXJJZCIsImRlYnVnIiwiY29uc29sZSIsImxvZyIsImVsZW1lbnQiLCJvZmZzZXQiLCJnZXRCb3VuZGluZ0NsaWVudFJlY3QiLCJ0b3AiLCJsZWZ0IiwiTWF0aCIsImNlaWwiLCJvZmZzZXRIZWlnaHQiLCJzZXRTdGF0ZSIsInJlZiIsImVsIiwiY2hpbGRyZW4iLCJkaXNwbGF5IiwiZXh0cmFjdGVkUHJvcHMiLCJDb21wb25lbnQiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7QUFBQTs7OztBQUVBOzs7O0FBQ0E7Ozs7Ozs7Ozs7OztJQUVNQSxlOzs7QUFDSiwyQkFBYUMsS0FBYixFQUFvQjtBQUFBOztBQUFBLGtJQUNaQSxLQURZOztBQUVsQixVQUFLQyxLQUFMLEdBQWEsRUFBRUMsVUFBVSxFQUFaLEVBQWI7QUFDQSxVQUFLQyxZQUFMLEdBQW9CLE1BQUtBLFlBQUwsQ0FBa0JDLElBQWxCLE9BQXBCO0FBQ0EsVUFBS0MsaUJBQUwsR0FBeUIsTUFBS0EsaUJBQUwsQ0FBdUJELElBQXZCLE9BQXpCO0FBQ0EsVUFBS0Usb0JBQUwsR0FBNEIsTUFBS0Esb0JBQUwsQ0FBMEJGLElBQTFCLE9BQTVCO0FBTGtCO0FBTW5COzs7O3dDQUVvQjtBQUFBOztBQUNuQixXQUFLRCxZQUFMO0FBQ0EsV0FBS0ksU0FBTCxHQUFpQjtBQUNmQyxnQkFBUSxpQkFBT0MsR0FBUCxDQUFXLFFBQVgsRUFBcUIsS0FBS04sWUFBMUIsQ0FETztBQUVmTyxnQkFBUSxpQkFBT0QsR0FBUCxDQUFXLFFBQVgsRUFBcUIsS0FBS04sWUFBMUIsQ0FGTztBQUdmUSxvQkFBWSxpQkFBT0YsR0FBUCxDQUFXLFlBQVgsRUFBeUIsS0FBS04sWUFBOUIsQ0FIRztBQUlmUyxvQkFBWSxpQkFBT0gsR0FBUCxDQUFXLFlBQVgsRUFBeUIsS0FBS04sWUFBOUI7QUFKRyxPQUFqQjtBQU1BVSxpQkFBVztBQUFBLGVBQU0sT0FBS1YsWUFBTCxFQUFOO0FBQUEsT0FBWCxFQUFzQyxHQUF0QztBQUNEOzs7MkNBRXVCO0FBQ3RCVyxhQUFPQyxNQUFQLENBQWMsS0FBS1IsU0FBbkIsRUFBOEJTLE9BQTlCLENBQXNDO0FBQUEsZUFBYyxpQkFBT0MsTUFBUCxDQUFjQyxVQUFkLENBQWQ7QUFBQSxPQUF0QztBQUNEOzs7bUNBRWU7QUFDZCxVQUFJLEtBQUtsQixLQUFMLENBQVdtQixLQUFmLEVBQXNCQyxRQUFRQyxHQUFSLENBQVksb0JBQVo7QUFEUixVQUVOQyxPQUZNLEdBRU0sSUFGTixDQUVOQSxPQUZNOztBQUdkLFVBQUksQ0FBQ0EsT0FBTCxFQUFjO0FBQ1osWUFBSSxLQUFLdEIsS0FBTCxDQUFXbUIsS0FBZixFQUFzQkMsUUFBUUMsR0FBUixDQUFZLDhCQUFaO0FBQ3RCO0FBQ0Q7QUFDRCxVQUFNRSxTQUFTRCxRQUFRRSxxQkFBUixFQUFmO0FBUGMsVUFRTkMsR0FSTSxHQVFRRixNQVJSLENBUU5FLEdBUk07QUFBQSxVQVFEQyxJQVJDLEdBUVFILE1BUlIsQ0FRREcsSUFSQzs7QUFTZCxVQUFNeEIsV0FBVyxFQUFFd0IsVUFBRixFQUFRRCxLQUFLRSxLQUFLQyxJQUFMLENBQVVILEdBQVYsSUFBaUJFLEtBQUtDLElBQUwsQ0FBVU4sUUFBUU8sWUFBbEIsQ0FBOUIsRUFBakI7QUFDQSxVQUFJLEtBQUs3QixLQUFMLENBQVdtQixLQUFmLEVBQXNCQyxRQUFRQyxHQUFSLENBQVksa0JBQVosRUFBZ0NuQixRQUFoQztBQUN0QixXQUFLNEIsUUFBTCxDQUFjLEVBQUU1QixrQkFBRixFQUFkO0FBQ0Q7Ozs2QkFFUztBQUFBOztBQUFBLFVBQ0FGLEtBREEsR0FDVSxJQURWLENBQ0FBLEtBREE7QUFBQSxVQUVBRSxRQUZBLEdBRWEsS0FBS0QsS0FGbEIsQ0FFQUMsUUFGQTs7QUFHUixVQUFNNkIsTUFBTSxTQUFOQSxHQUFNLENBQUNDLEVBQUQ7QUFBQSxlQUFRLE9BQUtWLE9BQUwsR0FBZVUsRUFBdkI7QUFBQSxPQUFaO0FBQ0EsVUFBTUMsV0FBWSx1Q0FBSyxLQUFLRixHQUFWLEVBQWUsT0FBTyxFQUFFRyxTQUFTLGNBQVgsRUFBdEIsRUFBbUQsVUFBVWxDLE1BQU1pQyxRQUFuRSxHQUFsQjtBQUNBLFVBQU1FLDhCQUFzQm5DLEtBQXRCLElBQTZCRSxrQkFBN0IsRUFBdUMrQixrQkFBdkMsR0FBTjs7QUFFQSxhQUNFO0FBQ0UsZ0JBQU8sVUFEVDtBQUVFLG1CQUFVO0FBRlosU0FHTUUsY0FITixFQURGO0FBT0Q7Ozs7RUFwRDJCLGdCQUFNQyxTOztBQXFEbkM7O2tCQUVjckMsZSIsImZpbGUiOiJBbmNob3JlZFRvb2x0aXAuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuXG5pbXBvcnQgVG9vbHRpcCBmcm9tICcuL1Rvb2x0aXAnO1xuaW1wb3J0IEV2ZW50cyBmcm9tICcuLi9VdGlscy9FdmVudHMnO1xuXG5jbGFzcyBBbmNob3JlZFRvb2x0aXAgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG4gICAgdGhpcy5zdGF0ZSA9IHsgcG9zaXRpb246IHt9IH07XG4gICAgdGhpcy51cGRhdGVPZmZzZXQgPSB0aGlzLnVwZGF0ZU9mZnNldC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuY29tcG9uZW50RGlkTW91bnQgPSB0aGlzLmNvbXBvbmVudERpZE1vdW50LmJpbmQodGhpcyk7XG4gICAgdGhpcy5jb21wb25lbnRXaWxsVW5tb3VudCA9IHRoaXMuY29tcG9uZW50V2lsbFVubW91bnQuYmluZCh0aGlzKTtcbiAgfVxuXG4gIGNvbXBvbmVudERpZE1vdW50ICgpIHtcbiAgICB0aGlzLnVwZGF0ZU9mZnNldCgpO1xuICAgIHRoaXMubGlzdGVuZXJzID0ge1xuICAgICAgc2Nyb2xsOiBFdmVudHMuYWRkKCdzY3JvbGwnLCB0aGlzLnVwZGF0ZU9mZnNldCksXG4gICAgICByZXNpemU6IEV2ZW50cy5hZGQoJ3Jlc2l6ZScsIHRoaXMudXBkYXRlT2Zmc2V0KSxcbiAgICAgIE1lc2FTY3JvbGw6IEV2ZW50cy5hZGQoJ01lc2FTY3JvbGwnLCB0aGlzLnVwZGF0ZU9mZnNldCksXG4gICAgICBNZXNhUmVmbG93OiBFdmVudHMuYWRkKCdNZXNhUmVmbG93JywgdGhpcy51cGRhdGVPZmZzZXQpXG4gICAgfTtcbiAgICBzZXRUaW1lb3V0KCgpID0+IHRoaXMudXBkYXRlT2Zmc2V0KCksIDMwMCk7XG4gIH1cblxuICBjb21wb25lbnRXaWxsVW5tb3VudCAoKSB7XG4gICAgT2JqZWN0LnZhbHVlcyh0aGlzLmxpc3RlbmVycykuZm9yRWFjaChsaXN0ZW5lcklkID0+IEV2ZW50cy5yZW1vdmUobGlzdGVuZXJJZCkpO1xuICB9XG5cbiAgdXBkYXRlT2Zmc2V0ICgpIHtcbiAgICBpZiAodGhpcy5wcm9wcy5kZWJ1ZykgY29uc29sZS5sb2coJ1VwZGF0aW5nIG9mZnNldC4uLicpO1xuICAgIGNvbnN0IHsgZWxlbWVudCB9ID0gdGhpcztcbiAgICBpZiAoIWVsZW1lbnQpIHtcbiAgICAgIGlmICh0aGlzLnByb3BzLmRlYnVnKSBjb25zb2xlLmxvZygnLi4uUmV0dXJuaW5nIG91dCwgbm8gZWxlbWVudCcpO1xuICAgICAgcmV0dXJuO1xuICAgIH07XG4gICAgY29uc3Qgb2Zmc2V0ID0gZWxlbWVudC5nZXRCb3VuZGluZ0NsaWVudFJlY3QoKTtcbiAgICBjb25zdCB7IHRvcCwgbGVmdCB9ID0gb2Zmc2V0O1xuICAgIGNvbnN0IHBvc2l0aW9uID0geyBsZWZ0LCB0b3A6IE1hdGguY2VpbCh0b3ApICsgTWF0aC5jZWlsKGVsZW1lbnQub2Zmc2V0SGVpZ2h0KSB9O1xuICAgIGlmICh0aGlzLnByb3BzLmRlYnVnKSBjb25zb2xlLmxvZygnT2Zmc2V0IGlzIG5vdy4uLicsIHBvc2l0aW9uKTtcbiAgICB0aGlzLnNldFN0YXRlKHsgcG9zaXRpb24gfSk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgcHJvcHMgfSA9IHRoaXM7XG4gICAgY29uc3QgeyBwb3NpdGlvbiB9ID0gdGhpcy5zdGF0ZTtcbiAgICBjb25zdCByZWYgPSAoZWwpID0+IHRoaXMuZWxlbWVudCA9IGVsO1xuICAgIGNvbnN0IGNoaWxkcmVuID0gKDxkaXYgcmVmPXtyZWZ9IHN0eWxlPXt7IGRpc3BsYXk6ICdpbmxpbmUtYmxvY2snIH19IGNoaWxkcmVuPXtwcm9wcy5jaGlsZHJlbn0gLz4pO1xuICAgIGNvbnN0IGV4dHJhY3RlZFByb3BzID0geyAuLi5wcm9wcywgcG9zaXRpb24sIGNoaWxkcmVuIH07XG5cbiAgICByZXR1cm4gKFxuICAgICAgPFRvb2x0aXBcbiAgICAgICAgY29ybmVyPVwidG9wLWxlZnRcIlxuICAgICAgICBjbGFzc05hbWU9XCJBbmNob3JlZFRvb2x0aXBcIlxuICAgICAgICB7Li4uZXh0cmFjdGVkUHJvcHN9XG4gICAgICAvPlxuICAgICk7XG4gIH1cbn07XG5cbmV4cG9ydCBkZWZhdWx0IEFuY2hvcmVkVG9vbHRpcDtcbiJdfQ==