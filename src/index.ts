import { NativeModulesProxy, EventEmitter, Subscription } from 'expo-modules-core';

// Import the native module. On web, it will be resolved to ExpoVideoConverter.web.ts
// and on native platforms to ExpoVideoConverter.ts
import ExpoVideoConverterModule from './ExpoVideoConverterModule';
import ExpoVideoConverterView from './ExpoVideoConverterView';
import { ChangeEventPayload, ExpoVideoConverterViewProps } from './ExpoVideoConverter.types';

// Get the native constant value.
export const PI = ExpoVideoConverterModule.PI;

export function hello(): string {
  return ExpoVideoConverterModule.hello();
}

export async function setValueAsync(value: string) {
  return await ExpoVideoConverterModule.setValueAsync(value);
}

const emitter = new EventEmitter(ExpoVideoConverterModule ?? NativeModulesProxy.ExpoVideoConverter);

export function addChangeListener(listener: (event: ChangeEventPayload) => void): Subscription {
  return emitter.addListener<ChangeEventPayload>('onChange', listener);
}

export { ExpoVideoConverterView, ExpoVideoConverterViewProps, ChangeEventPayload };
