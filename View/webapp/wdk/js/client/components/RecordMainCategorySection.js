import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import classnames from 'classnames';
import get from 'lodash/object/get';
import RecordAttribute from './RecordAttribute';
import RecordTable from './RecordTable';
import { wrappable } from '../utils/componentUtils';

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
    let targetType = get(category, [ 'properties', 'targetType', 0 ]);

    if (targetType === 'attribute') {
      // render attribute
      let name = get(category, [ 'properties', 'name', 0 ]);
      let attribute = recordClass.attributes.find(a => a.name === name);
      if (attribute == null) throw new Error('Expected attribute for `' + name + '`, but got null');
      let { displayName } = attribute;
      let value = record.attributes[name]
      if (value == null) return null;
      return (
        <div className={`wdk-RecordAttributeSectionItem wdk-RecordAttributeSectionItem__${name}`} key={name}>
          <div className="wdk-RecordAttributeName">
            <strong>{displayName}</strong>
          </div>
          <div className="wdk-RecordAttributeValue">
            <RecordAttribute value={value} record={record} recordClass={recordClass}/>
          </div>
        </div>
      );
    }

    if (targetType === 'table') {
      // render table
      let name = get(category, [ 'properties', 'name', 0 ]);
      let tableMeta = recordClass.tables.find(t => t.name === name);
      if (tableMeta == null) throw new Error('Expected table for `' + name + '`, but got null');
      let { displayName } = tableMeta;
      let table = record.tables[name];

      if (table == null) return null;

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
        <div key={name} className={wrapperClass}>
          <div className={headerClass}
            onClick={() => this.toggleTableCollapse(tableMeta, isCollapsed)}>
            {' ' + displayName}
          </div>
          {isCollapsed? null : <RecordTable table={table} tableMeta={tableMeta} record={record} recordClass={recordClass}/>}
        </div>
      );
    }

    let categoryName = get(category, [ 'properties', 'label', 0 ]);
    let Header = 'h' + Math.min(depth + 1, 6);
    let headerClass = classnames({
      'wdk-RecordSectionHeader': depth === 1,
      'wdk-RecordSectionSubHeader': depth !== 1,
      'wdk-RecordSectionSubHeader__collapsed': depth !== 1 && isCollapsed
    });

    return isCollapsed && depth === 1 ? null : (
      <div id={String(categoryName)} className="wdk-RecordSection">
        <Header className={headerClass} onClick={this.toggleCollapse}>
          <span className="wdk-RecordSectionEnumeration">{enumeration}</span> {categoryName}
        </Header>
        {isCollapsed || this.props.children}
      </div>
    );
  }

});

export default wrappable(RecordMainCategorySection);
