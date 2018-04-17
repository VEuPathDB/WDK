'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _HeadingRow = require('../Ui/HeadingRow');

var _HeadingRow2 = _interopRequireDefault(_HeadingRow);

var _DataRowList = require('../Ui/DataRowList');

var _DataRowList2 = _interopRequireDefault(_DataRowList);

var _Utils = require('../Utils/Utils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var dataTableClass = (0, _Utils.makeClassifier)('DataTable');
var hasWidthProperty = function hasWidthProperty(_ref) {
  var width = _ref.width;
  return typeof width === 'string';
};

var DataTable = function (_React$Component) {
  _inherits(DataTable, _React$Component);

  function DataTable(props) {
    _classCallCheck(this, DataTable);

    var _this = _possibleConstructorReturn(this, (DataTable.__proto__ || Object.getPrototypeOf(DataTable)).call(this, props));

    _this.widthCache = {};
    _this.state = { dynamicWidths: null };
    _this.renderPlainTable = _this.renderPlainTable.bind(_this);
    _this.renderStickyTable = _this.renderStickyTable.bind(_this);
    _this.componentDidMount = _this.componentDidMount.bind(_this);
    _this.getInnerCellWidth = _this.getInnerCellWidth.bind(_this);
    _this.hasSelectionColumn = _this.hasSelectionColumn.bind(_this);
    _this.shouldUseStickyHeader = _this.shouldUseStickyHeader.bind(_this);
    _this.handleTableBodyScroll = _this.handleTableBodyScroll.bind(_this);
    _this.componentWillReceiveProps = _this.componentWillReceiveProps.bind(_this);
    return _this;
  }

  _createClass(DataTable, [{
    key: 'shouldUseStickyHeader',
    value: function shouldUseStickyHeader() {
      var _props = this.props,
          columns = _props.columns,
          options = _props.options;

      if (!options || !options.useStickyHeader) return false;
      if (!options.tableBodyMaxHeight) return console.error('\n      "useStickyHeader" option enabled but no maxHeight for the table is set.\n      Use a css height as the "tableBodyMaxHeight" option to use this setting.\n    ');
      return true;
    }
  }, {
    key: 'componentDidMount',
    value: function componentDidMount() {
      this.setDynamicWidths();
    }
  }, {
    key: 'componentWillReceiveProps',
    value: function componentWillReceiveProps(newProps) {
      var _this2 = this;

      if (newProps && newProps.columns && newProps.columns !== this.props.columns) this.setState({ dynamicWidths: null }, function () {
        return _this2.setDynamicWidths();
      });
    }
  }, {
    key: 'setDynamicWidths',
    value: function setDynamicWidths() {
      var columns = this.props.columns;

      var hasSelectionColumn = this.hasSelectionColumn();
      var headingTable = this.headingTable,
          contentTable = this.contentTable,
          getInnerCellWidth = this.getInnerCellWidth;

      if (!headingTable || !contentTable) return;
      var headingCells = Array.from(headingTable.getElementsByTagName('th'));
      var contentCells = Array.from(contentTable.getElementsByTagName('td'));

      if (hasSelectionColumn) {
        headingCells.shift();
        contentCells.shift();
      }
      var dynamicWidths = columns.map(function (c, i) {
        return getInnerCellWidth(contentCells[i], headingCells[i], c) - (hasSelectionColumn && !i ? 1 : 0);
      });
      this.setState({ dynamicWidths: dynamicWidths }, function () {
        window.dispatchEvent(new Event('MesaReflow'));
      });
    }
  }, {
    key: 'getInnerCellWidth',
    value: function getInnerCellWidth(cell, headingCell, _ref2) {
      var key = _ref2.key;

      if (key && key in this.widthCache) return this.widthCache[key];

      var contentWidth = cell.clientWidth;
      var headingWidth = headingCell.clientWidth;
      var grabStyle = function grabStyle(prop) {
        return parseInt(window.getComputedStyle(cell, null).getPropertyValue(prop));
      };

      var leftPadding = grabStyle('padding-left');
      var rightPadding = grabStyle('padding-right');
      var leftBorder = grabStyle('border-left-width');
      var rightBorder = grabStyle('border-right-width');
      var widthOffset = leftPadding + rightPadding + leftBorder + rightBorder;

      var higher = Math.max(contentWidth, headingWidth);
      return this.widthCache[key] = higher;
    }
  }, {
    key: 'hasSelectionColumn',
    value: function hasSelectionColumn() {
      var _props2 = this.props,
          options = _props2.options,
          eventHandlers = _props2.eventHandlers;

      return typeof options.isRowSelected === 'function' && typeof eventHandlers.onRowSelect === 'function' && typeof eventHandlers.onRowDeselect === 'function';
    }
  }, {
    key: 'handleTableBodyScroll',
    value: function handleTableBodyScroll(e) {
      var offset = this.bodyNode.scrollLeft;
      this.headerNode.scrollLeft = offset;
      window.dispatchEvent(new Event('MesaScroll'));
    }

    // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

  }, {
    key: 'renderStickyTable',
    value: function renderStickyTable() {
      var _this3 = this;

      var _props3 = this.props,
          options = _props3.options,
          columns = _props3.columns,
          rows = _props3.rows,
          filteredRows = _props3.filteredRows,
          actions = _props3.actions,
          eventHandlers = _props3.eventHandlers,
          uiState = _props3.uiState;
      var dynamicWidths = this.state.dynamicWidths;

      var newColumns = columns.every(function (_ref3) {
        var width = _ref3.width;
        return width;
      }) || !dynamicWidths || dynamicWidths.length == 0 ? columns : columns.map(function (column, index) {
        return Object.assign({}, column, { width: dynamicWidths[index] });
      });
      var maxHeight = { maxHeight: options ? options.tableBodyMaxHeight : null };
      var maxWidth = { minWidth: dynamicWidths ? (0, _Utils.combineWidths)(columns.map(function (_ref4) {
          var width = _ref4.width;
          return width;
        })) : null };
      var tableLayout = { tableLayout: dynamicWidths ? 'fixed' : 'auto' };
      var tableProps = { options: options, rows: rows, filteredRows: filteredRows, actions: actions, eventHandlers: eventHandlers, uiState: uiState, columns: newColumns };
      return _react2.default.createElement(
        'div',
        { className: 'MesaComponent' },
        _react2.default.createElement(
          'div',
          { className: dataTableClass(), style: maxWidth },
          _react2.default.createElement(
            'div',
            { className: dataTableClass('Sticky'), style: maxWidth },
            _react2.default.createElement(
              'div',
              {
                ref: function ref(node) {
                  return _this3.headerNode = node;
                },
                className: dataTableClass('Header') },
              _react2.default.createElement(
                'table',
                {
                  cellSpacing: 0,
                  cellPadding: 0,
                  style: tableLayout,
                  ref: function ref(node) {
                    return _this3.headingTable = node;
                  } },
                _react2.default.createElement(_HeadingRow2.default, tableProps)
              )
            ),
            _react2.default.createElement(
              'div',
              {
                style: maxHeight,
                ref: function ref(node) {
                  return _this3.bodyNode = node;
                },
                className: dataTableClass('Body'),
                onScroll: this.handleTableBodyScroll },
              _react2.default.createElement(
                'table',
                {
                  cellSpacing: 0,
                  cellPadding: 0,
                  style: tableLayout,
                  ref: function ref(node) {
                    return _this3.contentTable = node;
                  } },
                _react2.default.createElement(_DataRowList2.default, tableProps)
              )
            )
          )
        )
      );
    }
  }, {
    key: 'renderPlainTable',
    value: function renderPlainTable() {
      var props = this.props;

      return _react2.default.createElement(
        'div',
        { className: 'MesaComponent' },
        _react2.default.createElement(
          'div',
          { className: dataTableClass() },
          _react2.default.createElement(
            'table',
            { cellSpacing: '0', cellPadding: '0' },
            _react2.default.createElement(
              'thead',
              null,
              _react2.default.createElement(_HeadingRow2.default, props)
            ),
            _react2.default.createElement(_DataRowList2.default, props)
          )
        )
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var shouldUseStickyHeader = this.shouldUseStickyHeader,
          renderStickyTable = this.renderStickyTable,
          renderPlainTable = this.renderPlainTable;

      return shouldUseStickyHeader() ? renderStickyTable() : renderPlainTable();
    }
  }]);

  return DataTable;
}(_react2.default.Component);

;

DataTable.propTypes = {
  rows: _propTypes2.default.array,
  columns: _propTypes2.default.array,
  options: _propTypes2.default.object,
  actions: _propTypes2.default.arrayOf(_propTypes2.default.shape({
    element: _propTypes2.default.oneOfType([_propTypes2.default.func, _propTypes2.default.node, _propTypes2.default.element]),
    handler: _propTypes2.default.func,
    callback: _propTypes2.default.func
  })),
  uiState: _propTypes2.default.object,
  eventHandlers: _propTypes2.default.objectOf(_propTypes2.default.func)
};

exports.default = DataTable;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9EYXRhVGFibGUuanN4Il0sIm5hbWVzIjpbImRhdGFUYWJsZUNsYXNzIiwiaGFzV2lkdGhQcm9wZXJ0eSIsIndpZHRoIiwiRGF0YVRhYmxlIiwicHJvcHMiLCJ3aWR0aENhY2hlIiwic3RhdGUiLCJkeW5hbWljV2lkdGhzIiwicmVuZGVyUGxhaW5UYWJsZSIsImJpbmQiLCJyZW5kZXJTdGlja3lUYWJsZSIsImNvbXBvbmVudERpZE1vdW50IiwiZ2V0SW5uZXJDZWxsV2lkdGgiLCJoYXNTZWxlY3Rpb25Db2x1bW4iLCJzaG91bGRVc2VTdGlja3lIZWFkZXIiLCJoYW5kbGVUYWJsZUJvZHlTY3JvbGwiLCJjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzIiwiY29sdW1ucyIsIm9wdGlvbnMiLCJ1c2VTdGlja3lIZWFkZXIiLCJ0YWJsZUJvZHlNYXhIZWlnaHQiLCJjb25zb2xlIiwiZXJyb3IiLCJzZXREeW5hbWljV2lkdGhzIiwibmV3UHJvcHMiLCJzZXRTdGF0ZSIsImhlYWRpbmdUYWJsZSIsImNvbnRlbnRUYWJsZSIsImhlYWRpbmdDZWxscyIsIkFycmF5IiwiZnJvbSIsImdldEVsZW1lbnRzQnlUYWdOYW1lIiwiY29udGVudENlbGxzIiwic2hpZnQiLCJtYXAiLCJjIiwiaSIsIndpbmRvdyIsImRpc3BhdGNoRXZlbnQiLCJFdmVudCIsImNlbGwiLCJoZWFkaW5nQ2VsbCIsImtleSIsImNvbnRlbnRXaWR0aCIsImNsaWVudFdpZHRoIiwiaGVhZGluZ1dpZHRoIiwiZ3JhYlN0eWxlIiwicHJvcCIsInBhcnNlSW50IiwiZ2V0Q29tcHV0ZWRTdHlsZSIsImdldFByb3BlcnR5VmFsdWUiLCJsZWZ0UGFkZGluZyIsInJpZ2h0UGFkZGluZyIsImxlZnRCb3JkZXIiLCJyaWdodEJvcmRlciIsIndpZHRoT2Zmc2V0IiwiaGlnaGVyIiwiTWF0aCIsIm1heCIsImV2ZW50SGFuZGxlcnMiLCJpc1Jvd1NlbGVjdGVkIiwib25Sb3dTZWxlY3QiLCJvblJvd0Rlc2VsZWN0IiwiZSIsIm9mZnNldCIsImJvZHlOb2RlIiwic2Nyb2xsTGVmdCIsImhlYWRlck5vZGUiLCJyb3dzIiwiZmlsdGVyZWRSb3dzIiwiYWN0aW9ucyIsInVpU3RhdGUiLCJuZXdDb2x1bW5zIiwiZXZlcnkiLCJsZW5ndGgiLCJjb2x1bW4iLCJpbmRleCIsIk9iamVjdCIsImFzc2lnbiIsIm1heEhlaWdodCIsIm1heFdpZHRoIiwibWluV2lkdGgiLCJ0YWJsZUxheW91dCIsInRhYmxlUHJvcHMiLCJub2RlIiwiQ29tcG9uZW50IiwicHJvcFR5cGVzIiwiYXJyYXkiLCJvYmplY3QiLCJhcnJheU9mIiwic2hhcGUiLCJlbGVtZW50Iiwib25lT2ZUeXBlIiwiZnVuYyIsImhhbmRsZXIiLCJjYWxsYmFjayIsIm9iamVjdE9mIl0sIm1hcHBpbmdzIjoiOzs7Ozs7OztBQUFBOzs7O0FBQ0E7Ozs7QUFFQTs7OztBQUNBOzs7O0FBQ0E7Ozs7Ozs7Ozs7QUFFQSxJQUFNQSxpQkFBaUIsMkJBQWUsV0FBZixDQUF2QjtBQUNBLElBQU1DLG1CQUFtQixTQUFuQkEsZ0JBQW1CO0FBQUEsTUFBR0MsS0FBSCxRQUFHQSxLQUFIO0FBQUEsU0FBZSxPQUFPQSxLQUFQLEtBQWlCLFFBQWhDO0FBQUEsQ0FBekI7O0lBRU1DLFM7OztBQUNKLHFCQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsc0hBQ1pBLEtBRFk7O0FBRWxCLFVBQUtDLFVBQUwsR0FBa0IsRUFBbEI7QUFDQSxVQUFLQyxLQUFMLEdBQWEsRUFBRUMsZUFBZSxJQUFqQixFQUFiO0FBQ0EsVUFBS0MsZ0JBQUwsR0FBd0IsTUFBS0EsZ0JBQUwsQ0FBc0JDLElBQXRCLE9BQXhCO0FBQ0EsVUFBS0MsaUJBQUwsR0FBeUIsTUFBS0EsaUJBQUwsQ0FBdUJELElBQXZCLE9BQXpCO0FBQ0EsVUFBS0UsaUJBQUwsR0FBeUIsTUFBS0EsaUJBQUwsQ0FBdUJGLElBQXZCLE9BQXpCO0FBQ0EsVUFBS0csaUJBQUwsR0FBeUIsTUFBS0EsaUJBQUwsQ0FBdUJILElB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QkFEQTtBQUFBLFVBQ3VCSixpQkFEdkIsR0FDK0QsSUFEL0QsQ0FDdUJBLGlCQUR2QjtBQUFBLFVBQzBDRixnQkFEMUMsR0FDK0QsSUFEL0QsQ0FDMENBLGdCQUQxQzs7QUFFUixhQUFPTSwwQkFBMEJKLG1CQUExQixHQUFnREYsa0JBQXZEO0FBQ0Q7Ozs7RUFwSnFCLGdCQUFNNkUsUzs7QUFxSjdCOztBQUVEbEYsVUFBVW1GLFNBQVYsR0FBc0I7QUFDcEJsQixRQUFNLG9CQUFVbUIsS0FESTtBQUVwQnRFLFdBQVMsb0JBQVVzRSxLQUZDO0FBR3BCckUsV0FBUyxvQkFBVXNFLE1BSEM7QUFJcEJsQixXQUFTLG9CQUFVbUIsT0FBVixDQUFrQixvQkFBVUMsS0FBVixDQUFnQjtBQUN6Q0MsYUFBUyxvQkFBVUMsU0FBVixDQUFvQixDQUFFLG9CQUFVQyxJQUFaLEVBQWtCLG9CQUFVVCxJQUE1QixFQUFrQyxvQkFBVU8sT0FBNUMsQ0FBcEIsQ0FEZ0M7QUFFekNHLGFBQVMsb0JBQVVELElBRnNCO0FBR3pDRSxjQUFVLG9CQUFVRjtBQUhxQixHQUFoQixDQUFsQixDQUpXO0FBU3BCdEIsV0FBUyxvQkFBVWlCLE1BVEM7QUFVcEI3QixpQkFBZSxvQkFBVXFDLFFBQVYsQ0FBbUIsb0JBQVVILElBQTdCO0FBVkssQ0FBdEI7O2tCQWFlMUYsUyIsImZpbGUiOiJEYXRhVGFibGUuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IFByb3BUeXBlcyBmcm9tICdwcm9wLXR5cGVzJztcblxuaW1wb3J0IEhlYWRpbmdSb3cgZnJvbSAnLi4vVWkvSGVhZGluZ1Jvdyc7XG5pbXBvcnQgRGF0YVJvd0xpc3QgZnJvbSAnLi4vVWkvRGF0YVJvd0xpc3QnO1xuaW1wb3J0IHsgbWFrZUNsYXNzaWZpZXIsIGNvbWJpbmVXaWR0aHMgfSBmcm9tICcuLi9VdGlscy9VdGlscyc7XG5cbmNvbnN0IGRhdGFUYWJsZUNsYXNzID0gbWFrZUNsYXNzaWZpZXIoJ0RhdGFUYWJsZScpO1xuY29uc3QgaGFzV2lkdGhQcm9wZXJ0eSA9ICh7IHdpZHRoIH0pID0+IHR5cGVvZiB3aWR0aCA9PT0gJ3N0cmluZyc7XG5cbmNsYXNzIERhdGFUYWJsZSBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgICB0aGlzLndpZHRoQ2FjaGUgPSB7fTtcbiAgICB0aGlzLnN0YXRlID0geyBkeW5hbWljV2lkdGhzOiBudWxsIH07XG4gICAgdGhpcy5yZW5kZXJQbGFpblRhYmxlID0gdGhpcy5yZW5kZXJQbGFpblRhYmxlLmJpbmQodGhpcyk7XG4gICAgdGhpcy5yZW5kZXJTdGlja3lUYWJsZSA9IHRoaXMucmVuZGVyU3RpY2t5VGFibGUuYmluZCh0aGlzKTtcbiAgICB0aGlzLmNvbXBvbmVudERpZE1vdW50ID0gdGhpcy5jb21wb25lbnREaWRNb3VudC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuZ2V0SW5uZXJDZWxsV2lkdGggPSB0aGlzLmdldElubmVyQ2VsbFdpZHRoLmJpbmQodGhpcyk7XG4gICAgdGhpcy5oYXNTZWxlY3Rpb25Db2x1bW4gPSB0aGlzLmhhc1NlbGVjdGlvbkNvbHVtbi5iaW5kKHRoaXMpO1xuICAgIHRoaXMuc2hvdWxkVXNlU3RpY2t5SGVhZGVyID0gdGhpcy5zaG91bGRVc2VTdGlja3lIZWFkZXIuYmluZCh0aGlzKTtcbiAgICB0aGlzLmhhbmRsZVRhYmxlQm9keVNjcm9sbCA9IHRoaXMuaGFuZGxlVGFibGVCb2R5U2Nyb2xsLmJpbmQodGhpcyk7XG4gICAgdGhpcy5jb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzID0gdGhpcy5jb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzLmJpbmQodGhpcyk7XG4gIH1cblxuICBzaG91bGRVc2VTdGlja3lIZWFkZXIgKCkge1xuICAgIGNvbnN0IHsgY29sdW1ucywgb3B0aW9ucyB9ID0gdGhpcy5wcm9wcztcbiAgICBpZiAoIW9wdGlvbnMgfHwgIW9wdGlvbnMudXNlU3RpY2t5SGVhZGVyKSByZXR1cm4gZmFsc2U7XG4gICAgaWYgKCFvcHRpb25zLnRhYmxlQm9keU1heEhlaWdodCkgcmV0dXJuIGNvbnNvbGUuZXJyb3IoYFxuICAgICAgXCJ1c2VTdGlja3lIZWFkZXJcIiBvcHRpb24gZW5hYmxlZCBidXQgbm8gbWF4SGVpZ2h0IGZvciB0aGUgdGFibGUgaXMgc2V0LlxuICAgICAgVXNlIGEgY3NzIGhlaWdodCBhcyB0aGUgXCJ0YWJsZUJvZHlNYXhIZWlnaHRcIiBvcHRpb24gdG8gdXNlIHRoaXMgc2V0dGluZy5cbiAgICBgKTtcbiAgICByZXR1cm4gdHJ1ZTtcbiAgfVxuXG4gIGNvbXBvbmVudERpZE1vdW50ICgpIHtcbiAgICB0aGlzLnNldER5bmFtaWNXaWR0aHMoKTtcbiAgfVxuXG4gIGNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHMgKG5ld1Byb3BzKSB7XG4gICAgaWYgKG5ld1Byb3BzICYmIG5ld1Byb3BzLmNvbHVtbnMgJiYgbmV3UHJvcHMuY29sdW1ucyAhPT0gdGhpcy5wcm9wcy5jb2x1bW5zKVxuICAgICAgdGhpcy5zZXRTdGF0ZSh7IGR5bmFtaWNXaWR0aHM6IG51bGwgfSwgKCkgPT4gdGhpcy5zZXREeW5hbWljV2lkdGhzKCkpO1xuICB9XG5cbiAgc2V0RHluYW1pY1dpZHRocyAoKSB7XG4gICAgY29uc3QgeyBjb2x1bW5zIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IGhhc1NlbGVjdGlvbkNvbHVtbiA9IHRoaXMuaGFzU2VsZWN0aW9uQ29sdW1uKCk7XG4gICAgY29uc3QgeyBoZWFkaW5nVGFibGUsIGNvbnRlbnRUYWJsZSwgZ2V0SW5uZXJDZWxsV2lkdGggfSA9IHRoaXM7XG4gICAgaWYgKCFoZWFkaW5nVGFibGUgfHwgIWNvbnRlbnRUYWJsZSkgcmV0dXJuO1xuICAgIGNvbnN0IGhlYWRpbmdDZWxscyA9IEFycmF5LmZyb20oaGVhZGluZ1RhYmxlLmdldEVsZW1lbnRzQnlUYWdOYW1lKCd0aCcpKTtcbiAgICBjb25zdCBjb250ZW50Q2VsbHMgPSBBcnJheS5mcm9tKGNvbnRlbnRUYWJsZS5nZXRFbGVtZW50c0J5VGFnTmFtZSgndGQnKSk7XG5cbiAgICBpZiAoaGFzU2VsZWN0aW9uQ29sdW1uKSB7XG4gICAgICBoZWFkaW5nQ2VsbHMuc2hpZnQoKTtcbiAgICAgIGNvbnRlbnRDZWxscy5zaGlmdCgpO1xuICAgIH1cbiAgICBjb25zdCBkeW5hbWljV2lkdGhzID0gY29sdW1ucy5tYXAoKGMsIGkpID0+IGdldElubmVyQ2VsbFdpZHRoKGNvbnRlbnRDZWxsc1tpXSwgaGVhZGluZ0NlbGxzW2ldLCBjKSAtIChoYXNTZWxlY3Rpb25Db2x1bW4gJiYgIWkgPyAxIDogMCkpO1xuICAgIHRoaXMuc2V0U3RhdGUoeyBkeW5hbWljV2lkdGhzIH0sICgpID0+IHtcbiAgICAgIHdpbmRvdy5kaXNwYXRjaEV2ZW50KG5ldyBFdmVudCgnTWVzYVJlZmxvdycpKTtcbiAgICB9KTtcbiAgfVxuXG4gIGdldElubmVyQ2VsbFdpZHRoIChjZWxsLCBoZWFkaW5nQ2VsbCwgeyBrZXkgfSkge1xuICAgIGlmIChrZXkgJiYga2V5IGluIHRoaXMud2lkdGhDYWNoZSkgcmV0dXJuIHRoaXMud2lkdGhDYWNoZVtrZXldO1xuXG4gICAgY29uc3QgY29udGVudFdpZHRoID0gY2VsbC5jbGllbnRXaWR0aDtcbiAgICBjb25zdCBoZWFkaW5nV2lkdGggPSBoZWFkaW5nQ2VsbC5jbGllbnRXaWR0aDtcbiAgICBjb25zdCBncmFiU3R5bGUgPSAocHJvcCkgPT4gcGFyc2VJbnQod2luZG93LmdldENvbXB1dGVkU3R5bGUoY2VsbCwgbnVsbCkuZ2V0UHJvcGVydHlWYWx1ZShwcm9wKSk7XG5cbiAgICBjb25zdCBsZWZ0UGFkZGluZyA9IGdyYWJTdHlsZSgncGFkZGluZy1sZWZ0Jyk7XG4gICAgY29uc3QgcmlnaHRQYWRkaW5nID0gZ3JhYlN0eWxlKCdwYWRkaW5nLXJpZ2h0Jyk7XG4gICAgY29uc3QgbGVmdEJvcmRlciA9IGdyYWJTdHlsZSgnYm9yZGVyLWxlZnQtd2lkdGgnKTtcbiAgICBjb25zdCByaWdodEJvcmRlciA9IGdyYWJTdHlsZSgnYm9yZGVyLXJpZ2h0LXdpZHRoJyk7XG4gICAgY29uc3Qgd2lkdGhPZmZzZXQgPSBsZWZ0UGFkZGluZyArIHJpZ2h0UGFkZGluZyArIGxlZnRCb3JkZXIgKyByaWdodEJvcmRlcjtcblxuICAgIGNvbnN0IGhpZ2hlciA9IE1hdGgubWF4KGNvbnRlbnRXaWR0aCwgaGVhZGluZ1dpZHRoKTtcbiAgICByZXR1cm4gdGhpcy53aWR0aENhY2hlW2tleV0gPSBoaWdoZXI7XG4gIH1cblxuICBoYXNTZWxlY3Rpb25Db2x1bW4gKCkge1xuICAgIGNvbnN0IHsgb3B0aW9ucywgZXZlbnRIYW5kbGVycyB9ID0gdGhpcy5wcm9wcztcbiAgICByZXR1cm4gdHlwZW9mIG9wdGlvbnMuaXNSb3dTZWxlY3RlZCA9PT0gJ2Z1bmN0aW9uJ1xuICAgICAgJiYgdHlwZW9mIGV2ZW50SGFuZGxlcnMub25Sb3dTZWxlY3QgPT09ICdmdW5jdGlvbidcbiAgICAgICYmIHR5cGVvZiBldmVudEhhbmRsZXJzLm9uUm93RGVzZWxlY3QgPT09ICdmdW5jdGlvbic7XG4gIH1cblxuICBoYW5kbGVUYWJsZUJvZHlTY3JvbGwgKGUpIHtcbiAgICBjb25zdCBvZmZzZXQgPSB0aGlzLmJvZHlOb2RlLnNjcm9sbExlZnQ7XG4gICAgdGhpcy5oZWFkZXJOb2RlLnNjcm9sbExlZnQgPSBvZmZzZXQ7XG4gICAgd2luZG93LmRpc3BhdGNoRXZlbnQobmV3IEV2ZW50KCdNZXNhU2Nyb2xsJykpO1xuICB9XG5cbiAgLy8gLT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT1cblxuICByZW5kZXJTdGlja3lUYWJsZSAoKSB7XG4gICAgY29uc3QgeyBvcHRpb25zLCBjb2x1bW5zLCByb3dzLCBmaWx0ZXJlZFJvd3MsIGFjdGlvbnMsIGV2ZW50SGFuZGxlcnMsIHVpU3RhdGUgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyBkeW5hbWljV2lkdGhzIH0gPSB0aGlzLnN0YXRlO1xuICAgIGNvbnN0IG5ld0NvbHVtbnMgPSBjb2x1bW5zLmV2ZXJ5KCh7IHdpZHRoIH0pID0+IHdpZHRoKSB8fCAhZHluYW1pY1dpZHRocyB8fCBkeW5hbWljV2lkdGhzLmxlbmd0aCA9PSAwXG4gICAgICA/IGNvbHVtbnNcbiAgICAgIDogY29sdW1ucy5tYXAoKGNvbHVtbiwgaW5kZXgpID0+IE9iamVjdC5hc3NpZ24oe30sIGNvbHVtbiwgeyB3aWR0aDogZHluYW1pY1dpZHRoc1tpbmRleF0gfSkpO1xuICAgIGNvbnN0IG1heEhlaWdodCA9IHsgbWF4SGVpZ2h0OiBvcHRpb25zID8gb3B0aW9ucy50YWJsZUJvZHlNYXhIZWlnaHQgOiBudWxsIH07XG4gICAgY29uc3QgbWF4V2lkdGggPSB7IG1pbldpZHRoOiBkeW5hbWljV2lkdGhzID8gY29tYmluZVdpZHRocyhjb2x1bW5zLm1hcCgoeyB3aWR0aCB9KSA9PiB3aWR0aCkpIDogbnVsbCB9O1xuICAgIGNvbnN0IHRhYmxlTGF5b3V0ID0geyB0YWJsZUxheW91dDogZHluYW1pY1dpZHRocyA/ICdmaXhlZCcgOiAnYXV0bycgfTtcbiAgICBjb25zdCB0YWJsZVByb3BzID0geyBvcHRpb25zLCByb3dzLCBmaWx0ZXJlZFJvd3MsIGFjdGlvbnMsIGV2ZW50SGFuZGxlcnMsIHVpU3RhdGUsIGNvbHVtbnM6IG5ld0NvbHVtbnMgfTtcbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJNZXNhQ29tcG9uZW50XCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPXtkYXRhVGFibGVDbGFzcygpfSBzdHlsZT17bWF4V2lkdGh9PlxuICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPXtkYXRhVGFibGVDbGFzcygnU3RpY2t5Jyl9IHN0eWxlPXttYXhXaWR0aH0+XG4gICAgICAgICAgICA8ZGl2XG4gICAgICAgICAgICAgIHJlZj17bm9kZSA9PiB0aGlzLmhlYWRlck5vZGUgPSBub2RlfVxuICAgICAgICAgICAgICBjbGFzc05hbWU9e2RhdGFUYWJsZUNsYXNzKCdIZWFkZXInKX0+XG4gICAgICAgICAgICAgIDx0YWJsZVxuICAgICAgICAgICAgICAgIGNlbGxTcGFjaW5nPXswfVxuICAgICAgICAgICAgICAgIGNlbGxQYWRkaW5nPXswfVxuICAgICAgICAgICAgICAgIHN0eWxlPXt0YWJsZUxheW91dH1cbiAgICAgICAgICAgICAgICByZWY9e25vZGUgPT4gdGhpcy5oZWFkaW5nVGFibGUgPSBub2RlfT5cbiAgICAgICAgICAgICAgICA8SGVhZGluZ1JvdyB7Li4udGFibGVQcm9wc30gLz5cbiAgICAgICAgICAgICAgPC90YWJsZT5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgPGRpdlxuICAgICAgICAgICAgICBzdHlsZT17bWF4SGVpZ2h0fVxuICAgICAgICAgICAgICByZWY9e25vZGUgPT4gdGhpcy5ib2R5Tm9kZSA9IG5vZGV9XG4gICAgICAgICAgICAgIGNsYXNzTmFtZT17ZGF0YVRhYmxlQ2xhc3MoJ0JvZHknKX1cbiAgICAgICAgICAgICAgb25TY3JvbGw9e3RoaXMuaGFuZGxlVGFibGVCb2R5U2Nyb2xsfT5cbiAgICAgICAgICAgICAgPHRhYmxlXG4gICAgICAgICAgICAgICAgY2VsbFNwYWNpbmc9ezB9XG4gICAgICAgICAgICAgICAgY2VsbFBhZGRpbmc9ezB9XG4gICAgICAgICAgICAgICAgc3R5bGU9e3RhYmxlTGF5b3V0fVxuICAgICAgICAgICAgICAgIHJlZj17bm9kZSA9PiB0aGlzLmNvbnRlbnRUYWJsZSA9IG5vZGV9PlxuICAgICAgICAgICAgICAgIDxEYXRhUm93TGlzdCB7Li4udGFibGVQcm9wc30gLz5cbiAgICAgICAgICAgICAgPC90YWJsZT5cbiAgICAgICAgICAgIDwvZGl2PlxuXG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxuXG4gIHJlbmRlclBsYWluVGFibGUgKCkge1xuICAgIGNvbnN0IHsgcHJvcHMgfSA9IHRoaXM7XG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiTWVzYUNvbXBvbmVudFwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT17ZGF0YVRhYmxlQ2xhc3MoKX0+XG4gICAgICAgICAgPHRhYmxlIGNlbGxTcGFjaW5nPVwiMFwiIGNlbGxQYWRkaW5nPVwiMFwiPlxuICAgICAgICAgICAgPHRoZWFkPlxuICAgICAgICAgICAgICA8SGVhZGluZ1JvdyB7Li4ucHJvcHN9IC8+XG4gICAgICAgICAgICA8L3RoZWFkPlxuICAgICAgICAgICAgPERhdGFSb3dMaXN0IHsuLi5wcm9wc30gLz5cbiAgICAgICAgICA8L3RhYmxlPlxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cblxuICByZW5kZXIgKCkge1xuICAgIGNvbnN0IHsgc2hvdWxkVXNlU3RpY2t5SGVhZGVyLCByZW5kZXJTdGlja3lUYWJsZSwgcmVuZGVyUGxhaW5UYWJsZSB9ID0gdGhpcztcbiAgICByZXR1cm4gc2hvdWxkVXNlU3RpY2t5SGVhZGVyKCkgPyByZW5kZXJTdGlja3lUYWJsZSgpIDogcmVuZGVyUGxhaW5UYWJsZSgpO1xuICB9XG59O1xuXG5EYXRhVGFibGUucHJvcFR5cGVzID0ge1xuICByb3dzOiBQcm9wVHlwZXMuYXJyYXksXG4gIGNvbHVtbnM6IFByb3BUeXBlcy5hcnJheSxcbiAgb3B0aW9uczogUHJvcFR5cGVzLm9iamVjdCxcbiAgYWN0aW9uczogUHJvcFR5cGVzLmFycmF5T2YoUHJvcFR5cGVzLnNoYXBlKHtcbiAgICBlbGVtZW50OiBQcm9wVHlwZXMub25lT2ZUeXBlKFsgUHJvcFR5cGVzLmZ1bmMsIFByb3BUeXBlcy5ub2RlLCBQcm9wVHlwZXMuZWxlbWVudCBdKSxcbiAgICBoYW5kbGVyOiBQcm9wVHlwZXMuZnVuYyxcbiAgICBjYWxsYmFjazogUHJvcFR5cGVzLmZ1bmNcbiAgfSkpLFxuICB1aVN0YXRlOiBQcm9wVHlwZXMub2JqZWN0LFxuICBldmVudEhhbmRsZXJzOiBQcm9wVHlwZXMub2JqZWN0T2YoUHJvcFR5cGVzLmZ1bmMpXG59O1xuXG5leHBvcnQgZGVmYXVsdCBEYXRhVGFibGU7XG4iXX0=