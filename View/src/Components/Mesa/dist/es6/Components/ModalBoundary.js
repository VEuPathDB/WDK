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

    _this.state = {
      modals: []
    };

    _this.addModal = _this.addModal.bind(_this);
    _this.removeModal = _this.removeModal.bind(_this);
    _this.updateModal = _this.updateModal.bind(_this);
    _this.getChildContext = _this.getChildContext.bind(_this);
    _this.renderModalWrapper = _this.renderModalWrapper.bind(_this);
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
    key: 'updateModal',
    value: function updateModal(id) {
      var modal = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};
      var modals = this.state.modals;

      var index = modals.findIndex(function (modal) {
        return modal._id === id;
      });
      if (index < 0) return;
      modals.splice(index, 1, modal);
      this.setState({ modals: modals });
    }
  }, {
    key: 'getChildContext',
    value: function getChildContext() {
      var addModal = this.addModal,
          removeModal = this.removeModal,
          updateModal = this.updateModal;

      return { addModal: addModal, removeModal: removeModal, updateModal: updateModal };
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
  updateModal: _propTypes2.default.func,
  removeModal: _propTypes2.default.func
};

exports.default = ModalBoundary;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL01vZGFsQm91bmRhcnkuanN4Il0sIm5hbWVzIjpbIm1vZGFsQm91bmRhcnlDbGFzcyIsIk1vZGFsQm91bmRhcnkiLCJwcm9wcyIsInN0YXRlIiwibW9kYWxzIiwiYWRkTW9kYWwiLCJiaW5kIiwicmVtb3ZlTW9kYWwiLCJ1cGRhdGVNb2RhbCIsImdldENoaWxkQ29udGV4dCIsInJlbmRlck1vZGFsV3JhcHBlciIsIm1vZGFsIiwiX2lkIiwicHVzaCIsInNldFN0YXRlIiwiaWQiLCJpbmRleCIsImZpbmRJbmRleCIsInNwbGljZSIsInN0eWxlIiwidG9wIiwibGVmdCIsIndpZHRoIiwiaGVpZ2h0IiwicG9zaXRpb24iLCJwb2ludGVyRXZlbnRzIiwibGVuZ3RoIiwibWFwIiwiRWxlbWVudCIsInJlbmRlciIsImNoaWxkcmVuIiwiTW9kYWxXcmFwcGVyIiwiZnVsbFN0eWxlIiwiT2JqZWN0IiwiYXNzaWduIiwiekluZGV4IiwieiIsIkNvbXBvbmVudCIsImNoaWxkQ29udGV4dFR5cGVzIiwiZnVuYyJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7OztBQUFBOzs7O0FBQ0E7Ozs7QUFFQTs7Ozs7Ozs7OztBQUVBLElBQU1BLHFCQUFxQiwyQkFBZSxlQUFmLENBQTNCOztJQUVNQyxhOzs7QUFDSix5QkFBYUMsS0FBYixFQUFvQjtBQUFBOztBQUFBLDhIQUNaQSxLQURZOztBQUdsQixVQUFLQyxLQUFMLEdBQWE7QUFDWEMsY0FBUTtBQURHLEtBQWI7O0FBSUEsVUFBS0MsUUFBTCxHQUFnQixNQUFLQSxRQUFMLENBQWNDLElBQWQsT0FBaEI7QUFDQSxVQUFLQyxXQUFMLEdBQW1CLE1BQUtBLFdBQUwsQ0FBaUJELElBQWpCLE9BQW5CO0FBQ0EsVUFBS0UsV0FBTCxHQUFtQixNQUFLQSxXQUFMLENBQWlCRixJQUFqQixPQUFuQjtBQUNBLFVBQUtHLGVBQUwsR0FBdUIsTUFBS0EsZUFBTCxDQUFxQkgsSUFBckIsT0FBdkI7QUFDQSxV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QUFoRTtBQUNFO0FBQUE7QUFBQSxZQUFLLE9BQU9HLE9BQU8sQ0FBUCxDQUFaO0FBQ0dMO0FBREgsU0FERjtBQUlFO0FBQUE7QUFBQSxZQUFLLE9BQU9LLE9BQU8sQ0FBUCxDQUFaO0FBQ0Usd0NBQUMsWUFBRDtBQURGO0FBSkYsT0FERjtBQVVEOzs7O0VBaEZ5QixnQkFBTUUsUzs7QUFpRmpDOztBQUVEcEMsY0FBY3FDLGlCQUFkLEdBQWtDO0FBQ2hDakMsWUFBVSxvQkFBVWtDLElBRFk7QUFFaEMvQixlQUFhLG9CQUFVK0IsSUFGUztBQUdoQ2hDLGVBQWEsb0JBQVVnQztBQUhTLENBQWxDOztrQkFNZXRDLGEiLCJmaWxlIjoiTW9kYWxCb3VuZGFyeS5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5pbXBvcnQgUHJvcFR5cGVzIGZyb20gJ3Byb3AtdHlwZXMnO1xuXG5pbXBvcnQgeyB1aWQsIG1ha2VDbGFzc2lmaWVyIH0gZnJvbSAnLi4vVXRpbHMvVXRpbHMnO1xuXG5jb25zdCBtb2RhbEJvdW5kYXJ5Q2xhc3MgPSBtYWtlQ2xhc3NpZmllcignTW9kYWxCb3VuZGFyeScpO1xuXG5jbGFzcyBNb2RhbEJvdW5kYXJ5IGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5zdGF0ZSA9IHtcbiAgICAgIG1vZGFsczogW11cbiAgICB9O1xuXG4gICAgdGhpcy5hZGRNb2RhbCA9IHRoaXMuYWRkTW9kYWwuYmluZCh0aGlzKTtcbiAgICB0aGlzLnJlbW92ZU1vZGFsID0gdGhpcy5yZW1vdmVNb2RhbC5iaW5kKHRoaXMpO1xuICAgIHRoaXMudXBkYXRlTW9kYWwgPSB0aGlzLnVwZGF0ZU1vZGFsLmJpbmQodGhpcyk7XG4gICAgdGhpcy5nZXRDaGlsZENvbnRleHQgPSB0aGlzLmdldENoaWxkQ29udGV4dC5iaW5kKHRoaXMpO1xuICAgIHRoaXMucmVuZGVyTW9kYWxXcmFwcGVyID0gdGhpcy5yZW5kZXJNb2RhbFdyYXBwZXIuYmluZCh0aGlzKTtcbiAgfVxuXG4gIGFkZE1vZGFsIChtb2RhbCkge1xuICAgIGxldCB7IG1vZGFscyB9ID0gdGhpcy5zdGF0ZTtcbiAgICBtb2RhbC5faWQgPSB1aWQoKTtcbiAgICBtb2RhbHMucHVzaChtb2RhbCk7XG4gICAgdGhpcy5zZXRTdGF0ZSh7IG1vZGFscyB9KTtcbiAgICByZXR1cm4gbW9kYWwuX2lkO1xuICB9XG5cbiAgcmVtb3ZlTW9kYWwgKGlkKSB7XG4gICAgbGV0IHsgbW9kYWxzIH0gPSB0aGlzLnN0YXRlO1xuICAgIGxldCBpbmRleCA9IG1vZGFscy5maW5kSW5kZXgobW9kYWwgPT4gbW9kYWwuX2lkID09PSBpZCk7XG4gICAgaWYgKGluZGV4IDwgMCkgcmV0dXJuO1xuICAgIG1vZGFscy5zcGxpY2UoaW5kZXgsIDEpO1xuICAgIHRoaXMuc2V0U3RhdGUoeyBtb2RhbHMgfSk7XG4gIH1cblxuICB1cGRhdGVNb2RhbCAoaWQsIG1vZGFsID0ge30pIHtcbiAgICBsZXQgeyBtb2RhbHMgfSA9IHRoaXMuc3RhdGU7XG4gICAgbGV0IGluZGV4ID0gbW9kYWxzLmZpbmRJbmRleChtb2RhbCA9PiBtb2RhbC5faWQgPT09IGlkKTtcbiAgICBpZiAoaW5kZXggPCAwKSByZXR1cm47XG4gICAgbW9kYWxzLnNwbGljZShpbmRleCwgMSwgbW9kYWwpO1xuICAgIHRoaXMuc2V0U3RhdGUoeyBtb2RhbHMgfSk7XG4gIH1cblxuICBnZXRDaGlsZENvbnRleHQgKCkge1xuICAgIGNvbnN0IHsgYWRkTW9kYWwsIHJlbW92ZU1vZGFsLCB1cGRhdGVNb2RhbCB9ID0gdGhpcztcbiAgICByZXR1cm4geyBhZGRNb2RhbCwgcmVtb3ZlTW9kYWwsIHVwZGF0ZU1vZGFsIH07XG4gIH1cblxuICByZW5kZXJNb2RhbFdyYXBwZXIgKCkge1xuICAgIGNvbnN0IHsgbW9kYWxzIH0gPSB0aGlzLnN0YXRlO1xuICAgIGNvbnN0IHN0eWxlID0ge1xuICAgICAgdG9wOiAwLFxuICAgICAgbGVmdDogMCxcbiAgICAgIHdpZHRoOiAnMTAwdncnLFxuICAgICAgaGVpZ2h0OiAnMTAwdmgnLFxuICAgICAgcG9zaXRpb246ICdmaXhlZCcsXG4gICAgICBwb2ludGVyRXZlbnRzOiAnbm9uZSdcbiAgICB9O1xuICAgIHJldHVybiAhbW9kYWxzLmxlbmd0aCA/IG51bGwgOiAoXG4gICAgICA8ZGl2IHN0eWxlPXtzdHlsZX0gY2xhc3NOYW1lPXttb2RhbEJvdW5kYXJ5Q2xhc3MoJ1dyYXBwZXInKX0+XG4gICAgICAgIHttb2RhbHMubWFwKChtb2RhbCwgaW5kZXgpID0+IHtcbiAgICAgICAgICBjb25zdCBFbGVtZW50ID0gbW9kYWwucmVuZGVyO1xuICAgICAgICAgIHJldHVybiA8RWxlbWVudCBrZXk9e2luZGV4fSB7Li4ubW9kYWx9IC8+XG4gICAgICAgIH0pfVxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxuXG4gIHJlbmRlciAoKSB7XG4gICAgY29uc3QgeyBjaGlsZHJlbiwgc3R5bGUgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgTW9kYWxXcmFwcGVyID0gdGhpcy5yZW5kZXJNb2RhbFdyYXBwZXI7XG4gICAgY29uc3QgZnVsbFN0eWxlID0gT2JqZWN0LmFzc2lnbih7fSwgc3R5bGUgPyBzdHlsZSA6IHt9LCB7IHBvc2l0aW9uOiAncmVsYXRpdmUnIH0pO1xuICAgIGNvbnN0IHpJbmRleCA9ICh6KSA9PiAoeyBwb3NpdGlvbjogJ3JlbGF0aXZlJywgekluZGV4OiB6IH0pO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPXttb2RhbEJvdW5kYXJ5Q2xhc3MoKSArICcgTWVzYUNvbXBvbmVudCd9IHN0eWxlPXtmdWxsU3R5bGV9PlxuICAgICAgICA8ZGl2IHN0eWxlPXt6SW5kZXgoMSl9PlxuICAgICAgICAgIHtjaGlsZHJlbn1cbiAgICAgICAgPC9kaXY+XG4gICAgICAgIDxkaXYgc3R5bGU9e3pJbmRleCgyKX0+XG4gICAgICAgICAgPE1vZGFsV3JhcHBlciAvPlxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn07XG5cbk1vZGFsQm91bmRhcnkuY2hpbGRDb250ZXh0VHlwZXMgPSB7XG4gIGFkZE1vZGFsOiBQcm9wVHlwZXMuZnVuYyxcbiAgdXBkYXRlTW9kYWw6IFByb3BUeXBlcy5mdW5jLFxuICByZW1vdmVNb2RhbDogUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IE1vZGFsQm91bmRhcnk7XG4iXX0=