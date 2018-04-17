'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

var _RowsPerPageMenu = require('../Ui/RowsPerPageMenu');

var _RowsPerPageMenu2 = _interopRequireDefault(_RowsPerPageMenu);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var settings = {
  overflowPoint: 8,
  innerRadius: 2
};

var PaginationMenu = function (_React$PureComponent) {
  _inherits(PaginationMenu, _React$PureComponent);

  function PaginationMenu(props) {
    _classCallCheck(this, PaginationMenu);

    var _this = _possibleConstructorReturn(this, (PaginationMenu.__proto__ || Object.getPrototypeOf(PaginationMenu)).call(this, props));

    _this.renderPageLink = _this.renderPageLink.bind(_this);
    _this.renderEllipsis = _this.renderEllipsis.bind(_this);
    _this.renderPageList = _this.renderPageList.bind(_this);
    _this.renderDynamicPageLink = _this.renderDynamicPageLink.bind(_this);
    return _this;
  }

  _createClass(PaginationMenu, [{
    key: 'renderEllipsis',
    value: function renderEllipsis() {
      var key = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : '';

      return _react2.default.createElement(
        'a',
        { key: 'ellipsis-' + key, className: 'ellipsis' },
        '...'
      );
    }
  }, {
    key: 'renderPageLink',
    value: function renderPageLink(page, current) {
      var _this2 = this;

      var handler = function handler() {
        return _this2.goToPage(page);
      };
      return _react2.default.createElement(
        'a',
        { onClick: handler, key: page, className: current === page ? 'active' : 'inactive' },
        page
      );
    }
  }, {
    key: 'getRelativePageNumber',
    value: function getRelativePageNumber(relative) {
      var _props = this.props,
          currentPage = _props.currentPage,
          totalPages = _props.totalPages;

      switch (relative.toLowerCase()) {
        case 'first':
        case 'start':
          return 1;
        case 'last':
        case 'end':
          return totalPages;
        case 'next':
          return currentPage < totalPages ? currentPage + 1 : 1;
        case 'prev':
        case 'previous':
          return currentPage > 1 ? currentPage - 1 : totalPages;
        default:
          return null;
      }
    }
  }, {
    key: 'getRelativeIcon',
    value: function getRelativeIcon(relative) {
      switch (relative.toLowerCase()) {
        case 'first':
        case 'start':
          return 'angle-double-left';
        case 'last':
        case 'end':
          return 'angle-double-right';
        case 'next':
          return 'caret-right';
        case 'prev':
        case 'previous':
          return 'caret-left';
        default:
          return null;
      }
    }
  }, {
    key: 'goToPage',
    value: function goToPage(page) {
      var onPageChange = this.props.onPageChange;

      if (onPageChange) onPageChange(page);
    }
  }, {
    key: 'renderRelativeLink',
    value: function renderRelativeLink(_ref) {
      var _this3 = this;

      var relative = _ref.relative;

      var page = this.getRelativePageNumber(relative);
      var icon = this.getRelativeIcon(relative);

      return !page || !icon ? null : _react2.default.createElement(
        'span',
        { className: 'Pagination-Nav' },
        _react2.default.createElement(
          'a',
          { onClick: function onClick() {
              return _this3.goToPage(page);
            }, title: 'Jump to the ' + relative + ' page' },
          _react2.default.createElement(_Icon2.default, { fa: icon })
        )
      );
    }
  }, {
    key: 'renderDynamicPageLink',
    value: function renderDynamicPageLink(page, current, total) {
      var link = this.renderPageLink(page, current);
      var dots = this.renderEllipsis(page);
      var innerRadius = settings.innerRadius;


      if (page === 1 || page === total) return link;
      if (page >= current - innerRadius && page <= current + innerRadius) return link;
      if (page === current - innerRadius - 1) return dots;
      if (page === current + innerRadius + 1) return dots;
      return null;
    }
  }, {
    key: 'renderPageList',
    value: function renderPageList() {
      var overflowPoint = settings.overflowPoint;
      var _props2 = this.props,
          totalPages = _props2.totalPages,
          currentPage = _props2.currentPage;


      var pageList = new Array(totalPages).fill({}).map(function (empty, index) {
        return index + 1;
      });

      var renderer = totalPages > overflowPoint ? this.renderDynamicPageLink : this.renderPageLink;
      return _react2.default.createElement(
        'span',
        { className: 'Pagination-Nav' },
        pageList.map(function (page) {
          return renderer(page, currentPage, totalPages);
        }).filter(function (e) {
          return e;
        })
      );
    }
  }, {
    key: 'renderPerPageMenu',
    value: function renderPerPageMenu() {
      var _props3 = this.props,
          rowsPerPage = _props3.rowsPerPage,
          rowsPerPageOptions = _props3.rowsPerPageOptions,
          onRowsPerPageChange = _props3.onRowsPerPageChange;

      if (!onRowsPerPageChange) return null;
      return _react2.default.createElement(
        'span',
        { className: 'Pagination-Editor' },
        _react2.default.createElement(_RowsPerPageMenu2.default, {
          rowsPerPage: rowsPerPage,
          rowsPerPageOptions: rowsPerPageOptions,
          onRowsPerPageChange: onRowsPerPageChange
        })
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var _props4 = this.props,
          totalPages = _props4.totalPages,
          currentPage = _props4.currentPage;


      var PageList = this.renderPageList;
      var PerPageMenu = this.renderPerPageMenu;
      var RelativeLink = this.renderRelativeLink;

      return !totalPages || !currentPage ? null : _react2.default.createElement(
        'div',
        { className: 'PaginationMenu' },
        _react2.default.createElement('span', { className: 'Pagination-Spacer' }),
        _react2.default.createElement(RelativeLink, { relative: 'previous' }),
        _react2.default.createElement(PageList, null),
        _react2.default.createElement(RelativeLink, { relative: 'next' }),
        _react2.default.createElement(PerPageMenu, null)
      );
    }
  }]);

  return PaginationMenu;
}(_react2.default.PureComponent);

;

PaginationMenu.propTypes = {
  totalPages: _propTypes2.default.number,
  currentPage: _propTypes2.default.number,
  rowsPerPage: _propTypes2.default.number,
  onPageChange: _propTypes2.default.func,
  rowsPerPageOptions: _propTypes2.default.array,
  onRowsPerPageChange: _propTypes2.default.func
};

exports.default = PaginationMenu;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9QYWdpbmF0aW9uTWVudS5qc3giXSwibmFtZXMiOlsic2V0dGluZ3MiLCJvdmVyZmxvd1BvaW50IiwiaW5uZXJSYWRpdXMiLCJQYWdpbmF0aW9uTWVudSIsInByb3BzIiwicmVuZGVyUGFnZUxpbmsiLCJiaW5kIiwicmVuZGVyRWxsaXBzaXMiLCJyZW5kZXJQYWdlTGlzdCIsInJlbmRlckR5bmFtaWNQYWdlTGluayIsImtleSIsInBhZ2UiLCJjdXJyZW50IiwiaGFuZGxlciIsImdvVG9QYWdlIiwicmVsYXRpdmUiLCJjdXJyZW50UGFnZSIsInRvdGFsUGFnZXMiLCJ0b0xvd2VyQ2FzZSIsIm9uUGFnZUNoYW5nZSIsImdldFJlbGF0aXZlUGFnZU51bWJlciIsImljb24iLCJnZXRSZWxhdGl2ZUljb24iLCJ0b3RhbCIsImxpbmsiLCJkb3RzIiwicGFnZUxpc3QiLCJBcnJheSIsImZpbGwiLCJtYXAiLCJlbXB0eSIsImluZGV4IiwicmVuZGVyZXIiLCJmaWx0ZXIiLCJlIiwicm93c1BlclBhZ2UiLCJyb3dzUGVyUGFnZU9wdGlvbnMiLCJvblJvd3NQZXJQYWdlQ2hhbmdlIiwiUGFnZUxpc3QiLCJQZXJQYWdlTWVudSIsInJlbmRlclBlclBhZ2VNZW51IiwiUmVsYXRpdmVMaW5rIiwicmVuZGVyUmVsYXRpdmVMaW5rIiwiUHVyZUNvbXBvbmVudCIsInByb3BUeXBlcyIsIm51bWJlciIsImZ1bmMiLCJhcnJheSJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7O0FBRUE7Ozs7QUFDQTs7Ozs7Ozs7Ozs7O0FBRUEsSUFBTUEsV0FBVztBQUNmQyxpQkFBZSxDQURBO0FBRWZDLGVBQWE7QUFGRSxDQUFqQjs7SUFLTUMsYzs7O0FBQ0osMEJBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSxnSUFDWkEsS0FEWTs7QUFFbEIsVUFBS0MsY0FBTCxHQUFzQixNQUFLQSxjQUFMLENBQW9CQyxJQUFwQixPQUF0QjtBQUNBLFVBQUtD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QkFEdEIsV0FDc0JBLG1CQUR0Qjs7QUFFbkIsVUFBSSxDQUFDQSxtQkFBTCxFQUEwQixPQUFPLElBQVA7QUFDMUIsYUFDRTtBQUFBO0FBQUEsVUFBTSxXQUFVLG1CQUFoQjtBQUNFO0FBQ0UsdUJBQWFGLFdBRGY7QUFFRSw4QkFBb0JDLGtCQUZ0QjtBQUdFLCtCQUFxQkM7QUFIdkI7QUFERixPQURGO0FBU0Q7Ozs2QkFFUztBQUFBLG9CQUM0QixLQUFLakMsS0FEakM7QUFBQSxVQUNBYSxVQURBLFdBQ0FBLFVBREE7QUFBQSxVQUNZRCxXQURaLFdBQ1lBLFdBRFo7OztBQUdSLFVBQU1zQixXQUFXLEtBQUs5QixjQUF0QjtBQUNBLFVBQU0rQixjQUFjLEtBQUtDLGlCQUF6QjtBQUNBLFVBQU1DLGVBQWUsS0FBS0Msa0JBQTFCOztBQUVBLGFBQU8sQ0FBQ3pCLFVBQUQsSUFBZSxDQUFDRCxXQUFoQixHQUE4QixJQUE5QixHQUNMO0FBQUE7QUFBQSxVQUFLLFdBQVUsZ0JBQWY7QUFDRSxnREFBTSxXQUFVLG1CQUFoQixHQURGO0FBRUUsc0NBQUMsWUFBRCxJQUFjLFVBQVMsVUFBdkIsR0FGRjtBQUdFLHNDQUFDLFFBQUQsT0FIRjtBQUlFLHNDQUFDLFlBQUQsSUFBYyxVQUFTLE1BQXZCLEdBSkY7QUFLRSxzQ0FBQyxXQUFEO0FBTEYsT0FERjtBQVNEOzs7O0VBM0kwQixnQkFBTTJCLGE7O0FBNElsQzs7QUFFRHhDLGVBQWV5QyxTQUFmLEdBQTJCO0FBQ3pCM0IsY0FBWSxvQkFBVTRCLE1BREc7QUFFekI3QixlQUFhLG9CQUFVNkIsTUFGRTtBQUd6QlYsZUFBYSxvQkFBVVUsTUFIRTtBQUl6QjFCLGdCQUFjLG9CQUFVMkIsSUFKQztBQUt6QlYsc0JBQW9CLG9CQUFVVyxLQUxMO0FBTXpCVix1QkFBcUIsb0JBQVVTO0FBTk4sQ0FBM0I7O2tCQVNlM0MsYyIsImZpbGUiOiJQYWdpbmF0aW9uTWVudS5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5pbXBvcnQgUHJvcFR5cGVzIGZyb20gJ3Byb3AtdHlwZXMnO1xuXG5pbXBvcnQgSWNvbiBmcm9tICcuLi9Db21wb25lbnRzL0ljb24nO1xuaW1wb3J0IFJvd3NQZXJQYWdlTWVudSBmcm9tICcuLi9VaS9Sb3dzUGVyUGFnZU1lbnUnO1xuXG5jb25zdCBzZXR0aW5ncyA9IHtcbiAgb3ZlcmZsb3dQb2ludDogOCxcbiAgaW5uZXJSYWRpdXM6IDJcbn1cblxuY2xhc3MgUGFnaW5hdGlvbk1lbnUgZXh0ZW5kcyBSZWFjdC5QdXJlQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuICAgIHRoaXMucmVuZGVyUGFnZUxpbmsgPSB0aGlzLnJlbmRlclBhZ2VMaW5rLmJpbmQodGhpcyk7XG4gICAgdGhpcy5yZW5kZXJFbGxpcHNpcyA9IHRoaXMucmVuZGVyRWxsaXBzaXMuYmluZCh0aGlzKTtcbiAgICB0aGlzLnJlbmRlclBhZ2VMaXN0ID0gdGhpcy5yZW5kZXJQYWdlTGlzdC5iaW5kKHRoaXMpO1xuICAgIHRoaXMucmVuZGVyRHluYW1pY1BhZ2VMaW5rID0gdGhpcy5yZW5kZXJEeW5hbWljUGFnZUxpbmsuYmluZCh0aGlzKTtcbiAgfVxuXG4gIHJlbmRlckVsbGlwc2lzIChrZXkgPSAnJykge1xuICAgIHJldHVybiAoXG4gICAgICA8YSBrZXk9eydlbGxpcHNpcy0nICsga2V5fSBjbGFzc05hbWU9XCJlbGxpcHNpc1wiPlxuICAgICAgICAuLi5cbiAgICAgIDwvYT5cbiAgICApO1xuICB9XG5cbiAgcmVuZGVyUGFnZUxpbmsgKHBhZ2UsIGN1cnJlbnQpIHtcbiAgICBsZXQgaGFuZGxlciA9ICgpID0+IHRoaXMuZ29Ub1BhZ2UocGFnZSk7XG4gICAgcmV0dXJuIChcbiAgICAgIDxhIG9uQ2xpY2s9e2hhbmRsZXJ9IGtleT17cGFnZX0gY2xhc3NOYW1lPXtjdXJyZW50ID09PSBwYWdlID8gJ2FjdGl2ZScgOiAnaW5hY3RpdmUnfT5cbiAgICAgICAge3BhZ2V9XG4gICAgICA8L2E+XG4gICAgKTtcbiAgfVxuXG4gIGdldFJlbGF0aXZlUGFnZU51bWJlciAocmVsYXRpdmUpIHtcbiAgICBjb25zdCB7IGN1cnJlbnRQYWdlLCB0b3RhbFBhZ2VzIH0gPSB0aGlzLnByb3BzO1xuICAgIHN3aXRjaCAocmVsYXRpdmUudG9Mb3dlckNhc2UoKSkge1xuICAgICAgY2FzZSAnZmlyc3QnOlxuICAgICAgY2FzZSAnc3RhcnQnOlxuICAgICAgICByZXR1cm4gMTtcbiAgICAgIGNhc2UgJ2xhc3QnOlxuICAgICAgY2FzZSAnZW5kJzpcbiAgICAgICAgcmV0dXJuIHRvdGFsUGFnZXM7XG4gICAgICBjYXNlICduZXh0JzpcbiAgICAgICAgcmV0dXJuIGN1cnJlbnRQYWdlIDwgdG90YWxQYWdlcyA/IGN1cnJlbnRQYWdlICsgMSA6IDE7XG4gICAgICBjYXNlICdwcmV2JzpcbiAgICAgIGNhc2UgJ3ByZXZpb3VzJzpcbiAgICAgICAgcmV0dXJuIGN1cnJlbnRQYWdlID4gMSA/IGN1cnJlbnRQYWdlIC0gMSA6IHRvdGFsUGFnZXM7XG4gICAgICBkZWZhdWx0OlxuICAgICAgICByZXR1cm4gbnVsbDtcbiAgICB9XG4gIH1cblxuICBnZXRSZWxhdGl2ZUljb24gKHJlbGF0aXZlKSB7XG4gICAgc3dpdGNoIChyZWxhdGl2ZS50b0xvd2VyQ2FzZSgpKSB7XG4gICAgICBjYXNlICdmaXJzdCc6XG4gICAgICBjYXNlICdzdGFydCc6XG4gICAgICAgIHJldHVybiAnYW5nbGUtZG91YmxlLWxlZnQnO1xuICAgICAgY2FzZSAnbGFzdCc6XG4gICAgICBjYXNlICdlbmQnOlxuICAgICAgICByZXR1cm4gJ2FuZ2xlLWRvdWJsZS1yaWdodCc7XG4gICAgICBjYXNlICduZXh0JzpcbiAgICAgICAgcmV0dXJuICdjYXJldC1yaWdodCc7XG4gICAgICBjYXNlICdwcmV2JzpcbiAgICAgIGNhc2UgJ3ByZXZpb3VzJzpcbiAgICAgICAgcmV0dXJuICdjYXJldC1sZWZ0JztcbiAgICAgIGRlZmF1bHQ6XG4gICAgICAgIHJldHVybiBudWxsO1xuICAgIH1cbiAgfVxuXG4gIGdvVG9QYWdlIChwYWdlKSB7XG4gICAgbGV0IHsgb25QYWdlQ2hhbmdlIH0gPSB0aGlzLnByb3BzO1xuICAgIGlmIChvblBhZ2VDaGFuZ2UpIG9uUGFnZUNoYW5nZShwYWdlKTtcbiAgfVxuXG4gIHJlbmRlclJlbGF0aXZlTGluayAoeyByZWxhdGl2ZSB9KSB7XG4gICAgY29uc3QgcGFnZSA9IHRoaXMuZ2V0UmVsYXRpdmVQYWdlTnVtYmVyKHJlbGF0aXZlKTtcbiAgICBjb25zdCBpY29uID0gdGhpcy5nZXRSZWxhdGl2ZUljb24ocmVsYXRpdmUpO1xuXG4gICAgcmV0dXJuICghcGFnZSB8fCAhaWNvbikgPyBudWxsIDogKFxuICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiUGFnaW5hdGlvbi1OYXZcIj5cbiAgICAgICAgPGEgb25DbGljaz17KCkgPT4gdGhpcy5nb1RvUGFnZShwYWdlKX0gdGl0bGU9eydKdW1wIHRvIHRoZSAnICsgcmVsYXRpdmUgKyAnIHBhZ2UnfT5cbiAgICAgICAgICA8SWNvbiBmYT17aWNvbn0gLz5cbiAgICAgICAgPC9hPlxuICAgICAgPC9zcGFuPlxuICAgIClcbiAgfVxuXG4gIHJlbmRlckR5bmFtaWNQYWdlTGluayAocGFnZSwgY3VycmVudCwgdG90YWwpIHtcbiAgICBjb25zdCBsaW5rID0gdGhpcy5yZW5kZXJQYWdlTGluayhwYWdlLCBjdXJyZW50KTtcbiAgICBjb25zdCBkb3RzID0gdGhpcy5yZW5kZXJFbGxpcHNpcyhwYWdlKTtcbiAgICBjb25zdCB7IGlubmVyUmFkaXVzIH0gPSBzZXR0aW5ncztcblxuICAgIGlmIChwYWdlID09PSAxIHx8IHBhZ2UgPT09IHRvdGFsKSByZXR1cm4gbGluaztcbiAgICBpZiAocGFnZSA+PSBjdXJyZW50IC0gaW5uZXJSYWRpdXMgJiYgcGFnZSA8PSBjdXJyZW50ICsgaW5uZXJSYWRpdXMpIHJldHVybiBsaW5rO1xuICAgIGlmIChwYWdlID09PSBjdXJyZW50IC0gaW5uZXJSYWRpdXMgLSAxKSByZXR1cm4gZG90cztcbiAgICBpZiAocGFnZSA9PT0gY3VycmVudCArIGlubmVyUmFkaXVzICsgMSkgcmV0dXJuIGRvdHM7XG4gICAgcmV0dXJuIG51bGw7XG4gIH1cblxuICByZW5kZXJQYWdlTGlzdCAoKSB7XG4gICAgY29uc3QgeyBvdmVyZmxvd1BvaW50IH0gPSBzZXR0aW5ncztcbiAgICBjb25zdCB7IHRvdGFsUGFnZXMsIGN1cnJlbnRQYWdlIH0gPSB0aGlzLnByb3BzO1xuXG4gICAgY29uc3QgcGFnZUxpc3QgPSBuZXcgQXJyYXkodG90YWxQYWdlcylcbiAgICAgIC5maWxsKHt9KVxuICAgICAgLm1hcCgoZW1wdHksIGluZGV4KSA9PiBpbmRleCArIDEpO1xuXG4gICAgY29uc3QgcmVuZGVyZXIgPSB0b3RhbFBhZ2VzID4gb3ZlcmZsb3dQb2ludCA/IHRoaXMucmVuZGVyRHluYW1pY1BhZ2VMaW5rIDogdGhpcy5yZW5kZXJQYWdlTGluaztcbiAgICByZXR1cm4gKFxuICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiUGFnaW5hdGlvbi1OYXZcIj5cbiAgICAgICAge3BhZ2VMaXN0Lm1hcChwYWdlID0+IHJlbmRlcmVyKHBhZ2UsIGN1cnJlbnRQYWdlLCB0b3RhbFBhZ2VzKSkuZmlsdGVyKGUgPT4gZSl9XG4gICAgICA8L3NwYW4+XG4gICAgKTtcbiAgfVxuXG4gIHJlbmRlclBlclBhZ2VNZW51ICgpIHtcbiAgICBjb25zdCB7IHJvd3NQZXJQYWdlLCByb3dzUGVyUGFnZU9wdGlvbnMsIG9uUm93c1BlclBhZ2VDaGFuZ2UgfSA9IHRoaXMucHJvcHM7XG4gICAgaWYgKCFvblJvd3NQZXJQYWdlQ2hhbmdlKSByZXR1cm4gbnVsbDtcbiAgICByZXR1cm4gKFxuICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiUGFnaW5hdGlvbi1FZGl0b3JcIj5cbiAgICAgICAgPFJvd3NQZXJQYWdlTWVudVxuICAgICAgICAgIHJvd3NQZXJQYWdlPXtyb3dzUGVyUGFnZX1cbiAgICAgICAgICByb3dzUGVyUGFnZU9wdGlvbnM9e3Jvd3NQZXJQYWdlT3B0aW9uc31cbiAgICAgICAgICBvblJvd3NQZXJQYWdlQ2hhbmdlPXtvblJvd3NQZXJQYWdlQ2hhbmdlfVxuICAgICAgICAvPlxuICAgICAgPC9zcGFuPlxuICAgIClcbiAgfVxuXG4gIHJlbmRlciAoKSB7XG4gICAgY29uc3QgeyB0b3RhbFBhZ2VzLCBjdXJyZW50UGFnZSB9ID0gdGhpcy5wcm9wcztcblxuICAgIGNvbnN0IFBhZ2VMaXN0ID0gdGhpcy5yZW5kZXJQYWdlTGlzdDtcbiAgICBjb25zdCBQZXJQYWdlTWVudSA9IHRoaXMucmVuZGVyUGVyUGFnZU1lbnU7XG4gICAgY29uc3QgUmVsYXRpdmVMaW5rID0gdGhpcy5yZW5kZXJSZWxhdGl2ZUxpbms7XG5cbiAgICByZXR1cm4gIXRvdGFsUGFnZXMgfHwgIWN1cnJlbnRQYWdlID8gbnVsbCA6IChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPVwiUGFnaW5hdGlvbk1lbnVcIj5cbiAgICAgICAgPHNwYW4gY2xhc3NOYW1lPVwiUGFnaW5hdGlvbi1TcGFjZXJcIiAvPlxuICAgICAgICA8UmVsYXRpdmVMaW5rIHJlbGF0aXZlPVwicHJldmlvdXNcIiAvPlxuICAgICAgICA8UGFnZUxpc3QgLz5cbiAgICAgICAgPFJlbGF0aXZlTGluayByZWxhdGl2ZT1cIm5leHRcIiAvPlxuICAgICAgICA8UGVyUGFnZU1lbnUgLz5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn07XG5cblBhZ2luYXRpb25NZW51LnByb3BUeXBlcyA9IHtcbiAgdG90YWxQYWdlczogUHJvcFR5cGVzLm51bWJlcixcbiAgY3VycmVudFBhZ2U6IFByb3BUeXBlcy5udW1iZXIsXG4gIHJvd3NQZXJQYWdlOiBQcm9wVHlwZXMubnVtYmVyLFxuICBvblBhZ2VDaGFuZ2U6IFByb3BUeXBlcy5mdW5jLFxuICByb3dzUGVyUGFnZU9wdGlvbnM6IFByb3BUeXBlcy5hcnJheSxcbiAgb25Sb3dzUGVyUGFnZUNoYW5nZTogUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IFBhZ2luYXRpb25NZW51O1xuIl19