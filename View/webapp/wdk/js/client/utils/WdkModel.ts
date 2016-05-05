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