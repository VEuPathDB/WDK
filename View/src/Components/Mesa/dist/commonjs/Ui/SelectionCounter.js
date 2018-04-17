'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var SelectionCounter = function (_React$Component) {
  _inherits(SelectionCounter, _React$Component);

  function SelectionCounter(props) {
    _classCallCheck(this, SelectionCounter);

    var _this = _possibleConstructorReturn(this, (SelectionCounter.__proto__ || Object.getPrototypeOf(SelectionCounter)).call(this, props));

    _this.selectAllRows = _this.selectAllRows.bind(_this);
    _this.deselectAllRows = _this.deselectAllRows.bind(_this);
    return _this;
  }

  _createClass(SelectionCounter, [{
    key: 'noun',
    value: function noun(size) {
      size = typeof size === 'number' ? size : size.length;
      return 'row' + (size === 1 ? '' : 's');
    }
  }, {
    key: 'selectAllRows',
    value: function selectAllRows() {
      var _props = this.props,
          rows = _props.rows,
          selection = _props.selection,
          onRowSelect = _props.onRowSelect;

      var unselectedRows = rows.map(function (row) {
        return !selection.includes(row);
      });
      unselectedRows.forEach(function (row) {
        return onRowSelect(row);
      });
    }
  }, {
    key: 'deselectAllRows',
    value: function deselectAllRows() {
      var _props2 = this.props,
          rows = _props2.rows,
          selection = _props2.selection,
          onRowDeselect = _props2.onRowDeselect;

      var selectedRows = rows.map(function (row) {
        return selection.includes(row);
      });
      selectedRows.forEach(function (row) {
        return onRowDeselect(row);
      });
    }
  }, {
    key: 'render',
    value: function render() {
      var _props3 = this.props,
          rows = _props3.rows,
          selection = _props3.selection;

      if (!selection || !selection.length) return null;
      var allSelected = rows.every(function (row) {
        return selection.includes(row);
      });

      return _react2.default.createElement(
        'div',
        { className: 'SelectionCounter' },
        allSelected ? 'All ' : '',
        _react2.default.createElement(
          'b',
          null,
          selection.length
        ),
        ' ',
        this.noun(selection),
        ' ',
        allSelected ? 'are' : '',
        ' selected.',
        _react2.default.createElement('br', null),
        _react2.default.createElement(
          'a',
          { onClick: this.deselectAllRows },
          'Clear selection.'
        )
      );
    }
  }]);

  return SelectionCounter;
}(_react2.default.Component);

;

exports.default = SelectionCounter;