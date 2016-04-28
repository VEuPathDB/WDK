
let ReporterSortMessage = props =>
    (props.scope === "record" ? "" :
      <div style={{margin: '1em 0 0 0'}}>**Note: The records in the report will be sorted by ID.</div>);

export default ReporterSortMessage;
