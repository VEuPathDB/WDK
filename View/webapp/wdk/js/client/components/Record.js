import { Component, PropTypes } from 'react';
import RecordMainSection from './RecordMainSection';
import { wrappable } from '../utils/componentUtils';


class Record extends Component {

  render() {
    let { tables, attributes } = this.props.recordClass;
    return (
      <div className="wdk-Record">
        <RecordMainSection
          record={this.props.record}
          recordClass={this.props.recordClass}
          categories={this.props.recordClass.categories}
          attributes={attributes}
          tables={tables}
          collapsedCategories={this.props.collapsedCategories}
          collapsedTables={this.props.collapsedTables}
          onCategoryToggle={this.props.onCategoryToggle}
          onTableToggle={this.props.onTableToggle}
        />
      </div>
    );
  }
}

Record.propTypes = {
  record: PropTypes.object.isRequired,
  recordClass: PropTypes.object.isRequired,
  onCategoryToggle: PropTypes.func.isRequired
};


export default wrappable(Record);
