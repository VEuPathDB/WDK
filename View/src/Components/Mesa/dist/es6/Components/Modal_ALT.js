'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Events = require('../Utils/Events');

var _Events2 = _interopRequireDefault(_Events);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Modal = function (_React$Component) {
  _inherits(Modal, _React$Component);

  function Modal(props) {
    _classCallCheck(this, Modal);

    var _this = _possibleConstructorReturn(this, (Modal.__proto__ || Object.getPrototypeOf(Modal)).call(this, props));

    _this.handleClose = _this.handleClose.bind(_this);
    return _this;
  }

  _createClass(Modal, [{
    key: 'componentWillReceiveProps',
    value: function componentWillReceiveProps(_ref) {
      var open = _ref.open;

      if (!!open === !!this.props.open) return;
      if (!!open && !this.props.open) return this.closeListener = _Events2.default.onKey('esc', this.handleClose);else _Events2.default.remove(this.closeListener);
    }
  }, {
    key: 'handleClose',
    value: function handleClose() {
      var onClose = this.props.onClose;

      return onClose && onClose();
    }
  }, {
    key: 'diffuseClick',
    value: function diffuseClick(event) {
      return event.stopPropagation();
    }
  }, {
    key: 'render',
    value: function render() {
      var _props = this.props,
          open = _props.open,
          children = _props.children,
          className = _props.className;

      var _className = 'Modal ' + (open ? 'Modal-Open' : 'Modal-Closed') + ' ' + (className || '');

      return _react2.default.createElement(
        'div',
        { className: 'Modal-Wrapper' },
        _react2.default.createElement(
          'div',
          { className: _className, onClick: this.handleClose },
          _react2.default.createElement(
            'div',
            { className: 'Modal-Body', onClick: this.diffuseClick },
            children
          )
        )
      );
    }
  }]);

  return Modal;
}(_react2.default.Component);

;

