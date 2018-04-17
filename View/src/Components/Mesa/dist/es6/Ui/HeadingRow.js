'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _HeadingCell = require('../Ui/HeadingCell');

var _HeadingCell2 = _interopRequireDefault(_HeadingCell);

var _SelectionCell = require('../Ui/SelectionCell');

var _SelectionCell2 = _interopRequireDefault(_SelectionCell);

var _Defaults = require('../Defaults');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var HeadingRow = function (_React$PureComponent) {
  _inherits(HeadingRow, _React$PureComponent);

  function HeadingRow(props) {
    _classCallCheck(this, HeadingRow);

    return _possibleConstructorReturn(this, (HeadingRow.__proto__ || Object.getPrototypeOf(HeadingRow)).call(this, props));
  }

  _createClass(HeadingRow, [{
    key: 'render',
    value: function render() {
      var _props = this.props,
          filteredRows = _props.filteredRows,
          options = _props.options,
          columns = _props.columns,
          actions = _props.actions,
          uiState = _props.uiState,
          eventHandlers = _props.eventHandlers,
          offsetLeft = _props.offsetLeft;

      var _ref = options ? options : {},
          isRowSelected = _ref.isRowSelected,
          columnDefaults = _ref.columnDefaults;

      var _ref2 = uiState ? uiState : {},
          sort = _ref2.sort;

      var _ref3 = eventHandlers ? eventHandlers : {},
          onRowSelect = _ref3.onRowSelect,
          onRowDeselect = _ref3.onRowDeselect;

      var hasSelectionColumn = [isRowSelected, onRowSelect, onRowDeselect].every(function (fn) {
        return typeof fn === 'function';
      });

      var nullRenderer = function nullRenderer() {
        return null;
      };

      var rowCount = columns.reduce(function (count, column) {
        var thisCount = Array.isArray(column.renderHeading) ? column.renderHeading.length : 1;
        return Math.max(thisCount, count);
      }, 1);

      var headingRows = new Array(rowCount).fill({}).map(function (blank, index) {
        var isFirstRow = !index;
        var cols = columns.map(function (col) {
          var output = Object.assign({}, col);
          if (Array.isArray(col.renderHeading)) {
            output.renderHeading = col.renderHeading.length > index ? col.renderHeading[index] : false;
          } else if (!isFirstRow) {
            output.renderHeading = false;
          };
          return output;
        });
        return { cols: cols, isFirstRow: isFirstRow };
      });

      return _react2.default.createElement(
        'thead',
        null,
        headingRows.map(function (_ref4, index) {
          var cols = _ref4.cols,
              isFirstRow = _ref4.isFirstRow;

          return _react2.default.createElement(
            'tr',
            { className: 'Row HeadingRow', key: index },
            !hasSelectionColumn ? null : _react2.default.createElement(_SelectionCell2.default, {
              inert: !isFirstRow,
              heading: true,
              rows: filteredRows,
              options: options,
              eventHandlers: eventHandlers,
              isRowSelected: isRowSelected
            }),
            cols.map(function (column, columnIndex) {
              if ((typeof columnDefaults === 'undefined' ? 'undefined' : _typeof(columnDefaults)) === 'object') column = Object.assign({}, columnDefaults, column);
              return _react2.default.createElement(_HeadingCell2.default, {
                sort: sort,
                key: column.key,
                primary: isFirstRow,
                column: column,
                headingRowIndex: index,
                offsetLeft: offsetLeft,
                columnIndex: columnIndex,
                eventHandlers: eventHandlers
              });
            })
          );
        })
      );
    }
  }]);

  return HeadingRow;
}(_react2.default.PureComponent);

;

exports.default = HeadingRow;