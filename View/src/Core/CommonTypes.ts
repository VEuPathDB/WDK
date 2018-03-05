import WdkStore from "Core/State/Stores/WdkStore";
import WdkDispatcher, { Action } from "Core/State/Dispatcher";
import GlobalDataStore from "Core/State/Stores/GlobalDataStore";
import { ActionCreator, ActionCreatorResult, ActionCreatorServices } from 'Utils/ActionCreatorUtils';
import { RouteComponentProps } from "react-router";
import { History } from 'history';
import AbstractViewController from 'Core/Controllers/AbstractViewController';
import { ComponentType } from "react";


export interface StoreConstructor<T extends WdkStore> {
  new(dispatcher: WdkDispatcher, channel: string, globalDataStore: GlobalDataStore, services: ActionCreatorServices): T;
}

export interface DispatchAction {
  (action: ActionCreatorResult<Action>): any;
}

export interface MakeDispatchAction {
  (channel: string): DispatchAction
}

export interface Constructor<T> {
  new(...args: any[]): T;
}

export interface Container<T> {
  get(Class: Constructor<T>): T;
}

export interface ViewControllerProps<Store> {
  stores: Container<Store>;
  makeDispatchAction: MakeDispatchAction;
}

export type AbstractViewControllerClass = typeof AbstractViewController;

export interface RouteSpec {
  path: string;
  component: ComponentType<ViewControllerProps<WdkStore>>
}
