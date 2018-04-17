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

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var TableSearch = function (_React$PureComponent) {
  _inherits(TableSearch, _React$PureComponent);

  function TableSearch(props) {
    _classCallCheck(this, TableSearch);

    var _this = _possibleConstructorReturn(this, (TableSearch.__proto__ || Object.getPrototypeOf(TableSearch)).call(this, props));

    _this.handleQueryChange = _this.handleQueryChange.bind(_this);
    _this.clearSearchQuery = _this.clearSearchQuery.bind(_this);
    return _this;
  }

  _createClass(TableSearch, [{
    key: 'handleQueryChange',
    value: function handleQueryChange(e) {
      var query = e.target.value;
      var onSearch = this.props.onSearch;

      if (onSearch) onSearch(query);
    }
  }, {
    key: 'clearSearchQuery',
    value: function clearSearchQuery() {
      var query = null;
      var onSearch = this.props.onSearch;

      if (onSearch) onSearch(query);
    }
  }, {
    key: 'render',
    value: function render() {
      var _props = this.props,
          options = _props.options,
          query = _props.query;
      var searchPlaceholder = options.searchPlaceholder;
      var handleQueryChange = this.handleQueryChange,
          clearSearchQuery = this.clearSearchQuery;


      return _react2.default.createElement(
        'div',
        { className: 'TableSearch' },
        _react2.default.createElement(_Icon2.default, { fa: 'search' }),
        _react2.default.createElement('input', {
          type: 'text',
          name: 'Search',
          value: searchQuery || '',
          onChange: handleQueryChange,
          placeholder: searchPlaceholder
        }),
        searchQuery && _react2.default.createElement(
          'button',
          { onClick: clearSearchQuery },
          _react2.default.createElement(_Icon2.default, { fa: 'times-circle' }),
          'Clear Search'
        )
      );
    }
  }]);

  return TableSearch;
}(_react2.default.PureComponent);

;

TableSearch.propTypes = {
  query: _propTypes2.default.string,
  options: _propTypes2.default.object,
  onSearch: _propTypes2.default.func
};

