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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9IZWFkaW5nUm93LmpzeCJdLCJuYW1lcyI6WyJIZWFkaW5nUm93IiwicHJvcHMiLCJmaWx0ZXJlZFJvd3MiLCJvcHRpb25zIiwiY29sdW1ucyIsImFjdGlvbnMiLCJ1aVN0YXRlIiwiZXZlbnRIYW5kbGVycyIsIm9mZnNldExlZnQiLCJpc1Jvd1NlbGVjdGVkIiwiY29sdW1uRGVmYXVsdHMiLCJzb3J0Iiwib25Sb3dTZWxlY3QiLCJvblJvd0Rlc2VsZWN0IiwiaGFzU2VsZWN0aW9uQ29sdW1uIiwiZXZlcnkiLCJmbiIsIm51bGxSZW5kZXJlciIsInJvd0NvdW50IiwicmVkdWNlIiwiY291bnQiLCJjb2x1bW4iLCJ0aGlzQ291bnQiLCJBcnJheSIsImlzQXJyYXkiLCJyZW5kZXJIZWFkaW5nIiwibGVuZ3RoIiwiTWF0aCIsIm1heCIsImhlYWRpbmdSb3dzIiwiZmlsbCIsIm1hcCIsImJsYW5rIiwiaW5kZXgiLCJpc0ZpcnN0Um93IiwiY29scyIsIm91dHB1dCIsIk9iamVjdCIsImFzc2lnbiIsImNvbCIsImNvbHVtbkluZGV4Iiwia2V5IiwiUHVyZUNvbXBvbmVudCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7OztBQUFBOzs7O0FBRUE7Ozs7QUFDQTs7OztBQUNBOzs7Ozs7Ozs7O0lBRU1BLFU7OztBQUNKLHNCQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsbUhBQ1pBLEtBRFk7QUFFbkI7Ozs7NkJBRVM7QUFBQSxtQkFDZ0YsS0FBS0EsS0FEckY7QUFBQSxVQUNBQyxZQURBLFVBQ0FBLFlBREE7QUFBQSxVQUNjQyxPQURkLFVBQ2NBLE9BRGQ7QUFBQSxVQUN1QkMsT0FEdkIsVUFDdUJBLE9BRHZCO0FBQUEsVUFDZ0NDLE9BRGhDLFVBQ2dDQSxPQURoQztBQUFBLFVBQ3lDQyxPQUR6QyxVQUN5Q0EsT0FEekM7QUFBQSxVQUNrREMsYUFEbEQsVUFDa0RBLGFBRGxEO0FBQUEsVUFDaUVDLFVBRGpFLFVBQ2lFQSxVQURqRTs7QUFBQSxp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Q0FBaUJZLEtBTG5CO0FBTUUsNEJBQVl6QixVQU5kO0FBT0UsNkJBQWFnQyxXQVBmO0FBUUUsK0JBQWVqQztBQVJqQixnQkFERjtBQVlELGFBZkE7QUFaSCxXQURGO0FBK0JELFNBaENBO0FBREgsT0FERjtBQXFDRDs7OztFQXZFc0IsZ0JBQU1tQyxhOztBQXdFOUI7O2tCQUVjMUMsVSIsImZpbGUiOiJIZWFkaW5nUm93LmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcblxuaW1wb3J0IEhlYWRpbmdDZWxsIGZyb20gJy4uL1VpL0hlYWRpbmdDZWxsJztcbmltcG9ydCBTZWxlY3Rpb25DZWxsIGZyb20gJy4uL1VpL1NlbGVjdGlvbkNlbGwnO1xuaW1wb3J0IHsgQ29sdW1uRGVmYXVsdHMgfSBmcm9tICcuLi9EZWZhdWx0cyc7XG5cbmNsYXNzIEhlYWRpbmdSb3cgZXh0ZW5kcyBSZWFjdC5QdXJlQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuICB9XG5cbiAgcmVuZGVyICgpIHtcbiAgICBjb25zdCB7IGZpbHRlcmVkUm93cywgb3B0aW9ucywgY29sdW1ucywgYWN0aW9ucywgdWlTdGF0ZSwgZXZlbnRIYW5kbGVycywgb2Zmc2V0TGVmdCB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IGlzUm93U2VsZWN0ZWQsIGNvbHVtbkRlZmF1bHRzIH0gPSBvcHRpb25zID8gb3B0aW9ucyA6IHt9O1xuICAgIGNvbnN0IHsgc29ydCB9ID0gdWlTdGF0ZSA/IHVpU3RhdGUgOiB7fTtcbiAgICBjb25zdCB7IG9uUm93U2VsZWN0LCBvblJvd0Rlc2VsZWN0IH0gPSBldmVudEhhbmRsZXJzID8gZXZlbnRIYW5kbGVycyA6IHt9O1xuICAgIGNvbnN0IGhhc1NlbGVjdGlvbkNvbHVtbiA9IFsgaXNSb3dTZWxlY3RlZCwgb25Sb3dTZWxlY3QsIG9uUm93RGVzZWxlY3QgXS5ldmVyeShmbiA9PiB0eXBlb2YgZm4gPT09ICdmdW5jdGlvbicpO1xuXG4gICAgY29uc3QgbnVsbFJlbmRlcmVyID0gKCkgPT4gbnVsbDtcblxuICAgIGNvbnN0IHJvd0NvdW50ID0gY29sdW1ucy5yZWR1Y2UoKGNvdW50LCBjb2x1bW4pID0+IHtcbiAgICAgIGNvbnN0IHRoaXNDb3VudCA9IEFycmF5LmlzQXJyYXkoY29sdW1uLnJlbmRlckhlYWRpbmcpID8gY29sdW1uLnJlbmRlckhlYWRpbmcubGVuZ3RoIDogMTtcbiAgICAgIHJldHVybiBNYXRoLm1heCh0aGlzQ291bnQsIGNvdW50KTtcbiAgICB9LCAxKTtcblxuICAgIGNvbnN0IGhlYWRpbmdSb3dzID0gbmV3IEFycmF5KHJvd0NvdW50KS5maWxsKHt9KVxuICAgICAgLm1hcCgoYmxhbmssIGluZGV4KSA9PiB7XG4gICAgICAgIGNvbnN0IGlzRmlyc3RSb3cgPSAhaW5kZXg7XG4gICAgICAgIGNvbnN0IGNvbHMgPSBjb2x1bW5zLm1hcChjb2wgPT4ge1xuICAgICAgICAgIGNvbnN0IG91dHB1dCA9IE9iamVjdC5hc3NpZ24oe30sIGNvbCk7XG4gICAgICAgICAgaWYgKEFycmF5LmlzQXJyYXkoY29sLnJlbmRlckhlYWRpbmcpKSB7XG4gICAgICAgICAgICBvdXRwdXQucmVuZGVySGVhZGluZyA9IChjb2wucmVuZGVySGVhZGluZy5sZW5ndGggPiBpbmRleCA/IGNvbC5yZW5kZXJIZWFkaW5nW2luZGV4XSA6IGZhbHNlKTtcbiAgICAgICAgICB9IGVsc2UgaWYgKCFpc0ZpcnN0Um93KSB7XG4gICAgICAgICAgICBvdXRwdXQucmVuZGVySGVhZGluZyA9IGZhbHNlO1xuICAgICAgICAgIH07XG4gICAgICAgICAgcmV0dXJuIG91dHB1dDtcbiAgICAgICAgfSk7XG4gICAgICAgIHJldHVybiB7IGNvbHMsIGlzRmlyc3RSb3cgfTtcbiAgICAgIH0pO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDx0aGVhZD5cbiAgICAgICAge2hlYWRpbmdSb3dzLm1hcCgoeyBjb2xzLCBpc0ZpcnN0Um93IH0sIGluZGV4KSA9PiB7XG4gICAgICAgICAgcmV0dXJuIChcbiAgICAgICAgICAgIDx0ciBjbGFzc05hbWU9XCJSb3cgSGVhZGluZ1Jvd1wiIGtleT17aW5kZXh9PlxuICAgICAgICAgICAgICB7IWhhc1NlbGVjdGlvbkNvbHVtblxuICAgICAgICAgICAgICAgID8gbnVsbFxuICAgICAgICAgICAgICAgIDogPFNlbGVjdGlvbkNlbGxcbiAgICAgICAgICAgICAgICAgICAgaW5lcnQ9eyFpc0ZpcnN0Um93fVxuICAgICAgICAgICAgICAgICAgICBoZWFkaW5nPXt0cnVlfVxuICAgICAgICAgICAgICAgICAgICByb3dzPXtmaWx0ZXJlZFJvd3N9XG4gICAgICAgICAgICAgICAgICAgIG9wdGlvbnM9e29wdGlvbnN9XG4gICAgICAgICAgICAgICAgICAgIGV2ZW50SGFuZGxlcnM9e2V2ZW50SGFuZGxlcnN9XG4gICAgICAgICAgICAgICAgICAgIGlzUm93U2VsZWN0ZWQ9e2lzUm93U2VsZWN0ZWR9XG4gICAgICAgICAgICAgICAgICAvPlxuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAge2NvbHMubWFwKChjb2x1bW4sIGNvbHVtbkluZGV4KSA9PiB7XG4gICAgICAgICAgICAgICAgaWYgKHR5cGVvZiBjb2x1bW5EZWZhdWx0cyA9PT0gJ29iamVjdCcpXG4gICAgICAgICAgICAgICAgICBjb2x1bW4gPSBPYmplY3QuYXNzaWduKHt9LCBjb2x1bW5EZWZhdWx0cywgY29sdW1uKTtcbiAgICAgICAgICAgICAgICByZXR1cm4gKFxuICAgICAgICAgICAgICAgICAgPEhlYWRpbmdDZWxsXG4gICAgICAgICAgICAgICAgICAgIHNvcnQ9e3NvcnR9XG4gICAgICAgICAgICAgICAgICAgIGtleT17Y29sdW1uLmtleX1cbiAgICAgICAgICAgICAgICAgICAgcHJpbWFyeT17aXNGaXJzdFJvd31cbiAgICAgICAgICAgICAgICAgICAgY29sdW1uPXtjb2x1bW59XG4gICAgICAgICAgICAgICAgICAgIGhlYWRpbmdSb3dJbmRleD17aW5kZXh9XG4gICAgICAgICAgICAgICAgICAgIG9mZnNldExlZnQ9e29mZnNldExlZnR9XG4gICAgICAgICAgICAgICAgICAgIGNvbHVtbkluZGV4PXtjb2x1bW5JbmRleH1cbiAgICAgICAgICAgICAgICAgICAgZXZlbnRIYW5kbGVycz17ZXZlbnRIYW5kbGVyc31cbiAgICAgICAgICAgICAgICAgIC8+XG4gICAgICAgICAgICAgICAgKTtcbiAgICAgICAgICAgICAgfSl9XG4gICAgICAgICAgICA8L3RyPlxuICAgICAgICAgICk7XG4gICAgICAgIH0pfVxuICAgICAgPC90aGVhZD5cbiAgICApO1xuICB9XG59O1xuXG5leHBvcnQgZGVmYXVsdCBIZWFkaW5nUm93O1xuIl19