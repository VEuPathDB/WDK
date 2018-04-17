'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Actions = require('../State/Actions');

var _Events = require('../Utils/Events');

var _Events2 = _interopRequireDefault(_Events);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

var _Toggle = require('../Components/Toggle');

var _Toggle2 = _interopRequireDefault(_Toggle);

var _Utils = require('../Utils/Utils');

var _Utils2 = _interopRequireDefault(_Utils);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ColumnFilter = function (_React$PureComponent) {
  _inherits(ColumnFilter, _React$PureComponent);

  function ColumnFilter(props) {
    _classCallCheck(this, ColumnFilter);

    var _this = _possibleConstructorReturn(this, (ColumnFilter.__proto__ || Object.getPrototypeOf(ColumnFilter)).call(this, props));

    _this.openMenu = _this.openMenu.bind(_this);
    _this.clearAll = _this.clearAll.bind(_this);
    _this.selectAll = _this.selectAll.bind(_this);
    _this.renderMenu = _this.renderMenu.bind(_this);
    _this.toggleFilter = _this.toggleFilter.bind(_this);
    _this.renderTrigger = _this.renderTrigger.bind(_this);
    _this.renderMenuItem = _this.renderMenuItem.bind(_this);
    _this.componentDidMount = _this.componentDidMount.bind(_this);
    _this.handleTriggerClick = _this.handleTriggerClick.bind(_this);
    _this.updateFilterableValues = _this.updateFilterableValues.bind(_this);
    _this.componentWillReceiveProps = _this.componentWillReceiveProps.bind(_this);
    _this.state = { filterableValues: [], touched: false };
    return _this;
  }

  _createClass(ColumnFilter, [{
    key: 'openMenu',
    value: function openMenu() {
      var _this2 = this;

      var _props = this.props,
          column = _props.column,
          dispatch = _props.dispatch;
      var filterState = column.filterState;

      if (filterState.visible) return;
      var touched = this.state.touched;


      dispatch((0, _Actions.toggleColumnFilterVisibility)(column));

      if (!touched && !filterState.enabled) {
        dispatch((0, _Actions.toggleColumnFilter)(column));
        this.setState({ touched: true });
      }

      this.filterCloseListener = _Events2.default.add('click', function (e) {
        var within = e.path.includes(_this2.refs.menu);
        if (!within) _this2.closeMenu();
      });
    }
  }, {
    key: 'closeMenu',
    value: function closeMenu() {
      var _props2 = this.props,
          column = _props2.column,
          dispatch = _props2.dispatch;

      if (!column.filterState.visible || !this.filterCloseListener) return;

      dispatch((0, _Actions.toggleColumnFilterVisibility)(column));
      _Events2.default.remove(this.filterCloseListener);
    }
  }, {
    key: 'componentWillReceiveProps',
    value: function componentWillReceiveProps(newProps) {
      var column = newProps.column,
          rows = newProps.rows;

      if (column === this.props.column && rows === this.props.rows) return;
      this.updateFilterableValues();
    }
  }, {
    key: 'componentDidMount',
    value: function componentDidMount() {
      this.updateFilterableValues();
    }
  }, {
    key: 'updateFilterableValues',
    value: function updateFilterableValues() {
      var _props3 = this.props,
          column = _props3.column,
          state = _props3.state;

      var filterableValues = Array.from(new Set(state.rows.map(function (row) {
        return row[column.key];
      })));
      this.setState({ filterableValues: filterableValues });
    }
  }, {
    key: 'handleTriggerClick',
    value: function handleTriggerClick() {
      var _props4 = this.props,
          column = _props4.column,
          dispatch = _props4.dispatch;

      if (!column.filterable) return;
      var filterState = column.filterState;

      return filterState.visible ? this.closeMenu() : this.openMenu();
    }
  }, {
    key: 'renderTrigger',
    value: function renderTrigger() {
      var _props5 = this.props,
          column = _props5.column,
          state = _props5.state;
      var filterState = column.filterState;

      var icon = 'filter ' + (filterState.enabled ? 'active' : 'inactive');
      var trigger = !column.filterable ? null : _react2.default.createElement(_Icon2.default, {
        fa: icon + ' Trigger FilterTrigger',
        onClick: this.handleTriggerClick
      });
      return trigger;
    }
  }, {
    key: 'toggleValue',
    value: function toggleValue(value) {
      var _props6 = this.props,
          column = _props6.column,
          dispatch = _props6.dispatch;

      dispatch((0, _Actions.toggleColumnFilterValue)(column, value));
    }
  }, {
    key: 'toggleFilter',
    value: function toggleFilter() {
      var _props7 = this.props,
          column = _props7.column,
          dispatch = _props7.dispatch;

      dispatch((0, _Actions.toggleColumnFilter)(column));
    }
  }, {
    key: 'selectAll',
    value: function selectAll() {
      var _props8 = this.props,
          dispatch = _props8.dispatch,
          column = _props8.column;
      var filterableValues = this.state.filterableValues;

      dispatch((0, _Actions.setColumnBlackList)(column, []));
    }
  }, {
    key: 'clearAll',
    value: function clearAll() {
      var _props9 = this.props,
          dispatch = _props9.dispatch,
          column = _props9.column;
      var filterableValues = this.state.filterableValues;

      dispatch((0, _Actions.setColumnBlackList)(column, [].concat(_toConsumableArray(filterableValues))));
    }
  }, {
    key: 'renderMenuItem',
    value: function renderMenuItem(value) {
      var _this3 = this;

      var _props$column$filterS = this.props.column.filterState,
          blacklist = _props$column$filterS.blacklist,
          enabled = _props$column$filterS.enabled;

      var checkbox = blacklist.includes(value) ? 'square' : 'check-square';
      return _react2.default.createElement(
        'div',
        { key: value, onClick: function onClick() {
            return _this3.toggleValue(value);
          } },
        _react2.default.createElement(_Icon2.default, { fa: checkbox, className: enabled ? '' : 'disabled' }),
        ' ',
        value
      );
    }
  }, {
    key: 'renderMenu',
    value: function renderMenu() {
      var _props10 = this.props,
          state = _props10.state,
          column = _props10.column;
      var filterable = column.filterable,
          filterState = column.filterState;
      var filterableValues = this.state.filterableValues;
      var blacklist = filterState.blacklist,
          enabled = filterState.enabled,
          visible = filterState.visible;

      var items = filterableValues.map(this.renderMenuItem);

      var menu = !filterable || !visible ? null : _react2.default.createElement(
        'div',
        { className: 'FilterMenu', ref: 'menu' },
        _react2.default.createElement(
          'big',
          null,
          _react2.default.createElement(
            'b',
            null,
            column.name
          ),
          ' filter',
          _react2.default.createElement(
            'span',
            { className: 'faded' },
            ' (',
            enabled ? 'on' : 'off',
            ')'
          ),
          _react2.default.createElement(_Toggle2.default, {
            style: { float: 'right' },
            onChange: this.toggleFilter,
            enabled: enabled
          })
        ),
        _react2.default.createElement(
          'div',
          null,
          _react2.default.createElement(
            'a',
            { onClick: this.selectAll },
            'Select All'
          ),
          ' | ',
          _react2.default.createElement(
            'a',
            { onClick: this.clearAll },
            'Clear All'
          )
        ),
        _react2.default.createElement(
          'div',
          { className: 'FilterMenu-CheckList' },
          items
        )
      );
      return menu;
    }
  }, {
    key: 'render',
    value: function render() {
      var trigger = this.renderTrigger();
      var menu = this.renderMenu();

      return _react2.default.createElement(
        'div',
        { className: 'ColumnFilter-Wrapper' },
        trigger,
        menu
      );
    }
  }]);

  return ColumnFilter;
}(_react2.default.PureComponent);

;

exports.default = ColumnFilter;