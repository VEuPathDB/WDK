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
    value: function renderActionItemList() {
      var actions = this.props.actions;

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
          eventHandlers = _props3.eventHandlers,
          children = _props3.children,
          options = _props3.options;

      var _ref2 = options ? options : {},
          selectedNoun = _ref2.selectedNoun,
          selectedPluralNoun = _ref2.selectedPluralNoun,
          isRowSelected = _ref2.isRowSelected;

      var _ref3 = eventHandlers ? eventHandlers : {},
          onRowSelect = _ref3.onRowSelect,
          onRowDeselect = _ref3.onRowDeselect,
          onMultipleRowSelect = _ref3.onMultipleRowSelect,
          onMultipleRowDeselect = _ref3.onMultipleRowDeselect;

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
        _react2.default.createElement(ActionList, null)
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9BY3Rpb25Ub29sYmFyLmpzeCJdLCJuYW1lcyI6WyJhY3Rpb25Ub29sYmFyQ2xhc3MiLCJBY3Rpb25Ub29sYmFyIiwicHJvcHMiLCJkaXNwYXRjaEFjdGlvbiIsImJpbmQiLCJyZW5kZXJBY3Rpb25JdGVtIiwicmVuZGVyQWN0aW9uSXRlbUxpc3QiLCJyb3dzIiwib3B0aW9ucyIsImlzUm93U2VsZWN0ZWQiLCJmaWx0ZXIiLCJhY3Rpb24iLCJoYW5kbGVyIiwiY2FsbGJhY2siLCJjb2x1bW5zIiwic2VsZWN0aW9uIiwiZ2V0U2VsZWN0aW9uIiwic2VsZWN0aW9uUmVxdWlyZWQiLCJsZW5ndGgiLCJmb3JFYWNoIiwicm93IiwiZWxlbWVudCIsImRpc2FibGVkIiwiaXNWYWxpZEVsZW1lbnQiLCJfX2lkIiwiYWN0aW9ucyIsIkFjdGlvbkl0ZW0iLCJtYXAiLCJpZHgiLCJldmVudEhhbmRsZXJzIiwiY2hpbGRyZW4iLCJzZWxlY3RlZE5vdW4iLCJzZWxlY3RlZFBsdXJhbE5vdW4iLCJvblJvd1NlbGVjdCIsIm9uUm93RGVzZWxlY3QiLCJvbk11bHRpcGxlUm93U2VsZWN0Iiwib25NdWx0aXBsZVJvd0Rlc2VsZWN0IiwiQWN0aW9uTGlzdCIsInNlbGVjdGlvbkNvdW50ZXJQcm9wcyIsIlB1cmVDb21wb25lbnQiLCJwcm9wVHlwZXMiLCJhcnJheSIsIm9iamVjdCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7O0FBRUE7Ozs7QUFDQTs7Ozs7Ozs7OztBQUVBLElBQU1BLHFCQUFxQiwyQkFBZSxlQUFmLENBQTNCOztJQUVNQyxhOzs7QUFDSix5QkFBYUMsS0FBYixFQUFvQjtBQUFBOztBQUFBLDhIQUNaQSxLQURZOztBQUVsQixVQUFLQyxjQUFMLEdBQXNCLE1BQUtBLGNBQUwsQ0FBb0JDLElBQXBCLE9BQXRCO0FBQ0EsVUFBS0MsZ0JBQUwsR0FBd0IsTUFBS0EsZ0JBQUwsQ0FBc0JELElBQXRCLE9BQXhCO0FBQ0Es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bGUiOiJBY3Rpb25Ub29sYmFyLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcbmltcG9ydCBQcm9wVHlwZXMgZnJvbSAncHJvcC10eXBlcyc7XG5cbmltcG9ydCBTZWxlY3Rpb25Db3VudGVyIGZyb20gJy4uL1VpL1NlbGVjdGlvbkNvdW50ZXInO1xuaW1wb3J0IHsgbWFrZUNsYXNzaWZpZXIgfSBmcm9tICcuLi9VdGlscy9VdGlscyc7XG5cbmNvbnN0IGFjdGlvblRvb2xiYXJDbGFzcyA9IG1ha2VDbGFzc2lmaWVyKCdBY3Rpb25Ub29sYmFyJyk7XG5cbmNsYXNzIEFjdGlvblRvb2xiYXIgZXh0ZW5kcyBSZWFjdC5QdXJlQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuICAgIHRoaXMuZGlzcGF0Y2hBY3Rpb24gPSB0aGlzLmRpc3BhdGNoQWN0aW9uLmJpbmQodGhpcyk7XG4gICAgdGhpcy5yZW5kZXJBY3Rpb25JdGVtID0gdGhpcy5yZW5kZXJBY3Rpb25JdGVtLmJpbmQodGhpcyk7XG4gICAgdGhpcy5yZW5kZXJBY3Rpb25JdGVtTGlzdCA9IHRoaXMucmVuZGVyQWN0aW9uSXRlbUxpc3QuYmluZCh0aGlzKTtcbiAgfVxuXG4gIGdldFNlbGVjdGlvbiAoKSB7XG4gICAgY29uc3QgeyByb3dzLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgaXNSb3dTZWxlY3RlZCB9ID0gb3B0aW9ucztcblxuICAgIGlmICh0eXBlb2YgaXNSb3dTZWxlY3RlZCAhPT0gJ2Z1bmN0aW9uJykgcmV0dXJuIFtdO1xuICAgIHJldHVybiByb3dzLmZpbHRlcihpc1Jvd1NlbGVjdGVkKTtcbiAgfVxuXG4gIGRpc3BhdGNoQWN0aW9uIChhY3Rpb24pIHtcbiAgICBjb25zdCB7IGhhbmRsZXIsIGNhbGxiYWNrIH0gPSBhY3Rpb247XG4gICAgY29uc3QgeyByb3dzLCBjb2x1bW5zIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHNlbGVjdGlvbiA9IHRoaXMuZ2V0U2VsZWN0aW9uKCk7XG5cbiAgICBpZiAoYWN0aW9uLnNlbGVjdGlvblJlcXVpcmVkICYmICFzZWxlY3Rpb24ubGVuZ3RoKSByZXR1cm47XG4gICAgaWYgKHR5cGVvZiBoYW5kbGVyID09PSAnZnVuY3Rpb24nKSBzZWxlY3Rpb24uZm9yRWFjaChyb3cgPT4gaGFuZGxlcihyb3csIGNvbHVtbnMpKTtcbiAgICBpZiAodHlwZW9mIGNhbGxiYWNrID09PSAnZnVuY3Rpb24nKSByZXR1cm4gY2FsbGJhY2soc2VsZWN0aW9uLCBjb2x1bW5zLCByb3dzKTtcbiAgfVxuXG4gIHJlbmRlckFjdGlvbkl0ZW0gKHsgYWN0aW9uIH0pIHtcbiAgICBsZXQgeyBlbGVtZW50IH0gPSBhY3Rpb247XG4gICAgbGV0IHNlbGVjdGlvbiA9IHRoaXMuZ2V0U2VsZWN0aW9uKCk7XG4gICAgbGV0IGRpc2FibGVkID0gKGFjdGlvbi5zZWxlY3Rpb25SZXF1aXJlZCAmJiAhc2VsZWN0aW9uLmxlbmd0aCA/ICdkaXNhYmxlZCcgOiBudWxsKTtcblxuICAgIGlmICh0eXBlb2YgZWxlbWVudCAhPT0gJ3N0cmluZycgJiYgIVJlYWN0LmlzVmFsaWRFbGVtZW50KGVsZW1lbnQpKSB7XG4gICAgICBpZiAodHlwZW9mIGVsZW1lbnQgPT09ICdmdW5jdGlvbicpIGVsZW1lbnQgPSBlbGVtZW50KHNlbGVjdGlvbik7XG4gICAgfVxuXG4gICAgbGV0IGhhbmRsZXIgPSAoKSA9PiB0aGlzLmRpc3BhdGNoQWN0aW9uKGFjdGlvbik7XG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXZcbiAgICAgICAga2V5PXthY3Rpb24uX19pZH1cbiAgICAgICAgb25DbGljaz17aGFuZGxlcn1cbiAgICAgICAgY2xhc3NOYW1lPXthY3Rpb25Ub29sYmFyQ2xhc3MoJ0l0ZW0nLCBkaXNhYmxlZCl9PlxuICAgICAgICB7ZWxlbWVudH1cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cblxuICByZW5kZXJBY3Rpb25JdGVtTGlzdCAoKSB7XG4gICAgY29uc3QgeyBhY3Rpb25zIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IEFjdGlvbkl0ZW0gPSB0aGlzLnJlbmRlckFjdGlvbkl0ZW07XG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPXthY3Rpb25Ub29sYmFyQ2xhc3MoJ0l0ZW1MaXN0Jyl9PlxuICAgICAgICB7IWFjdGlvbnMgPyBudWxsIDogYWN0aW9uc1xuICAgICAgICAgIC5maWx0ZXIoYWN0aW9uID0+IGFjdGlvbi5lbGVtZW50KVxuICAgICAgICAgIC5tYXAoKGFjdGlvbiwgaWR4KSA9PiA8QWN0aW9uSXRlbSBhY3Rpb249e2FjdGlvbn0ga2V5PXtpZHh9IC8+KVxuICAgICAgICB9XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG5cbiAgcmVuZGVyICgpIHtcbiAgICBjb25zdCB7IHJvd3MsIGV2ZW50SGFuZGxlcnMsIGNoaWxkcmVuLCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgc2VsZWN0ZWROb3VuLCBzZWxlY3RlZFBsdXJhbE5vdW4sIGlzUm93U2VsZWN0ZWQgfSA9IG9wdGlvbnMgPyBvcHRpb25zIDoge307XG4gICAgY29uc3QgeyBvblJvd1NlbGVjdCwgb25Sb3dEZXNlbGVjdCwgb25NdWx0aXBsZVJvd1NlbGVjdCwgb25NdWx0aXBsZVJvd0Rlc2VsZWN0IH0gPSBldmVudEhhbmRsZXJzID8gZXZlbnRIYW5kbGVycyA6IHt9O1xuXG4gICAgY29uc3QgQWN0aW9uTGlzdCA9IHRoaXMucmVuZGVyQWN0aW9uSXRlbUxpc3Q7XG4gICAgY29uc3Qgc2VsZWN0aW9uID0gdGhpcy5nZXRTZWxlY3Rpb24oKTtcblxuICAgIGNvbnN0IHNlbGVjdGlvbkNvdW50ZXJQcm9wcyA9IHsgcm93cywgaXNSb3dTZWxlY3RlZCwgb25Sb3dTZWxlY3QsIG9uUm93RGVzZWxlY3QsIG9uTXVsdGlwbGVSb3dTZWxlY3QsIG9uTXVsdGlwbGVSb3dEZXNlbGVjdCwgc2VsZWN0ZWROb3VuLCBzZWxlY3RlZFBsdXJhbE5vdW4gfTtcblxuICAgIHJldHVybiAoXG4gICAgICAgPGRpdiBjbGFzc05hbWU9e2FjdGlvblRvb2xiYXJDbGFzcygpICsgJyBUb29sYmFyJ30+XG4gICAgICAgICB7IWNoaWxkcmVuID8gbnVsbCA6IChcbiAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9e2FjdGlvblRvb2xiYXJDbGFzcygnQ2hpbGRyZW4nKX0+XG4gICAgICAgICAgICAge2NoaWxkcmVufVxuICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICl9XG4gICAgICAgICA8ZGl2IGNsYXNzTmFtZT17YWN0aW9uVG9vbGJhckNsYXNzKCdJbmZvJyl9PlxuICAgICAgICAgICA8U2VsZWN0aW9uQ291bnRlciB7Li4uc2VsZWN0aW9uQ291bnRlclByb3BzfSAvPlxuICAgICAgICAgPC9kaXY+XG4gICAgICAgICA8QWN0aW9uTGlzdCAvPlxuICAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn07XG5cbkFjdGlvblRvb2xiYXIucHJvcFR5cGVzID0ge1xuICByb3dzOiBQcm9wVHlwZXMuYXJyYXksXG4gIGFjdGlvbnM6IFByb3BUeXBlcy5hcnJheSxcbiAgb3B0aW9uczogUHJvcFR5cGVzLm9iamVjdCxcbiAgZXZlbnRIYW5kbGVyczogUHJvcFR5cGVzLm9iamVjdFxufTtcblxuZXhwb3J0IGRlZmF1bHQgQWN0aW9uVG9vbGJhcjtcbiJdfQ==