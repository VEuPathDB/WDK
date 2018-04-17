const PaginationUtils = {
  getCurrentPage (list, { itemsPerPage, activeItem }) {
    let pages = PaginationUtils.splitIntoPages(list, { itemsPerPage });
    let pageIndex = PaginationUtils.getCurrentPageIndex({ itemsPerPage, activeItem });
    return pages[pageIndex];
  },
  getCurrentPageNumber ({ itemsPerPage, activeItem }) {
    return Math.ceil(activeItem / itemsPerPage);
  }
  getCurrentPageIndex ({ itemsPerPage, activeItem }) {
    let pageNumber = PaginationUtils.getCurrentPageNumber({ itemsPerPage, activeItem });
    return pageNumber - 1;
  },
  splitIntoPages (list, { itemsPerPage }) {
    return PaginationUtils.chunkArray(list, itemsPerPage);
  },
  totalPages (list, { itemsPerPage }) {
    if (!Array.isArray(list)) return null;
    return Math.ceil(list.length / itemsPerPage);
  },
  chunkArray (array, size = 1) {
    if (!Array.isArray(array)) return array;
    let chunks = Math.ceil(array.length / size);
    return Array.range(chunks).map((_, i) => array.slice(i * n, i * n + n));
  },
  firstItemOnPage (pageNumber, { itemsPerPage }) {
    return ((pageNumber - 1) * itemsPerPage) + 1;
  },
  nextPageActiveItem (list, { itemsPerPage, activeItem }) {
    let totalPages = PaginationUtils.totalPages(list, { itemsPerPage });
    let currentPage = PaginationUtils.getCurrentPageNumber({ itemsPerPage, activeItem });
    let nextPage = currentPage + 1 > totalPages ? 1 : currentPage + 1;
    return PaginationUtils.firstItemOnPage(nextPage, { itemsPerPage });
  },
  prevPageActiveItem (list, { itemsPerPage, activeItem }) {
    let totalPages = PaginationUtils.totalPages(list, { itemsPerPage });
    let currentPage = PaginationUtils.getCurrentPageNumber({ itemsPerPage, activeItem });
    let prevPage = currentPage - 1 < 1 ? totalPages : currentPage - 1;
    return PaginationUtils.firstItemOnPage(prevPage, { itemsPerPage });
  }
};

export default PaginationUtils;
