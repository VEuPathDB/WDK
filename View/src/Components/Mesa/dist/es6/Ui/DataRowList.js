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
        { className: 'DataRowList' },
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9VaS9EYXRhUm93TGlzdC5qc3giXSwibmFtZXMiOlsiRGF0YVJvd0xpc3QiLCJwcm9wcyIsInJvd3MiLCJmaWx0ZXJlZFJvd3MiLCJtYXAiLCJyb3ciLCJyb3dJbmRleCIsIkNvbXBvbmVudCJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7OztBQUFBOzs7O0FBRUE7Ozs7QUFDQTs7Ozs7Ozs7OztJQUVNQSxXOzs7QUFDSix1QkFBYUMsS0FBYixFQUFvQjtBQUFBOztBQUFBLHFIQUNaQSxLQURZO0FBRW5COzs7OzZCQUVTO0FBQUEsVUFDQUEsS0FEQSxHQUNVLElBRFYsQ0FDQUEsS0FEQTtBQUFBLFVBRUFDLElBRkEsR0FFdUJELEtBRnZCLENBRUFDLElBRkE7QUFBQSxVQUVNQyxZQUZOLEdBRXVCRixLQUZ2QixDQUVNRSxZQUZOOzs7QUFJUixhQUNFO0FBQUE7QUFBQSxVQUFPLFdBQVUsYUFBakI7QUFDR0EscUJBQWFDLEdBQWIsQ0FBaUIsVUFBQ0MsR0FBRCxFQUFNQyxRQUFOO0FBQUEsaUJBQ2hCO0FBQ0UsaUJBQUtELEdBRFA7QUFFRSxpQkFBS0MsUUFGUDtBQUdFLHNCQUFVQTtBQUhaLGFBSU1MLEtBSk4sRUFEZ0I7QUFBQSxTQUFqQjtBQURILE9BREY7QUFZRDs7OztFQXJCdUIsZ0JBQU1NLFM7O0FBc0IvQjs7a0JBRWNQLFciLCJmaWxlIjoiRGF0YVJvd0xpc3QuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuXG5pbXBvcnQgRGF0YVJvdyBmcm9tICcuLi9VaS9EYXRhUm93JztcbmltcG9ydCB7IG1ha2VDbGFzc2lmaWVyIH0gZnJvbSAnLi4vVXRpbHMvVXRpbHMnO1xuXG5jbGFzcyBEYXRhUm93TGlzdCBleHRlbmRzIFJlYWN0LkNvbXBvbmVudCB7XG4gIGNvbnN0cnVjdG9yIChwcm9wcykge1xuICAgIHN1cGVyKHByb3BzKTtcbiAgfVxuXG4gIHJlbmRlciAoKSB7XG4gICAgY29uc3QgeyBwcm9wcyB9ID0gdGhpcztcbiAgICBjb25zdCB7IHJvd3MsIGZpbHRlcmVkUm93cyB9ID0gcHJvcHM7XG5cbiAgICByZXR1cm4gKFxuICAgICAgPHRib2R5IGNsYXNzTmFtZT1cIkRhdGFSb3dMaXN0XCI+XG4gICAgICAgIHtmaWx0ZXJlZFJvd3MubWFwKChyb3csIHJvd0luZGV4KSA9PiAoXG4gICAgICAgICAgPERhdGFSb3dcbiAgICAgICAgICAgIHJvdz17cm93fVxuICAgICAgICAgICAga2V5PXtyb3dJbmRleH1cbiAgICAgICAgICAgIHJvd0luZGV4PXtyb3dJbmRleH1cbiAgICAgICAgICAgIHsuLi5wcm9wc31cbiAgICAgICAgICAvPlxuICAgICAgICApKX1cbiAgICAgIDwvdGJvZHk+XG4gICAgKTtcbiAgfVxufTtcblxuZXhwb3J0IGRlZmF1bHQgRGF0YVJvd0xpc3Q7XG4iXX0=