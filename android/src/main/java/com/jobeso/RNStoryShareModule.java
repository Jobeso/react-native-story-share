
package com.jobeso;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.snapchat.kit.sdk.SnapCreative;
import com.snapchat.kit.sdk.creative.api.SnapCreativeKitApi;
import com.snapchat.kit.sdk.creative.exceptions.SnapMediaSizeException;
import com.snapchat.kit.sdk.creative.media.SnapMediaFactory;
import com.snapchat.kit.sdk.creative.media.SnapPhotoFile;
import com.snapchat.kit.sdk.creative.models.SnapPhotoContent;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RNStoryShareModule extends ReactContextBaseJavaModule {
  private static final  String FILE_PATH= "file_path";
  private static final  String  BASE64 = "base64";
  private static final String DEFAULT_IMAGE_NAME = "rnstoryshare.png";

  private static final String SUCCESS = "success";
  private static final String UNKNOWN_ERROR = "An unknown error occured in RNStoryShare";
  private static final String ERROR_TYPE_NOT_SUPPORTED = "Type not supported by RNStoryShare";
  private static final String TYPE_ERROR = "Type Error";
  private static final String MEDIA_TYPE_IMAGE = "image/*";

  private static final  String  instagramScheme = "com.instagram.android";
  private static final  String  snapchatScheme = "snapchat://";

  private final ReactApplicationContext reactContext;

  public RNStoryShareModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNStoryShare";
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put("BASE64", BASE64);
    constants.put("FILE_PATH", FILE_PATH);
    constants.put("SUCCESS", SUCCESS);
    constants.put("UNKNOWN_ERROR", UNKNOWN_ERROR);
    constants.put("TYPE_ERROR", TYPE_ERROR);
    return constants;
  }

  private String getFilePath(String imageName) {
    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + imageName;
  }

  private static File getSavedImageFile(final String imageData, final String imagePath) {
    final byte[] imgBytesData = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT);

    final File file = new File(imagePath);

    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    }

    final FileOutputStream fileOutputStream;
    try {
      fileOutputStream = new FileOutputStream(file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }

    final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
    try {
      bufferedOutputStream.write(imgBytesData);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    } finally {
      try {
        bufferedOutputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return file;
  }

  private File legacy_getFileFromBase64String(String base64ImageData){
    String backgroundAssetPath = getFilePath(DEFAULT_IMAGE_NAME);
    String ct = base64ImageData.substring(base64ImageData.indexOf(",") + 1);
    File file = getSavedImageFile(ct, backgroundAssetPath);

    return file;
  }

  private Uri legacy_getUriForBase64Image(String base64ImageData){
    File file = legacy_getFileFromBase64String(base64ImageData);
    Activity activity = getCurrentActivity();
    String packageName = this.getReactApplicationContext().getPackageName();

    Uri imageUri = FileProvider.getUriForFile(activity,
            packageName + ".provider", file);

    return imageUri;
  }

  private void _shareToInstagram(String backgroundAsset, String stickerAsset, String attributionLink, Promise promise){
    try {
      Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
      intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      Activity activity = getCurrentActivity();

      if(backgroundAsset != null) {
        // TODO deprecate
        Uri backgroundImageUri = legacy_getUriForBase64Image(backgroundAsset);
        intent.setDataAndType(backgroundImageUri, MEDIA_TYPE_IMAGE);

        // TODO investigate solution without creating image
        // intent.setDataAndNormalize(Uri.parse(backgroundAsset));
      }

      if(stickerAsset != null){
        Uri stickerAssetUri = Uri.parse(stickerAsset);

        intent.putExtra("interactive_asset_uri", stickerAssetUri);
        activity.grantUriPermission(
                "com.instagram.android", stickerAssetUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
      }

      if(attributionLink != null){
        intent.putExtra("content_url", attributionLink);
      }



      if (activity.getPackageManager().resolveActivity(intent, 0) != null) {
        activity.startActivityForResult(intent, 0);
        promise.resolve(SUCCESS);
      }else{
        throw new Exception("Couldn't open intent");
      }
    }catch (Exception e){
      promise.reject(UNKNOWN_ERROR, e);
    }
  }

  @ReactMethod
  public void shareToInstagram(ReadableMap config, Promise promise){
    try{
      String backgroundAsset = config.hasKey("backgroundAsset") ? config.getString("backgroundAsset") : null;
      String stickerAsset = config.hasKey("stickerAsset") ? config.getString("stickerAsset") : null;
      String attributionLink = config.hasKey("attributionLink") ? config.getString("attributionLink") : null;

      if(backgroundAsset == null && stickerAsset == null){
        Error e = new Error("backgroundAsset and stickerAsset are not allowed to both be null.");
        promise.reject("Error in RNStory Share: No asset paths provided", e);
      }

      _shareToInstagram(backgroundAsset, stickerAsset, attributionLink, promise);
    }catch (Exception e){
      promise.reject(UNKNOWN_ERROR, e);
    }
  }

  @ReactMethod
  public void shareToSnapchat(ReadableMap config, Promise promise) {
    try {
      String backgroundAsset = config.hasKey("backgroundAsset") ? config.getString("backgroundAsset") : null;
      String stickerAsset = config.hasKey("stickerAsset") ? config.getString("stickerAsset") : null;
      String attributionLink = config.hasKey("attributionLink") ? config.getString("attributionLink") : null;
      String type = config.hasKey("type") ? config.getString("type") : null;

      if(type == null || type != BASE64 ){
        Exception e =  new Exception(ERROR_TYPE_NOT_SUPPORTED);
        promise.reject(TYPE_ERROR, e);
      }

      if(backgroundAsset == null && stickerAsset == null){
        Error e = new Error("backgroundAsset and stickerAsset are not allowed to both be null.");
        promise.reject("Error in RNStory Share: No asset paths provided", e);
      }

      File file = legacy_getFileFromBase64String(backgroundAsset);

      Activity activity = getCurrentActivity();
      SnapMediaFactory snapMediaFactory = SnapCreative.getMediaFactory(activity);
      SnapPhotoFile photoFile;

      SnapCreativeKitApi snapCreativeKitApi = SnapCreative.getApi(activity);
      photoFile = snapMediaFactory.getSnapPhotoFromFile(file);
      SnapPhotoContent snapPhotoContent = new SnapPhotoContent(photoFile);
      snapPhotoContent.setAttachmentUrl(attributionLink);
      snapCreativeKitApi.send(snapPhotoContent);

      promise.resolve(SUCCESS);
    } catch (SnapMediaSizeException e) {
      promise.reject("RNStoryShare: Snapchat Exception", e.getMessage());
    } catch (Exception e){
      promise.reject(UNKNOWN_ERROR, e);
    }
  }


  private void canOpenUrl(String packageScheme, Promise promise){
    try{
      Activity activity = getCurrentActivity();
      activity.getPackageManager().getPackageInfo(packageScheme, PackageManager.GET_ACTIVITIES);
      promise.resolve(true);
    } catch (PackageManager.NameNotFoundException e) {
      promise.resolve(false);
    } catch (Exception e) {
      promise.reject(new JSApplicationIllegalArgumentException(
              "Could not check if URL '" + packageScheme + "' can be opened: " + e.getMessage()));
    }
  }

  @ReactMethod
  public void isInstagramAvailable(Promise promise){
    canOpenUrl(instagramScheme, promise);
  }

  @ReactMethod
  public void isSnapchatAvailable(Promise promise){
    canOpenUrl(snapchatScheme, promise);
  }
}
