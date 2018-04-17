'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

var _Utils = require('../Utils/Utils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var EmptyState = function (_React$PureComponent) {
  _inherits(EmptyState, _React$PureComponent);

  function EmptyState(props) {
    _classCallCheck(this, EmptyState);

    var _this = _possibleConstructorReturn(this, (EmptyState.__proto__ || Object.getPrototypeOf(EmptyState)).call(this, props));

    _this.getCulprit = _this.getCulprit.bind(_this);
    return _this;
  }

  _createClass(EmptyState, [{
    key: 'getCulprit',
    value: function getCulprit() {
      var culprit = this.props.culprit;

      switch (culprit) {
        case 'search':
          return {
            icon: 'search',
            title: 'No Results',
            content: _react2.default.createElement(
              'div',
              null,
              _react2.default.createElement(
                'p',
                null,
                'Sorry, your search returned no results.'
              )
            )
          };
        case 'nocolumns':
          return {
            icon: 'columns',
            title: 'No Columns Shown',
            content: _react2.default.createElement(
              'div',
              null,
              _react2.default.createElement(
                'p',
                null,
                'Whoops, looks like you\'ve hidden all columns. Use the column editor to show some columns.'
              )
            )
          };
        case 'filters':
          return {
            icon: 'filter',
            title: 'No Filter Results',
            content: _react2.default.createElement(
              'div',
              null,
              _react2.default.createElement(
                'p',
                null,
                'No rows exist that match all of your column filter settings.'
              )
            )
          };
        case 'nodata':
        default:
          return {
            icon: 'table',
            title: 'No Data',
            content: _react2.default.createElement(
              'div',
              null,
              _react2.default.createElement(
                'p',
                null,
                'Whoops! Either no table data was provided, or the data provided could not be parsed.'
              )
            )
          };
      }
    }
  }, {
    key: 'render',
    value: function render() {
      var culprit = this.getCulprit();
      var emptyStateClass = (0, _Utils.makeClassifier)('EmptyState');

      return _react2.default.createElement(
        'div',
        { className: emptyStateClass() },
        _react2.default.createElement(
          'div',
          { className: emptyStateClass('BodyWrapper') },
          _react2.default.createElement(
            'div',
            { className: emptyStateClass('Body') },
            _react2.default.createElement(_Icon2.default, { fa: culprit.icon, className: emptyStateClass('Icon') }),
            _react2.default.createElement(
              'h2',
              null,
              culprit.title
            ),
            culprit.content
          )
        )
      );
    }
  }]);

  return EmptyState;
}(_react2.default.PureComponent);

;

