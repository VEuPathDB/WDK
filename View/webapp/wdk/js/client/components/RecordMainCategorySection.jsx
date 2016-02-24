import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import classnames from 'classnames';
import RecordAttribute from './RecordAttribute';
import RecordTable from './RecordTable';
import CollapsibleSection from './CollapsibleSection';
import { wrappable } from '../utils/componentUtils';
import {
  getId,
  getTargetType,
  getRefName,
  getDisplayName
} from '../utils/OntologyUtils';

let RecordMainCategorySection = React.createClass({

  propTypes: {
    isCollapsed: React.PropTypes.bool.isRequired,
    category: React.PropTypes.object.isRequired,
    depth: React.PropTypes.number,
    record: React.PropTypes.object.isRequired,
    recordClass: React.PropTypes.object.isRequired
  },

  mixins: [ PureRenderMixin ],

  toggleCollapse() {
    if (this.props.depth > 1) {
      let { category, isCollapsed } = this.props;
      this.props.onCategoryToggle(category, !isCollapsed);
    }
  },

  toggleTableCollapse(table, isCollapsed) {
    this.props.onTableToggle(table, !isCollapsed);
  },

  render() {
    let {
      category,
      depth,
      record,
      recordClass,
      attributes,
      tables,
      isCollapsed,
      collapsedTables,
      enumeration
    } = this.props;
    let targetType = getTargetType(category);

    if (targetType === 'attribute') {
      // render attribute
      let attribute = category.wdkReference;
      let { name, displayName } = attribute;
      let value = record.attributes[name]
      if (value == null) return null;
      return (
        <RecordAttribute
          {...attribute}
          value={value}
          record={record}
          recordClass={recordClass}
          id={name}
          className={`wdk-RecordAttributeSectionItem wdk-RecordAttributeSectionItem__${name}`}
        />
      );
    }

    if (targetType === 'table') {
      // render table
      let table = category.wdkReference;
      let { name, displayName } = table;
      let value = record.tables[name];

      if (value == null) return null;

      let isCollapsed = collapsedTables.includes(name)

      let wrapperClassBase = 'wdk-RecordTableWrapper';
      let wrapperClass = classnames(
        wrapperClassBase,
        `${wrapperClassBase}__${name}`
      );

      let headerClassBase = 'wdk-RecordTableHeader';
      let headerClass = classnames({
        [headerClassBase]: true,
        [`${headerClassBase}__collapsed`]: isCollapsed
      });

      return (
        <CollapsibleSection
          id={name}
          className="wdk-RecordTableContainer"
          headerContent={displayName}
          isCollapsed={collapsedTables.includes(name)}
          onCollapsedChange={() => this.toggleTableCollapse(table, isCollapsed)}
        >
          <RecordTable value={value} table={table} record={record} recordClass={recordClass}/>
        </CollapsibleSection>
      );
    }

    let id = getId(category);
    let categoryName = getDisplayName(category);
    let Header = 'h' + Math.min(depth + 1, 6);
    let headerContent = (
      <div>
        <span className="wdk-RecordSectionEnumeration">{enumeration}</span> {categoryName}
      </div>
    );
    return (
      <CollapsibleSection
        id={id}
        className={depth === 1 ? 'wdk-RecordSection' : 'wdk-RecordSubsection'}
        headerComponent={Header}
        headerContent={headerContent}
        isCollapsed={isCollapsed}
        onCollapsedChange={this.toggleCollapse}
      >
        {this.props.children}
      </CollapsibleSection>

    );
  }

});

export default wrappable(RecordMainCategorySection);