exports.default = TableSearch;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9UYWJsZVNlYXJjaC5qc3giXSwibmFtZXMiOlsiVGFibGVTZWFyY2giLCJwcm9wcyIsImhhbmRsZVF1ZXJ5Q2hhbmdlIiwiYmluZCIsImNsZWFyU2VhcmNoUXVlcnkiLCJlIiwicXVlcnkiLCJ0YXJnZXQiLCJ2YWx1ZSIsIm9uU2VhcmNoIiwib3B0aW9ucyIsInNlYXJjaFBsYWNlaG9sZGVyIiwic2VhcmNoUXVlcnkiLCJQdXJlQ29tcG9uZW50IiwicHJvcFR5cGVzIiwic3RyaW5nIiwib2JqZWN0IiwiZnVuYyJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7O0FBQ0E7Ozs7Ozs7Ozs7OztJQUVNQSxXOzs7QUFDSix1QkFBYUMsS0FBYixFQUFvQjtBQUFBOztBQUFBLDBIQUNaQSxLQURZOztBQUVsQixVQUFLQyxpQkFBTCxHQUF5QixNQUFLQSxpQkFBTCxDQUF1QkMsSUFBdkIsT0FBekI7QUFDQSxVQUFLQyxnQkFBTCxHQUF3QixNQUFLQSxnQkFBTCxDQUFzQkQsSUFBdEIsT0FBeEI7QUFIa0I7QUFJbkI7Ozs7c0NBRWtCRSxDLEVBQUc7QUFDcEIsVUFBTUMsUUFBUUQsRUFBRUUsTUFBRixDQUFTQyxLQUF2QjtBQURvQixVQUVaQyxRQUZZLEdBRUMsS0FBS1IsS0FGTixDQUVaUSxRQUZZOztBQUdwQixVQUFJQSxRQUFKLEVBQWNBLFNBQVNILEtBQVQ7QUFDZjs7O3VDQUVtQjtBQUNsQixVQUFNQSxRQUFRLElBQWQ7QUFEa0IsVUFFVkcsUUFGVSxHQUVHLEtBQUtSLEtBRlIsQ0FFVlEsUUFGVTs7QUFHbEIsVUFBSUEsUUFBSixFQUFjQSxTQUFTSCxLQUFUO0FBQ2Y7Ozs2QkFFUztBQUFBLG1CQUNtQixLQUFLTCxLQUR4QjtBQUFBLFVBQ0FTLE9BREEsVUFDQUEsT0FEQTtBQUFBLFVBQ1NKLEtBRFQsVUFDU0EsS0FEVDtBQUFBLFVBRUFLLGlCQUZBLEdBRXNCRCxPQUZ0QixDQUVBQyxpQkFGQTtBQUFBLFVBR0FULGlCQUhBLEdBR3dDLElBSHhDLENBR0FBLGlCQUhBO0FBQUEsVUFHbUJFLGdCQUhuQixHQUd3QyxJQUh4QyxDQUdtQkEsZ0JBSG5COzs7QUFLUixhQUNFO0FBQUE7QUFBQSxVQUFLLFdBQVUsYUFBZjtBQUNFLHdEQUFNLElBQUksUUFBVixHQURGO0FBRUU7QUFDRSxnQkFBSyxNQURQO0FBRUUsZ0JBQUssUUFGUDtBQUdFLGlCQUFPUSxlQUFlLEVBSHhCO0FBSUUsb0JBQVVWLGlCQUpaO0FBS0UsdUJBQWFTO0FBTGYsVUFGRjtBQVNHQyx1QkFDQztBQUFBO0FBQUEsWUFBUSxTQUFTUixnQkFBakI7QUFDRSwwREFBTSxJQUFJLGNBQVYsR0FERjtBQUFBO0FBQUE7QUFWSixPQURGO0FBa0JEOzs7O0VBMUN1QixnQkFBTVMsYTs7QUEyQy9COztBQUVEYixZQUFZYyxTQUFaLEdBQXdCO0FBQ3RCUixTQUFPLG9CQUFVUyxNQURLO0FBRXRCTCxXQUFTLG9CQUFVTSxNQUZHO0FBR3RCUCxZQUFVLG9CQUFVUTtBQUhFLENBQXhCOztrQkFNZWpCLFciLCJmaWxlIjoiVGFibGVTZWFyY2guanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuaW1wb3J0IFByb3BUeXBlcyBmcm9tICdwcm9wLXR5cGVzJztcbmltcG9ydCBJY29uIGZyb20gJy4uL0NvbXBvbmVudHMvSWNvbic7XG5cbmNsYXNzIFRhYmxlU2VhcmNoIGV4dGVuZHMgUmVhY3QuUHVyZUNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgICB0aGlzLmhhbmRsZVF1ZXJ5Q2hhbmdlID0gdGhpcy5oYW5kbGVRdWVyeUNoYW5nZS5iaW5kKHRoaXMpO1xuICAgIHRoaXMuY2xlYXJTZWFyY2hRdWVyeSA9IHRoaXMuY2xlYXJTZWFyY2hRdWVyeS5iaW5kKHRoaXMpO1xuICB9XG5cbiAgaGFuZGxlUXVlcnlDaGFuZ2UgKGUpIHtcbiAgICBjb25zdCBxdWVyeSA9IGUudGFyZ2V0LnZhbHVlO1xuICAgIGNvbnN0IHsgb25TZWFyY2ggfSA9IHRoaXMucHJvcHM7XG4gICAgaWYgKG9uU2VhcmNoKSBvblNlYXJjaChxdWVyeSk7XG4gIH1cblxuICBjbGVhclNlYXJjaFF1ZXJ5ICgpIHtcbiAgICBjb25zdCBxdWVyeSA9IG51bGw7XG4gICAgY29uc3QgeyBvblNlYXJjaCB9ID0gdGhpcy5wcm9wcztcbiAgICBpZiAob25TZWFyY2gpIG9uU2VhcmNoKHF1ZXJ5KTtcbiAgfVxuXG4gIHJlbmRlciAoKSB7XG4gICAgY29uc3QgeyBvcHRpb25zLCBxdWVyeSB9ID0gdGhpcy5wcm9wcztcbiAgICBjb25zdCB7IHNlYXJjaFBsYWNlaG9sZGVyIH0gPSBvcHRpb25zO1xuICAgIGNvbnN0IHsgaGFuZGxlUXVlcnlDaGFuZ2UsIGNsZWFyU2VhcmNoUXVlcnkgfSA9IHRoaXM7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJUYWJsZVNlYXJjaFwiPlxuICAgICAgICA8SWNvbiBmYT17J3NlYXJjaCd9IC8+XG4gICAgICAgIDxpbnB1dFxuICAgICAgICAgIHR5cGU9XCJ0ZXh0XCJcbiAgICAgICAgICBuYW1lPVwiU2VhcmNoXCJcbiAgICAgICAgICB2YWx1ZT17c2VhcmNoUXVlcnkgfHwgJyd9XG4gICAgICAgICAgb25DaGFuZ2U9e2hhbmRsZVF1ZXJ5Q2hhbmdlfVxuICAgICAgICAgIHBsYWNlaG9sZGVyPXtzZWFyY2hQbGFjZWhvbGRlcn1cbiAgICAgICAgLz5cbiAgICAgICAge3NlYXJjaFF1ZXJ5ICYmIChcbiAgICAgICAgICA8YnV0dG9uIG9uQ2xpY2s9e2NsZWFyU2VhcmNoUXVlcnl9PlxuICAgICAgICAgICAgPEljb24gZmE9eyd0aW1lcy1jaXJjbGUnfSAvPlxuICAgICAgICAgICAgQ2xlYXIgU2VhcmNoXG4gICAgICAgICAgPC9idXR0b24+XG4gICAgICAgICl9XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59O1xuXG5UYWJsZVNlYXJjaC5wcm9wVHlwZXMgPSB7XG4gIHF1ZXJ5OiBQcm9wVHlwZXMuc3RyaW5nLFxuICBvcHRpb25zOiBQcm9wVHlwZXMub2JqZWN0LFxuICBvblNlYXJjaDogUHJvcFR5cGVzLmZ1bmNcbn07XG5cbmV4cG9ydCBkZWZhdWx0IFRhYmxlU2VhcmNoO1xuIl19