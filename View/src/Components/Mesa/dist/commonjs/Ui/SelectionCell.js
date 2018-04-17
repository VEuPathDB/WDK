'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Checkbox = require('../Components/Checkbox');

var _Checkbox2 = _interopRequireDefault(_Checkbox);

var _PaginationUtils = require('../Utils/PaginationUtils');

var _PaginationUtils2 = _interopRequireDefault(_PaginationUtils);

var _Actions = require('../State/Actions');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var SelectionCell = function (_React$PureComponent) {
  _inherits(SelectionCell, _React$PureComponent);

  function SelectionCell(props) {
    _classCallCheck(this, SelectionCell);

    var _this = _possibleConstructorReturn(this, (SelectionCell.__proto__ || Object.getPrototypeOf(SelectionCell)).call(this, props));

    _this.renderPageCheckbox = _this.renderPageCheckbox.bind(_this);
    _this.renderRowCheckbox = _this.renderRowCheckbox.bind(_this);
    return _this;
  }

  _createClass(SelectionCell, [{
    key: 'diffuseClick',
    value: function diffuseClick(e) {
      return e.stopPropagation();
    }
  }, {
    key: 'renderPageCheckbox',
    value: function renderPageCheckbox() {
      var _props = this.props,
          filteredRows = _props.filteredRows,
          state = _props.state,
          dispatch = _props.dispatch;
      var _state$uiState = state.uiState,
          selection = _state$uiState.selection,
          paginationState = _state$uiState.paginationState;
      var paginate = state.options.paginate;

      var spread = _PaginationUtils2.default.getSpread(filteredRows, paginationState, paginate);
      var checked = filteredRows.length && _PaginationUtils2.default.isSpreadSelected(spread, selection);

      var handler = function handler(e) {
        e.stopPropagation();
        dispatch(checked ? (0, _Actions.deselectRowsByIds)(spread) : (0, _Actions.selectRowsByIds)(spread));
      };

      return _react2.default.createElement(
        'th',
        { className: 'SelectionCell', onClick: handler },
        _react2.default.createElement(_Checkbox2.default, { checked: checked })
      );
    }
  }, {
    key: 'renderRowCheckbox',
    value: function renderRowCheckbox() {
      var _props2 = this.props,
          row = _props2.row,
          state = _props2.state,
          dispatch = _props2.dispatch;
      var selection = state.uiState.selection;

      var checked = selection.includes(row.__id);

      var handler = function handler(e) {
        e.stopPropagation();
        dispatch((0, _Actions.toggleRowSelectionById)(row.__id));
      };

      return _react2.default.createElement(
        'td',
        { className: 'SelectionCell', onClick: handler },
        _react2.default.createElement(_Checkbox2.default, { checked: checked })
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var heading = this.props.heading;

      return heading ? this.renderPageCheckbox() : this.renderRowCheckbox();
    }
  }]);

  return SelectionCell;
}(_react2.default.PureComponent);

;

exports.default = SelectionCell;