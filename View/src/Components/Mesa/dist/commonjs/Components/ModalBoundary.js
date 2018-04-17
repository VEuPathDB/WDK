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

      if (!'id' in modal) throw new Error('Modals must have an "id" property.');
      if (modals.indexOf(modal) < 0) modals.push(modal);
      this.setState({ modals: modals });
    }
  }, {
    key: 'removeModal',
    value: function removeModal(id) {
      var modals = this.state.modals;

      var index = modals.findIndex(function (modal) {
        return modal.id === id;
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
      console.log('rendering with modals...', modals);
      return !modals.length ? null : _react2.default.createElement(
        'div',
        { style: style, className: modalBoundaryClass('Wrapper') },
        modals.map(function (modal, index) {
          console.log('rendering modal:', modal);
          var Element = modal.render;
          console.log('element:', Element);
          return _react2.default.createElement(Element, { key: index });
        })
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var children = this.props.children;

      var ModalWrapper = this.renderModalWrapper;
      var style = { position: 'relative' };
      var zIndex = function zIndex(z) {
        return { position: 'relative', zIndex: z };
      };

      return _react2.default.createElement(
        'div',
        { className: modalBoundaryClass(), style: style },
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