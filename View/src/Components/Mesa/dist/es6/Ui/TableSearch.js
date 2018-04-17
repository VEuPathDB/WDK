'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

var _Actions = require('../State/Actions');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var TableSearch = function (_React$PureComponent) {
  _inherits(TableSearch, _React$PureComponent);

  function TableSearch(props) {
    _classCallCheck(this, TableSearch);

    var _this = _possibleConstructorReturn(this, (TableSearch.__proto__ || Object.getPrototypeOf(TableSearch)).call(this, props));

    _this.handleQueryChange = _this.handleQueryChange.bind(_this);
    _this.clearSearchQuery = _this.clearSearchQuery.bind(_this);
    return _this;
  }

  _createClass(TableSearch, [{
    key: 'handleQueryChange',
    value: function handleQueryChange(e) {
      var dispatch = this.props.dispatch;

      var query = e.target.value;
      dispatch((0, _Actions.searchByQuery)(query));
    }
  }, {
    key: 'clearSearchQuery',
    value: function clearSearchQuery() {
      var dispatch = this.props.dispatch;

      dispatch((0, _Actions.searchByQuery)(null));
    }
  }, {
    key: 'render',
    value: function render() {
      var state = this.props.state;
      var uiState = state.uiState,
          options = state.options;
      var searchQuery = uiState.searchQuery;


      return _react2.default.createElement(
        'div',
        { className: 'TableSearch' },
        _react2.default.createElement(_Icon2.default, { fa: 'search' }),
        _react2.default.createElement('input', {
          type: 'text',
          onChange: this.handleQueryChange,
          value: searchQuery || '',
          placeholder: options.searchPlaceholder
        }),
        searchQuery && _react2.default.createElement(
          'button',
          { onClick: this.clearSearchQuery },
          _react2.default.createElement(_Icon2.default, { fa: 'times-circle' }),
          'Clear Search'
        )
      );
    }
  }]);

  return TableSearch;
}(_react2.default.PureComponent);

;

exports.default = TableSearch;