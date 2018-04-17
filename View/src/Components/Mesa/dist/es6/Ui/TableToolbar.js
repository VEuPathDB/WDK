'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

var _TableSearch = require('../Ui/TableSearch');

var _TableSearch2 = _interopRequireDefault(_TableSearch);

var _ColumnEditor = require('../Ui/ColumnEditor');

var _ColumnEditor2 = _interopRequireDefault(_ColumnEditor);

var _RowCounter = require('../Ui/RowCounter');

var _RowCounter2 = _interopRequireDefault(_RowCounter);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var TableToolbar = function (_React$PureComponent) {
  _inherits(TableToolbar, _React$PureComponent);

  function TableToolbar(props) {
    _classCallCheck(this, TableToolbar);

    var _this = _possibleConstructorReturn(this, (TableToolbar.__proto__ || Object.getPrototypeOf(TableToolbar)).call(this, props));

    _this.renderTitle = _this.renderTitle.bind(_this);
    _this.renderSearch = _this.renderSearch.bind(_this);
    _this.renderCounter = _this.renderCounter.bind(_this);
    _this.renderChildren = _this.renderChildren.bind(_this);
    _this.renderAddRemoveColumns = _this.renderAddRemoveColumns.bind(_this);
    return _this;
  }

  _createClass(TableToolbar, [{
    key: 'renderTitle',
    value: function renderTitle() {
      var options = this.props.options;
      var title = options.title;


      if (!title) return null;
      return _react2.default.createElement(
        'h1',
        { className: 'TableToolbar-Title' },
        title
      );
    }
  }, {
    key: 'renderSearch',
    value: function renderSearch() {
      var _props = this.props,
          options = _props.options,
          uiState = _props.uiState,
          eventHandlers = _props.eventHandlers;
      var onSearch = eventHandlers.onSearch;
      var searchQuery = uiState.searchQuery;


      if (!onSearch) return;
      return _react2.default.createElement(_TableSearch2.default, {
        query: searchQuery,
        onSearch: onSearch
      });
    }
  }, {
    key: 'renderCounter',
    value: function renderCounter() {
      var _props2 = this.props,
          rows = _props2.rows,
          options = _props2.options,
          uiState = _props2.uiState,
          eventHandlers = _props2.eventHandlers;
      var pagination = uiState.pagination,
          filteredRowCount = uiState.filteredRowCount;
      var totalRows = pagination.totalRows,
          rowsPerPage = pagination.rowsPerPage;
      var showCount = options.showCount;

      if (!showCount) return null;

      var isPaginated = 'onPageChange' in eventHandlers;
      var isSearching = uiState.searchQuery && uiState.searchQuery.length;

      var count = totalRows ? totalRows : rows.length;
      var noun = (isSearching ? 'result' : 'row') + (count % rowsPerPage === 1 ? '' : 's');
      var start = !isPaginated ? null : (pagination.currentPage - 1) * rowsPerPage + 1;
      var end = !isPaginated ? null : start + rowsPerPage > count ? count : start - 1 + rowsPerPage;

      var props = { count: count, noun: noun, start: start, end: end, filteredRowCount: filteredRowCount };

      return _react2.default.createElement(
        'div',
        { className: 'TableToolbar-Info' },
        _react2.default.createElement(_RowCounter2.default, props)
      );
    }
  }, {
    key: 'renderChildren',
    value: function renderChildren() {
      var children = this.props.children;

      if (!children) return null;

      return _react2.default.createElement(
        'div',
        { className: 'TableToolbar-Children' },
        children
      );
    }
  }, {
    key: 'renderAddRemoveColumns',
    value: function renderAddRemoveColumns() {
      var _props3 = this.props,
          options = _props3.options,
          columns = _props3.columns,
          eventHandlers = _props3.eventHandlers;
      var editableColumns = options.editableColumns;

      var columnsAreHideable = columns.some(function (column) {
        return column.hideable;
      });
      if (!editableColumns || !columnsAreHideable) return null;

      var onShowColumn = eventHandlers.onShowColumn,
          onHideColumn = eventHandlers.onHideColumn;

      return _react2.default.createElement(
        _ColumnEditor2.default,
        {
          columns: columns,
          onShowColumn: onShowColumn,
          onHideColumn: onHideColumn
        },
        _react2.default.createElement(
          'button',
          null,
          _react2.default.createElement(_Icon2.default, { fa: 'columns' }),
          _react2.default.createElement(
            'span',
            null,
            'Add/Remove Columns'
          )
        )
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var Title = this.renderTitle;
      var Search = this.renderSearch;
      var Counter = this.renderCounter;
      var Children = this.renderChildren;
      var AddRemove = this.renderAddRemoveColumns;

      return _react2.default.createElement(
        'div',
        { className: 'Toolbar TableToolbar' },
        _react2.default.createElement(Title, null),
        _react2.default.createElement(Search, null),
        _react2.default.createElement(Counter, null),
        _react2.default.createElement(Children, null),
        _react2.default.createElement(AddRemove, null)
      );
    }
  }]);

  return TableToolbar;
}(_react2.default.PureComponent);

;

exports.default = TableToolbar;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9UYWJsZVRvb2xiYXIuanN4Il0sIm5hbWVzIjpbIlRhYmxlVG9vbGJhciIsInByb3BzIiwicmVuZGVyVGl0bGUiLCJiaW5kIiwicmVuZGVyU2VhcmNoIiwicmVuZGVyQ291bnRlciIsInJlbmRlckNoaWxkcmVuIiwicmVuZGVyQWRkUmVtb3ZlQ29sdW1ucyIsIm9wdGlvbnMiLCJ0aXRsZSIsInVpU3RhdGUiLCJldmVudEhhbmRsZXJzIiwib25TZWFyY2giLCJzZWFyY2hRdWVyeSIsInJvd3MiLCJwYWdpbmF0aW9uIiwiZmlsdGVyZWRSb3dDb3VudCIsInRvdGFsUm93cyIsInJvd3NQZXJQYWdlIiwic2hvd0NvdW50IiwiaXNQYWdpbmF0ZWQiLCJpc1NlYXJjaGluZyIsImxlbmd0aCIsImNvdW50Iiwibm91biIsInN0YXJ0IiwiY3VycmVudFBhZ2UiLCJlbmQiLCJjaGlsZHJlbiIsImNvbHVtbnMiLCJlZGl0YWJsZUNvbHVtbnMiLCJjb2x1bW5zQXJlSGlkZWFibGUiLCJzb21lIiwiY29sdW1uIiwiaGlkZWFibGUiLCJvblNob3dDb2x1bW4iLCJvbkhpZGVDb2x1bW4iLCJUaXRsZSIsIlNlYXJjaCIsIkNvdW50ZXIiLCJDaGlsZHJlbiIsIkFkZFJlbW92ZSIsIlB1cmVDb21wb25lbnQiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7O0FBQUE7Ozs7QUFFQTs7OztBQUNBOzs7O0FBQ0E7Ozs7QUFDQTs7Ozs7Ozs7Ozs7O0lBRU1BLFk7OztBQUNKLHdCQUFhQyxLQUFiLEVBQW9CO0FBQUE7O0FBQUEsNEhBQ1pBLEtBRFk7O0FBR2xCLFVBQUtDLFdBQUwsR0FBbUIsTUFBS0EsV0FBTCxDQUFpQkMsSUFBakIsT0FBbkI7QUFDQSxVQUFLQyxZQUFMLEdBQW9CLE1BQUtBLFlBQUwsQ0FBa0JELElBQWxCLE9BQXBCO0FBQ0EsVUFBS0UsYUFBTCxHQUFxQixNQUFLQSxhQUFMLENBQW1CRixJQUFuQixPQUFyQjtBQUNBLFVBQUtHLGNBQUwsR0FBc0Is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LFdBQ0VBLGFBREY7QUFBQSxVQUVoQm1CLGVBRmdCLEdBRUl0QixPQUZKLENBRWhCc0IsZUFGZ0I7O0FBR3hCLFVBQU1DLHFCQUFxQkYsUUFBUUcsSUFBUixDQUFhO0FBQUEsZUFBVUMsT0FBT0MsUUFBakI7QUFBQSxPQUFiLENBQTNCO0FBQ0EsVUFBSSxDQUFDSixlQUFELElBQXFCLENBQUNDLGtCQUExQixFQUE4QyxPQUFPLElBQVA7O0FBSnRCLFVBTWhCSSxZQU5nQixHQU1leEIsYUFOZixDQU1oQndCLFlBTmdCO0FBQUEsVUFNRkMsWUFORSxHQU1lekIsYUFOZixDQU1GeUIsWUFORTs7QUFPeEIsYUFDRTtBQUFBO0FBQUE7QUFDRSxtQkFBU1AsT0FEWDtBQUVFLHdCQUFjTSxZQUZoQjtBQUdFLHdCQUFjQztBQUhoQjtBQUtFO0FBQUE7QUFBQTtBQUNFLDBEQUFNLElBQUksU0FBVixHQURGO0FBRUU7QUFBQTtBQUFBO0FBQUE7QUFBQTtBQUZGO0FBTEYsT0FERjtBQVlEOzs7NkJBRVM7QUFDUixVQUFNQyxRQUFRLEtBQUtuQyxXQUFuQjtBQUNBLFVBQU1vQyxTQUFTLEtBQUtsQyxZQUFwQjtBQUNBLFVBQU1tQyxVQUFVLEtBQUtsQyxhQUFyQjtBQUNBLFVBQU1tQyxXQUFXLEtBQUtsQyxjQUF0QjtBQUNBLFVBQU1tQyxZQUFZLEtBQUtsQyxzQkFBdkI7O0FBRUEsYUFDRTtBQUFBO0FBQUEsVUFBSyxXQUFVLHNCQUFmO0FBQ0Usc0NBQUMsS0FBRCxPQURGO0FBRUUsc0NBQUMsTUFBRCxPQUZGO0FBR0Usc0NBQUMsT0FBRCxPQUhGO0FBSUUsc0NBQUMsUUFBRCxPQUpGO0FBS0Usc0NBQUMsU0FBRDtBQUxGLE9BREY7QUFTRDs7OztFQTNHd0IsZ0JBQU1tQyxhOztBQTRHaEM7O2tCQUVjMUMsWSIsImZpbGUiOiJUYWJsZVRvb2xiYXIuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuXG5pbXBvcnQgSWNvbiBmcm9tICcuLi9Db21wb25lbnRzL0ljb24nO1xuaW1wb3J0IFRhYmxlU2VhcmNoIGZyb20gJy4uL1VpL1RhYmxlU2VhcmNoJztcbmltcG9ydCBDb2x1bW5FZGl0b3IgZnJvbSAnLi4vVWkvQ29sdW1uRWRpdG9yJztcbmltcG9ydCBSb3dDb3VudGVyIGZyb20gJy4uL1VpL1Jvd0NvdW50ZXInO1xuXG5jbGFzcyBUYWJsZVRvb2xiYXIgZXh0ZW5kcyBSZWFjdC5QdXJlQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuXG4gICAgdGhpcy5yZW5kZXJUaXRsZSA9IHRoaXMucmVuZGVyVGl0bGUuYmluZCh0aGlzKTtcbiAgICB0aGlzLnJlbmRlclNlYXJjaCA9IHRoaXMucmVuZGVyU2VhcmNoLmJpbmQodGhpcyk7XG4gICAgdGhpcy5yZW5kZXJDb3VudGVyID0gdGhpcy5yZW5kZXJDb3VudGVyLmJpbmQodGhpcyk7XG4gICAgdGhpcy5yZW5kZXJDaGlsZHJlbiA9IHRoaXMucmVuZGVyQ2hpbGRyZW4uYmluZCh0aGlzKTtcbiAgICB0aGlzLnJlbmRlckFkZFJlbW92ZUNvbHVtbnMgPSB0aGlzLnJlbmRlckFkZFJlbW92ZUNvbHVtbnMuYmluZCh0aGlzKTtcbiAgfVxuXG4gIHJlbmRlclRpdGxlICgpIHtcbiAgICBjb25zdCB7IG9wdGlvbnMgfSA9IHRoaXMucHJvcHM7XG4gICAgY29uc3QgeyB0aXRsZSB9ID0gb3B0aW9ucztcblxuICAgIGlmICghdGl0bGUpIHJldHVybiBudWxsO1xuICAgIHJldHVybiAoXG4gICAgICA8aDEgY2xhc3NOYW1lPVwiVGFibGVUb29sYmFyLVRpdGxlXCI+e3RpdGxlfTwvaDE+XG4gICAgKTtcbiAgfVxuXG4gIHJlbmRlclNlYXJjaCAoKSB7XG4gICAgY29uc3QgeyBvcHRpb25zLCB1aVN0YXRlLCBldmVudEhhbmRsZXJzIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgb25TZWFyY2ggfSA9IGV2ZW50SGFuZGxlcnM7XG4gICAgY29uc3QgeyBzZWFyY2hRdWVyeSB9ID0gdWlTdGF0ZTtcblxuICAgIGlmICghb25TZWFyY2gpIHJldHVybjtcbiAgICByZXR1cm4gKFxuICAgICAgPFRhYmxlU2VhcmNoXG4gICAgICAgIHF1ZXJ5PXtzZWFyY2hRdWVyeX1cbiAgICAgICAgb25TZWFyY2g9e29uU2VhcmNofVxuICAgICAgLz5cbiAgICApO1xuICB9XG5cbiAgcmVuZGVyQ291bnRlciAoKSB7XG4gICAgY29uc3QgeyByb3dzLCBvcHRpb25zLCB1aVN0YXRlLCBldmVudEhhbmRsZXJzIH0gPSB0aGlzLnByb3BzO1xuICAgIGNvbnN0IHsgcGFnaW5hdGlvbiwgZmlsdGVyZWRSb3dDb3VudCB9ID0gdWlTdGF0ZTtcbiAgICBjb25zdCB7IHRvdGFsUm93cywgcm93c1BlclBhZ2UgfSA9IHBhZ2luYXRpb247XG4gICAgY29uc3QgeyBzaG93Q291bnQgfSA9IG9wdGlvbnM7XG4gICAgaWYgKCFzaG93Q291bnQpIHJldHVybiBudWxsO1xuXG4gICAgY29uc3QgaXNQYWdpbmF0ZWQgPSAoJ29uUGFnZUNoYW5nZScgaW4gZXZlbnRIYW5kbGVycyk7XG4gICAgY29uc3QgaXNTZWFyY2hpbmcgPSB1aVN0YXRlLnNlYXJjaFF1ZXJ5ICYmIHVpU3RhdGUuc2VhcmNoUXVlcnkubGVuZ3RoO1xuXG4gICAgY29uc3QgY291bnQgPSB0b3RhbFJvd3MgPyB0b3RhbFJvd3MgOiByb3dzLmxlbmd0aDtcbiAgICBjb25zdCBub3VuID0gKGlzU2VhcmNoaW5nID8gJ3Jlc3VsdCcgOiAncm93JykgKyAoY291bnQgJSByb3dzUGVyUGFnZSA9PT0gMSA/ICcnIDogJ3MnKTtcbiAgICBjb25zdCBzdGFydCA9ICFpc1BhZ2luYXRlZCA/IG51bGwgOiAoKHBhZ2luYXRpb24uY3VycmVudFBhZ2UgLSAxKSAqIHJvd3NQZXJQYWdlKSArIDE7XG4gICAgY29uc3QgZW5kID0gIWlzUGFnaW5hdGVkID8gbnVsbCA6IChzdGFydCArIHJvd3NQZXJQYWdlID4gY291bnQgPyBjb3VudCA6IChzdGFydCAtIDEpICsgcm93c1BlclBhZ2UpO1xuXG4gICAgY29uc3QgcHJvcHMgPSB7IGNvdW50LCBub3VuLCBzdGFydCwgZW5kLCBmaWx0ZXJlZFJvd0NvdW50IH07XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJUYWJsZVRvb2xiYXItSW5mb1wiPlxuICAgICAgICA8Um93Q291bnRlciB7Li4ucHJvcHN9IC8+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG5cbiAgcmVuZGVyQ2hpbGRyZW4gKCkge1xuICAgIGNvbnN0IHsgY2hpbGRyZW4gfSA9IHRoaXMucHJvcHM7XG4gICAgaWYgKCFjaGlsZHJlbikgcmV0dXJuIG51bGw7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJUYWJsZVRvb2xiYXItQ2hpbGRyZW5cIj5cbiAgICAgICAge2NoaWxkcmVufVxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxuXG4gIHJlbmRlckFkZFJlbW92ZUNvbHVtbnMgKCkge1xuICAgIGNvbnN0IHsgb3B0aW9ucywgY29sdW1ucywgZXZlbnRIYW5kbGVycyB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IGVkaXRhYmxlQ29sdW1ucyB9ID0gb3B0aW9ucztcbiAgICBjb25zdCBjb2x1bW5zQXJlSGlkZWFibGUgPSBjb2x1bW5zLnNvbWUoY29sdW1uID0+IGNvbHVtbi5oaWRlYWJsZSk7XG4gICAgaWYgKCFlZGl0YWJsZUNvbHVtbnMgIHx8ICFjb2x1bW5zQXJlSGlkZWFibGUpIHJldHVybiBudWxsO1xuXG4gICAgY29uc3QgeyBvblNob3dDb2x1bW4sIG9uSGlkZUNvbHVtbiB9ID0gZXZlbnRIYW5kbGVycztcbiAgICByZXR1cm4gKFxuICAgICAgPENvbHVtbkVkaXRvclxuICAgICAgICBjb2x1bW5zPXtjb2x1bW5zfVxuICAgICAgICBvblNob3dDb2x1bW49e29uU2hvd0NvbHVtbn1cbiAgICAgICAgb25IaWRlQ29sdW1uPXtvbkhpZGVDb2x1bW59XG4gICAgICA+XG4gICAgICAgIDxidXR0b24+XG4gICAgICAgICAgPEljb24gZmE9eydjb2x1bW5zJ30gLz5cbiAgICAgICAgICA8c3Bhbj5BZGQvUmVtb3ZlIENvbHVtbnM8L3NwYW4+XG4gICAgICAgIDwvYnV0dG9uPlxuICAgICAgPC9Db2x1bW5FZGl0b3I+XG4gICAgKTtcbiAgfVxuXG4gIHJlbmRlciAoKSB7XG4gICAgY29uc3QgVGl0bGUgPSB0aGlzLnJlbmRlclRpdGxlO1xuICAgIGNvbnN0IFNlYXJjaCA9IHRoaXMucmVuZGVyU2VhcmNoO1xuICAgIGNvbnN0IENvdW50ZXIgPSB0aGlzLnJlbmRlckNvdW50ZXI7XG4gICAgY29uc3QgQ2hpbGRyZW4gPSB0aGlzLnJlbmRlckNoaWxkcmVuO1xuICAgIGNvbnN0IEFkZFJlbW92ZSA9IHRoaXMucmVuZGVyQWRkUmVtb3ZlQ29sdW1ucztcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cIlRvb2xiYXIgVGFibGVUb29sYmFyXCI+XG4gICAgICAgIDxUaXRsZSAvPlxuICAgICAgICA8U2VhcmNoIC8+XG4gICAgICAgIDxDb3VudGVyIC8+XG4gICAgICAgIDxDaGlsZHJlbiAvPlxuICAgICAgICA8QWRkUmVtb3ZlIC8+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59O1xuXG5leHBvcnQgZGVmYXVsdCBUYWJsZVRvb2xiYXI7XG4iXX0=