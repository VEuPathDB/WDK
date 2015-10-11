import { Component, PropTypes } from 'react';
import RecordMainSection from './RecordMainSection';
import RecordHeading from './RecordHeading';
import { wrappable } from '../utils/componentUtils';


class Record extends Component {

  render() {
    let { attributeCategories: categories, tables, attributes } = this.props.recordClass;
    return (
      <div className="wdk-Record">
        <RecordHeading {...this.props}/>
        <RecordMainSection
          record={this.props.record}
          recordClass={this.props.recordClass}
          categories={categories}
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
