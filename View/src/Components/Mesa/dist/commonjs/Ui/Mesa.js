'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Importer = require('../Utils/Importer');

var _Importer2 = _interopRequireDefault(_Importer);

var _StoreFactory = require('../State/StoreFactory');

var _StoreFactory2 = _interopRequireDefault(_StoreFactory);

var _TableController = require('../Ui/TableController');

var _TableController2 = _interopRequireDefault(_TableController);

var _Actions = require('../State/Actions');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Mesa = function (_React$Component) {
  _inherits(Mesa, _React$Component);

  function Mesa(props) {
    _classCallCheck(this, Mesa);

    var _this = _possibleConstructorReturn(this, (Mesa.__proto__ || Object.getPrototypeOf(Mesa)).call(this, props));

    _this.state = {};
    _this.componentDidMount = _this.componentDidMount.bind(_this);
    _this.componentWillMount = _this.componentWillMount.bind(_this);
    _this.componentWillUnmount = _this.componentWillUnmount.bind(_this);
    return _this;
  }

  _createClass(Mesa, [{
    key: 'componentWillMount',
    value: function componentWillMount() {
      var _props = this.props,
          options = _props.options,
          columns = _props.columns,
          rows = _props.rows,
          actions = _props.actions;


      rows = _Importer2.default.importRows(rows);
      options = _Importer2.default.importOptions(options);
      columns = _Importer2.default.importColumns(columns, rows, options);
      actions = _Importer2.default.importActions(actions, options);
      this.store = _StoreFactory2.default.create({ options: options, columns: columns, rows: rows, actions: actions });
      this.setState(this.store.getState());
    }
  }, {
    key: 'componentWillReceiveProps',
    value: function componentWillReceiveProps(newProps) {
      var dispatch = this.store.dispatch;
      var _props2 = this.props,
          options = _props2.options,
          columns = _props2.columns,
          rows = _props2.rows,
          actions = _props2.actions;


      if (newProps.rows !== rows) {
        rows = _Importer2.default.importRows(newProps.rows);
        dispatch((0, _Actions.updateRows)([].concat(_toConsumableArray(rows))));
      };
      if (newProps.options !== options) {
        options = _Importer2.default.importOptions(newProps.options);
        dispatch((0, _Actions.updateOptions)(Object.assign({}, options)));
      };
      if (newProps.columns !== columns) {
        columns = _Importer2.default.importColumns(newProps.columns, rows, options);
        dispatch((0, _Actions.updateColumns)([].concat(_toConsumableArray(columns))));
        dispatch((0, _Actions.resetUiState)());
      };
      if (newProps.actions !== actions) {
        actions = _Importer2.default.importActions(newProps.actions, options);
        dispatch((0, _Actions.updateActions)([].concat(_toConsumableArray(actions))));
      };
    }
  }, {
    key: 'componentDidMount',
    value: function componentDidMount() {
      var _this2 = this;

      this.unsubscribe = this.store.subscribe(function () {
        _this2.setState(_this2.store.getState());
      });
    }
  }, {
    key: 'componentWillUnmount',
    value: function componentWillUnmount() {
      this.unsubscribe();
    }
  }, {
    key: 'render',
    value: function render() {
      var state = this.state;
      var children = this.props.children;
      var dispatch = this.store.dispatch;


      return _react2.default.createElement(
        'div',
        { className: 'Mesa' },
        _react2.default.createElement(
          _TableController2.default,
          {
            state: state,
            dispatch: dispatch
          },
          children
        )
      );
    }
  }]);

  return Mesa;
}(_react2.default.Component);

;

exports.default = Mesa;