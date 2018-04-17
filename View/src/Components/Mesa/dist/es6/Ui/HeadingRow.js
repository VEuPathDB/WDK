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
              key: '_selection',
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9IZWFkaW5nUm93LmpzeCJdLCJuYW1lcyI6WyJIZWFkaW5nUm93IiwicHJvcHMiLCJmaWx0ZXJlZFJvd3MiLCJvcHRpb25zIiwiY29sdW1ucyIsImFjdGlvbnMiLCJ1aVN0YXRlIiwiZXZlbnRIYW5kbGVycyIsIm9mZnNldExlZnQiLCJpc1Jvd1NlbGVjdGVkIiwiY29sdW1uRGVmYXVsdHMiLCJzb3J0Iiwib25Sb3dTZWxlY3QiLCJvblJvd0Rlc2VsZWN0IiwiaGFzU2VsZWN0aW9uQ29sdW1uIiwiZXZlcnkiLCJmbiIsIm51bGxSZW5kZXJlciIsInJvd0NvdW50IiwicmVkdWNlIiwiY291bnQiLCJjb2x1bW4iLCJ0aGlzQ291bnQiLCJBcnJheSIsImlzQXJyYXkiLCJyZW5kZXJIZWFkaW5nIiwibGVuZ3RoIiwiTWF0aCIsIm1heCIsImhlYWRpbmdSb3dzIiwiZmlsbCIsIm1hcCIsImJsYW5rIiwiaW5kZXgiLCJpc0ZpcnN0Um93IiwiY29scyIsIm91dHB1dCIsIk9iamVjdCIsImFzc2lnbiIsImNvbCIsImNvbHVtbkluZGV4Iiwia2V5IiwiUHVyZUNvbXBvbmVudCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7OztBQUFBOzs7O0FBRUE7Ozs7QUFDQTs7OztBQUNBOzs7Ozs7Ozs7O0lBRU1BLFU7OztBQUNKLHNCQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsbUhBQ1pBLEtBRFk7QUFFbkI7Ozs7NkJBRVM7QUFBQSxtQkFDZ0YsS0FBS0EsS0FEckY7QUFBQSxVQUNBQyxZQURBLFVBQ0FBLFlBREE7QUFBQSxVQUNjQyxPQURkLFVBQ2NBLE9BRGQ7QUFBQSxVQUN1QkMsT0FEdkIsVUFDdUJBLE9BRHZCO0FBQUEsVUFDZ0NDLE9BRGhDLFVBQ2dDQSxPQURoQztBQUFBLFVBQ3lDQyxPQUR6QyxVQUN5Q0EsT0FEekM7QUFBQSxVQUNrREMsYUFEbEQsVUFDa0RBLGFBRGxEO0FBQUEsVUFDaUVDLFVBRGpFLFVBQ2lFQSxVQURqRTs7QUFBQSxp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QUFRYixNQUpWO0FBS0UsaUNBQWlCWSxLQUxuQjtBQU1FLDRCQUFZekIsVUFOZDtBQU9FLDZCQUFhZ0MsV0FQZjtBQVFFLCtCQUFlakM7QUFSakIsZ0JBREY7QUFZRCxhQWZBO0FBYkgsV0FERjtBQWdDRCxTQWpDQTtBQURILE9BREY7QUFzQ0Q7Ozs7RUF4RXNCLGdCQUFNbUMsYTs7QUF5RTlCOztrQkFFYzFDLFUiLCJmaWxlIjoiSGVhZGluZ1Jvdy5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5cbmltcG9ydCBIZWFkaW5nQ2VsbCBmcm9tICcuLi9VaS9IZWFkaW5nQ2VsbCc7XG5pbXBvcnQgU2VsZWN0aW9uQ2VsbCBmcm9tICcuLi9VaS9TZWxlY3Rpb25DZWxsJztcbmltcG9ydCB7IENvbHVtbkRlZmF1bHRzIH0gZnJvbSAnLi4vRGVmYXVsdHMnO1xuXG5jbGFzcyBIZWFkaW5nUm93IGV4dGVuZHMgUmVhY3QuUHVyZUNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgfVxuXG4gIHJlbmRlciAoKSB7XG4gICAgY29uc3QgeyBmaWx0ZXJlZFJvd3MsIG9wdGlvbnMsIGNvbHVtbnMsIGFjdGlvbnMsIHVpU3RhdGUsIGV2ZW50SGFuZGxlcnMsIG9mZnNldExlZnQgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBpc1Jvd1NlbGVjdGVkLCBjb2x1bW5EZWZhdWx0cyB9ID0gb3B0aW9ucyA/IG9wdGlvbnMgOiB7fTtcbiAgICBjb25zdCB7IHNvcnQgfSA9IHVpU3RhdGUgPyB1aVN0YXRlIDoge307XG4gICAgY29uc3QgeyBvblJvd1NlbGVjdCwgb25Sb3dEZXNlbGVjdCB9ID0gZXZlbnRIYW5kbGVycyA/IGV2ZW50SGFuZGxlcnMgOiB7fTtcbiAgICBjb25zdCBoYXNTZWxlY3Rpb25Db2x1bW4gPSBbIGlzUm93U2VsZWN0ZWQsIG9uUm93U2VsZWN0LCBvblJvd0Rlc2VsZWN0IF0uZXZlcnkoZm4gPT4gdHlwZW9mIGZuID09PSAnZnVuY3Rpb24nKTtcblxuICAgIGNvbnN0IG51bGxSZW5kZXJlciA9ICgpID0+IG51bGw7XG5cbiAgICBjb25zdCByb3dDb3VudCA9IGNvbHVtbnMucmVkdWNlKChjb3VudCwgY29sdW1uKSA9PiB7XG4gICAgICBjb25zdCB0aGlzQ291bnQgPSBBcnJheS5pc0FycmF5KGNvbHVtbi5yZW5kZXJIZWFkaW5nKSA/IGNvbHVtbi5yZW5kZXJIZWFkaW5nLmxlbmd0aCA6IDE7XG4gICAgICByZXR1cm4gTWF0aC5tYXgodGhpc0NvdW50LCBjb3VudCk7XG4gICAgfSwgMSk7XG5cbiAgICBjb25zdCBoZWFkaW5nUm93cyA9IG5ldyBBcnJheShyb3dDb3VudCkuZmlsbCh7fSlcbiAgICAgIC5tYXAoKGJsYW5rLCBpbmRleCkgPT4ge1xuICAgICAgICBjb25zdCBpc0ZpcnN0Um93ID0gIWluZGV4O1xuICAgICAgICBjb25zdCBjb2xzID0gY29sdW1ucy5tYXAoY29sID0+IHtcbiAgICAgICAgICBjb25zdCBvdXRwdXQgPSBPYmplY3QuYXNzaWduKHt9LCBjb2wpO1xuICAgICAgICAgIGlmIChBcnJheS5pc0FycmF5KGNvbC5yZW5kZXJIZWFkaW5nKSkge1xuICAgICAgICAgICAgb3V0cHV0LnJlbmRlckhlYWRpbmcgPSAoY29sLnJlbmRlckhlYWRpbmcubGVuZ3RoID4gaW5kZXggPyBjb2wucmVuZGVySGVhZGluZ1tpbmRleF0gOiBmYWxzZSk7XG4gICAgICAgICAgfSBlbHNlIGlmICghaXNGaXJzdFJvdykge1xuICAgICAgICAgICAgb3V0cHV0LnJlbmRlckhlYWRpbmcgPSBmYWxzZTtcbiAgICAgICAgICB9O1xuICAgICAgICAgIHJldHVybiBvdXRwdXQ7XG4gICAgICAgIH0pO1xuICAgICAgICByZXR1cm4geyBjb2xzLCBpc0ZpcnN0Um93IH07XG4gICAgICB9KTtcblxuICAgIHJldHVybiAoXG4gICAgICA8dGhlYWQ+XG4gICAgICAgIHtoZWFkaW5nUm93cy5tYXAoKHsgY29scywgaXNGaXJzdFJvdyB9LCBpbmRleCkgPT4ge1xuICAgICAgICAgIHJldHVybiAoXG4gICAgICAgICAgICA8dHIgY2xhc3NOYW1lPVwiUm93IEhlYWRpbmdSb3dcIiBrZXk9e2luZGV4fT5cbiAgICAgICAgICAgICAgeyFoYXNTZWxlY3Rpb25Db2x1bW5cbiAgICAgICAgICAgICAgICA/IG51bGxcbiAgICAgICAgICAgICAgICA6IDxTZWxlY3Rpb25DZWxsXG4gICAgICAgICAgICAgICAgICAgIGluZXJ0PXshaXNGaXJzdFJvd31cbiAgICAgICAgICAgICAgICAgICAgaGVhZGluZz17dHJ1ZX1cbiAgICAgICAgICAgICAgICAgICAga2V5PVwiX3NlbGVjdGlvblwiXG4gICAgICAgICAgICAgICAgICAgIHJvd3M9e2ZpbHRlcmVkUm93c31cbiAgICAgICAgICAgICAgICAgICAgb3B0aW9ucz17b3B0aW9uc31cbiAgICAgICAgICAgICAgICAgICAgZXZlbnRIYW5kbGVycz17ZXZlbnRIYW5kbGVyc31cbiAgICAgICAgICAgICAgICAgICAgaXNSb3dTZWxlY3RlZD17aXNSb3dTZWxlY3RlZH1cbiAgICAgICAgICAgICAgICAgIC8+XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgICB7Y29scy5tYXAoKGNvbHVtbiwgY29sdW1uSW5kZXgpID0+IHtcbiAgICAgICAgICAgICAgICBpZiAodHlwZW9mIGNvbHVtbkRlZmF1bHRzID09PSAnb2JqZWN0JylcbiAgICAgICAgICAgICAgICAgIGNvbHVtbiA9IE9iamVjdC5hc3NpZ24oe30sIGNvbHVtbkRlZmF1bHRzLCBjb2x1bW4pO1xuICAgICAgICAgICAgICAgIHJldHVybiAoXG4gICAgICAgICAgICAgICAgICA8SGVhZGluZ0NlbGxcbiAgICAgICAgICAgICAgICAgICAgc29ydD17c29ydH1cbiAgICAgICAgICAgICAgICAgICAga2V5PXtjb2x1bW4ua2V5fVxuICAgICAgICAgICAgICAgICAgICBwcmltYXJ5PXtpc0ZpcnN0Um93fVxuICAgICAgICAgICAgICAgICAgICBjb2x1bW49e2NvbHVtbn1cbiAgICAgICAgICAgICAgICAgICAgaGVhZGluZ1Jvd0luZGV4PXtpbmRleH1cbiAgICAgICAgICAgICAgICAgICAgb2Zmc2V0TGVmdD17b2Zmc2V0TGVmdH1cbiAgICAgICAgICAgICAgICAgICAgY29sdW1uSW5kZXg9e2NvbHVtbkluZGV4fVxuICAgICAgICAgICAgICAgICAgICBldmVudEhhbmRsZXJzPXtldmVudEhhbmRsZXJzfVxuICAgICAgICAgICAgICAgICAgLz5cbiAgICAgICAgICAgICAgICApO1xuICAgICAgICAgICAgICB9KX1cbiAgICAgICAgICAgIDwvdHI+XG4gICAgICAgICAgKTtcbiAgICAgICAgfSl9XG4gICAgICA8L3RoZWFkPlxuICAgICk7XG4gIH1cbn07XG5cbmV4cG9ydCBkZWZhdWx0IEhlYWRpbmdSb3c7XG4iXX0=