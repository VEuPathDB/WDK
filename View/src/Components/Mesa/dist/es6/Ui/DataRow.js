'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _DataCell = require('../Ui/DataCell');

var _DataCell2 = _interopRequireDefault(_DataCell);

var _SelectionCell = require('../Ui/SelectionCell');

var _SelectionCell2 = _interopRequireDefault(_SelectionCell);

var _Utils = require('../Utils/Utils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var dataRowClass = (0, _Utils.makeClassifier)('DataRow');

var DataRow = function (_React$PureComponent) {
  _inherits(DataRow, _React$PureComponent);

  function DataRow(props) {
    _classCallCheck(this, DataRow);

    var _this = _possibleConstructorReturn(this, (DataRow.__proto__ || Object.getPrototypeOf(DataRow)).call(this, props));

    _this.state = { expanded: false };
    _this.handleRowClick = _this.handleRowClick.bind(_this);
    _this.expandRow = _this.expandRow.bind(_this);
    _this.collapseRow = _this.collapseRow.bind(_this);
    _this.componentWillReceiveProps = _this.componentWillReceiveProps.bind(_this);
    return _this;
  }

  _createClass(DataRow, [{
    key: 'componentWillReceiveProps',
    value: function componentWillReceiveProps(newProps) {
      var row = this.props.row;

      if (newProps.row !== row) this.collapseRow();
    }
  }, {
    key: 'expandRow',
    value: function expandRow() {
      var options = this.props.options;

      if (!options.inline) return;
      this.setState({ expanded: true });
    }
  }, {
    key: 'collapseRow',
    value: function collapseRow() {
      var options = this.props.options;

      if (!options.inline) return;
      this.setState({ expanded: false });
    }
  }, {
    key: 'handleRowClick',
    value: function handleRowClick() {
      var _props = this.props,
          row = _props.row,
          rowIndex = _props.rowIndex,
          options = _props.options;
      var inline = options.inline,
          onRowClick = options.onRowClick;

      if (!inline && !onRowClick) return;

      if (inline) this.setState({ expanded: !this.state.expanded });
      if (typeof onRowClick === 'function') onRowClick(row, rowIndex);
    }
  }, {
    key: 'render',
    value: function render() {
      var _props2 = this.props,
          row = _props2.row,
          rowIndex = _props2.rowIndex,
          columns = _props2.columns,
          options = _props2.options,
          actions = _props2.actions,
          eventHandlers = _props2.eventHandlers;
      var expanded = this.state.expanded;

      var _ref = options ? options : {},
          columnDefaults = _ref.columnDefaults;

      var inline = options.inline ? !expanded : false;

      var hasSelectionColumn = typeof options.isRowSelected === 'function' && typeof eventHandlers.onRowSelect === 'function' && typeof eventHandlers.onRowDeselect === 'function';

      var rowStyle = !inline ? {} : { whiteSpace: 'nowrap', textOverflow: 'ellipsis' };
      var className = dataRowClass(null, inline ? 'inline' : '');

      var deriveRowClassName = options.deriveRowClassName;

      if (typeof deriveRowClassName === 'function') {
        var derivedClassName = deriveRowClassName(row);
        className += typeof derivedClassName === 'string' ? ' ' + derivedClassName : '';
      };

      var sharedProps = { row: row, inline: inline, options: options, rowIndex: rowIndex };

      return _react2.default.createElement(
        'tr',
        { className: className, style: rowStyle, onClick: this.handleRowClick },
        !hasSelectionColumn ? null : _react2.default.createElement(_SelectionCell2.default, {
          row: row,
          eventHandlers: eventHandlers,
          isRowSelected: options.isRowSelected
        }),
        columns.map(function (column, columnIndex) {
          if ((typeof columnDefaults === 'undefined' ? 'undefined' : _typeof(columnDefaults)) === 'object') column = Object.assign({}, columnDefaults, column);
          return _react2.default.createElement(_DataCell2.default, _extends({
            key: column.key,
            column: column,
            columnIndex: columnIndex
          }, sharedProps));
        })
      );
    }
  }]);

  return DataRow;
}(_react2.default.PureComponent);

;

DataRow.propTypes = {
  row: _propTypes2.default.object.isRequired,
  rowIndex: _propTypes2.default.number.isRequired,
  columns: _propTypes2.default.array.isRequired,

  options: _propTypes2.default.object,
  actions: _propTypes2.default.array,
  eventHandlers: _propTypes2.default.object
};

exports.default = DataRow;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9EYXRhUm93LmpzeCJdLCJuYW1lcyI6WyJkYXRhUm93Q2xhc3MiLCJEYXRhUm93IiwicHJvcHMiLCJzdGF0ZSIsImV4cGFuZGVkIiwiaGFuZGxlUm93Q2xpY2siLCJiaW5kIiwiZXhwYW5kUm93IiwiY29sbGFwc2VSb3ciLCJjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzIiwibmV3UHJvcHMiLCJyb3ciLCJvcHRpb25zIiwiaW5saW5lIiwic2V0U3RhdGUiLCJyb3dJbmRleCIsIm9uUm93Q2xpY2siLCJjb2x1bW5zIiwiYWN0aW9ucyIsImV2ZW50SGFuZGxlcnMiLCJjb2x1bW5EZWZhdWx0cyIsImhhc1NlbGVjdGlvbkNvbHVtbiIsImlzUm93U2VsZWN0ZWQiLCJvblJvd1NlbGVjdCIsIm9uUm93RGVzZWxlY3QiLCJyb3dTdHlsZSIsIndoaXRlU3BhY2UiLCJ0ZXh0T3ZlcmZsb3ciLCJjbGFzc05hbWUiLCJkZXJpdmVSb3dDbGFzc05hbWUiLCJkZXJpdmVkQ2xhc3NOYW1lIiwic2hhcmVkUHJvcHMiLCJtYXAiLCJjb2x1bW4iLCJjb2x1bW5JbmRleCIsIk9iamVjdCIsImFzc2lnbiIsImtleSIsIlB1cmVDb21wb25lbnQiLCJwcm9wVHlwZXMiLCJvYmplY3QiLCJpc1JlcXVpcmVkIiwibnVtYmVyIiwiYXJyYXkiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7OztBQUFBOzs7O0FBQ0E7Ozs7QUFFQTs7OztBQUNBOzs7O0FBQ0E7Ozs7Ozs7Ozs7QUFFQSxJQUFNQSxlQUFlLDJCQUFlLFNBQWYsQ0FBckI7O0lBRU1DLE87OztBQUNKLG1CQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsa0hBQ1pBLEtBRFk7O0FBRWxCLFVBQUtDLEtBQUwsR0FBYSxFQUFFQyxVQUFVLEtBQVosRUFBYjtBQUNBLFVBQUtDLGNBQUwsR0FBc0IsTUFBS0EsY0FBTCxDQUFvQkMsSUFBcEIsT0FBdEI7QUFDQSxVQUFLQyxTQUFMLEdBQWlCLE1BQUtBLFNBQUwsQ0FBZUQsSUFBZixPQUFqQjtBQUNBLFVBQUtFLFdBQUwsR0FBbUIsTUFBS0EsV0FBTCxDQUFpQkYsSUFBakIsT0FBbkI7QUFDQSxVQUFLRyx5QkFBTCxHQUFpQyxNQUFLQSx5QkFBTCxDQUErQkgsSUFBL0IsT0FBakM7QUFOa0I7QUFPbkI7Ozs7OENBRTBCSSxRLEVBQVU7QUFBQSxVQUMzQkMsR0FEMkIsR0FDbkIsS0FBS1QsS0FEYyxDQUMzQlMsR0FEMkI7O0FBRW5DLFVBQUlELFNBQVNDLEdBQVQsS0FBaUJBLEdBQXJCLEVBQTBCLEtBQUtILFdBQUw7QUFDM0I7OztnQ0FFWTtBQUFBLFVBQ0hJLE9BREcsR0FDUyxLQUFLVixLQURkLENBQ0hVLE9BREc7O0FBRVgsVUFBSSxDQUFDQSxRQUFRQyxNQUFiLEVBQXFCO0FBQ3JCLFdBQUtDLFFBQUwsQ0FBYyxFQUFFVixVQUFVLElBQVosRUFBZDtBQUNEOzs7a0NBRWM7QUFBQSxVQUNMUSxPQURLLEdBQ08sS0FBS1YsS0FEWixDQUNMVSxPQURLOztBQUViLFVBQUksQ0FBQ0EsUUFBUUMsTUFBYixFQUFxQjtBQUNyQixXQUFLQyxRQUFMLENBQWMsRUFBRVYsVUFBVSxLQUFaLEVBQWQ7QUFDRDs7O3FDQUVpQjtBQUFBLG1CQUNtQixLQUFLRixLQUR4QjtBQUFBLFVBQ1JTLEdBRFEsVUFDUkEsR0FEUTtBQUFBLFVBQ0hJLFFBREcsVUFDSEEsUUFERztBQUFBLFVBQ09ILE9BRFAsVUFDT0EsT0FEUDtBQUFBLFVBRVJDLE1BRlEsR0FFZUQsT0FGZixDQUVSQyxNQUZRO0FBQUEsVUFFQUcsVUFGQSxHQUVlSixPQUZmLENBRUFJLFVBRkE7O0FBR2hCLFVBQUksQ0FBQ0gsTUFBRCxJQUFXLENBQUNHLFVBQWhCLEVBQTRCOztBQUU1QixVQUFJSCxNQUFKLEVBQWEsS0FBS0MsUUFBTCxDQUFjLEVBQUVWLFVBQVUsQ0FBQyxLQUFLRCxLQUFMLENBQVdDLFFBQXhCLEVBQWQ7QUFDYixVQUFJLE9BQU9ZLFVBQVAsS0FBc0IsVUFBMUIsRUFBc0NBLFdBQVdMLEdBQVgsRUFBZ0JJLFFBQWhCO0FBQ3ZDOzs7NkJBRVM7QUFBQSxvQkFDNEQsS0FBS2IsS0FEakU7QUFBQSxVQUNBUyxHQURBLFdBQ0FBLEdBREE7QUFBQSxVQUNLSSxRQURMLFdBQ0tBLFFBREw7QUFBQSxVQUNlRSxPQURmLFdBQ2VBLE9BRGY7QUFBQSxVQUN3QkwsT0FEeEIsV0FDd0JBLE9BRHhCO0FBQUEsVUFDaUNNLE9BRGpDLFdBQ2lDQSxPQURqQztBQUFBLFVBQzBDQyxhQUQxQyxXQUMwQ0EsYUFEMUM7QUFBQSxVQUVBZixRQUZBLEdBRWEsS0FBS0QsS0FGbEIsQ0FFQUMsUUFGQTs7QUFBQSxpQkFHbUJRLFVBQVVBLE9BQVYsR0FBb0IsRUFIdkM7QUFBQSxVQUdBUSxjQUhBLFFBR0FBLGNBSEE7O0FBSVIsVUFBTVAsU0FBU0QsUUFBUUMsTUFBUixHQUFpQixDQUFDVCxRQUFsQixHQUE2QixLQUE1Qzs7QUFFQSxVQUFNaUIscUJBQXFCLE9BQU9ULFFBQVFVLGFBQWYsS0FBaUMsVUFBakMsSUFDdEIsT0FBT0gsY0FBY0ksV0FBckIsS0FBcUMsVUFEZixJQUV0QixPQUFPSixjQUFjSyxhQUFyQixLQUF1QyxVQUY1Qzs7QUFJQSxVQUFNQyxXQUFXLENBQUNaLE1BQUQsR0FBVSxFQUFWLEdBQWUsRUFBRWEsWUFBWSxRQUFkLEVBQXdCQyxjQUFjLFVBQXRDLEVBQWhDO0FBQ0EsVUFBSUMsWUFBWTVCLGFBQWEsSUFBYixFQUFtQmEsU0FBUyxRQUFULEdBQW9CLEVBQXZDLENBQWhCOztBQVhRLFVBYUFnQixrQkFiQSxHQWF1QmpCLE9BYnZCLENBYUFpQixrQkFiQTs7QUFjUixVQUFJLE9BQU9BLGtCQUFQLEtBQThCLFVBQWxDLEVBQThDO0FBQzVDLFlBQUlDLG1CQUFtQkQsbUJBQW1CbEIsR0FBbkIsQ0FBdkI7QUFDQWlCLHFCQUFjLE9BQU9FLGdCQUFQLEtBQTRCLFFBQTdCLEdBQXlDLE1BQU1BLGdCQUEvQyxHQUFrRSxFQUEvRTtBQUNEOztBQUVELFVBQU1DLGNBQWMsRUFBRXBCLFFBQUYsRUFBT0UsY0FBUCxFQUFlRCxnQkFBZixFQUF3Qkcsa0JBQXhCLEVBQXBCOztBQUVBLGFBQ0U7QUFBQTtBQUFBLFVBQUksV0FBV2EsU0FBZixFQUEwQixPQUFPSCxRQUFqQyxFQUEyQyxTQUFTLEtBQUtwQixjQUF6RDtBQUNHLFNBQUNnQixrQkFBRCxHQUNHLElBREgsR0FFRztBQUNFLGVBQUtWLEdBRFA7QUFFRSx5QkFBZVEsYUFGakI7QUFHRSx5QkFBZVAsUUFBUVU7QUFIekIsVUFITjtBQVNHTCxnQkFBUWUsR0FBUixDQUFZLFVBQUNDLE1BQUQsRUFBU0MsV0FBVCxFQUF5QjtBQUNwQyxjQUFJLFFBQU9kLGNBQVAseUNBQU9BLGNBQVAsT0FBMEIsUUFBOUIsRUFDRWEsU0FBU0UsT0FBT0MsTUFBUCxDQUFjLEVBQWQsRUFBa0JoQixjQUFsQixFQUFrQ2EsTUFBbEMsQ0FBVDtBQUNGLGlCQUNFO0FBQ0UsaUJBQUtBLE9BQU9JLEdBRGQ7QUFFRSxvQkFBUUosTUFGVjtBQUdFLHlCQUFhQztBQUhmLGFBSU1ILFdBSk4sRUFERjtBQVFELFNBWEE7QUFUSCxPQURGO0FBd0JEOzs7O0VBakZtQixnQkFBTU8sYTs7QUFrRjNCOztBQUVEckMsUUFBUXNDLFNBQVIsR0FBb0I7QUFDbEI1QixPQUFLLG9CQUFVNkIsTUFBVixDQUFpQkMsVUFESjtBQUVsQjFCLFlBQVUsb0JBQVUyQixNQUFWLENBQWlCRCxVQUZUO0FBR2xCeEIsV0FBUyxvQkFBVTBCLEtBQVYsQ0FBZ0JGLFVBSFA7O0FBS2xCN0IsV0FBUyxvQkFBVTRCLE1BTEQ7QUFNbEJ0QixXQUFTLG9CQUFVeUIsS0FORDtBQU9sQnhCLGlCQUFlLG9CQUFVcUI7QUFQUCxDQUFwQjs7a0JBVWV2QyxPIiwiZmlsZSI6IkRhdGFSb3cuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IFByb3BUeXBlcyBmcm9tICdwcm9wLXR5cGVzJztcblxuaW1wb3J0IERhdGFDZWxsIGZyb20gJy4uL1VpL0RhdGFDZWxsJztcbmltcG9ydCBTZWxlY3Rpb25DZWxsIGZyb20gJy4uL1VpL1NlbGVjdGlvbkNlbGwnO1xuaW1wb3J0IHsgbWFrZUNsYXNzaWZpZXIgfSBmcm9tICcuLi9VdGlscy9VdGlscyc7XG5cbmNvbnN0IGRhdGFSb3dDbGFzcyA9IG1ha2VDbGFzc2lmaWVyKCdEYXRhUm93Jyk7XG5cbmNsYXNzIERhdGFSb3cgZXh0ZW5kcyBSZWFjdC5QdXJlQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuICAgIHRoaXMuc3RhdGUgPSB7IGV4cGFuZGVkOiBmYWxzZSB9O1xuICAgIHRoaXMuaGFuZGxlUm93Q2xpY2sgPSB0aGlzLmhhbmRsZVJvd0NsaWNrLmJpbmQodGhpcyk7XG4gICAgdGhpcy5leHBhbmRSb3cgPSB0aGlzLmV4cGFuZFJvdy5iaW5kKHRoaXMpO1xuICAgIHRoaXMuY29sbGFwc2VSb3cgPSB0aGlzLmNvbGxhcHNlUm93LmJpbmQodGhpcyk7XG4gICAgdGhpcy5jb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzID0gdGhpcy5jb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzLmJpbmQodGhpcyk7XG4gIH1cblxuICBjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzIChuZXdQcm9wcykge1xuICAgIGNvbnN0IHsgcm93IH0gPSB0aGlzLnByb3BzO1xuICAgIGlmIChuZXdQcm9wcy5yb3cgIT09IHJvdykgdGhpcy5jb2xsYXBzZVJvdygpO1xuICB9XG5cbiAgZXhwYW5kUm93ICgpIHtcbiAgICBjb25zdCB7IG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG4gICAgaWYgKCFvcHRpb25zLmlubGluZSkgcmV0dXJuO1xuICAgIHRoaXMuc2V0U3RhdGUoeyBleHBhbmRlZDogdHJ1ZSB9KTtcbiAgfVxuXG4gIGNvbGxhcHNlUm93ICgpIHtcbiAgICBjb25zdCB7IG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG4gICAgaWYgKCFvcHRpb25zLmlubGluZSkgcmV0dXJuO1xuICAgIHRoaXMuc2V0U3RhdGUoeyBleHBhbmRlZDogZmFsc2UgfSk7XG4gIH1cblxuICBoYW5kbGVSb3dDbGljayAoKSB7XG4gICAgY29uc3QgeyByb3csIHJvd0luZGV4LCBvcHRpb25zIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgaW5saW5lLCBvblJvd0NsaWNrIH0gPSBvcHRpb25zO1xuICAgIGlmICghaW5saW5lICYmICFvblJvd0NsaWNrKSByZXR1cm47XG5cbiAgICBpZiAoaW5saW5lKSAgdGhpcy5zZXRTdGF0ZSh7IGV4cGFuZGVkOiAhdGhpcy5zdGF0ZS5leHBhbmRlZCB9KTtcbiAgICBpZiAodHlwZW9mIG9uUm93Q2xpY2sgPT09ICdmdW5jdGlvbicpIG9uUm93Q2xpY2socm93LCByb3dJbmRleCk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgcm93LCByb3dJbmRleCwgY29sdW1ucywgb3B0aW9ucywgYWN0aW9ucywgZXZlbnRIYW5kbGVycyB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IGV4cGFuZGVkIH0gPSB0aGlzLnN0YXRlO1xuICAgIGNvbnN0IHsgY29sdW1uRGVmYXVsdHMgfSA9IG9wdGlvbnMgPyBvcHRpb25zIDoge307XG4gICAgY29uc3QgaW5saW5lID0gb3B0aW9ucy5pbmxpbmUgPyAhZXhwYW5kZWQgOiBmYWxzZTtcblxuICAgIGNvbnN0IGhhc1NlbGVjdGlvbkNvbHVtbiA9IHR5cGVvZiBvcHRpb25zLmlzUm93U2VsZWN0ZWQgPT09ICdmdW5jdGlvbidcbiAgICAgICYmIHR5cGVvZiBldmVudEhhbmRsZXJzLm9uUm93U2VsZWN0ID09PSAnZnVuY3Rpb24nXG4gICAgICAmJiB0eXBlb2YgZXZlbnRIYW5kbGVycy5vblJvd0Rlc2VsZWN0ID09PSAnZnVuY3Rpb24nO1xuXG4gICAgY29uc3Qgcm93U3R5bGUgPSAhaW5saW5lID8ge30gOiB7IHdoaXRlU3BhY2U6ICdub3dyYXAnLCB0ZXh0T3ZlcmZsb3c6ICdlbGxpcHNpcycgfTtcbiAgICBsZXQgY2xhc3NOYW1lID0gZGF0YVJvd0NsYXNzKG51bGwsIGlubGluZSA/ICdpbmxpbmUnIDogJycpO1xuXG4gICAgY29uc3QgeyBkZXJpdmVSb3dDbGFzc05hbWUgfSA9IG9wdGlvbnM7XG4gICAgaWYgKHR5cGVvZiBkZXJpdmVSb3dDbGFzc05hbWUgPT09ICdmdW5jdGlvbicpIHtcbiAgICAgIGxldCBkZXJpdmVkQ2xhc3NOYW1lID0gZGVyaXZlUm93Q2xhc3NOYW1lKHJvdyk7XG4gICAgICBjbGFzc05hbWUgKz0gKHR5cGVvZiBkZXJpdmVkQ2xhc3NOYW1lID09PSAnc3RyaW5nJykgPyAnICcgKyBkZXJpdmVkQ2xhc3NOYW1lIDogJyc7XG4gICAgfTtcblxuICAgIGNvbnN0IHNoYXJlZFByb3BzID0geyByb3csIGlubGluZSwgb3B0aW9ucywgcm93SW5kZXggfTtcblxuICAgIHJldHVybiAoXG4gICAgICA8dHIgY2xhc3NOYW1lPXtjbGFzc05hbWV9IHN0eWxlPXtyb3dTdHlsZX0gb25DbGljaz17dGhpcy5oYW5kbGVSb3dDbGlja30+XG4gICAgICAgIHshaGFzU2VsZWN0aW9uQ29sdW1uXG4gICAgICAgICAgPyBudWxsXG4gICAgICAgICAgOiA8U2VsZWN0aW9uQ2VsbFxuICAgICAgICAgICAgICByb3c9e3Jvd31cbiAgICAgICAgICAgICAgZXZlbnRIYW5kbGVycz17ZXZlbnRIYW5kbGVyc31cbiAgICAgICAgICAgICAgaXNSb3dTZWxlY3RlZD17b3B0aW9ucy5pc1Jvd1NlbGVjdGVkfVxuICAgICAgICAgICAgLz5cbiAgICAgICAgfVxuICAgICAgICB7Y29sdW1ucy5tYXAoKGNvbHVtbiwgY29sdW1uSW5kZXgpID0+IHtcbiAgICAgICAgICBpZiAodHlwZW9mIGNvbHVtbkRlZmF1bHRzID09PSAnb2JqZWN0JylcbiAgICAgICAgICAgIGNvbHVtbiA9IE9iamVjdC5hc3NpZ24oe30sIGNvbHVtbkRlZmF1bHRzLCBjb2x1bW4pO1xuICAgICAgICAgIHJldHVybiAoXG4gICAgICAgICAgICA8RGF0YUNlbGxcbiAgICAgICAgICAgICAga2V5PXtjb2x1bW4ua2V5fVxuICAgICAgICAgICAgICBjb2x1bW49e2NvbHVtbn1cbiAgICAgICAgICAgICAgY29sdW1uSW5kZXg9e2NvbHVtbkluZGV4fVxuICAgICAgICAgICAgICB7Li4uc2hhcmVkUHJvcHN9XG4gICAgICAgICAgICAvPlxuICAgICAgICAgICk7XG4gICAgICAgIH0pfVxuICAgICAgPC90cj5cbiAgICApXG4gIH1cbn07XG5cbkRhdGFSb3cucHJvcFR5cGVzID0ge1xuICByb3c6IFByb3BUeXBlcy5vYmplY3QuaXNSZXF1aXJlZCxcbiAgcm93SW5kZXg6IFByb3BUeXBlcy5udW1iZXIuaXNSZXF1aXJlZCxcbiAgY29sdW1uczogUHJvcFR5cGVzLmFycmF5LmlzUmVxdWlyZWQsXG5cbiAgb3B0aW9uczogUHJvcFR5cGVzLm9iamVjdCxcbiAgYWN0aW9uczogUHJvcFR5cGVzLmFycmF5LFxuICBldmVudEhhbmRsZXJzOiBQcm9wVHlwZXMub2JqZWN0XG59O1xuXG5leHBvcnQgZGVmYXVsdCBEYXRhUm93O1xuIl19