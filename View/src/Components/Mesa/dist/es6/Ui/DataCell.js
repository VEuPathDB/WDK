'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _Templates = require('../Templates');

var _Templates2 = _interopRequireDefault(_Templates);

var _Utils = require('../Utils/Utils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var dataCellClass = (0, _Utils.makeClassifier)('DataCell');

var DataCell = function (_React$PureComponent) {
  _inherits(DataCell, _React$PureComponent);

  function DataCell(props) {
    _classCallCheck(this, DataCell);

    var _this = _possibleConstructorReturn(this, (DataCell.__proto__ || Object.getPrototypeOf(DataCell)).call(this, props));

    _this.renderContent = _this.renderContent.bind(_this);
    return _this;
  }

  _createClass(DataCell, [{
    key: 'renderContent',
    value: function renderContent() {
      var _props = this.props,
          row = _props.row,
          column = _props.column,
          rowIndex = _props.rowIndex,
          columnIndex = _props.columnIndex,
          inline = _props.inline;
      var key = column.key,
          getValue = column.getValue;

      var value = typeof getValue === 'function' ? getValue({ row: row, key: key }) : row[key];
      var cellProps = { key: key, value: value, row: row, column: column, rowIndex: rowIndex, columnIndex: columnIndex };

      if ('renderCell' in column) {
        return column.renderCell(cellProps);
      }

      if (!column.type) return _Templates2.default.textCell(cellProps);

      switch (column.type.toLowerCase()) {
        case 'link':
          return _Templates2.default.linkCell(cellProps);
        case 'number':
          return _Templates2.default.numberCell(cellProps);
        case 'html':
          return _Templates2.default[inline ? 'textCell' : 'htmlCell'](cellProps);
        case 'text':
        default:
          return _Templates2.default.textCell(cellProps);
      };
    }
  }, {
    key: 'render',
    value: function render() {
      var _props2 = this.props,
          column = _props2.column,
          row = _props2.row,
          inline = _props2.inline;
      var style = column.style,
          width = column.width,
          className = column.className,
          key = column.key;


      var whiteSpace = !inline ? {} : {
        textOverflow: 'ellipsis',
        overflow: 'hidden',
        maxWidth: options.inlineMaxWidth ? options.inlineMaxWidth : '20vw',
        maxHeight: options.inlineMaxHeight ? options.inlineMaxHeight : '2em'
      };

      width = typeof width === 'number' ? width + 'px' : width;
      width = width ? { width: width, maxWidth: width, minWidth: width } : {};
      style = Object.assign({}, style, width, whiteSpace);
      className = dataCellClass() + (className ? ' ' + className : '');
      var children = this.renderContent();
      var props = { style: style, children: children, key: key, className: className };

      return column.hidden ? null : _react2.default.createElement('td', props);
    }
  }]);

  return DataCell;
}(_react2.default.PureComponent);

;

DataCell.propTypes = {
  column: _propTypes2.default.object,
  row: _propTypes2.default.object,
  inline: _propTypes2.default.bool
};

exports.default = DataCell;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9EYXRhQ2VsbC5qc3giXSwibmFtZXMiOlsiZGF0YUNlbGxDbGFzcyIsIkRhdGFDZWxsIiwicHJvcHMiLCJyZW5kZXJDb250ZW50IiwiYmluZCIsInJvdyIsImNvbHVtbiIsInJvd0luZGV4IiwiY29sdW1uSW5kZXgiLCJpbmxpbmUiLCJrZXkiLCJnZXRWYWx1ZSIsInZhbHVlIiwiY2VsbFByb3BzIiwicmVuZGVyQ2VsbCIsInR5cGUiLCJ0ZXh0Q2VsbCIsInRvTG93ZXJDYXNlIiwibGlua0NlbGwiLCJudW1iZXJDZWxsIiwic3R5bGUiLCJ3aWR0aCIsImNsYXNzTmFtZSIsIndoaXRlU3BhY2UiLCJ0ZXh0T3ZlcmZsb3ciLCJvdmVyZmxvdyIsIm1heFdpZHRoIiwib3B0aW9ucyIsImlubGluZU1heFdpZHRoIiwibWF4SGVpZ2h0IiwiaW5saW5lTWF4SGVpZ2h0IiwibWluV2lkdGgiLCJPYmplY3QiLCJhc3NpZ24iLCJjaGlsZHJlbiIsImhpZGRlbiIsIlB1cmVDb21wb25lbnQiLCJwcm9wVHlwZXMiLCJvYmplY3QiLCJib29sIl0sIm1hcHBpbmdzIjoiOzs7Ozs7OztBQUFBOzs7O0FBQ0E7Ozs7QUFFQTs7OztBQUNBOzs7Ozs7Ozs7O0FBRUEsSUFBTUEsZ0JBQWdCLDJCQUFlLFVBQWYsQ0FBdEI7O0lBRU1DLFE7OztBQUNKLG9CQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsb0hBQ1pBLEtBRFk7O0FBRWxCLFVBQUtDLGFBQUwsR0FBcUIsTUFBS0EsYUFBTCxDQUFtQkMsSUFBbkIsT0FBckI7QUFGa0I7QUFHbkI7Ozs7b0NBRWdCO0FBQUEsbUJBQ3dDLEtBQUtGLEtBRDdDO0FBQUEsVUFDUEcsR0FETyxVQUNQQSxHQURPO0FBQUEsVUFDRkMsTUFERSxVQUNGQSxNQURFO0FBQUEsVUFDTUMsUUFETixVQUNNQSxRQUROO0FBQUEsVUFDZ0JDLFdBRGhCLFVBQ2dCQSxXQURoQjtBQUFBLFVBQzZCQyxNQUQ3QixVQUM2QkEsTUFEN0I7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OztrQkFNZXRDLFEiLCJmaWxlIjoiRGF0YUNlbGwuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IFByb3BUeXBlcyBmcm9tICdwcm9wLXR5cGVzJztcblxuaW1wb3J0IFRlbXBsYXRlcyBmcm9tICcuLi9UZW1wbGF0ZXMnO1xuaW1wb3J0IHsgbWFrZUNsYXNzaWZpZXIgfSBmcm9tICcuLi9VdGlscy9VdGlscyc7XG5cbmNvbnN0IGRhdGFDZWxsQ2xhc3MgPSBtYWtlQ2xhc3NpZmllcignRGF0YUNlbGwnKTtcblxuY2xhc3MgRGF0YUNlbGwgZXh0ZW5kcyBSZWFjdC5QdXJlQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuICAgIHRoaXMucmVuZGVyQ29udGVudCA9IHRoaXMucmVuZGVyQ29udGVudC5iaW5kKHRoaXMpO1xuICB9XG5cbiAgcmVuZGVyQ29udGVudCAoKSB7XG4gICAgY29uc3QgeyByb3csIGNvbHVtbiwgcm93SW5kZXgsIGNvbHVtbkluZGV4LCBpbmxpbmUgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBrZXksIGdldFZhbHVlIH0gPSBjb2x1bW47XG4gICAgY29uc3QgdmFsdWUgPSB0eXBlb2YgZ2V0VmFsdWUgPT09ICdmdW5jdGlvbicgPyBnZXRWYWx1ZSh7IHJvdywga2V5IH0pIDogcm93W2tleV07XG4gICAgY29uc3QgY2VsbFByb3BzID0geyBrZXksIHZhbHVlLCByb3csIGNvbHVtbiwgcm93SW5kZXgsIGNvbHVtbkluZGV4IH07XG5cbiAgICBpZiAoJ3JlbmRlckNlbGwnIGluIGNvbHVtbikge1xuICAgICAgcmV0dXJuIGNvbHVtbi5yZW5kZXJDZWxsKGNlbGxQcm9wcyk7XG4gICAgfVxuXG4gICAgaWYgKCFjb2x1bW4udHlwZSkgcmV0dXJuIFRlbXBsYXRlcy50ZXh0Q2VsbChjZWxsUHJvcHMpO1xuXG4gICAgc3dpdGNoIChjb2x1bW4udHlwZS50b0xvd2VyQ2FzZSgpKSB7XG4gICAgICBjYXNlICdsaW5rJzpcbiAgICAgICAgcmV0dXJuIFRlbXBsYXRlcy5saW5rQ2VsbChjZWxsUHJvcHMpO1xuICAgICAgY2FzZSAnbnVtYmVyJzpcbiAgICAgICAgcmV0dXJuIFRlbXBsYXRlcy5udW1iZXJDZWxsKGNlbGxQcm9wcyk7XG4gICAgICBjYXNlICdodG1sJzpcbiAgICAgICAgcmV0dXJuIFRlbXBsYXRlc1tpbmxpbmUgPyAndGV4dENlbGwnIDogJ2h0bWxDZWxsJ10oY2VsbFByb3BzKTtcbiAgICAgIGNhc2UgJ3RleHQnOlxuICAgICAgZGVmYXVsdDpcbiAgICAgICAgcmV0dXJuIFRlbXBsYXRlcy50ZXh0Q2VsbChjZWxsUHJvcHMpO1xuICAgIH07XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGxldCB7IGNvbHVtbiwgcm93LCBpbmxpbmUgfSA9IHRoaXMucHJvcHM7XG4gICAgbGV0IHsgc3R5bGUsIHdpZHRoLCBjbGFzc05hbWUsIGtleSB9ID0gY29sdW1uO1xuXG5cbiAgICBsZXQgd2hpdGVTcGFjZSA9ICFpbmxpbmUgPyB7fSA6IHtcbiAgICAgIHRleHRPdmVyZmxvdzogJ2VsbGlwc2lzJyxcbiAgICAgIG92ZXJmbG93OiAnaGlkZGVuJyxcbiAgICAgIG1heFdpZHRoOiBvcHRpb25zLmlubGluZU1heFdpZHRoID8gb3B0aW9ucy5pbmxpbmVNYXhXaWR0aCA6ICcyMHZ3JyxcbiAgICAgIG1heEhlaWdodDogb3B0aW9ucy5pbmxpbmVNYXhIZWlnaHQgPyBvcHRpb25zLmlubGluZU1heEhlaWdodCA6ICcyZW0nLFxuICAgIH07XG5cbiAgICB3aWR0aCA9ICh0eXBlb2Ygd2lkdGggPT09ICdudW1iZXInID8gd2lkdGggKyAncHgnIDogd2lkdGgpO1xuICAgIHdpZHRoID0gd2lkdGggPyB7IHdpZHRoLCBtYXhXaWR0aDogd2lkdGgsIG1pbldpZHRoOiB3aWR0aCB9IDoge307XG4gICAgc3R5bGUgPSBPYmplY3QuYXNzaWduKHt9LCBzdHlsZSwgd2lkdGgsIHdoaXRlU3BhY2UpO1xuICAgIGNsYXNzTmFtZSA9IGRhdGFDZWxsQ2xhc3MoKSArIChjbGFzc05hbWUgPyAnICcgKyBjbGFzc05hbWUgOiAnJyk7XG4gICAgY29uc3QgY2hpbGRyZW4gPSB0aGlzLnJlbmRlckNvbnRlbnQoKTtcbiAgICBjb25zdCBwcm9wcyA9IHsgc3R5bGUsIGNoaWxkcmVuLCBrZXksIGNsYXNzTmFtZSB9O1xuXG4gICAgcmV0dXJuIGNvbHVtbi5oaWRkZW4gPyBudWxsIDogPHRkIHsuLi5wcm9wc30gLz5cbiAgfVxufTtcblxuRGF0YUNlbGwucHJvcFR5cGVzID0ge1xuICBjb2x1bW46IFByb3BUeXBlcy5vYmplY3QsXG4gIHJvdzogUHJvcFR5cGVzLm9iamVjdCxcbiAgaW5saW5lOiBQcm9wVHlwZXMuYm9vbFxufTtcblxuZXhwb3J0IGRlZmF1bHQgRGF0YUNlbGw7XG4iXX0=