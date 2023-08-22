import { NativeModules, DeviceEventEmitter } from 'react-native';

const Mediarecorder = NativeModules.Mediarecorder;

DeviceEventEmitter.addListener('error', (data: Object) => {
  if (typeof data === 'string') {
    console.error(data);
  } else {
    console.error(JSON.stringify(data));
  }
});

DeviceEventEmitter.addListener('info', (data: Object) => {
  if (typeof data === 'string') {
    console.info(data);
  } else {
    console.info(JSON.stringify(data));
  }
});

/*export function initMediaRecorder(): Promise<number> {
  return Mediarecorder.initMediaRecorder();
}*/

/*export function startRecord(): Promise<number> {
  return Mediarecorder.start();
}*/

/*export function stopRecord(): Promise<number> {
  return Mediarecorder.stop();
}*/

export default class MediaRecorder {
  public MediaRecorder() {

  }

  public initMediaRecorder(): Promise<number> {
    return Mediarecorder.initMediaRecorder();
  }

  public startRecord(): Promise<number> {
    return Mediarecorder.start();
  }

  public stopRecord(): Promise<number> {
    return Mediarecorder.stop();
  }

  /*public static sendEvent(type: string, data: string) {
    MediaRecorder.dispatchEvent(type, data);
  }*/
}
