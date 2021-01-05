# react-native-story-share

Share your images to instagram and snapchat stories.

| OS | Type | Supported |
|---|---|:---:|
| iOS | BASE64 | YES |
|  | FILE | YES |
| Android | BASE64 | YES |
|  | FILE | YES |

## Getting started

`$ yarn add react-native-story-share`

or

`$ npm install react-native-story-share --save`

## Installation
1. Either choose `Mostly automatic installation` or `Manual installation`
2. Follow the `Integration` guide

### Mostly automatic installation

`$ react-native link react-native-story-share`

### Manual installation

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-story-share` and add `RNStoryShare.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNStoryShare.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`

- Add `import com.jobeso.RNStorySharePackage;` to the imports at the top of the file
- Add `new RNStorySharePackage()` to the list returned by the `getPackages()` method

2. Append the following lines to `android/settings.gradle`:
   ```
   include ':react-native-story-share'
   project(':react-native-story-share').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-story-share/android')
   ```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
   ```
     compile project(':react-native-story-share')
   ```

## Integration

### Android

 + add snap client id
```diff
<application ...>
   ...

+   <meta-data android:name="com.snapchat.kit.sdk.clientId" android:value="your app’s client id" />

   ...
</application>
```

+ add snap sdk
```
maven { url "https://storage.googleapis.com/snap-kit-build/maven" }
```

### iOS

#### Swift

1. Under `Build Settings` section `Build Options` set `Always Embed Swift Started Libraries` to `true`
2. Make sure you have the following under `library search paths`

```
$(inherited)
$(TOOLCHAIN_DIR)/usr/lib/swift/$(PLATFORM_NAME)
```

#### Info.plist

+ add `instagram-stories` and `snapchat` to the `LSApplicationQueriesSchemes` key in your app's Info.plist.

```diff
...
<key>LSApplicationQueriesSchemes</key>
<array>
	...
+	<string>instagram-stories</string>
+	<string>snapchat</string>
</array>
...
```

#### snapchat
+ add `SCSDKClientId` to your `Info.plist`
+ add `pod 'SnapSDK', :subspecs => ['SCSDKCreativeKit']` to your Podfile with `use_frameworks!`
+ *optional* add [build script](https://docs.snapchat.com/docs/submitting) to reduce bundle size

## Usage

### Share To Instagram
```javascript
import RNStoryShare from "react-native-story-share";

RNStoryShare.isInstagramAvailable()
  .then(isAvailable => {

    if(isAvailable){
      RNStoryShare.shareToInstagram({
        type: RNStoryShare.BASE64, // or RNStoryShare.FILE
        attributionLink: 'https://myproject.com',
        backgroundAsset: 'data:image/png;base64,iVBO...',
        stickerAsset: 'data:image/png;base64,iVBO...',
        backgroundBottomColor: '#f44162',
        backgroundTopColor: '#f4a142'
      });
    }
  })
  .catch(e => console.log(e));
```

### Share To Snapchat
```javascript
import RNStoryShare from "react-native-story-share";

RNStoryShare.isSnapchatAvailable()
  .then(isAvailable => {

    if(isAvailable){
      RNStoryShare.shareToSnapchat({
        type: RNStoryShare.BASE64, // or RNStoryShare.FILE
        attributionLink: 'https://myproject.com',
        backgroundAsset: 'data:image/png;base64,iVBO...',
        stickerAsset: 'data:image/png;base64,iVBO...',
	captionText: 'text exemple',
	media: "photo" // or "video"
        stickerOptions: {
          height: 900,
          width: 900
        }
      });
    }
  })
  .catch(e => console.log(e));

```

## API

### Constants

| Name | Value |
|---|---|
| BASE64 | "base64" |
| FILE | "file" |

### Methods

#### `isInstagramAvailable(): Promise<boolean>`
Return a boolean indicating if Instagram is available on the phone.

#### `isSnapchatAvailable(): Promise<boolean>`
Return a boolean indicating if Snapchat is available on the phone.

#### `shareToInstagram(config: ShareConfigObject): Promise<void>`
```
type ShareConfigObject = {
  type: "base64" || "file",
	attributionLink: string,
	backgroundAsset: string,
	stickerAsset: string,
	stickerOptions: {
		height: integer,
		width: integer
	}
}
```
Shares a file or base64 image as background, sticker or both to Instagram. The background colors are only applyed when no background asset is set.

#### `shareToSnapchat(config: ShareConfigObject): Promise<void>`
```
type ShareConfigObject = {
  type: "base64" || "file",
	attributionLink: string,
	backgroundAsset: string,
	stickerAsset: string,
	backgroundBottomColor: string,
	backgroundTopColor: string
}
```
Shares a file or base64 image as background, sticker or both to Snapchat. `stickerOptions` are only supported by Android.
