/**
 * Type definitions for WDK Model entities
 */

interface ModelEntity {
  name: string;
  displayName: string;
  properties?: { [key: string]: string };
}

export interface RecordClass extends ModelEntity {
  description: string;
  attributes: AttributeField[];
  tables: TableField[];
  attributesMap: Map<string, AttributeField>;
  tablesMap: Map<string, TableField>;
}

export interface Question extends ModelEntity {
  description: string;
  shortDisplayName: string;
  help: string;
  newBuild: string;
  reviseBuild: string;
  urlSegment: string;
  class: string;
  parameters: QuestionParameter[];
  defaultAttributes: string[];
  dynamicAttributes: AttributeField[];
  defaultSummaryView: string;
  summaryViewPlugins: string[];
  stepAnalysisPlugins: string[];
}

export interface QuestionParameter extends ModelEntity {
  help: string;
  type: string;
  isVisible: boolean;
  group: string;
  isReadOnly: boolean;
  defaultValue: string;
}

export interface AttributeField extends ModelEntity {
  help: string;
  align: string;
  isSortable: boolean;
  isRemovable: boolean;
  type: string;
  truncateTo: number;
}

export interface TableField extends ModelEntity {
  help: string;
  type: string;
  description: string;
  attributes: AttributeField[];
}

export interface Record {
  displayName: string;
  overview?: string;
  id: PrimaryKey;
  recordClassName: string;
  attributes: { [key: string]: AttributeValue };
  tables: { [key: string]: TableValue };
}

export interface PrimaryKey extends Array<{
  name: string;
  value: string;
}> {}

export type AttributeValue = string & LinkAttributeValue;

export interface LinkAttributeValue {
  url: string;
  displayText: string;
}

export interface TableValue {
  [index: number]: AttributeValue;
}

export interface Answer {
  records: Record[];
  meta: {
    attributes: AttributeField[];
    tables: TableField[];
    recordClassName: string;
    responseCount: number;
    totalCount: number;
  }
}

export interface AnswerSpec {
  questionName: string;
  parameters?: { [key: string]: string };
  legacyFilterName?: string;
  filters?: { name: string; value: string; }[];
  viewFilters?: { name: string; value: string; }[];
  wdk_weight?: number;
}

export interface AnswerFormatting {
  pagination: { offset: number; numRecords: number; };
  attributes: string[] | '__ALL_ATTRIBUTES__' | '__DISPLAYABLE_ATTRIBUTES__';
  tables: string[] | '__ALL_TABLES__' | '__DISPLAYABLE_TABLES__';
  sorting: [ { attributeName: string; direction: 'ASC' | 'DESC' } ];
  contentDisposition?: 'inline' | 'attatchment';
}