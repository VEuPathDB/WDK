wdk.namespace('wdk.dataRestriction', (ns, $) => {

  function getIdFromRecordClass (recordClass) {
    if (typeof recordClass !== 'string') return null;
    if (recordClass.length > 13) recordClass = recordClass.slice(0, 13);
    const [ studyId ] = recordClass.match(/^DS_[^_]+/g);
    return studyId;
  };

  function emit (action, details) {
    const detail = Object.assign({}, details, { action });
    const event = new CustomEvent('DataRestricted', { detail });
    document.dispatchEvent(event);
  };

  ns.pagingController = (elem) => {
    const { recordClass } = elem.data();
    const studyId = getIdFromRecordClass(recordClass);
    elem.on('click', 'a, input[type="button"]', (event) => {
      emit('paginate', { studyId, event });
    });
  };

  ns.downloadLinkController = (elem) => {
    const { recordClass } = elem.data();
    const studyId = getIdFromRecordClass(recordClass);
    elem.on('click', (event) => {
      emit('download', { studyId, event });
    });
  };

});
