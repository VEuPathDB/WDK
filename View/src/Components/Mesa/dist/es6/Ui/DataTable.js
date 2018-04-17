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
            _react2.default.createElement(_HeadingRow2.default, props),
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9EYXRhVGFibGUuanN4Il0sIm5hbWVzIjpbImRhdGFUYWJsZUNsYXNzIiwiaGFzV2lkdGhQcm9wZXJ0eSIsIndpZHRoIiwiRGF0YVRhYmxlIiwicHJvcHMiLCJ3aWR0aENhY2hlIiwic3RhdGUiLCJkeW5hbWljV2lkdGhzIiwicmVuZGVyUGxhaW5UYWJsZSIsImJpbmQiLCJyZW5kZXJTdGlja3lUYWJsZSIsImNvbXBvbmVudERpZE1vdW50IiwiZ2V0SW5uZXJDZWxsV2lkdGgiLCJoYXNTZWxlY3Rpb25Db2x1bW4iLCJzaG91bGRVc2VTdGlja3lIZWFkZXIiLCJoYW5kbGVUYWJsZUJvZHlTY3JvbGwiLCJjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzIiwiY29sdW1ucyIsIm9wdGlvbnMiLCJ1c2VTdGlja3lIZWFkZXIiLCJ0YWJsZUJvZHlNYXhIZWlnaHQiLCJjb25zb2xlIiwiZXJyb3IiLCJzZXREeW5hbWljV2lkdGhzIiwibmV3UHJvcHMiLCJzZXRTdGF0ZSIsImhlYWRpbmdUYWJsZSIsImNvbnRlbnRUYWJsZSIsImhlYWRpbmdDZWxscyIsIkFycmF5IiwiZnJvbSIsImdldEVsZW1lbnRzQnlUYWdOYW1lIiwiY29udGVudENlbGxzIiwic2hpZnQiLCJtYXAiLCJjIiwiaSIsIndpbmRvdyIsImRpc3BhdGNoRXZlbnQiLCJFdmVudCIsImNlbGwiLCJoZWFkaW5nQ2VsbCIsImtleSIsImNvbnRlbnRXaWR0aCIsImNsaWVudFdpZHRoIiwiaGVhZGluZ1dpZHRoIiwiZ3JhYlN0eWxlIiwicHJvcCIsInBhcnNlSW50IiwiZ2V0Q29tcHV0ZWRTdHlsZSIsImdldFByb3BlcnR5VmFsdWUiLCJsZWZ0UGFkZGluZyIsInJpZ2h0UGFkZGluZyIsImxlZnRCb3JkZXIiLCJyaWdodEJvcmRlciIsIndpZHRoT2Zmc2V0IiwiaGlnaGVyIiwiTWF0aCIsIm1heCIsImV2ZW50SGFuZGxlcnMiLCJpc1Jvd1NlbGVjdGVkIiwib25Sb3dTZWxlY3QiLCJvblJvd0Rlc2VsZWN0IiwiZSIsIm9mZnNldCIsImJvZHlOb2RlIiwic2Nyb2xsTGVmdCIsImhlYWRlck5vZGUiLCJyb3dzIiwiZmlsdGVyZWRSb3dzIiwiYWN0aW9ucyIsInVpU3RhdGUiLCJuZXdDb2x1bW5zIiwiZXZlcnkiLCJsZW5ndGgiLCJjb2x1bW4iLCJpbmRleCIsIk9iamVjdCIsImFzc2lnbiIsIm1heEhlaWdodCIsIm1heFdpZHRoIiwibWluV2lkdGgiLCJ0YWJsZUxheW91dCIsInRhYmxlUHJvcHMiLCJub2RlIiwiQ29tcG9uZW50IiwicHJvcFR5cGVzIiwiYXJyYXkiLCJvYmplY3QiLCJhcnJheU9mIiwic2hhcGUiLCJlbGVtZW50Iiwib25lT2ZUeXBlIiwiZnVuYyIsImhhbmRsZXIiLCJjYWxsYmFjayIsIm9iamVjdE9mIl0sIm1hcHBpbmdzIjoiOzs7Ozs7OztBQUFBOzs7O0FBQ0E7Ozs7QUFFQTs7OztBQUNBOzs7O0FBQ0E7Ozs7Ozs7Ozs7QUFFQSxJQUFNQSxpQkFBaUIsMkJBQWUsV0FBZixDQUF2QjtBQUNBLElBQU1DLG1CQUFtQixTQUFuQkEsZ0JBQW1CO0FBQUEsTUFBR0MsS0FBSCxRQUFHQSxLQUFIO0FBQUEsU0FBZSxPQUFPQSxLQUFQLEtBQWlCLFFBQWhDO0FBQUEsQ0FBekI7O0lBRU1DLFM7OztBQUNKLHFCQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsc0hBQ1pBLEtBRFk7O0FBRWxCLFVBQUtDLFVBQUwsR0FBa0IsRUFBbEI7QUFDQSxVQUFLQyxLQUFMLEdBQWEsRUFBRUMsZUFBZSxJQUFqQixFQUFiO0FBQ0EsVUFBS0MsZ0JBQUwsR0FBd0IsTUFBS0EsZ0JBQUwsQ0FBc0JDLElBQXRCLE9BQXhCO0FBQ0EsVUFBS0MsaUJBQUwsR0FBeUIsTUFBS0EsaUJBQUwsQ0FBdUJELElBQXZCLE9BQXpCO0FBQ0EsVUFBS0UsaUJBQUwsR0FBeUIsTUFBS0EsaUJBQUwsQ0FBdUJGLElBQXZCLE9BQXpCO0FBQ0EsVUFBS0csaUJBQUwsR0FBeUIsTUFBS0EsaUJBQUwsQ0FBdUJILElB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RHZCLEdBQytELElBRC9ELENBQ3VCQSxpQkFEdkI7QUFBQSxVQUMwQ0YsZ0JBRDFDLEdBQytELElBRC9ELENBQzBDQSxnQkFEMUM7O0FBRVIsYUFBT00sMEJBQTBCSixtQkFBMUIsR0FBZ0RGLGtCQUF2RDtBQUNEOzs7O0VBbEpxQixnQkFBTTZFLFM7O0FBbUo3Qjs7QUFFRGxGLFVBQVVtRixTQUFWLEdBQXNCO0FBQ3BCbEIsUUFBTSxvQkFBVW1CLEtBREk7QUFFcEJ0RSxXQUFTLG9CQUFVc0UsS0FGQztBQUdwQnJFLFdBQVMsb0JBQVVzRSxNQUhDO0FBSXBCbEIsV0FBUyxvQkFBVW1CLE9BQVYsQ0FBa0Isb0JBQVVDLEtBQVYsQ0FBZ0I7QUFDekNDLGFBQVMsb0JBQVVDLFNBQVYsQ0FBb0IsQ0FBRSxvQkFBVUMsSUFBWixFQUFrQixvQkFBVVQsSUFBNUIsRUFBa0Msb0JBQVVPLE9BQTVDLENBQXBCLENBRGdDO0FBRXpDRyxhQUFTLG9CQUFVRCxJQUZzQjtBQUd6Q0UsY0FBVSxvQkFBVUY7QUFIcUIsR0FBaEIsQ0FBbEIsQ0FKVztBQVNwQnRCLFdBQVMsb0JBQVVpQixNQVRDO0FBVXBCN0IsaUJBQWUsb0JBQVVxQyxRQUFWLENBQW1CLG9CQUFVSCxJQUE3QjtBQVZLLENBQXRCOztrQkFhZTFGLFMiLCJmaWxlIjoiRGF0YVRhYmxlLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IFJlYWN0IGZyb20gJ3JlYWN0JztcbmltcG9ydCBQcm9wVHlwZXMgZnJvbSAncHJvcC10eXBlcyc7XG5cbmltcG9ydCBIZWFkaW5nUm93IGZyb20gJy4uL1VpL0hlYWRpbmdSb3cnO1xuaW1wb3J0IERhdGFSb3dMaXN0IGZyb20gJy4uL1VpL0RhdGFSb3dMaXN0JztcbmltcG9ydCB7IG1ha2VDbGFzc2lmaWVyLCBjb21iaW5lV2lkdGhzIH0gZnJvbSAnLi4vVXRpbHMvVXRpbHMnO1xuXG5jb25zdCBkYXRhVGFibGVDbGFzcyA9IG1ha2VDbGFzc2lmaWVyKCdEYXRhVGFibGUnKTtcbmNvbnN0IGhhc1dpZHRoUHJvcGVydHkgPSAoeyB3aWR0aCB9KSA9PiB0eXBlb2Ygd2lkdGggPT09ICdzdHJpbmcnO1xuXG5jbGFzcyBEYXRhVGFibGUgZXh0ZW5kcyBSZWFjdC5Db21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG4gICAgdGhpcy53aWR0aENhY2hlID0ge307XG4gICAgdGhpcy5zdGF0ZSA9IHsgZHluYW1pY1dpZHRoczogbnVsbCB9O1xuICAgIHRoaXMucmVuZGVyUGxhaW5UYWJsZSA9IHRoaXMucmVuZGVyUGxhaW5UYWJsZS5iaW5kKHRoaXMpO1xuICAgIHRoaXMucmVuZGVyU3RpY2t5VGFibGUgPSB0aGlzLnJlbmRlclN0aWNreVRhYmxlLmJpbmQodGhpcyk7XG4gICAgdGhpcy5jb21wb25lbnREaWRNb3VudCA9IHRoaXMuY29tcG9uZW50RGlkTW91bnQuYmluZCh0aGlzKTtcbiAgICB0aGlzLmdldElubmVyQ2VsbFdpZHRoID0gdGhpcy5nZXRJbm5lckNlbGxXaWR0aC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuaGFzU2VsZWN0aW9uQ29sdW1uID0gdGhpcy5oYXNTZWxlY3Rpb25Db2x1bW4uYmluZCh0aGlzKTtcbiAgICB0aGlzLnNob3VsZFVzZVN0aWNreUhlYWRlciA9IHRoaXMuc2hvdWxkVXNlU3RpY2t5SGVhZGVyLmJpbmQodGhpcyk7XG4gICAgdGhpcy5oYW5kbGVUYWJsZUJvZHlTY3JvbGwgPSB0aGlzLmhhbmRsZVRhYmxlQm9keVNjcm9sbC5iaW5kKHRoaXMpO1xuICAgIHRoaXMuY29tcG9uZW50V2lsbFJlY2VpdmVQcm9wcyA9IHRoaXMuY29tcG9uZW50V2lsbFJlY2VpdmVQcm9wcy5iaW5kKHRoaXMpO1xuICB9XG5cbiAgc2hvdWxkVXNlU3RpY2t5SGVhZGVyICgpIHtcbiAgICBjb25zdCB7IGNvbHVtbnMsIG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG4gICAgaWYgKCFvcHRpb25zIHx8ICFvcHRpb25zLnVzZVN0aWNreUhlYWRlcikgcmV0dXJuIGZhbHNlO1xuICAgIGlmICghb3B0aW9ucy50YWJsZUJvZHlNYXhIZWlnaHQpIHJldHVybiBjb25zb2xlLmVycm9yKGBcbiAgICAgIFwidXNlU3RpY2t5SGVhZGVyXCIgb3B0aW9uIGVuYWJsZWQgYnV0IG5vIG1heEhlaWdodCBmb3IgdGhlIHRhYmxlIGlzIHNldC5cbiAgICAgIFVzZSBhIGNzcyBoZWlnaHQgYXMgdGhlIFwidGFibGVCb2R5TWF4SGVpZ2h0XCIgb3B0aW9uIHRvIHVzZSB0aGlzIHNldHRpbmcuXG4gICAgYCk7XG4gICAgcmV0dXJuIHRydWU7XG4gIH1cblxuICBjb21wb25lbnREaWRNb3VudCAoKSB7XG4gICAgdGhpcy5zZXREeW5hbWljV2lkdGhzKCk7XG4gIH1cblxuICBjb21wb25lbnRXaWxsUmVjZWl2ZVByb3BzIChuZXdQcm9wcykge1xuICAgIGlmIChuZXdQcm9wcyAmJiBuZXdQcm9wcy5jb2x1bW5zICYmIG5ld1Byb3BzLmNvbHVtbnMgIT09IHRoaXMucHJvcHMuY29sdW1ucylcbiAgICAgIHRoaXMuc2V0U3RhdGUoeyBkeW5hbWljV2lkdGhzOiBudWxsIH0sICgpID0+IHRoaXMuc2V0RHluYW1pY1dpZHRocygpKTtcbiAgfVxuXG4gIHNldER5bmFtaWNXaWR0aHMgKCkge1xuICAgIGNvbnN0IHsgY29sdW1ucyB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCBoYXNTZWxlY3Rpb25Db2x1bW4gPSB0aGlzLmhhc1NlbGVjdGlvbkNvbHVtbigpO1xuICAgIGNvbnN0IHsgaGVhZGluZ1RhYmxlLCBjb250ZW50VGFibGUsIGdldElubmVyQ2VsbFdpZHRoIH0gPSB0aGlzO1xuICAgIGlmICghaGVhZGluZ1RhYmxlIHx8ICFjb250ZW50VGFibGUpIHJldHVybjtcbiAgICBjb25zdCBoZWFkaW5nQ2VsbHMgPSBBcnJheS5mcm9tKGhlYWRpbmdUYWJsZS5nZXRFbGVtZW50c0J5VGFnTmFtZSgndGgnKSk7XG4gICAgY29uc3QgY29udGVudENlbGxzID0gQXJyYXkuZnJvbShjb250ZW50VGFibGUuZ2V0RWxlbWVudHNCeVRhZ05hbWUoJ3RkJykpO1xuXG4gICAgaWYgKGhhc1NlbGVjdGlvbkNvbHVtbikge1xuICAgICAgaGVhZGluZ0NlbGxzLnNoaWZ0KCk7XG4gICAgICBjb250ZW50Q2VsbHMuc2hpZnQoKTtcbiAgICB9XG4gICAgY29uc3QgZHluYW1pY1dpZHRocyA9IGNvbHVtbnMubWFwKChjLCBpKSA9PiBnZXRJbm5lckNlbGxXaWR0aChjb250ZW50Q2VsbHNbaV0sIGhlYWRpbmdDZWxsc1tpXSwgYykgLSAoaGFzU2VsZWN0aW9uQ29sdW1uICYmICFpID8gMSA6IDApKTtcbiAgICB0aGlzLnNldFN0YXRlKHsgZHluYW1pY1dpZHRocyB9LCAoKSA9PiB7XG4gICAgICB3aW5kb3cuZGlzcGF0Y2hFdmVudChuZXcgRXZlbnQoJ01lc2FSZWZsb3cnKSk7XG4gICAgfSk7XG4gIH1cblxuICBnZXRJbm5lckNlbGxXaWR0aCAoY2VsbCwgaGVhZGluZ0NlbGwsIHsga2V5IH0pIHtcbiAgICBpZiAoa2V5ICYmIGtleSBpbiB0aGlzLndpZHRoQ2FjaGUpIHJldHVybiB0aGlzLndpZHRoQ2FjaGVba2V5XTtcblxuICAgIGNvbnN0IGNvbnRlbnRXaWR0aCA9IGNlbGwuY2xpZW50V2lkdGg7XG4gICAgY29uc3QgaGVhZGluZ1dpZHRoID0gaGVhZGluZ0NlbGwuY2xpZW50V2lkdGg7XG4gICAgY29uc3QgZ3JhYlN0eWxlID0gKHByb3ApID0+IHBhcnNlSW50KHdpbmRvdy5nZXRDb21wdXRlZFN0eWxlKGNlbGwsIG51bGwpLmdldFByb3BlcnR5VmFsdWUocHJvcCkpO1xuXG4gICAgY29uc3QgbGVmdFBhZGRpbmcgPSBncmFiU3R5bGUoJ3BhZGRpbmctbGVmdCcpO1xuICAgIGNvbnN0IHJpZ2h0UGFkZGluZyA9IGdyYWJTdHlsZSgncGFkZGluZy1yaWdodCcpO1xuICAgIGNvbnN0IGxlZnRCb3JkZXIgPSBncmFiU3R5bGUoJ2JvcmRlci1sZWZ0LXdpZHRoJyk7XG4gICAgY29uc3QgcmlnaHRCb3JkZXIgPSBncmFiU3R5bGUoJ2JvcmRlci1yaWdodC13aWR0aCcpO1xuICAgIGNvbnN0IHdpZHRoT2Zmc2V0ID0gbGVmdFBhZGRpbmcgKyByaWdodFBhZGRpbmcgKyBsZWZ0Qm9yZGVyICsgcmlnaHRCb3JkZXI7XG5cbiAgICBjb25zdCBoaWdoZXIgPSBNYXRoLm1heChjb250ZW50V2lkdGgsIGhlYWRpbmdXaWR0aCk7XG4gICAgcmV0dXJuIHRoaXMud2lkdGhDYWNoZVtrZXldID0gaGlnaGVyO1xuICB9XG5cbiAgaGFzU2VsZWN0aW9uQ29sdW1uICgpIHtcbiAgICBjb25zdCB7IG9wdGlvbnMsIGV2ZW50SGFuZGxlcnMgfSA9IHRoaXMucHJvcHM7XG4gICAgcmV0dXJuIHR5cGVvZiBvcHRpb25zLmlzUm93U2VsZWN0ZWQgPT09ICdmdW5jdGlvbidcbiAgICAgICYmIHR5cGVvZiBldmVudEhhbmRsZXJzLm9uUm93U2VsZWN0ID09PSAnZnVuY3Rpb24nXG4gICAgICAmJiB0eXBlb2YgZXZlbnRIYW5kbGVycy5vblJvd0Rlc2VsZWN0ID09PSAnZnVuY3Rpb24nO1xuICB9XG5cbiAgaGFuZGxlVGFibGVCb2R5U2Nyb2xsIChlKSB7XG4gICAgY29uc3Qgb2Zmc2V0ID0gdGhpcy5ib2R5Tm9kZS5zY3JvbGxMZWZ0O1xuICAgIHRoaXMuaGVhZGVyTm9kZS5zY3JvbGxMZWZ0ID0gb2Zmc2V0O1xuICAgIHdpbmRvdy5kaXNwYXRjaEV2ZW50KG5ldyBFdmVudCgnTWVzYVNjcm9sbCcpKTtcbiAgfVxuXG4gIC8vIC09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09LT0tPS09XG5cbiAgcmVuZGVyU3RpY2t5VGFibGUgKCkge1xuICAgIGNvbnN0IHsgb3B0aW9ucywgY29sdW1ucywgcm93cywgZmlsdGVyZWRSb3dzLCBhY3Rpb25zLCBldmVudEhhbmRsZXJzLCB1aVN0YXRlIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgZHluYW1pY1dpZHRocyB9ID0gdGhpcy5zdGF0ZTtcbiAgICBjb25zdCBuZXdDb2x1bW5zID0gY29sdW1ucy5ldmVyeSgoeyB3aWR0aCB9KSA9PiB3aWR0aCkgfHwgIWR5bmFtaWNXaWR0aHMgfHwgZHluYW1pY1dpZHRocy5sZW5ndGggPT0gMFxuICAgICAgPyBjb2x1bW5zXG4gICAgICA6IGNvbHVtbnMubWFwKChjb2x1bW4sIGluZGV4KSA9PiBPYmplY3QuYXNzaWduKHt9LCBjb2x1bW4sIHsgd2lkdGg6IGR5bmFtaWNXaWR0aHNbaW5kZXhdIH0pKTtcbiAgICBjb25zdCBtYXhIZWlnaHQgPSB7IG1heEhlaWdodDogb3B0aW9ucyA/IG9wdGlvbnMudGFibGVCb2R5TWF4SGVpZ2h0IDogbnVsbCB9O1xuICAgIGNvbnN0IG1heFdpZHRoID0geyBtaW5XaWR0aDogZHluYW1pY1dpZHRocyA/IGNvbWJpbmVXaWR0aHMoY29sdW1ucy5tYXAoKHsgd2lkdGggfSkgPT4gd2lkdGgpKSA6IG51bGwgfTtcbiAgICBjb25zdCB0YWJsZUxheW91dCA9IHsgdGFibGVMYXlvdXQ6IGR5bmFtaWNXaWR0aHMgPyAnZml4ZWQnIDogJ2F1dG8nIH07XG4gICAgY29uc3QgdGFibGVQcm9wcyA9IHsgb3B0aW9ucywgcm93cywgZmlsdGVyZWRSb3dzLCBhY3Rpb25zLCBldmVudEhhbmRsZXJzLCB1aVN0YXRlLCBjb2x1bW5zOiBuZXdDb2x1bW5zIH07XG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiTWVzYUNvbXBvbmVudFwiPlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT17ZGF0YVRhYmxlQ2xhc3MoKX0gc3R5bGU9e21heFdpZHRofT5cbiAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT17ZGF0YVRhYmxlQ2xhc3MoJ1N0aWNreScpfSBzdHlsZT17bWF4V2lkdGh9PlxuICAgICAgICAgICAgPGRpdlxuICAgICAgICAgICAgICByZWY9e25vZGUgPT4gdGhpcy5oZWFkZXJOb2RlID0gbm9kZX1cbiAgICAgICAgICAgICAgY2xhc3NOYW1lPXtkYXRhVGFibGVDbGFzcygnSGVhZGVyJyl9PlxuICAgICAgICAgICAgICA8dGFibGVcbiAgICAgICAgICAgICAgICBjZWxsU3BhY2luZz17MH1cbiAgICAgICAgICAgICAgICBjZWxsUGFkZGluZz17MH1cbiAgICAgICAgICAgICAgICBzdHlsZT17dGFibGVMYXlvdXR9XG4gICAgICAgICAgICAgICAgcmVmPXtub2RlID0+IHRoaXMuaGVhZGluZ1RhYmxlID0gbm9kZX0+XG4gICAgICAgICAgICAgICAgPEhlYWRpbmdSb3cgey4uLnRhYmxlUHJvcHN9IC8+XG4gICAgICAgICAgICAgIDwvdGFibGU+XG4gICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgIDxkaXZcbiAgICAgICAgICAgICAgc3R5bGU9e21heEhlaWdodH1cbiAgICAgICAgICAgICAgcmVmPXtub2RlID0+IHRoaXMuYm9keU5vZGUgPSBub2RlfVxuICAgICAgICAgICAgICBjbGFzc05hbWU9e2RhdGFUYWJsZUNsYXNzKCdCb2R5Jyl9XG4gICAgICAgICAgICAgIG9uU2Nyb2xsPXt0aGlzLmhhbmRsZVRhYmxlQm9keVNjcm9sbH0+XG4gICAgICAgICAgICAgIDx0YWJsZVxuICAgICAgICAgICAgICAgIGNlbGxTcGFjaW5nPXswfVxuICAgICAgICAgICAgICAgIGNlbGxQYWRkaW5nPXswfVxuICAgICAgICAgICAgICAgIHN0eWxlPXt0YWJsZUxheW91dH1cbiAgICAgICAgICAgICAgICByZWY9e25vZGUgPT4gdGhpcy5jb250ZW50VGFibGUgPSBub2RlfT5cbiAgICAgICAgICAgICAgICA8RGF0YVJvd0xpc3Qgey4uLnRhYmxlUHJvcHN9IC8+XG4gICAgICAgICAgICAgIDwvdGFibGU+XG4gICAgICAgICAgICA8L2Rpdj5cblxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cblxuICByZW5kZXJQbGFpblRhYmxlICgpIHtcbiAgICBjb25zdCB7IHByb3BzIH0gPSB0aGlzO1xuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cIk1lc2FDb21wb25lbnRcIj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9e2RhdGFUYWJsZUNsYXNzKCl9PlxuICAgICAgICAgIDx0YWJsZSBjZWxsU3BhY2luZz1cIjBcIiBjZWxsUGFkZGluZz1cIjBcIj5cbiAgICAgICAgICAgIDxIZWFkaW5nUm93IHsuLi5wcm9wc30gLz5cbiAgICAgICAgICAgIDxEYXRhUm93TGlzdCB7Li4ucHJvcHN9IC8+XG4gICAgICAgICAgPC90YWJsZT5cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG5cbiAgcmVuZGVyICgpIHtcbiAgICBjb25zdCB7IHNob3VsZFVzZVN0aWNreUhlYWRlciwgcmVuZGVyU3RpY2t5VGFibGUsIHJlbmRlclBsYWluVGFibGUgfSA9IHRoaXM7XG4gICAgcmV0dXJuIHNob3VsZFVzZVN0aWNreUhlYWRlcigpID8gcmVuZGVyU3RpY2t5VGFibGUoKSA6IHJlbmRlclBsYWluVGFibGUoKTtcbiAgfVxufTtcblxuRGF0YVRhYmxlLnByb3BUeXBlcyA9IHtcbiAgcm93czogUHJvcFR5cGVzLmFycmF5LFxuICBjb2x1bW5zOiBQcm9wVHlwZXMuYXJyYXksXG4gIG9wdGlvbnM6IFByb3BUeXBlcy5vYmplY3QsXG4gIGFjdGlvbnM6IFByb3BUeXBlcy5hcnJheU9mKFByb3BUeXBlcy5zaGFwZSh7XG4gICAgZWxlbWVudDogUHJvcFR5cGVzLm9uZU9mVHlwZShbIFByb3BUeXBlcy5mdW5jLCBQcm9wVHlwZXMubm9kZSwgUHJvcFR5cGVzLmVsZW1lbnQgXSksXG4gICAgaGFuZGxlcjogUHJvcFR5cGVzLmZ1bmMsXG4gICAgY2FsbGJhY2s6IFByb3BUeXBlcy5mdW5jXG4gIH0pKSxcbiAgdWlTdGF0ZTogUHJvcFR5cGVzLm9iamVjdCxcbiAgZXZlbnRIYW5kbGVyczogUHJvcFR5cGVzLm9iamVjdE9mKFByb3BUeXBlcy5mdW5jKVxufTtcblxuZXhwb3J0IGRlZmF1bHQgRGF0YVRhYmxlO1xuIl19