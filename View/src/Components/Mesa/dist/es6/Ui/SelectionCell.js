'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Checkbox = require('../Components/Checkbox');

var _Checkbox2 = _interopRequireDefault(_Checkbox);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var SelectionCell = function (_React$PureComponent) {
  _inherits(SelectionCell, _React$PureComponent);

  function SelectionCell(props) {
    _classCallCheck(this, SelectionCell);

    var _this = _possibleConstructorReturn(this, (SelectionCell.__proto__ || Object.getPrototypeOf(SelectionCell)).call(this, props));

    _this.selectAllRows = _this.selectAllRows.bind(_this);
    _this.deselectAllRows = _this.deselectAllRows.bind(_this);
    _this.renderPageCheckbox = _this.renderPageCheckbox.bind(_this);
    _this.renderRowCheckbox = _this.renderRowCheckbox.bind(_this);
    return _this;
  }

  _createClass(SelectionCell, [{
    key: 'selectAllRows',
    value: function selectAllRows() {
      var _props = this.props,
          rows = _props.rows,
          options = _props.options,
          eventHandlers = _props.eventHandlers;
      var isRowSelected = options.isRowSelected;
      var onRowSelect = eventHandlers.onRowSelect,
          onMultipleRowSelect = eventHandlers.onMultipleRowSelect;

      var unselectedRows = rows.filter(function (row) {
        return !isRowSelected(row);
      });
      if (onMultipleRowSelect) return onMultipleRowSelect(unselectedRows);
      return unselectedRows.forEach(onRowSelect);
    }
  }, {
    key: 'deselectAllRows',
    value: function deselectAllRows() {
      var _props2 = this.props,
          rows = _props2.rows,
          options = _props2.options,
          eventHandlers = _props2.eventHandlers;
      var isRowSelected = options.isRowSelected;
      var onRowDeselect = eventHandlers.onRowDeselect,
          onMultipleRowDeselect = eventHandlers.onMultipleRowDeselect;

      var selection = rows.filter(isRowSelected);
      if (onMultipleRowDeselect) return onMultipleRowDeselect(selection);
      return selection.forEach(onRowDeselect);
    }
  }, {
    key: 'renderPageCheckbox',
    value: function renderPageCheckbox() {
      var _this2 = this;

      var _props3 = this.props,
          rows = _props3.rows,
          isRowSelected = _props3.isRowSelected,
          eventHandlers = _props3.eventHandlers;

      var selection = rows.filter(isRowSelected);
      var checked = rows.every(isRowSelected);

      var handler = function handler(e) {
        e.stopPropagation();
        return checked ? _this2.deselectAllRows() : _this2.selectAllRows();
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
      var _props4 = this.props,
          row = _props4.row,
          isRowSelected = _props4.isRowSelected,
          eventHandlers = _props4.eventHandlers;
      var onRowSelect = eventHandlers.onRowSelect,
          onRowDeselect = eventHandlers.onRowDeselect;

      var checked = isRowSelected(row);

      var handler = function handler(e) {
        e.stopPropagation();
        return checked ? onRowDeselect(row) : onRowSelect(row);
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