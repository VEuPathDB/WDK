'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _DataRow = require('../Ui/DataRow');

var _DataRow2 = _interopRequireDefault(_DataRow);

var _Utils = require('../Utils/Utils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var dataRowListClass = (0, _Utils.makeClassifier)('DataRowList');

var DataRowList = function (_React$Component) {
  _inherits(DataRowList, _React$Component);

  function DataRowList(props) {
    _classCallCheck(this, DataRowList);

    return _possibleConstructorReturn(this, (DataRowList.__proto__ || Object.getPrototypeOf(DataRowList)).call(this, props));
  }

  _createClass(DataRowList, [{
    key: 'render',
    value: function render() {
      var props = this.props;
      var rows = props.rows,
          filteredRows = props.filteredRows;


      return _react2.default.createElement(
        'tbody',
        { className: dataRowListClass() },
        filteredRows.map(function (row, rowIndex) {
          return _react2.default.createElement(_DataRow2.default, _extends({
            row: row,
            key: rowIndex,
            rowIndex: rowIndex
          }, props));
        })
      );
    }
  }]);

  return DataRowList;
}(_react2.default.Component);

;

exports.default = DataRowList;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9EYXRhUm93TGlzdC5qc3giXSwibmFtZXMiOlsiZGF0YVJvd0xpc3RDbGFzcyIsIkRhdGFSb3dMaXN0IiwicHJvcHMiLCJyb3dzIiwiZmlsdGVyZWRSb3dzIiwibWFwIiwicm93Iiwicm93SW5kZXgiLCJDb21wb25lbnQiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7QUFBQTs7OztBQUVBOzs7O0FBQ0E7Ozs7Ozs7Ozs7QUFFQSxJQUFNQSxtQkFBbUIsMkJBQWUsYUFBZixDQUF6Qjs7SUFFTUMsVzs7O0FBQ0osdUJBQWFDLEtBQWIsRUFBb0I7QUFBQTs7QUFBQSxxSEFDWkEsS0FEWTtBQUVuQjs7Ozs2QkFFUztBQUFBLFVBQ0FBLEtBREEsR0FDVSxJQURWLENBQ0FBLEtBREE7QUFBQSxVQUVBQyxJQUZBLEdBRXVCRCxLQUZ2QixDQUVBQyxJQUZBO0FBQUEsVUFFTUMsWUFGTixHQUV1QkYsS0FGdkIsQ0FFTUUsWUFGTjs7O0FBSVIsYUFDRTtBQUFBO0FBQUEsVUFBTyxXQUFXSixrQkFBbEI7QUFDR0kscUJBQWFDLEdBQWIsQ0FBaUIsVUFBQ0MsR0FBRCxFQUFNQyxRQUFOO0FBQUEsaUJBQ2hCO0FBQ0UsaUJBQUtELEdBRFA7QUFFRSxpQkFBS0MsUUFGUDtBQUdFLHNCQUFVQTtBQUhaLGFBSU1MLEtBSk4sRUFEZ0I7QUFBQSxTQUFqQjtBQURILE9BREY7QUFZRDs7OztFQXJCdUIsZ0JBQU1NLFM7O0FBc0IvQjs7a0JBRWNQLFciLCJmaWxlIjoiRGF0YVJvd0xpc3QuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuXG5pbXBvcnQgRGF0YVJvdyBmcm9tICcuLi9VaS9EYXRhUm93JztcbmltcG9ydCB7IG1ha2VDbGFzc2lmaWVyIH0gZnJvbSAnLi4vVXRpbHMvVXRpbHMnO1xuXG5jb25zdCBkYXRhUm93TGlzdENsYXNzID0gbWFrZUNsYXNzaWZpZXIoJ0RhdGFSb3dMaXN0Jyk7XG5cbmNsYXNzIERhdGFSb3dMaXN0IGV4dGVuZHMgUmVhY3QuQ29tcG9uZW50IHtcbiAgY29uc3RydWN0b3IgKHByb3BzKSB7XG4gICAgc3VwZXIocHJvcHMpO1xuICB9XG5cbiAgcmVuZGVyICgpIHtcbiAgICBjb25zdCB7IHByb3BzIH0gPSB0aGlzO1xuICAgIGNvbnN0IHsgcm93cywgZmlsdGVyZWRSb3dzIH0gPSBwcm9wcztcblxuICAgIHJldHVybiAoXG4gICAgICA8dGJvZHkgY2xhc3NOYW1lPXtkYXRhUm93TGlzdENsYXNzKCl9PlxuICAgICAgICB7ZmlsdGVyZWRSb3dzLm1hcCgocm93LCByb3dJbmRleCkgPT4gKFxuICAgICAgICAgIDxEYXRhUm93XG4gICAgICAgICAgICByb3c9e3Jvd31cbiAgICAgICAgICAgIGtleT17cm93SW5kZXh9XG4gICAgICAgICAgICByb3dJbmRleD17cm93SW5kZXh9XG4gICAgICAgICAgICB7Li4ucHJvcHN9XG4gICAgICAgICAgLz5cbiAgICAgICAgKSl9XG4gICAgICA8L3Rib2R5PlxuICAgICk7XG4gIH1cbn07XG5cbmV4cG9ydCBkZWZhdWx0IERhdGFSb3dMaXN0O1xuIl19