exports.default = Modal;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL01vZGFsX0FMVC5qc3giXSwibmFtZXMiOlsiTW9kYWwiLCJwcm9wcyIsImhhbmRsZUNsb3NlIiwiYmluZCIsIm9wZW4iLCJjbG9zZUxpc3RlbmVyIiwib25LZXkiLCJyZW1vdmUiLCJvbkNsb3NlIiwiZXZlbnQiLCJzdG9wUHJvcGFnYXRpb24iLCJjaGlsZHJlbiIsImNsYXNzTmFtZSIsIl9jbGFzc05hbWUiLCJkaWZmdXNlQ2xpY2siLCJDb21wb25lbnQiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7O0FBQUE7Ozs7QUFFQTs7Ozs7Ozs7Ozs7O0lBRU1BLEs7OztBQUNKLGlCQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsOEdBQ1pBLEtBRFk7O0FBRWxCLFVBQUtDLFdBQUwsR0FBbUIsTUFBS0EsV0FBTCxDQUFpQkMsSUFBakIsT0FBbkI7QUFGa0I7QUFHbkI7Ozs7b0RBRW9DO0FBQUEsVUFBUkMsSUFBUSxRQUFSQSxJQUFROztBQUNuQyxVQUFJLENBQUMsQ0FBQ0EsSUFBRixLQUFXLENBQUMsQ0FBQyxLQUFLSCxLQUFMLENBQVdHLElBQTVCLEVBQWtDO0FBQ2xDLFVBQUksQ0FBQyxDQUFDQSxJQUFGLElBQVUsQ0FBQyxLQUFLSCxLQUFMLENBQVdHLElBQTFCLEVBQWdDLE9BQU8sS0FBS0MsYUFBTCxHQUFxQixpQkFBT0MsS0FBUCxDQUFhLEtBQWIsRUFBb0IsS0FBS0osV0FBekIsQ0FBNUIsQ0FBaEMsS0FDSyxpQkFBT0ssTUFBUCxDQUFjLEtBQUtGLGFBQW5CO0FBQ047OztrQ0FFYztBQUFBLFVBQ0xHLE9BREssR0FDTyxLQUFLUCxLQURaLENBQ0xPLE9BREs7O0FBRWIsYUFBT0EsV0FBV0EsU0FBbEI7QUFDRDs7O2lDQUVhQyxLLEVBQU87QUFDbkIsYUFBT0EsTUFBTUMsZUFBTixFQUFQO0FBQ0Q7Ozs2QkFFUztBQUFBLG1CQUM4QixLQUFLVCxLQURuQztBQUFBLFVBQ0FHLElBREEsVUFDQUEsSUFEQTtBQUFBLFVBQ01PLFFBRE4sVUFDTUEsUUFETjtBQUFBLFVBQ2dCQyxTQURoQixVQUNnQkEsU0FEaEI7O0FBRVIsVUFBTUMseUJBQXNCVCxPQUFPLFlBQVAsR0FBc0IsY0FBNUMsV0FBOERRLGFBQWEsRUFBM0UsQ0FBTjs7QUFFQSxhQUNFO0FBQUE7QUFBQSxVQUFLLFdBQVUsZUFBZjtBQUNFO0FBQUE7QUFBQSxZQUFLLFdBQVdDLFVBQWhCLEVBQTRCLFNBQVMsS0FBS1gsV0FBMUM7QUFDRTtBQUFBO0FBQUEsY0FBSyxXQUFVLFlBQWYsRUFBNEIsU0FBUyxLQUFLWSxZQUExQztBQUNHSDtBQURIO0FBREY7QUFERixPQURGO0FBU0Q7Ozs7RUFsQ2lCLGdCQUFNSSxTOztBQW1DekI7O2tCQUVjZixLIiwiZmlsZSI6Ik1vZGFsX0FMVC5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5cbmltcG9ydCBFdmVudHMgZnJvbSAnLi4vVXRpbHMvRXZlbnRzJztcblxuY2xhc3MgTW9kYWwgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG4gICAgdGhpcy5oYW5kbGVDbG9zZSA9IHRoaXMuaGFuZGxlQ2xvc2UuYmluZCh0aGlzKTtcbiAgfVxuXG4gIGNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMgKHsgb3BlbiB9KSB7XG4gICAgaWYgKCEhb3BlbiA9PT0gISF0aGlzLnByb3BzLm9wZW4pIHJldHVybjtcbiAgICBpZiAoISFvcGVuICYmICF0aGlzLnByb3BzLm9wZW4pIHJldHVybiB0aGlzLmNsb3NlTGlzdGVuZXIgPSBFdmVudHMub25LZXkoJ2VzYycsIHRoaXMuaGFuZGxlQ2xvc2UpO1xuICAgIGVsc2UgRXZlbnRzLnJlbW92ZSh0aGlzLmNsb3NlTGlzdGVuZXIpO1xuICB9XG5cbiAgaGFuZGxlQ2xvc2UgKCkge1xuICAgIGNvbnN0IHsgb25DbG9zZSB9ID0gdGhpcy5wcm9wcztcbiAgICByZXR1cm4gb25DbG9zZSAmJiBvbkNsb3NlKCk7XG4gIH1cblxuICBkaWZmdXNlQ2xpY2sgKGV2ZW50KSB7XG4gICAgcmV0dXJuIGV2ZW50LnN0b3BQcm9wYWdhdGlvbigpO1xuICB9XG5cbiAgcmVuZGVyICgpIHtcbiAgICBjb25zdCB7IG9wZW4sIGNoaWxkcmVuLCBjbGFzc05hbWUgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgX2NsYXNzTmFtZSA9IGBNb2RhbCAke29wZW4gPyAnTW9kYWwtT3BlbicgOiAnTW9kYWwtQ2xvc2VkJ30gJHtjbGFzc05hbWUgfHwgJyd9YFxuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiTW9kYWwtV3JhcHBlclwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT17X2NsYXNzTmFtZX0gb25DbGljaz17dGhpcy5oYW5kbGVDbG9zZX0+XG4gICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJNb2RhbC1Cb2R5XCIgb25DbGljaz17dGhpcy5kaWZmdXNlQ2xpY2t9PlxuICAgICAgICAgICAge2NoaWxkcmVufVxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn07XG5cbmV4cG9ydCBkZWZhdWx0IE1vZGFsO1xuIl19