declare module 'Components/Mesa';

// Type definitions for legacy code base
// -------------------------------------

declare const wdk: {
  namespace(nsString: string, nsFactory: (ns: any) => any): void;
}

declare const wdkConfig: {
  readonly wdkServiceUrl: string;
}
