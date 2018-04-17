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
    _this.updateModal = _this.updateModal.bind(_this);
    _this.getChildContext = _this.getChildContext.bind(_this);
    _this.renderModalWrapper = _this.renderModalWrapper.bind(_this);
    _this.triggerModalRefresh = _this.triggerModalRefresh.bind(_this);
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
          updateModal = this.updateModal,
          triggerModalRefresh = this.triggerModalRefresh;

      return { addModal: addModal, removeModal: removeModal, updateModal: updateModal, triggerModalRefresh: triggerModalRefresh };
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
  removeModal: _propTypes2.default.func,
  triggerModalRefresh: _propTypes2.default.func
};

exports.default = ModalBoundary;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL01vZGFsQm91bmRhcnkuanN4Il0sIm5hbWVzIjpbIm1vZGFsQm91bmRhcnlDbGFzcyIsIk1vZGFsQm91bmRhcnkiLCJwcm9wcyIsInN0YXRlIiwibW9kYWxzIiwiYWRkTW9kYWwiLCJiaW5kIiwicmVtb3ZlTW9kYWwiLCJ1cGRhdGVNb2RhbCIsImdldENoaWxkQ29udGV4dCIsInJlbmRlck1vZGFsV3JhcHBlciIsInRyaWdnZXJNb2RhbFJlZnJlc2giLCJtb2RhbCIsIl9pZCIsInB1c2giLCJzZXRTdGF0ZSIsImZvcmNlVXBkYXRlIiwiaWQiLCJpbmRleCIsImZpbmRJbmRleCIsInNwbGljZSIsInN0eWxlIiwidG9wIiwibGVmdCIsIndpZHRoIiwiaGVpZ2h0IiwicG9zaXRpb24iLCJwb2ludGVyRXZlbnRzIiwibGVuZ3RoIiwibWFwIiwiRWxlbWVudCIsInJlbmRlciIsImNoaWxkcmVuIiwiTW9kYWxXcmFwcGVyIiwiZnVsbFN0eWxlIiwiT2JqZWN0IiwiYXNzaWduIiwiekluZGV4IiwieiIsIkNvbXBvbmVudCIsImNoaWxkQ29udGV4dFR5cGVzIiwiZnVuYyJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7OztBQUFBOzs7O0FBQ0E7Ozs7QUFFQTs7Ozs7Ozs7OztBQUVBLElBQU1BLHFCQUFxQiwyQkFBZSxlQUFmLENBQTNCOztJQUVNQyxhOzs7QUFDSix5QkFBYUMsS0FBYixFQUFvQjtBQUFBOztBQUFBLDhIQUNaQSxLQURZOztBQUdsQixVQUFLQyxLQUFMLEdBQWEsRUFBRUMsUUFBUSxFQUFWLEVBQWI7O0FBRUEsVUFBS0MsUUFBTCxHQUFnQixNQUFLQSxRQUFMLENBQWNDLElBQWQsT0FBaEI7QUFDQSxVQUFLQyxXQUFMLEdBQW1CLE1BQUtBLFdBQUwsQ0FBaUJELElBQWpCLE9BQW5CO0FBQ0EsVUFBS0UsV0FBTCxHQUFtQixNQUFLQSxXQUFMLENBQWlCRixJQUFqQixPQUFuQjtBQUNBLFVBQUtHLGVBQUwsR0FBdUIs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SyxVQUFVLFVBQVosRUFBdEMsQ0FBbEI7QUFDQSxVQUFNVyxTQUFTLFNBQVRBLE1BQVMsQ0FBQ0MsQ0FBRDtBQUFBLGVBQVEsRUFBRVosVUFBVSxVQUFaLEVBQXdCVyxRQUFRQyxDQUFoQyxFQUFSO0FBQUEsT0FBZjs7QUFFQSxhQUNFO0FBQUE7QUFBQSxVQUFLLFdBQVd0Qyx1QkFBdUIsZ0JBQXZDLEVBQXlELE9BQU9rQyxTQUFoRTtBQUNFO0FBQUE7QUFBQSxZQUFLLE9BQU9HLE9BQU8sQ0FBUCxDQUFaO0FBQ0dMO0FBREgsU0FERjtBQUlFO0FBQUE7QUFBQSxZQUFLLE9BQU9LLE9BQU8sQ0FBUCxDQUFaO0FBQ0Usd0NBQUMsWUFBRDtBQURGO0FBSkYsT0FERjtBQVVEOzs7O0VBbkZ5QixnQkFBTUUsUzs7QUFvRmpDOztBQUVEdEMsY0FBY3VDLGlCQUFkLEdBQWtDO0FBQ2hDbkMsWUFBVSxvQkFBVW9DLElBRFk7QUFFaENqQyxlQUFhLG9CQUFVaUMsSUFGUztBQUdoQ2xDLGVBQWEsb0JBQVVrQyxJQUhTO0FBSWhDOUIsdUJBQXFCLG9CQUFVOEI7QUFKQyxDQUFsQzs7a0JBT2V4QyxhIiwiZmlsZSI6Ik1vZGFsQm91bmRhcnkuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IFByb3BUeXBlcyBmcm9tICdwcm9wLXR5cGVzJztcblxuaW1wb3J0IHsgdWlkLCBtYWtlQ2xhc3NpZmllciB9IGZyb20gJy4uL1V0aWxzL1V0aWxzJztcblxuY29uc3QgbW9kYWxCb3VuZGFyeUNsYXNzID0gbWFrZUNsYXNzaWZpZXIoJ01vZGFsQm91bmRhcnknKTtcblxuY2xhc3MgTW9kYWxCb3VuZGFyeSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcblxuICAgIHRoaXMuc3RhdGUgPSB7IG1vZGFsczogW10gfTtcblxuICAgIHRoaXMuYWRkTW9kYWwgPSB0aGlzLmFkZE1vZGFsLmJpbmQodGhpcyk7XG4gICAgdGhpcy5yZW1vdmVNb2RhbCA9IHRoaXMucmVtb3ZlTW9kYWwuYmluZCh0aGlzKTtcbiAgICB0aGlzLnVwZGF0ZU1vZGFsID0gdGhpcy51cGRhdGVNb2RhbC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuZ2V0Q2hpbGRDb250ZXh0ID0gdGhpcy5nZXRDaGlsZENvbnRleHQuYmluZCh0aGlzKTtcbiAgICB0aGlzLnJlbmRlck1vZGFsV3JhcHBlciA9IHRoaXMucmVuZGVyTW9kYWxXcmFwcGVyLmJpbmQodGhpcyk7XG4gICAgdGhpcy50cmlnZ2VyTW9kYWxSZWZyZXNoID0gdGhpcy50cmlnZ2VyTW9kYWxSZWZyZXNoLmJpbmQodGhpcyk7XG4gIH1cblxuICBhZGRNb2RhbCAobW9kYWwpIHtcbiAgICBsZXQgeyBtb2RhbHMgfSA9IHRoaXMuc3RhdGU7XG4gICAgbW9kYWwuX2lkID0gdWlkKCk7XG4gICAgbW9kYWxzLnB1c2gobW9kYWwpO1xuICAgIHRoaXMuc2V0U3RhdGUoeyBtb2RhbHMgfSk7XG4gICAgcmV0dXJuIG1vZGFsLl9pZDtcbiAgfVxuXG4gIHRyaWdnZXJNb2RhbFJlZnJlc2ggKCkge1xuICAgIHRoaXMuZm9yY2VVcGRhdGUoKTtcbiAgfVxuXG4gIHJlbW92ZU1vZGFsIChpZCkge1xuICAgIGxldCB7IG1vZGFscyB9ID0gdGhpcy5zdGF0ZTtcbiAgICBsZXQgaW5kZXggPSBtb2RhbHMuZmluZEluZGV4KG1vZGFsID0+IG1vZGFsLl9pZCA9PT0gaWQpO1xuICAgIGlmIChpbmRleCA8IDApIHJldHVybjtcbiAgICBtb2RhbHMuc3BsaWNlKGluZGV4LCAxKTtcbiAgICB0aGlzLnNldFN0YXRlKHsgbW9kYWxzIH0pO1xuICB9XG5cbiAgdXBkYXRlTW9kYWwgKGlkLCBtb2RhbCA9IHt9KSB7XG4gICAgbGV0IHsgbW9kYWxzIH0gPSB0aGlzLnN0YXRlO1xuICAgIGxldCBpbmRleCA9IG1vZGFscy5maW5kSW5kZXgobW9kYWwgPT4gbW9kYWwuX2lkID09PSBpZCk7XG4gICAgaWYgKGluZGV4IDwgMCkgcmV0dXJuO1xuICAgIG1vZGFscy5zcGxpY2UoaW5kZXgsIDEsIG1vZGFsKTtcbiAgICB0aGlzLnNldFN0YXRlKHsgbW9kYWxzIH0pO1xuICB9XG5cbiAgZ2V0Q2hpbGRDb250ZXh0ICgpIHtcbiAgICBjb25zdCB7IGFkZE1vZGFsLCByZW1vdmVNb2RhbCwgdXBkYXRlTW9kYWwsIHRyaWdnZXJNb2RhbFJlZnJlc2ggfSA9IHRoaXM7XG4gICAgcmV0dXJuIHsgYWRkTW9kYWwsIHJlbW92ZU1vZGFsLCB1cGRhdGVNb2RhbCwgdHJpZ2dlck1vZGFsUmVmcmVzaCB9O1xuICB9XG5cbiAgcmVuZGVyTW9kYWxXcmFwcGVyICgpIHtcbiAgICBjb25zdCB7IG1vZGFscyB9ID0gdGhpcy5zdGF0ZTtcbiAgICBjb25zdCBzdHlsZSA9IHtcbiAgICAgIHRvcDogMCxcbiAgICAgIGxlZnQ6IDAsXG4gICAgICB3aWR0aDogJzEwMHZ3JyxcbiAgICAgIGhlaWdodDogJzEwMHZoJyxcbiAgICAgIHBvc2l0aW9uOiAnZml4ZWQnLFxuICAgICAgcG9pbnRlckV2ZW50czogJ25vbmUnXG4gICAgfTtcbiAgICByZXR1cm4gIW1vZGFscy5sZW5ndGggPyBudWxsIDogKFxuICAgICAgPGRpdiBzdHlsZT17c3R5bGV9IGNsYXNzTmFtZT17bW9kYWxCb3VuZGFyeUNsYXNzKCdXcmFwcGVyJyl9PlxuICAgICAgICB7bW9kYWxzLm1hcCgobW9kYWwsIGluZGV4KSA9PiB7XG4gICAgICAgICAgY29uc3QgRWxlbWVudCA9IG1vZGFsLnJlbmRlcjtcbiAgICAgICAgICByZXR1cm4gPEVsZW1lbnQga2V5PXtpbmRleH0gey4uLm1vZGFsfSAvPlxuICAgICAgICB9KX1cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgY2hpbGRyZW4sIHN0eWxlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IE1vZGFsV3JhcHBlciA9IHRoaXMucmVuZGVyTW9kYWxXcmFwcGVyO1xuICAgIGNvbnN0IGZ1bGxTdHlsZSA9IE9iamVjdC5hc3NpZ24oe30sIHN0eWxlID8gc3R5bGUgOiB7fSwgeyBwb3NpdGlvbjogJ3JlbGF0aXZlJyB9KTtcbiAgICBjb25zdCB6SW5kZXggPSAoeikgPT4gKHsgcG9zaXRpb246ICdyZWxhdGl2ZScsIHpJbmRleDogeiB9KTtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT17bW9kYWxCb3VuZGFyeUNsYXNzKCkgKyAnIE1lc2FDb21wb25lbnQnfSBzdHlsZT17ZnVsbFN0eWxlfT5cbiAgICAgICAgPGRpdiBzdHlsZT17ekluZGV4KDEpfT5cbiAgICAgICAgICB7Y2hpbGRyZW59XG4gICAgICAgIDwvZGl2PlxuICAgICAgICA8ZGl2IHN0eWxlPXt6SW5kZXgoMil9PlxuICAgICAgICAgIDxNb2RhbFdyYXBwZXIgLz5cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59O1xuXG5Nb2RhbEJvdW5kYXJ5LmNoaWxkQ29udGV4dFR5cGVzID0ge1xuICBhZGRNb2RhbDogUHJvcFR5cGVzLmZ1bmMsXG4gIHVwZGF0ZU1vZGFsOiBQcm9wVHlwZXMuZnVuYyxcbiAgcmVtb3ZlTW9kYWw6IFByb3BUeXBlcy5mdW5jLFxuICB0cmlnZ2VyTW9kYWxSZWZyZXNoOiBQcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgTW9kYWxCb3VuZGFyeTtcbiJdfQ==