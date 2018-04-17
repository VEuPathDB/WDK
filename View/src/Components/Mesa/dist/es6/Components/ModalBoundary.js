'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _Utils = require('../Utils/Utils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var modalBoundaryClass = (0, _Utils.makeClassifier)('ModalBoundary');

var ModalBoundary = function (_React$Component) {
  _inherits(ModalBoundary, _React$Component);

  function ModalBoundary(props) {
    _classCallCheck(this, ModalBoundary);

    var _this = _possibleConstructorReturn(this, (ModalBoundary.__proto__ || Object.getPrototypeOf(ModalBoundary)).call(this, props));

    _this.state = { modals: [] };

    _this.addModal = _this.addModal.bind(_this);
    _this.removeModal = _this.removeModal.bind(_this);
    _this.getChildContext = _this.getChildContext.bind(_this);
    _this.renderModalWrapper = _this.renderModalWrapper.bind(_this);
    _this.triggerModalRefresh = _this.triggerModalRefresh.bind(_this);

    console.error('\n      <ModalBoundary> is Deprecated!\n      Now declaratively render your modals inside a <BodyLayer>.\n      Stop using ModalBoundary.\n      Props Received:\n    ', { props: props });
    return _this;
  }

  _createClass(ModalBoundary, [{
    key: 'addModal',
    value: function addModal(modal) {
      var modals = this.state.modals;

      modal._id = (0, _Utils.uid)();
      modals.push(modal);
      this.setState({ modals: modals });
      return modal._id;
    }
  }, {
    key: 'triggerModalRefresh',
    value: function triggerModalRefresh() {
      this.forceUpdate();
    }
  }, {
    key: 'removeModal',
    value: function removeModal(id) {
      var modals = this.state.modals;

      var index = modals.findIndex(function (modal) {
        return modal._id === id;
      });
      if (index < 0) return;
      modals.splice(index, 1);
      this.setState({ modals: modals });
    }
  }, {
    key: 'getChildContext',
    value: function getChildContext() {
      var addModal = this.addModal,
          removeModal = this.removeModal,
          triggerModalRefresh = this.triggerModalRefresh;

      return { addModal: addModal, removeModal: removeModal, triggerModalRefresh: triggerModalRefresh };
    }
  }, {
    key: 'renderModalWrapper',
    value: function renderModalWrapper() {
      var modals = this.state.modals;

      var style = {
        top: 0,
        left: 0,
        width: '100vw',
        height: '100vh',
        position: 'fixed',
        pointerEvents: 'none'
      };
      return !modals.length ? null : _react2.default.createElement(
        'div',
        { style: style, className: modalBoundaryClass('Wrapper') },
        modals.map(function (modal, index) {
          var Element = modal.render;
          return _react2.default.createElement(Element, _extends({ key: index }, modal));
        })
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var _props = this.props,
          children = _props.children,
          style = _props.style;

      var ModalWrapper = this.renderModalWrapper;
      var fullStyle = Object.assign({}, style ? style : {}, { position: 'relative' });
      var zIndex = function zIndex(z) {
        return { position: 'relative', zIndex: z };
      };

      return _react2.default.createElement(
        'div',
        { className: modalBoundaryClass() + ' MesaComponent', style: fullStyle },
        _react2.default.createElement(
          'div',
          { style: zIndex(1) },
          children
        ),
        _react2.default.createElement(
          'div',
          { style: zIndex(2) },
          _react2.default.createElement(ModalWrapper, null)
        )
      );
    }
  }]);

  return ModalBoundary;
}(_react2.default.Component);

;

ModalBoundary.childContextTypes = {
  addModal: _propTypes2.default.func,
  removeModal: _propTypes2.default.func,
  triggerModalRefresh: _propTypes2.default.func
};

exports.default = ModalBoundary;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL01vZGFsQm91bmRhcnkuanN4Il0sIm5hbWVzIjpbIm1vZGFsQm91bmRhcnlDbGFzcyIsIk1vZGFsQm91bmRhcnkiLCJwcm9wcyIsInN0YXRlIiwibW9kYWxzIiwiYWRkTW9kYWwiLCJiaW5kIiwicmVtb3ZlTW9kYWwiLCJnZXRDaGlsZENvbnRleHQiLCJyZW5kZXJNb2RhbFdyYXBwZXIiLCJ0cmlnZ2VyTW9kYWxSZWZyZXNoIiwiY29uc29sZSIsImVycm9yIiwibW9kYWwiLCJfaWQiLCJwdXNoIiwic2V0U3RhdGUiLCJmb3JjZVVwZGF0ZSIsImlkIiwiaW5kZXgiLCJmaW5kSW5kZXgiLCJzcGxpY2UiLCJzdHlsZSIsInRvcCIsImxlZnQiLCJ3aWR0aCIsImhlaWdodCIsInBvc2l0aW9uIiwicG9pbnRlckV2ZW50cyIsImxlbmd0aCIsIm1hcCIsIkVsZW1lbnQiLCJyZW5kZXIiLCJjaGlsZHJlbiIsIk1vZGFsV3JhcHBlciIsImZ1bGxTdHlsZSIsIk9iamVjdCIsImFzc2lnbiIsInpJbmRleCIsInoiLCJDb21wb25lbnQiLCJjaGlsZENvbnRleHRUeXBlcyIsImZ1bmMiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7O0FBRUE7Ozs7Ozs7Ozs7QUFFQSxJQUFNQSxxQkFBcUIsMkJBQWUsZUFBZixDQUEzQjs7SUFFTUMsYTs7O0FBQ0oseUJBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSw4SEFDWkEsS0FEWTs7QUFHbEIsVUFBS0MsS0FBTCxHQUFhLEVBQUVDLFFBQVEsRUFBVixFQUFiOztBQUVBLFVBQUtDLFFBQUwsR0FBZ0IsTUFBS0EsUUFBTCxDQUFjQyxJQUFkLE9BQWhCO0FBQ0EsVUFBS0MsV0FBTCxHQUFtQixNQUFLQSxXQUFMLENBQWlCRCxJQUFqQixPQUFuQjtBQUNBLFVBQUtFLGVBQUwsR0FBdUIsTUFBS0EsZUFBTCxDQUFxQkYsSUFBckIsT0FBdkI7QUFDQSxVQUFLRyxrQkFBTCxH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QyxpQkFBZCxHQUFrQztBQUNoQ3BDLFlBQVUsb0JBQVVxQyxJQURZO0FBRWhDbkMsZUFBYSxvQkFBVW1DLElBRlM7QUFHaENoQyx1QkFBcUIsb0JBQVVnQztBQUhDLENBQWxDOztrQkFNZXpDLGEiLCJmaWxlIjoiTW9kYWxCb3VuZGFyeS5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5pbXBvcnQgUHJvcFR5cGVzIGZyb20gJ3Byb3AtdHlwZXMnO1xuXG5pbXBvcnQgeyB1aWQsIG1ha2VDbGFzc2lmaWVyIH0gZnJvbSAnLi4vVXRpbHMvVXRpbHMnO1xuXG5jb25zdCBtb2RhbEJvdW5kYXJ5Q2xhc3MgPSBtYWtlQ2xhc3NpZmllcignTW9kYWxCb3VuZGFyeScpO1xuXG5jbGFzcyBNb2RhbEJvdW5kYXJ5IGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5zdGF0ZSA9IHsgbW9kYWxzOiBbXSB9O1xuXG4gICAgdGhpcy5hZGRNb2RhbCA9IHRoaXMuYWRkTW9kYWwuYmluZCh0aGlzKTtcbiAgICB0aGlzLnJlbW92ZU1vZGFsID0gdGhpcy5yZW1vdmVNb2RhbC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuZ2V0Q2hpbGRDb250ZXh0ID0gdGhpcy5nZXRDaGlsZENvbnRleHQuYmluZCh0aGlzKTtcbiAgICB0aGlzLnJlbmRlck1vZGFsV3JhcHBlciA9IHRoaXMucmVuZGVyTW9kYWxXcmFwcGVyLmJpbmQodGhpcyk7XG4gICAgdGhpcy50cmlnZ2VyTW9kYWxSZWZyZXNoID0gdGhpcy50cmlnZ2VyTW9kYWxSZWZyZXNoLmJpbmQodGhpcyk7XG5cbiAgICBjb25zb2xlLmVycm9yKGBcbiAgICAgIDxNb2RhbEJvdW5kYXJ5PiBpcyBEZXByZWNhdGVkIVxuICAgICAgTm93IGRlY2xhcmF0aXZlbHkgcmVuZGVyIHlvdXIgbW9kYWxzIGluc2lkZSBhIDxCb2R5TGF5ZXI+LlxuICAgICAgU3RvcCB1c2luZyBNb2RhbEJvdW5kYXJ5LlxuICAgICAgUHJvcHMgUmVjZWl2ZWQ6XG4gICAgYCwgeyBwcm9wcyB9KTtcbiAgfVxuXG4gIGFkZE1vZGFsIChtb2RhbCkge1xuICAgIGxldCB7IG1vZGFscyB9ID0gdGhpcy5zdGF0ZTtcbiAgICBtb2RhbC5faWQgPSB1aWQoKTtcbiAgICBtb2RhbHMucHVzaChtb2RhbCk7XG4gICAgdGhpcy5zZXRTdGF0ZSh7IG1vZGFscyB9KTtcbiAgICByZXR1cm4gbW9kYWwuX2lkO1xuICB9XG5cbiAgdHJpZ2dlck1vZGFsUmVmcmVzaCAoKSB7XG4gICAgdGhpcy5mb3JjZVVwZGF0ZSgpO1xuICB9XG5cbiAgcmVtb3ZlTW9kYWwgKGlkKSB7XG4gICAgbGV0IHsgbW9kYWxzIH0gPSB0aGlzLnN0YXRlO1xuICAgIGxldCBpbmRleCA9IG1vZGFscy5maW5kSW5kZXgobW9kYWwgPT4gbW9kYWwuX2lkID09PSBpZCk7XG4gICAgaWYgKGluZGV4IDwgMCkgcmV0dXJuO1xuICAgIG1vZGFscy5zcGxpY2UoaW5kZXgsIDEpO1xuICAgIHRoaXMuc2V0U3RhdGUoeyBtb2RhbHMgfSk7XG4gIH1cblxuICBnZXRDaGlsZENvbnRleHQgKCkge1xuICAgIGNvbnN0IHsgYWRkTW9kYWwsIHJlbW92ZU1vZGFsLCB0cmlnZ2VyTW9kYWxSZWZyZXNoIH0gPSB0aGlzO1xuICAgIHJldHVybiB7IGFkZE1vZGFsLCByZW1vdmVNb2RhbCwgdHJpZ2dlck1vZGFsUmVmcmVzaCB9O1xuICB9XG5cbiAgcmVuZGVyTW9kYWxXcmFwcGVyICgpIHtcbiAgICBjb25zdCB7IG1vZGFscyB9ID0gdGhpcy5zdGF0ZTtcbiAgICBjb25zdCBzdHlsZSA9IHtcbiAgICAgIHRvcDogMCxcbiAgICAgIGxlZnQ6IDAsXG4gICAgICB3aWR0aDogJzEwMHZ3JyxcbiAgICAgIGhlaWdodDogJzEwMHZoJyxcbiAgICAgIHBvc2l0aW9uOiAnZml4ZWQnLFxuICAgICAgcG9pbnRlckV2ZW50czogJ25vbmUnXG4gICAgfTtcbiAgICByZXR1cm4gIW1vZGFscy5sZW5ndGggPyBudWxsIDogKFxuICAgICAgPGRpdiBzdHlsZT17c3R5bGV9IGNsYXNzTmFtZT17bW9kYWxCb3VuZGFyeUNsYXNzKCdXcmFwcGVyJyl9PlxuICAgICAgICB7bW9kYWxzLm1hcCgobW9kYWwsIGluZGV4KSA9PiB7XG4gICAgICAgICAgY29uc3QgRWxlbWVudCA9IG1vZGFsLnJlbmRlcjtcbiAgICAgICAgICByZXR1cm4gPEVsZW1lbnQga2V5PXtpbmRleH0gey4uLm1vZGFsfSAvPlxuICAgICAgICB9KX1cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgY2hpbGRyZW4sIHN0eWxlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IE1vZGFsV3JhcHBlciA9IHRoaXMucmVuZGVyTW9kYWxXcmFwcGVyO1xuICAgIGNvbnN0IGZ1bGxTdHlsZSA9IE9iamVjdC5hc3NpZ24oe30sIHN0eWxlID8gc3R5bGUgOiB7fSwgeyBwb3NpdGlvbjogJ3JlbGF0aXZlJyB9KTtcbiAgICBjb25zdCB6SW5kZXggPSAoeikgPT4gKHsgcG9zaXRpb246ICdyZWxhdGl2ZScsIHpJbmRleDogeiB9KTtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT17bW9kYWxCb3VuZGFyeUNsYXNzKCkgKyAnIE1lc2FDb21wb25lbnQnfSBzdHlsZT17ZnVsbFN0eWxlfT5cbiAgICAgICAgPGRpdiBzdHlsZT17ekluZGV4KDEpfT5cbiAgICAgICAgICB7Y2hpbGRyZW59XG4gICAgICAgIDwvZGl2PlxuICAgICAgICA8ZGl2IHN0eWxlPXt6SW5kZXgoMil9PlxuICAgICAgICAgIDxNb2RhbFdyYXBwZXIgLz5cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59O1xuXG5Nb2RhbEJvdW5kYXJ5LmNoaWxkQ29udGV4dFR5cGVzID0ge1xuICBhZGRNb2RhbDogUHJvcFR5cGVzLmZ1bmMsXG4gIHJlbW92ZU1vZGFsOiBQcm9wVHlwZXMuZnVuYyxcbiAgdHJpZ2dlck1vZGFsUmVmcmVzaDogUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IE1vZGFsQm91bmRhcnk7XG4iXX0=