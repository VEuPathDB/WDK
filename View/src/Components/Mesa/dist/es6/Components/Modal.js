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

var modalClass = (0, _Utils.makeClassifier)('Modal');

// TODO: Delete or rewrite me.

var Modal = function (_React$Component) {
  _inherits(Modal, _React$Component);

  function Modal() {
    _classCallCheck(this, Modal);

    return _possibleConstructorReturn(this, (Modal.__proto__ || Object.getPrototypeOf(Modal)).apply(this, arguments));
  }

  _createClass(Modal, [{
    key: 'render',
    value: function render() {
      return null;
    }
  }]);

  return Modal;
}(_react2.default.Component);

;

Modal.contextTypes = {
  addModal: _propTypes2.default.func,
  removeModal: _propTypes2.default.func
};

exports.default = Modal;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9Db21wb25lbnRzL01vZGFsLmpzeCJdLCJuYW1lcyI6WyJtb2RhbENsYXNzIiwiTW9kYWwiLCJDb21wb25lbnQiLCJjb250ZXh0VHlwZXMiLCJhZGRNb2RhbCIsImZ1bmMiLCJyZW1vdmVNb2RhbCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7O0FBRUE7Ozs7Ozs7Ozs7QUFFQSxJQUFNQSxhQUFhLDJCQUFlLE9BQWYsQ0FBbkI7O0FBRUE7O0lBRU1DLEs7Ozs7Ozs7Ozs7OzZCQUNNO0FBQ1IsYUFBTyxJQUFQO0FBQ0Q7Ozs7RUFIaUIsZ0JBQU1DLFM7O0FBSXpCOztBQUVERCxNQUFNRSxZQUFOLEdBQXFCO0FBQ25CQyxZQUFVLG9CQUFVQyxJQUREO0FBRW5CQyxlQUFhLG9CQUFVRDtBQUZKLENBQXJCOztrQkFLZUosSyIsImZpbGUiOiJNb2RhbC5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5pbXBvcnQgUHJvcFR5cGVzIGZyb20gJ3Byb3AtdHlwZXMnO1xuXG5pbXBvcnQgeyBtYWtlQ2xhc3NpZmllciB9IGZyb20gJy4uL1V0aWxzL1V0aWxzJztcblxuY29uc3QgbW9kYWxDbGFzcyA9IG1ha2VDbGFzc2lmaWVyKCdNb2RhbCcpO1xuXG4vLyBUT0RPOiBEZWxldGUgb3IgcmV3cml0ZSBtZS5cblxuY2xhc3MgTW9kYWwgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICByZW5kZXIgKCkge1xuICAgIHJldHVybiBudWxsO1xuICB9XG59O1xuXG5Nb2RhbC5jb250ZXh0VHlwZXMgPSB7XG4gIGFkZE1vZGFsOiBQcm9wVHlwZXMuZnVuYyxcbiAgcmVtb3ZlTW9kYWw6IFByb3BUeXBlcy5mdW5jXG59O1xuXG5leHBvcnQgZGVmYXVsdCBNb2RhbDtcbiJdfQ==