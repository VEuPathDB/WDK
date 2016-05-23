import {AnswerSpec} from './WdkModel';

export interface User {
  id: number;
  firstName: string;
  middleName: string;
  lastName: string;
  organization: string;
  email: string;
}

export interface UserPreferences {
  [key: string]: string;
}

export interface Step {
  answerSpec: AnswerSpec;
  customName: string;
  description: string;
  displayName: string;
  estimatedSize: number;
  hasCompleteStepAnalyses: boolean;
  id: number;
  ownerId: number;
  recordClassName: string;
  shortDisplayName: string;
  strategyId: number;
}