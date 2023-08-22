# mediarecorder

react native media recorder

## Usage

```js
import MediaRecorder from 'mediarecorder';
```

```js
MediaRecorder.initMediaRecorder()
      .then(() => {
        console.info('mediaRecorder is setup');
      })
      .catch((e) => {
        console.error('error : ', e);
      });
```

then

```js
MediaRecorder.start().then(() => {
      console.info('start');
    });

MediaRecorder.stop().then(() => {
      console.info('start');
    });
```

## Installation for dev

On root repertory

```sh
npm install
```

Run the example app

```sh
cd example/ && npx react-native start
```

### Error SDK not found
Run the command and attribute ANDROID_HOME variable :

```sh
ANDROID_HOME=/path/to/android/Sdk npx react-native start
```

## Error

See [issues page](https://github.com/SavinienBarbotaud/mediaRecorder/issues) even for questions

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
