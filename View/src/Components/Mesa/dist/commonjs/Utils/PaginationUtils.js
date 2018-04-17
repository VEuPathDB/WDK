"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
var PaginationUtils = {
  getCurrentPage: function getCurrentPage(list, _ref) {
    var itemsPerPage = _ref.itemsPerPage,
        anchorIndex = _ref.anchorIndex;

    var pages = PaginationUtils.splitIntoPages(list, { itemsPerPage: itemsPerPage });
    var pageIndex = PaginationUtils.getCurrentPageIndex({ itemsPerPage: itemsPerPage, anchorIndex: anchorIndex });
    return pages[pageIndex];
  },
  getCurrentPageNumber: function getCurrentPageNumber(_ref2) {
    var itemsPerPage = _ref2.itemsPerPage,
        anchorIndex = _ref2.anchorIndex;

    return Math.ceil(anchorIndex / itemsPerPage);
  },
  getCurrentPageIndex: function getCurrentPageIndex(_ref3) {
    var itemsPerPage = _ref3.itemsPerPage,
        anchorIndex = _ref3.anchorIndex;

    var pageNumber = PaginationUtils.getCurrentPageNumber({ itemsPerPage: itemsPerPage, anchorIndex: anchorIndex });
    return pageNumber - 1;
  },
  getSpread: function getSpread(list, _ref4, usePagination) {
    var itemsPerPage = _ref4.itemsPerPage,
        anchorIndex = _ref4.anchorIndex;

    var spread = usePagination ? PaginationUtils.getCurrentPage(list, { itemsPerPage: itemsPerPage, anchorIndex: anchorIndex }) : list;
    return spread ? spread.map(function (item) {
      return item.__id;
    }) : [];
  },
  isSpreadSelected: function isSpreadSelected(spread, selection) {
    if (!Array.isArray(spread) || !Array.isArray(selection)) return;
    return selection.length >= spread.length && spread.every(function (id) {
      return selection.includes(id);
    });
  },
  countSelectedInSpread: function countSelectedInSpread(spread, selection) {
    if (!Array.isArray(spread) || !Array.isArray(selection)) return;
    var selected = spread.filter(function (id) {
      return selection.includes(id);
    });
    return selected.length;
  },
  splitIntoPages: function splitIntoPages(list, _ref5) {
    var itemsPerPage = _ref5.itemsPerPage;

    return PaginationUtils.chunkArray(list, itemsPerPage);
  },
  totalPages: function totalPages(list, _ref6) {
    var itemsPerPage = _ref6.itemsPerPage;

    if (!Array.isArray(list)) return null;
    return Math.ceil(list.length / itemsPerPage);
  },
  generatePageList: function generatePageList(size) {
    return new Array(size).fill({}).map(function (empty, index) {
      return index + 1;
    });
  },
  chunkArray: function chunkArray(array) {
    var size = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 1;

    if (!Array.isArray(array)) return array;
    var chunks = Math.ceil(array.length / size);
    return Array(chunks).fill({}).map(function (_, i) {
      return array.slice(i * size, i * size + size);
    });
  },
  firstItemOnPage: function firstItemOnPage(pageNumber, _ref7) {
    var itemsPerPage = _ref7.itemsPerPage;

    var result = (pageNumber - 1) * itemsPerPage + 1;
    return result;
  },
  lastItemOnPage: function lastItemOnPage(pageNumber, _ref8, list) {
    var itemsPerPage = _ref8.itemsPerPage;

    var result = pageNumber * itemsPerPage;
    return list && result > list.length ? list.length : result;
  },
  nextPageNumber: function nextPageNumber(list, _ref9) {
    var itemsPerPage = _ref9.itemsPerPage,
        anchorIndex = _ref9.anchorIndex;

    var totalPages = PaginationUtils.totalPages(list, { itemsPerPage: itemsPerPage });
    var currentPage = PaginationUtils.getCurrentPageNumber({ itemsPerPage: itemsPerPage, anchorIndex: anchorIndex });
    var nextPage = currentPage + 1 > totalPages ? 1 : currentPage + 1;
    return nextPage;
  },
  prevPageNumber: function prevPageNumber(list, _ref10) {
    var itemsPerPage = _ref10.itemsPerPage,
        anchorIndex = _ref10.anchorIndex;

    var totalPages = PaginationUtils.totalPages(list, { itemsPerPage: itemsPerPage });
    var currentPage = PaginationUtils.getCurrentPageNumber({ itemsPerPage: itemsPerPage, anchorIndex: anchorIndex });
    var prevPage = currentPage - 1 < 1 ? totalPages : currentPage - 1;
    return prevPage;
  }
};

exports.default = PaginationUtils;