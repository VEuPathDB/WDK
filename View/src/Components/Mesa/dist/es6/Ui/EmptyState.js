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
            title: 'No Results',
            content: null
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
            { className: emptyStateClass('Body'), style: { textAlign: 'center' } },
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9FbXB0eVN0YXRlLmpzeCJdLCJuYW1lcyI6WyJFbXB0eVN0YXRlIiwicHJvcHMiLCJnZXRDdWxwcml0IiwiYmluZCIsImN1bHByaXQiLCJpY29uIiwidGl0bGUiLCJjb250ZW50IiwiZW1wdHlTdGF0ZUNsYXNzIiwidGV4dEFsaWduIiwiUHVyZUNvbXBvbmVudCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7QUFBQTs7OztBQUVBOzs7O0FBQ0E7Ozs7Ozs7Ozs7SUFFTUEsVTs7O0FBQ0osc0JBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSx3SEFDWkEsS0FEWTs7QUFFbEIsVUFBS0MsVUFBTCxHQUFrQixNQUFLQSxVQUFMLENBQWdCQyxJQUFoQixPQUFsQjtBQUZrQjtBQUduQjs7OztpQ0FFYTtBQUFBLFVBQ0pDLE9BREksR0FDUSxLQUFLSCxLQURiLENBQ0pHLE9BREk7O0FBRVosY0FBUUEsT0FBUjtBQUNFLGFBQUssUUFBTDtBQUNFLGlCQUFPO0FBQ0xDLGtCQUFNLFFBREQ7QUFFTEMsbUJBQU8sWUFGRjtBQUdMQyxxQkFDRTtBQUFBO0FBQUE7QUFDRTtBQUFBO0FBQUE7QUFBQTtBQUFBO0FBREY7QUFKRyxXQUFQO0FBU0YsYUFBSyxXQUFMO0FBQ0UsaUJBQU87QUFDTEYsa0JBQU0sU0FERDtBQUVMQyxtQkFBTyxrQkFGRjtBQUdMQyxxQkFDRTtBQUFBO0FBQUE7QUFDRTtBQUFBO0FBQUE7QUFBQTtBQUFBO0FBREY7QUFKRyxXQUFQO0FBU0YsYUFBSyxTQUFMO0FBQ0UsaUJBQU87QUFDTEYsa0JBQU0sUUFERDtBQUVMQyxtQkFBTyxtQkFGRjtBQUdMQyxxQkFDRTtBQUFBO0FBQUE7QUFDRTtBQUFBO0FBQUE7QUFBQTtBQUFBO0FBREY7QUFKRyxXQUFQO0FBU0YsYUFBSyxRQUFMO0FBQ0E7QUFDRSxpQkFBTztBQUNMRixrQkFBTSxPQUREO0FBRUxDLG1CQUFPLFlBRkY7QUFHTEMscUJBQVM7QUFISixXQUFQO0FBakNKO0FBdUNEOzs7NkJBRVM7QUFDUixVQUFNSCxVQUFVLEtBQUtGLFVBQUwsRUFBaEI7QUFDQSxVQUFNTSxrQkFBa0IsMkJBQWUsWUFBZixDQUF4Qjs7QUFFQSxhQUNFO0FBQUE7QUFBQSxVQUFLLFdBQVdBLGlCQUFoQjtBQUNFO0FBQUE7QUFBQSxZQUFLLFdBQVdBLGdCQUFnQixhQUFoQixDQUFoQjtBQUNFO0FBQUE7QUFBQSxjQUFLLFdBQVdBLGdCQUFnQixNQUFoQixDQUFoQixFQUF5QyxPQUFPLEVBQUVDLFdBQVcsUUFBYixFQUFoRDtBQUNFLDREQUFNLElBQUlMLFFBQVFDLElBQWxCLEVBQXdCLFdBQVdHLGdCQUFnQixNQUFoQixDQUFuQyxHQURGO0FBRUU7QUFBQTtBQUFBO0FBQUtKLHNCQUFRRTtBQUFiLGFBRkY7QUFHR0Ysb0JBQVFHO0FBSFg7QUFERjtBQURGLE9BREY7QUFXRDs7OztFQWhFc0IsZ0JBQU1HLGE7O0FBaUU5Qjs7a0JBRWNWLFUiLCJmaWxlIjoiRW1wdHlTdGF0ZS5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBSZWFjdCBmcm9tICdyZWFjdCc7XG5cbmltcG9ydCBJY29uIGZyb20gJy4uL0NvbXBvbmVudHMvSWNvbic7XG5pbXBvcnQgeyBtYWtlQ2xhc3NpZmllciB9IGZyb20gJy4uL1V0aWxzL1V0aWxzJztcblxuY2xhc3MgRW1wdHlTdGF0ZSBleHRlbmRzIFJlYWN0LlB1cmVDb21wb25lbnQge1xuICBjb25zdHJ1Y3RvciAocHJvcHMpIHtcbiAgICBzdXBlcihwcm9wcyk7XG4gICAgdGhpcy5nZXRDdWxwcml0ID0gdGhpcy5nZXRDdWxwcml0LmJpbmQodGhpcyk7XG4gIH1cblxuICBnZXRDdWxwcml0ICgpIHtcbiAgICBjb25zdCB7IGN1bHByaXQgfSA9IHRoaXMucHJvcHM7XG4gICAgc3dpdGNoIChjdWxwcml0KSB7XG4gICAgICBjYXNlICdzZWFyY2gnOlxuICAgICAgICByZXR1cm4ge1xuICAgICAgICAgIGljb246ICdzZWFyY2gnLFxuICAgICAgICAgIHRpdGxlOiAnTm8gUmVzdWx0cycsXG4gICAgICAgICAgY29udGVudDogKFxuICAgICAgICAgICAgPGRpdj5cbiAgICAgICAgICAgICAgPHA+U29ycnksIHlvdXIgc2VhcmNoIHJldHVybmVkIG5vIHJlc3VsdHMuPC9wPlxuICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgKVxuICAgICAgICB9O1xuICAgICAgY2FzZSAnbm9jb2x1bW5zJzpcbiAgICAgICAgcmV0dXJuIHtcbiAgICAgICAgICBpY29uOiAnY29sdW1ucycsXG4gICAgICAgICAgdGl0bGU6ICdObyBDb2x1bW5zIFNob3duJyxcbiAgICAgICAgICBjb250ZW50OiAoXG4gICAgICAgICAgICA8ZGl2PlxuICAgICAgICAgICAgICA8cD5XaG9vcHMsIGxvb2tzIGxpa2UgeW91J3ZlIGhpZGRlbiBhbGwgY29sdW1ucy4gVXNlIHRoZSBjb2x1bW4gZWRpdG9yIHRvIHNob3cgc29tZSBjb2x1bW5zLjwvcD5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgIClcbiAgICAgICAgfTtcbiAgICAgIGNhc2UgJ2ZpbHRlcnMnOlxuICAgICAgICByZXR1cm4ge1xuICAgICAgICAgIGljb246ICdmaWx0ZXInLFxuICAgICAgICAgIHRpdGxlOiAnTm8gRmlsdGVyIFJlc3VsdHMnLFxuICAgICAgICAgIGNvbnRlbnQ6IChcbiAgICAgICAgICAgIDxkaXY+XG4gICAgICAgICAgICAgIDxwPk5vIHJvd3MgZXhpc3QgdGhhdCBtYXRjaCBhbGwgb2YgeW91ciBjb2x1bW4gZmlsdGVyIHNldHRpbmdzLjwvcD5cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgIClcbiAgICAgICAgfTtcbiAgICAgIGNhc2UgJ25vZGF0YSc6XG4gICAgICBkZWZhdWx0OlxuICAgICAgICByZXR1cm4ge1xuICAgICAgICAgIGljb246ICd0YWJsZScsXG4gICAgICAgICAgdGl0bGU6ICdObyBSZXN1bHRzJyxcbiAgICAgICAgICBjb250ZW50OiBudWxsXG4gICAgICAgIH07XG4gICAgfVxuICB9XG5cbiAgcmVuZGVyICgpIHtcbiAgICBjb25zdCBjdWxwcml0ID0gdGhpcy5nZXRDdWxwcml0KCk7XG4gICAgY29uc3QgZW1wdHlTdGF0ZUNsYXNzID0gbWFrZUNsYXNzaWZpZXIoJ0VtcHR5U3RhdGUnKTtcblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT17ZW1wdHlTdGF0ZUNsYXNzKCl9PlxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT17ZW1wdHlTdGF0ZUNsYXNzKCdCb2R5V3JhcHBlcicpfT5cbiAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT17ZW1wdHlTdGF0ZUNsYXNzKCdCb2R5Jyl9IHN0eWxlPXt7IHRleHRBbGlnbjogJ2NlbnRlcicgfX0+XG4gICAgICAgICAgICA8SWNvbiBmYT17Y3VscHJpdC5pY29ufSBjbGFzc05hbWU9e2VtcHR5U3RhdGVDbGFzcygnSWNvbicpfSAvPlxuICAgICAgICAgICAgPGgyPntjdWxwcml0LnRpdGxlfTwvaDI+XG4gICAgICAgICAgICB7Y3VscHJpdC5jb250ZW50fVxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICA8L2Rpdj5cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn07XG5cbmV4cG9ydCBkZWZhdWx0IEVtcHR5U3RhdGU7XG4iXX0=