exports.default = EmptyState;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9FbXB0eVN0YXRlLmpzeCJdLCJuYW1lcyI6WyJFbXB0eVN0YXRlIiwicHJvcHMiLCJnZXRDdWxwcml0IiwiYmluZCIsImN1bHByaXQiLCJpY29uIiwidGl0bGUiLCJjb250ZW50IiwiZW1wdHlTdGF0ZUNsYXNzIiwiUHVyZUNvbXBvbmVudCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7QUFBQTs7OztBQUVBOzs7O0FBQ0E7Ozs7Ozs7Ozs7SUFFTUEsVTs7O0FBQ0osc0JBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSx3SEFDWkEsS0FEWTs7QUFFbEIsVUFBS0MsVUFBTCxHQUFrQixNQUFLQSxVQUFMLENBQWdCQyxJQUFoQixPQUFsQjtBQUZrQjtBQUduQjs7OztpQ0FFYTtBQUFBLFVBQ0pDLE9BREksR0FDUSxLQUFLSCxLQURiLENBQ0pHLE9BREk7O0FBRVosY0FBUUEsT0FBUjtBQUNFLGFBQUssUUFBTDtBQUNFLGlCQUFPO0FBQ0xDLGtCQUFNLFFBREQ7QUFFTEMsbUJBQU8sWUFGRjtBQUdMQyxxQkFDRTtBQUFBO0FBQUE7QUFDRTtBQUFBO0FBQUE7QUFBQTtBQUFBO0FBREY7QUFKRyxXQUFQO0FBU0YsYUFBSyxXQUFMO0FBQ0UsaUJBQU87QUFDTEYsa0JBQU0sU0FERDtBQUVMQyxtQkFBTyxrQkFGRjtBQUdMQyxxQkFDRTtBQUFBO0FBQUE7QUFDRTtBQUFBO0FBQUE7QUFBQTtBQUFBO0FBREY7QUFKRyxXQUFQO0FBU0YsYUFBSyxTQUFMO0FBQ0UsaUJBQU87QUFDTEYsa0JBQU0sUUFERDtBQUVMQyxtQkFBTyxtQkFGRjtBQUdMQyxxQkFDRTtBQUFBO0FBQUE7QUFDRTtBQUFBO0FBQUE7QUFBQTtBQUFBO0FBREY7QUFKRyxXQUFQO0FBU0YsYUFBSyxRQUFMO0FBQ0E7QUFDRSxpQkFBTztBQUNMRixrQkFBTSxPQUREO0FBRUxDLG1CQUFPLFNBRkY7QUFHTEMscUJBQ0U7QUFBQTtBQUFBO0FBQ0U7QUFBQTtBQUFBO0FBQUE7QUFBQTtBQURGO0FBSkcsV0FBUDtBQWpDSjtBQTJDRDs7OzZCQUVTO0FBQ1IsVUFBTUgsVUFBVSxLQUFLRixVQUFMLEVBQWhCO0FBQ0EsVUFBTU0sa0JBQWtCLDJCQUFlLFlBQWYsQ0FBeEI7O0FBRUEsYUFDRTtBQUFBO0FBQUEsVUFBSyxXQUFXQSxpQkFBaEI7QUFDRTtBQUFBO0FBQUEsWUFBSyxXQUFXQSxnQkFBZ0IsYUFBaEIsQ0FBaEI7QUFDRTtBQUFBO0FBQUEsY0FBSyxXQUFXQSxnQkFBZ0IsTUFBaEIsQ0FBaEI7QUFDRSw0REFBTSxJQUFJSixRQUFRQyxJQUFsQixFQUF3QixXQUFXRyxnQkFBZ0IsTUFBaEIsQ0FBbkMsR0FERjtBQUVFO0FBQUE7QUFBQTtBQUFLSixzQkFBUUU7QUFBYixhQUZGO0FBR0dGLG9CQUFRRztBQUhYO0FBREY7QUFERixPQURGO0FBV0Q7Ozs7RUFwRXNCLGdCQUFNRSxhOztBQXFFOUI7O2tCQUVjVCxVIiwiZmlsZSI6IkVtcHR5U3RhdGUuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuXG5pbXBvcnQgSWNvbiBmcm9tICcuLi9Db21wb25lbnRzL0ljb24nO1xuaW1wb3J0IHsgbWFrZUNsYXNzaWZpZXIgfSBmcm9tICcuLi9VdGlscy9VdGlscyc7XG5cbmNsYXNzIEVtcHR5U3RhdGUgZXh0ZW5kcyBSZWFjdC5QdXJlQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuICAgIHRoaXMuZ2V0Q3VscHJpdCA9IHRoaXMuZ2V0Q3VscHJpdC5iaW5kKHRoaXMpO1xuICB9XG5cbiAgZ2V0Q3VscHJpdCAoKSB7XG4gICAgY29uc3QgeyBjdWxwcml0IH0gPSB0aGlzLnByb3BzO1xuICAgIHN3aXRjaCAoY3VscHJpdCkge1xuICAgICAgY2FzZSAnc2VhcmNoJzpcbiAgICAgICAgcmV0dXJuIHtcbiAgICAgICAgICBpY29uOiAnc2VhcmNoJyxcbiAgICAgICAgICB0aXRsZTogJ05vIFJlc3VsdHMnLFxuICAgICAgICAgIGNvbnRlbnQ6IChcbiAgICAgICAgICAgIDxkaXY+XG4gICAgICAgICAgICAgIDxwPlNvcnJ5LCB5b3VyIHNlYXJjaCByZXR1cm5lZCBubyByZXN1bHRzLjwvcD5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgIClcbiAgICAgICAgfTtcbiAgICAgIGNhc2UgJ25vY29sdW1ucyc6XG4gICAgICAgIHJldHVybiB7XG4gICAgICAgICAgaWNvbjogJ2NvbHVtbnMnLFxuICAgICAgICAgIHRpdGxlOiAnTm8gQ29sdW1ucyBTaG93bicsXG4gICAgICAgICAgY29udGVudDogKFxuICAgICAgICAgICAgPGRpdj5cbiAgICAgICAgICAgICAgPHA+V2hvb3BzLCBsb29rcyBsaWtlIHlvdSd2ZSBoaWRkZW4gYWxsIGNvbHVtbnMuIFVzZSB0aGUgY29sdW1uIGVkaXRvciB0byBzaG93IHNvbWUgY29sdW1ucy48L3A+XG4gICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICApXG4gICAgICAgIH07XG4gICAgICBjYXNlICdmaWx0ZXJzJzpcbiAgICAgICAgcmV0dXJuIHtcbiAgICAgICAgICBpY29uOiAnZmlsdGVyJyxcbiAgICAgICAgICB0aXRsZTogJ05vIEZpbHRlciBSZXN1bHRzJyxcbiAgICAgICAgICBjb250ZW50OiAoXG4gICAgICAgICAgICA8ZGl2PlxuICAgICAgICAgICAgICA8cD5ObyByb3dzIGV4aXN0IHRoYXQgbWF0Y2ggYWxsIG9mIHlvdXIgY29sdW1uIGZpbHRlciBzZXR0aW5ncy48L3A+XG4gICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICApXG4gICAgICAgIH07XG4gICAgICBjYXNlICdub2RhdGEnOlxuICAgICAgZGVmYXVsdDpcbiAgICAgICAgcmV0dXJuIHtcbiAgICAgICAgICBpY29uOiAndGFibGUnLFxuICAgICAgICAgIHRpdGxlOiAnTm8gRGF0YScsXG4gICAgICAgICAgY29udGVudDogKFxuICAgICAgICAgICAgPGRpdj5cbiAgICAgICAgICAgICAgPHA+V2hvb3BzISBFaXRoZXIgbm8gdGFibGUgZGF0YSB3YXMgcHJvdmlkZWQsIG9yIHRoZSBkYXRhIHByb3ZpZGVkIGNvdWxkIG5vdCBiZSBwYXJzZWQuPC9wPlxuICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgKVxuICAgICAgICB9O1xuICAgIH1cbiAgfVxuXG4gIHJlbmRlciAoKSB7XG4gICAgY29uc3QgY3VscHJpdCA9IHRoaXMuZ2V0Q3VscHJpdCgpO1xuICAgIGNvbnN0IGVtcHR5U3RhdGVDbGFzcyA9IG1ha2VDbGFzc2lmaWVyKCdFbXB0eVN0YXRlJyk7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9e2VtcHR5U3RhdGVDbGFzcygpfT5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9e2VtcHR5U3RhdGVDbGFzcygnQm9keVdyYXBwZXInKX0+XG4gICAgICAgICAgPGRpdiBjbGFzc05hbWU9e2VtcHR5U3RhdGVDbGFzcygnQm9keScpfT5cbiAgICAgICAgICAgIDxJY29uIGZhPXtjdWxwcml0Lmljb259IGNsYXNzTmFtZT17ZW1wdHlTdGF0ZUNsYXNzKCdJY29uJyl9IC8+XG4gICAgICAgICAgICA8aDI+e2N1bHByaXQudGl0bGV9PC9oMj5cbiAgICAgICAgICAgIHtjdWxwcml0LmNvbnRlbnR9XG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKVxuICB9XG59O1xuXG5leHBvcnQgZGVmYXVsdCBFbXB0eVN0YXRlO1xuIl19