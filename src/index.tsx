import { NativeModules, DeviceEventEmitter } from 'react-native';

const Mediarecorder = NativeModules.Mediarecorder;

DeviceEventEmitter.addListener('error', (data: Object) => {
  if (typeof data === 'string') {
    console.info(data);
  } else {
    console.info(JSON.stringify(data));
  }
});

DeviceEventEmitter.addListener('info', (data: Object) => {
  if (typeof data === 'string') {
    console.info(data);
  } else {
    console.info(JSON.stringify(data));
  }
});

export function multiply(a: number, b: number): Promise<number> {
  return Mediarecorder.multiply(a, b);
}

export function initMediaRecorder(): Promise<number> {
  return Mediarecorder.initMediaRecorder();
}

export function startRecord(): Promise<number> {
  return Mediarecorder.start();
}

export function stopRecord(): Promise<number> {
  return Mediarecorder.stop();
}
