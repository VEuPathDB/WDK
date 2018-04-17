'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

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
    key: 'getChildContext',
    value: function getChildContext() {
      var addModal = this.addModal,
          removeModal = this.removeModal;

      return { addModal: addModal, removeModal: removeModal };
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
          return _react2.default.createElement(Element, { key: index });
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
  removeModal: _propTypes2.default.func
};

exports.default = ModalBoundary;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL01vZGFsQm91bmRhcnkuanN4Il0sIm5hbWVzIjpbIm1vZGFsQm91bmRhcnlDbGFzcyIsIk1vZGFsQm91bmRhcnkiLCJwcm9wcyIsInN0YXRlIiwibW9kYWxzIiwiYWRkTW9kYWwiLCJiaW5kIiwicmVtb3ZlTW9kYWwiLCJnZXRDaGlsZENvbnRleHQiLCJyZW5kZXJNb2RhbFdyYXBwZXIiLCJtb2RhbCIsIl9pZCIsInB1c2giLCJzZXRTdGF0ZSIsImlkIiwiaW5kZXgiLCJmaW5kSW5kZXgiLCJzcGxpY2UiLCJzdHlsZSIsInRvcCIsImxlZnQiLCJ3aWR0aCIsImhlaWdodCIsInBvc2l0aW9uIiwicG9pbnRlckV2ZW50cyIsImxlbmd0aCIsIm1hcCIsIkVsZW1lbnQiLCJyZW5kZXIiLCJjaGlsZHJlbiIsIk1vZGFsV3JhcHBlciIsImZ1bGxTdHlsZSIsIk9iamVjdCIsImFzc2lnbiIsInpJbmRleCIsInoiLCJDb21wb25lbnQiLCJjaGlsZENvbnRleHRUeXBlcyIsImZ1bmMiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7O0FBQUE7Ozs7QUFDQTs7OztBQUVBOzs7Ozs7Ozs7O0FBRUEsSUFBTUEscUJBQXFCLDJCQUFlLGVBQWYsQ0FBM0I7O0lBRU1DLGE7OztBQUNKLHlCQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsOEhBQ1pBLEtBRFk7O0FBR2xCLFVBQUtDLEtBQUwsR0FBYTtBQUNYQyxjQUFRO0FBREcsS0FBYjs7QUFJQSxVQUFLQyxRQUFMLEdBQWdCLE1BQUtBLFFBQUwsQ0FBY0MsSUFBZCxPQUFoQjtBQUNBLFVBQUtDLFdBQUwsR0FBbUIsTUFBS0EsV0FBTCxDQUFpQkQsSUFBakIsT0FBbkI7QUFDQSxVQUFLRSxlQUFMLEdBQXVCLE1BQUtBLGVBQUwsQ0FBcUJGLElBQXJCLE9BQXZCO0FBQ0EsVUFBS0csa0JBQUwsR0FBMEIsTUFBS0Esa0JBQUwsQ0FBd0JILElBQXhCLE9BQTFCO0FBVmtCO0FBV25COzs7OzZCQUVTSSxLLEVBQU87QUFBQSxVQUNUTixNQURTLEdBQ0UsS0FBS0QsS0FEUCxDQUNUQyxNQURTOztBQUVmTSxZQUFNQyxHQUFOLEdBQVksaUJBQVo7QUFDQVAsYUFBT1EsSUFBUCxDQUFZRixLQUFaO0FBQ0EsV0FBS0csUUFBTCxDQUFjLEVBQUVULGNBQUYsRUFBZDtBQUNBLGFBQU9NLE1BQU1DLEdBQWI7QUFDRDs7O2dDQUVZRyxFLEVBQUk7QUFBQSxVQUNUVixNQURTLEdBQ0UsS0FBS0QsS0FEUCxDQUNUQyxNQURTOztBQUVmLFVBQUlXLFFBQVFYLE9BQU9ZLFNBQVAsQ0FBaUI7QUFBQSxlQUFTTixNQUFNQyxHQUFOLEtBQWNHLEVBQXZCO0FBQUEsT0FBakIsQ0FBWjtBQUNBLFVBQUlDLFFBQVEsQ0FBWixFQUFlO0FBQ2ZYLGFBQU9hLE1BQVAsQ0FBY0YsS0FBZCxFQUFxQixDQUFyQjtBQUNBLFdBQUtGLFFBQUwsQ0FBYyxFQUFFVCxjQUFGLEVBQWQ7QUFDRDs7O3NDQUVrQjtBQUFBLFVBQ1RDLFFBRFMsR0FDaUIsSUFEakIsQ0FDVEEsUUFEUztBQUFBLFVBQ0NFLFdBREQsR0FDaUIsSUFEakIsQ0FDQ0EsV0FERDs7QUFFakIsYUFBTyxFQUFFRixrQkFBRixFQUFZRSx3QkFBWixFQUFQO0FBQ0Q7Ozt5Q0FFcUI7QUFBQSxVQUNaSCxNQURZLEdBQ0QsS0FBS0QsS0FESixDQUNaQyxNQURZOztBQUVwQixVQUFNYyxRQUFRO0FBQ1pDLGFBQUssQ0FETztBQUVaQyxjQUFNLENBRk07QUFHWkMsZUFBTyxPQUhLO0FBSVpDLGdCQUFRLE9BSkk7QUFLWkMsa0JBQVUsT0FMRTtBQU1aQyx1QkFBZTtBQU5ILE9BQWQ7QUFRQSxhQUFPLENBQUNwQixPQUFPcUIsTUFBUixHQUFpQixJQUFqQixHQUNMO0FBQUE7QUFBQSxVQUFLLE9BQU9QLEtBQVosRUFBbUIsV0FBV2xCLG1CQUFtQixTQUFuQixDQUE5QjtBQUNHSSxlQUFPc0IsR0FBUCxDQUFXLFVBQUNoQixLQUFELEVBQVFLLEtBQVIsRUFBa0I7QUFDNUIsY0FBTVksVUFBVWpCLE1BQU1rQixNQUF0QjtBQUNBLGlCQUFPLDhCQUFDLE9BQUQsSUFBUyxLQUFLYixLQUFkLEdBQVA7QUFDRCxTQUhBO0FBREgsT0FERjtBQVFEOzs7NkJBRVM7QUFBQSxtQkFDb0IsS0FBS2IsS0FEekI7QUFBQSxVQUNBMkIsUUFEQSxVQUNBQSxRQURBO0FBQUEsVUFDVVgsS0FEVixVQUNVQSxLQURWOztBQUVSLFVBQU1ZLGVBQWUsS0FBS3JCLGtCQUExQjtBQUNBLFVBQU1zQixZQUFZQyxPQUFPQyxNQUFQLENBQWMsRUFBZCxFQUFrQmYsUUFBUUEsS0FBUixHQUFnQixFQUFsQyxFQUFzQyxFQUFFSyxVQUFVLFVBQVosRUFBdEMsQ0FBbEI7QUFDQSxVQUFNVyxTQUFTLFNBQVRBLE1BQVMsQ0FBQ0MsQ0FBRDtBQUFBLGVBQVEsRUFBRVosVUFBVSxVQUFaLEVBQXdCVyxRQUFRQyxDQUFoQyxFQUFSO0FBQUEsT0FBZjs7QUFFQSxhQUNFO0FBQUE7QUFBQSxVQUFLLFdBQVduQyx1QkFBdUIsZ0JBQXZDLEVBQXlELE9BQU8rQixTQUFoRTtBQUNFO0FBQUE7QUFBQSxZQUFLLE9BQU9HLE9BQU8sQ0FBUCxDQUFaO0FBQ0dMO0FBREgsU0FERjtBQUlFO0FBQUE7QUFBQSxZQUFLLE9BQU9LLE9BQU8sQ0FBUCxDQUFaO0FBQ0Usd0NBQUMsWUFBRDtBQURGO0FBSkYsT0FERjtBQVVEOzs7O0VBdkV5QixnQkFBTUUsUzs7QUF3RWpDOztBQUVEbkMsY0FBY29DLGlCQUFkLEdBQWtDO0FBQ2hDaEMsWUFBVSxvQkFBVWlDLElBRFk7QUFFaEMvQixlQUFhLG9CQUFVK0I7QUFGUyxDQUFsQzs7a0JBS2VyQyxhIiwiZmlsZSI6Ik1vZGFsQm91bmRhcnkuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IFByb3BUeXBlcyBmcm9tICdwcm9wLXR5cGVzJztcblxuaW1wb3J0IHsgdWlkLCBtYWtlQ2xhc3NpZmllciB9IGZyb20gJy4uL1V0aWxzL1V0aWxzJztcblxuY29uc3QgbW9kYWxCb3VuZGFyeUNsYXNzID0gbWFrZUNsYXNzaWZpZXIoJ01vZGFsQm91bmRhcnknKTtcblxuY2xhc3MgTW9kYWxCb3VuZGFyeSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcblxuICAgIHRoaXMuc3RhdGUgPSB7XG4gICAgICBtb2RhbHM6IFtdXG4gICAgfTtcblxuICAgIHRoaXMuYWRkTW9kYWwgPSB0aGlzLmFkZE1vZGFsLmJpbmQodGhpcyk7XG4gICAgdGhpcy5yZW1vdmVNb2RhbCA9IHRoaXMucmVtb3ZlTW9kYWwuYmluZCh0aGlzKTtcbiAgICB0aGlzLmdldENoaWxkQ29udGV4dCA9IHRoaXMuZ2V0Q2hpbGRDb250ZXh0LmJpbmQodGhpcyk7XG4gICAgdGhpcy5yZW5kZXJNb2RhbFdyYXBwZXIgPSB0aGlzLnJlbmRlck1vZGFsV3JhcHBlci5iaW5kKHRoaXMpO1xuICB9XG5cbiAgYWRkTW9kYWwgKG1vZGFsKSB7XG4gICAgbGV0IHsgbW9kYWxzIH0gPSB0aGlzLnN0YXRlO1xuICAgIG1vZGFsLl9pZCA9IHVpZCgpO1xuICAgIG1vZGFscy5wdXNoKG1vZGFsKTtcbiAgICB0aGlzLnNldFN0YXRlKHsgbW9kYWxzIH0pO1xuICAgIHJldHVybiBtb2RhbC5faWQ7XG4gIH1cblxuICByZW1vdmVNb2RhbCAoaWQpIHtcbiAgICBsZXQgeyBtb2RhbHMgfSA9IHRoaXMuc3RhdGU7XG4gICAgbGV0IGluZGV4ID0gbW9kYWxzLmZpbmRJbmRleChtb2RhbCA9PiBtb2RhbC5faWQgPT09IGlkKTtcbiAgICBpZiAoaW5kZXggPCAwKSByZXR1cm47XG4gICAgbW9kYWxzLnNwbGljZShpbmRleCwgMSk7XG4gICAgdGhpcy5zZXRTdGF0ZSh7IG1vZGFscyB9KTtcbiAgfVxuXG4gIGdldENoaWxkQ29udGV4dCAoKSB7XG4gICAgY29uc3QgeyBhZGRNb2RhbCwgcmVtb3ZlTW9kYWwgfSA9IHRoaXM7XG4gICAgcmV0dXJuIHsgYWRkTW9kYWwsIHJlbW92ZU1vZGFsIH07XG4gIH1cblxuICByZW5kZXJNb2RhbFdyYXBwZXIgKCkge1xuICAgIGNvbnN0IHsgbW9kYWxzIH0gPSB0aGlzLnN0YXRlO1xuICAgIGNvbnN0IHN0eWxlID0ge1xuICAgICAgdG9wOiAwLFxuICAgICAgbGVmdDogMCxcbiAgICAgIHdpZHRoOiAnMTAwdncnLFxuICAgICAgaGVpZ2h0OiAnMTAwdmgnLFxuICAgICAgcG9zaXRpb246ICdmaXhlZCcsXG4gICAgICBwb2ludGVyRXZlbnRzOiAnbm9uZSdcbiAgICB9O1xuICAgIHJldHVybiAhbW9kYWxzLmxlbmd0aCA/IG51bGwgOiAoXG4gICAgICA8ZGl2IHN0eWxlPXtzdHlsZX0gY2xhc3NOYW1lPXttb2RhbEJvdW5kYXJ5Q2xhc3MoJ1dyYXBwZXInKX0+XG4gICAgICAgIHttb2RhbHMubWFwKChtb2RhbCwgaW5kZXgpID0+IHtcbiAgICAgICAgICBjb25zdCBFbGVtZW50ID0gbW9kYWwucmVuZGVyO1xuICAgICAgICAgIHJldHVybiA8RWxlbWVudCBrZXk9e2luZGV4fSAvPlxuICAgICAgICB9KX1cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgY2hpbGRyZW4sIHN0eWxlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IE1vZGFsV3JhcHBlciA9IHRoaXMucmVuZGVyTW9kYWxXcmFwcGVyO1xuICAgIGNvbnN0IGZ1bGxTdHlsZSA9IE9iamVjdC5hc3NpZ24oe30sIHN0eWxlID8gc3R5bGUgOiB7fSwgeyBwb3NpdGlvbjogJ3JlbGF0aXZlJyB9KTtcbiAgICBjb25zdCB6SW5kZXggPSAoeikgPT4gKHsgcG9zaXRpb246ICdyZWxhdGl2ZScsIHpJbmRleDogeiB9KTtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT17bW9kYWxCb3VuZGFyeUNsYXNzKCkgKyAnIE1lc2FDb21wb25lbnQnfSBzdHlsZT17ZnVsbFN0eWxlfT5cbiAgICAgICAgPGRpdiBzdHlsZT17ekluZGV4KDEpfT5cbiAgICAgICAgICB7Y2hpbGRyZW59XG4gICAgICAgIDwvZGl2PlxuICAgICAgICA8ZGl2IHN0eWxlPXt6SW5kZXgoMil9PlxuICAgICAgICAgIDxNb2RhbFdyYXBwZXIgLz5cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59O1xuXG5Nb2RhbEJvdW5kYXJ5LmNoaWxkQ29udGV4dFR5cGVzID0ge1xuICBhZGRNb2RhbDogUHJvcFR5cGVzLmZ1bmMsXG4gIHJlbW92ZU1vZGFsOiBQcm9wVHlwZXMuZnVuY1xufTtcblxuZXhwb3J0IGRlZmF1bHQgTW9kYWxCb3VuZGFyeTtcbiJdfQ==