'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var SelectionCounter = function (_React$Component) {
  _inherits(SelectionCounter, _React$Component);

  function SelectionCounter(props) {
    _classCallCheck(this, SelectionCounter);

    var _this = _possibleConstructorReturn(this, (SelectionCounter.__proto__ || Object.getPrototypeOf(SelectionCounter)).call(this, props));

    _this.noun = _this.noun.bind(_this);
    _this.selectAllRows = _this.selectAllRows.bind(_this);
    _this.deselectAllRows = _this.deselectAllRows.bind(_this);
    return _this;
  }

  _createClass(SelectionCounter, [{
    key: 'noun',
    value: function noun(size) {
      var _props = this.props,
          selectedNoun = _props.selectedNoun,
          selectedPluralNoun = _props.selectedPluralNoun;

      size = typeof size === 'number' ? size : size.length;
      return !selectedNoun && !selectedPluralNoun ? 'row' + (size === 1 ? '' : 's') : size === 1 ? selectedNoun || 'row' : selectedPluralNoun || 'rows';
    }
  }, {
    key: 'selectAllRows',
    value: function selectAllRows() {
      var _props2 = this.props,
          rows = _props2.rows,
          selection = _props2.selection,
          onRowSelect = _props2.onRowSelect,
          onMultipleRowSelect = _props2.onMultipleRowSelect;

      var unselectedRows = rows.map(function (row) {
        return !selection.includes(row);
      });
      if (typeof onMultipleRowSelect === 'function') onMultipleRowSelect(unselectedRows);else unselectedRows.forEach(function (row) {
        return onRowSelect(row);
      });
    }
  }, {
    key: 'deselectAllRows',
    value: function deselectAllRows() {
      var _props3 = this.props,
          selection = _props3.selection,
          onRowDeselect = _props3.onRowDeselect,
          onMultipleRowDeselect = _props3.onMultipleRowDeselect;

      if (typeof onMultipleRowDeselect === 'function') onMultipleRowDeselect(selection);else selection.forEach(function (row) {
        return onRowDeselect(row);
      });
    }
  }, {
    key: 'render',
    value: function render() {
      var _props4 = this.props,
          rows = _props4.rows,
          selection = _props4.selection,
          onRowDeselect = _props4.onRowDeselect,
          onMultipleRowDeselect = _props4.onMultipleRowDeselect;

      if (!selection || !selection.length) return null;
      var allSelected = rows.every(function (row) {
        return selection.includes(row);
      });

      return _react2.default.createElement(
        'div',
        { className: 'SelectionCounter' },
        _react2.default.createElement(
          'b',
          null,
          selection.length,
          ' '
        ),
        this.noun(selection),
        ' selected.',
        _react2.default.createElement('br', null),
        !onRowDeselect && !onMultipleRowDeselect ? null : _react2.default.createElement(
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

SelectionCounter.propTypes = {
  // all/total "rows" in the table
  rows: _propTypes2.default.array,
  // exclusively the selected rows (checked by ref/inclusion)
  selection: _propTypes2.default.array.isRequired,
  // noun and plural to use for selections (e.g. "25 Datasets selected")
  selectedNoun: _propTypes2.default.string,
  selectedPluralNoun: _propTypes2.default.string,
  // predicate to test for 'selectedness'
  isRowSelected: _propTypes2.default.func,
  // single and multiple select/deselect handlers
  onRowSelect: _propTypes2.default.func,
  onRowDeselect: _propTypes2.default.func,
  onMultipleRowSelect: _propTypes2.default.func,
  onMultipleRowDeselect: _propTypes2.default.func
};

exports.default = SelectionCounter;