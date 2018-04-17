'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _SelectionCounter = require('../Ui/SelectionCounter');

var _SelectionCounter2 = _interopRequireDefault(_SelectionCounter);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ActionToolbar = function (_React$PureComponent) {
  _inherits(ActionToolbar, _React$PureComponent);

  function ActionToolbar(props) {
    _classCallCheck(this, ActionToolbar);

    var _this = _possibleConstructorReturn(this, (ActionToolbar.__proto__ || Object.getPrototypeOf(ActionToolbar)).call(this, props));

    _this.dispatchAction = _this.dispatchAction.bind(_this);
    _this.renderActionItem = _this.renderActionItem.bind(_this);
    return _this;
  }

  _createClass(ActionToolbar, [{
    key: 'getSelectedRows',
    value: function getSelectedRows() {
      var state = this.props.state;
      var rows = state.rows,
          ui = state.ui;
      var selection = ui.selection;

      return selection.map(function (id) {
        return rows.find(function (row) {
          return row.__id === id;
        });
      }).filter(function (row) {
        return row;
      });
    }
  }, {
    key: 'dispatchAction',
    value: function dispatchAction(action) {
      var handler = action.handler,
          callback = action.callback;
      var _props$state = this.props.state,
          columns = _props$state.columns,
          rows = _props$state.rows;

      var selectedRows = this.getSelectedRows();

      if (typeof handler === 'function') selectedRows.forEach(function (row) {
        return handler(row, columns);
      });
      if (typeof callback === 'function') callback(selectedRows, columns, rows);
    }
  }, {
    key: 'renderActionItem',
    value: function renderActionItem(action) {
      var _this2 = this;

      var element = action.element;

      var selectedRows = this.getSelectedRows();
      var className = 'ActionToolbar-Item' + (action.selectionRequired && !selectedRows.length ? ' disabled' : '');

      if (typeof element !== 'string' && !_react2.default.isValidElement(element)) {
        if (typeof element === 'function') element = element(selectedRows);
      }
      var handler = function handler() {
        return _this2.dispatchAction(action);
      };
      return _react2.default.createElement(
        'div',
        { key: action.__id, className: className, onClick: handler },
        element
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var _props = this.props,
          state = _props.state,
          dispatch = _props.dispatch,
          filteredRows = _props.filteredRows;
      var actions = state.actions;


      var list = actions.filter(function (action) {
        return action.element;
      }).map(this.renderActionItem);

      return _react2.default.createElement(
        'div',
        { className: 'Toolbar ActionToolbar' },
        _react2.default.createElement(
          'div',
          { className: 'ActionToolbar-Info' },
          _react2.default.createElement(_SelectionCounter2.default, {
            state: state,
            dispatch: dispatch,
            filteredRows: filteredRows
          })
        ),
        list
      );
    }
  }]);

  return ActionToolbar;
}(_react2.default.PureComponent);

;

exports.default = ActionToolbar;