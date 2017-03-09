/**
 * Type definitions for WDK Model entities
 */

interface ModelEntity {
  name: string;
  displayName: string;
  properties?: Record<string, string>;
}

export interface RecordClass extends ModelEntity {
  recordIdAttributeName: string;
  primaryKeyColumnRefs: string[];
  description: string;
  urlSegment: string;
  attributes: AttributeField[];
  tables: TableField[];
  attributesMap: Record<string, AttributeField>;
  tablesMap: Record<string, TableField>;
  formats: Reporter[];
  useBasket: boolean;
}

export interface Reporter {
  name: string;
  displayName: string;
  description: string;
  isInReport: boolean;
  scopes: string[];
}

export interface Question extends ModelEntity {
  description: string;
  shortDisplayName: string;
  recordClassName: string;
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

export interface RecordInstance {
  displayName: string;
  id: PrimaryKey;
  recordClassName: string;
  attributes: Record<string, AttributeValue>;
  tables: Record<string, TableValue> & {
    _errors: string[];
  };
}

export interface PrimaryKey extends Array<{
  name: string;
  value: string;
}> {}

export type AttributeValue = string | LinkAttributeValue;

export interface LinkAttributeValue {
  url: string;
  displayText: string;
}

export interface TableValue extends Array<AttributeValue> { }

export interface Answer {
  records: RecordInstance[];
  meta: {
    attributes: string[];
    tables: string[];
    recordClassName: string;
    responseCount: number;
    totalCount: number;
  }
}

export interface AnswerSpec {
  questionName: string;
  parameters?: Record<string, string>;
  legacyFilterName?: string;
  filters?: { name: string; value: string; }[];
  viewFilters?: { name: string; value: string; }[];
  wdk_weight?: number;
}

export interface AnswerFormatting {
  format: string
  formatConfig: {}
}

export type UserDatasetMeta = {
  description: string;
  name: string;
  summary: string;
};

export type UserDatasetShare = {
  time: number;
  user: number;
  userDisplayName: string;
};

export type UserDataset = {
  created: number;
  isInstalled: boolean;
  dependencies: Array<{
    resourceDisplayName: string;
    resourceIdentifier: string;
    resourceVersion: string;
  }>;
  datafiles: Array<{
    name: string;
    size: number;
  }>;
  projects: string[];
  id: number;
  meta: UserDatasetMeta;
  modified: number;
  owner: string;
  ownerUserId: number;
  percentQuotaUsed: number;
  sharedWith: UserDatasetShare[];
  questions: string[];
  size: number;
  type: {
    name: string;
    version: string;
  };
  updloaded: number;
}

