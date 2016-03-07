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
          categories={this.props.categoryTree.children}
          attributes={attributes}
          tables={tables}
          collapsedSections={this.props.collapsedSections}
          onSectionToggle={this.props.onSectionToggle}
          onTableToggle={this.props.onTableToggle}
        />
      </div>
    );
  }
}

Record.propTypes = {
  record: PropTypes.object.isRequired,
  recordClass: PropTypes.object.isRequired,
  onSectionToggle: PropTypes.func.isRequired
};


export default wrappable(Record);
