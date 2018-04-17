'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _SelectionCounter = require('../Ui/SelectionCounter');

var _SelectionCounter2 = _interopRequireDefault(_SelectionCounter);

var _Utils = require('../Utils/Utils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var actionToolbarClass = (0, _Utils.makeClassifier)('ActionToolbar');

var ActionToolbar = function (_React$PureComponent) {
  _inherits(ActionToolbar, _React$PureComponent);

  function ActionToolbar(props) {
    _classCallCheck(this, ActionToolbar);

    var _this = _possibleConstructorReturn(this, (ActionToolbar.__proto__ || Object.getPrototypeOf(ActionToolbar)).call(this, props));

    _this.dispatchAction = _this.dispatchAction.bind(_this);
    _this.renderActionItem = _this.renderActionItem.bind(_this);
    _this.renderActionItemList = _this.renderActionItemList.bind(_this);
    return _this;
  }

  _createClass(ActionToolbar, [{
    key: 'getSelection',
    value: function getSelection() {
      var _props = this.props,
          rows = _props.rows,
          options = _props.options;
      var isRowSelected = options.isRowSelected;


      if (typeof isRowSelected !== 'function') return [];
      return rows.filter(isRowSelected);
    }
  }, {
    key: 'dispatchAction',
    value: function dispatchAction(action) {
      var handler = action.handler,
          callback = action.callback;
      var _props2 = this.props,
          rows = _props2.rows,
          columns = _props2.columns;

      var selection = this.getSelection();

      if (action.selectionRequired && !selection.length) return;
      if (typeof handler === 'function') selection.forEach(function (row) {
        return handler(row, columns);
      });
      if (typeof callback === 'function') return callback(selection, columns, rows);
    }
  }, {
    key: 'renderActionItem',
    value: function renderActionItem(_ref) {
      var _this2 = this;

      var action = _ref.action;
      var element = action.element;

      var selection = this.getSelection();
      var disabled = action.selectionRequired && !selection.length ? 'disabled' : null;

      if (typeof element !== 'string' && !_react2.default.isValidElement(element)) {
        if (typeof element === 'function') element = element(selection);
      }

      var handler = function handler() {
        return _this2.dispatchAction(action);
      };
      return _react2.default.createElement(
        'div',
        {
          key: action.__id,
          onClick: handler,
          className: actionToolbarClass('Item', disabled) },
        element
      );
    }
  }, {
    key: 'renderActionItemList',
    value: function renderActionItemList(_ref2) {
      var actions = _ref2.actions;

      var ActionItem = this.renderActionItem;
      return _react2.default.createElement(
        'div',
        { className: actionToolbarClass('ItemList') },
        !actions ? null : actions.filter(function (action) {
          return action.element;
        }).map(function (action, idx) {
          return _react2.default.createElement(ActionItem, { action: action, key: idx });
        })
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var _props3 = this.props,
          rows = _props3.rows,
          actions = _props3.actions,
          eventHandlers = _props3.eventHandlers,
          children = _props3.children,
          options = _props3.options;

      var _ref3 = options ? options : {},
          selectedNoun = _ref3.selectedNoun,
          selectedPluralNoun = _ref3.selectedPluralNoun,
          isRowSelected = _ref3.isRowSelected;

      var _ref4 = eventHandlers ? eventHandlers : {},
          onRowSelect = _ref4.onRowSelect,
          onRowDeselect = _ref4.onRowDeselect,
          onMultipleRowSelect = _ref4.onMultipleRowSelect,
          onMultipleRowDeselect = _ref4.onMultipleRowDeselect;

      var ActionList = this.renderActionItemList;
      var selection = this.getSelection();

      var selectionCounterProps = { rows: rows, isRowSelected: isRowSelected, onRowSelect: onRowSelect, onRowDeselect: onRowDeselect, onMultipleRowSelect: onMultipleRowSelect, onMultipleRowDeselect: onMultipleRowDeselect, selectedNoun: selectedNoun, selectedPluralNoun: selectedPluralNoun };

      return _react2.default.createElement(
        'div',
        { className: actionToolbarClass() + ' Toolbar' },
        !children ? null : _react2.default.createElement(
          'div',
          { className: actionToolbarClass('Children') },
          children
        ),
        _react2.default.createElement(
          'div',
          { className: actionToolbarClass('Info') },
          _react2.default.createElement(_SelectionCounter2.default, selectionCounterProps)
        ),
        _react2.default.createElement(ActionList, { actions: actions })
      );
    }
  }]);

  return ActionToolbar;
}(_react2.default.PureComponent);

;

ActionToolbar.propTypes = {
  rows: _propTypes2.default.array,
  actions: _propTypes2.default.array,
  options: _propTypes2.default.object,
  eventHandlers: _propTypes2.default.object
};

exports.default = ActionToolbar;