
package com.jobeso;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.snap.creativekit.SnapCreative;
import com.snap.creativekit.api.SnapCreativeKitApi;
import com.snap.creativekit.exceptions.SnapMediaSizeException;
import com.snap.creativekit.media.SnapMediaFactory;
import com.snap.creativekit.media.SnapPhotoFile;
import com.snap.creativekit.media.SnapSticker;
import com.snap.creativekit.media.SnapVideoFile;
import com.snap.creativekit.models.SnapContent;
import com.snap.creativekit.models.SnapLiveCameraContent;
import com.snap.creativekit.models.SnapPhotoContent;
import com.snap.creativekit.models.SnapVideoContent;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.net.URL;

public class RNStoryShareModule extends ReactContextBaseJavaModule {
  private static final String FILE= "file";
  private static final String BASE64 = "base64";
  private static final String INTERNAL_DIR_NAME = "rnstoryshare";

  private static final String SUCCESS = "success";
  private static final String UNKNOWN_ERROR = "An unknown error occured in RNStoryShare";
  private static final String ERROR_TYPE_NOT_SUPPORTED = "Type not supported by RNStoryShare";
  private static final String ERROR_NO_PERMISSIONS = "Permissions Missing";
  private static final String TYPE_ERROR = "Type Error";
  private static final String MEDIA_TYPE_IMAGE = "image/*";
  private static final String MEDIA_TYPE_VIDEO = "video/*";
  private static final String PHOTO = "photo";
  private static final String VIDEO = "video";

  private static final String instagramScheme = "com.instagram.android";
  private static final String snapchatScheme = "com.snapchat.android";

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
    constants.put("FILE", FILE);
    constants.put("SUCCESS", SUCCESS);
    constants.put("UNKNOWN_ERROR", UNKNOWN_ERROR);
    constants.put("TYPE_ERROR", TYPE_ERROR);
    return constants;
  }

  private String generateFileName(){
    Random r = new Random();
    int hash = r.nextInt(999999);

    return "image-" + hash + ".png";
  }

  private String getFilePath() {
    String externalDir = this.getReactApplicationContext().getExternalCacheDir() + "/";
    String namespaceDir = externalDir + INTERNAL_DIR_NAME + "/";
    String fileName = generateFileName();

    File folder = new File(namespaceDir);

    if (!folder.exists()) {
      Boolean isCreated = folder.mkdir();

      if(!isCreated){
        return externalDir + fileName;
      }
    }

    return namespaceDir + fileName;
  }

  private static File createFile(final String path){
    final File file = new File(path);

    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    }

    return file;
  }

  private static File getSavedImageFileForBase64(final String path, final String data) {
    final byte[] imgBytesData = android.util.Base64.decode(data, android.util.Base64.DEFAULT);
    final File file = createFile(path);
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

  private File getFileFromBase64String(String base64ImageData){
    String backgroundAssetPath = getFilePath();
    String data = base64ImageData.substring(base64ImageData.indexOf(",") + 1);

    return getSavedImageFileForBase64(backgroundAssetPath, data);
  }

  private static void copyFile(File src, File dst) throws IOException {
    InputStream in = new FileInputStream(src);
    try {
      OutputStream out = new FileOutputStream(dst);
      try {
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
      } finally {
        out.close();
      }
    } finally {
      in.close();
    }
  }

    public String getBase64(String imagePath) {
        try {
            boolean isPNG = imagePath.contains(".png") ? true : false;

            InputStream in = new URL(imagePath).openConnection().getInputStream();

            Bitmap bm = BitmapFactory.decodeStream(in);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Bitmap.CompressFormat compressFormat = isPNG ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
            bm.compress(compressFormat, 100, baos);

            String base64prefix = String.format("data:image/%s;charset=utf-8;base64, ", isPNG ? "png" : "jpeg");
            return base64prefix + Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

  private void _shareToInstagram(
      @Nullable File backgroundFile,
      @Nullable File stickerFile,
      @Nullable String attributionLink,
      @Nullable String backgroundBottomColor,
      @Nullable String backgroundTopColor,
      String media,
      Promise promise
  ){
    try {
      Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
      String providerName = this.getReactApplicationContext().getPackageName() + ".fileprovider";
      Activity activity = getCurrentActivity();

      if (backgroundFile != null){
        Uri backgroundImageUri = FileProvider.getUriForFile(activity, providerName, backgroundFile);

        //intent.setDataAndType(backgroundImageUri, MEDIA_TYPE_IMAGE);
        intent.setDataAndType(backgroundImageUri, media.equals(VIDEO) ? MEDIA_TYPE_VIDEO : MEDIA_TYPE_IMAGE);
      } else {
        intent.setType(MEDIA_TYPE_IMAGE);
      }

      if(stickerFile != null){
        Uri stickerAssetUri = FileProvider.getUriForFile(activity, providerName, stickerFile);

        intent.putExtra("interactive_asset_uri", stickerAssetUri );
        activity.grantUriPermission(
                "com.instagram.android", stickerAssetUri , Intent.FLAG_GRANT_READ_URI_PERMISSION);
      }

      intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

      if(backgroundBottomColor != null){
        intent.putExtra("bottom_background_color", backgroundBottomColor);
      }

      if(backgroundTopColor != null){
        intent.putExtra("top_background_color", backgroundTopColor);
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
      String backgroundBottomColor = config.hasKey("backgroundBottomColor") ? config.getString("backgroundBottomColor") : null;
      String backgroundTopColor = config.hasKey("backgroundTopColor") ? config.getString("backgroundTopColor") : null;
      String stickerAsset = config.hasKey("stickerAsset") ? config.getString("stickerAsset") : null;
      String attributionLink = config.hasKey("attributionLink") ? config.getString("attributionLink") : null;
      String type = config.hasKey("type") ? config.getString("type") : FILE;
      String media = config.hasKey("media") ? config.getString("media") : PHOTO;

      if(backgroundAsset == null && stickerAsset == null){
        Error e = new Error("backgroundAsset and stickerAsset are not allowed to both be null.");
        promise.reject("Error in RNStory Share: No asset paths provided", e);
      }

      if (!media.equals(PHOTO) && !media.equals(VIDEO)) {
        throw new Error(ERROR_TYPE_NOT_SUPPORTED);
      }

      File backgroundFile = null;
      File stickerFile = null;

      switch(type){
        case BASE64: {
          if(backgroundAsset != null){
            backgroundFile = getFileFromBase64String(backgroundAsset);

            if(backgroundFile == null){
              throw new Error("Could not create file from Base64 in RNStoryShare");
            }
          }

          if(stickerAsset != null){
            stickerFile = getFileFromBase64String(stickerAsset);

            if(stickerFile == null){
              throw new Error("Could not create file from Base64 in RNStoryShare");
            }
          }
          break;
        }

        case FILE: {
          if (backgroundAsset != null) {
            backgroundFile = getFileFromBase64String(getBase64(backgroundAsset));

            if (backgroundFile == null) {
              throw new Error("Could not create file from Base64 in RNStoryShare");
            }
          }

          if (stickerAsset != null) {
            stickerFile = getFileFromBase64String(getBase64(stickerAsset));

            if (stickerFile == null) {
              throw new Error("Could not create file from Base64 in RNStoryShare");
            }
          }

          break;
        }

        default: {
          throw new Error(ERROR_TYPE_NOT_SUPPORTED);
        }
      }

      _shareToInstagram(backgroundFile, stickerFile, attributionLink, backgroundBottomColor, backgroundTopColor, media, promise);
    } catch (NullPointerException e){
      promise.reject(e.getMessage(), e);
    } catch (Exception e){
      promise.reject(UNKNOWN_ERROR, e);
    } catch(Error e) {
      promise.reject(e.getMessage(), e);
    }
  }

  private void _shareToSnapchat(
      @Nullable File backgroundFile,
      @Nullable File stickerFile,
      @Nullable ReadableMap stickerOptions,
      @Nullable String attributionLink,
      @Nullable String captionText,
      String media,
      Promise promise
  ) {
    try {
      Activity activity = getCurrentActivity();
      SnapMediaFactory snapMediaFactory = SnapCreative.getMediaFactory(activity);
      SnapContent snapContent;
      SnapCreativeKitApi snapCreativeKitApi = SnapCreative.getApi(activity);

      if (backgroundFile != null) {
          if (media.equals(PHOTO)) {
            SnapPhotoFile photoFile = snapMediaFactory.getSnapPhotoFromFile(backgroundFile);
            snapContent = new SnapPhotoContent(photoFile);
          } else {
            SnapVideoFile videoFile = snapMediaFactory.getSnapVideoFromFile(backgroundFile);
            snapContent = new SnapVideoContent(videoFile);
          }

      } else {
        snapContent = new SnapLiveCameraContent();
      }

      if(stickerFile != null){
        SnapSticker snapSticker = snapMediaFactory.getSnapStickerFromFile(stickerFile);

        if(stickerOptions != null){
          Integer width = stickerOptions.hasKey("width") ? stickerOptions.getInt("width") : null;
          Integer height = stickerOptions.hasKey("height") ? stickerOptions.getInt("height") : null;

          if(width != null){
            snapSticker.setWidth(width);
          }

          if(height != null){
            snapSticker.setHeight(height);
          }
        }

        snapContent.setSnapSticker(snapSticker);
      }

      if(attributionLink != null){
        snapContent.setAttachmentUrl(attributionLink);
      }

      if (captionText != null){
        snapContent.setCaptionText(captionText);
      }

      snapCreativeKitApi.send(snapContent);
      promise.resolve(SUCCESS);
    } catch (SnapMediaSizeException e) {
      promise.reject("RNStoryShare: Snapchat Exception", e.getMessage());
    } catch (Exception e){
      promise.reject(UNKNOWN_ERROR, e);
    }
  }

  @ReactMethod
  public void shareToSnapchat(ReadableMap config, Promise promise) {
    try {
      String backgroundAsset = config.hasKey("backgroundAsset") ? config.getString("backgroundAsset") : null;
      String stickerAsset = config.hasKey("stickerAsset") ? config.getString("stickerAsset") : null;
      ReadableMap stickerOptions = config.hasKey("stickerOptions") ? config.getMap("stickerOptions") : null;
      String attributionLink = config.hasKey("attributionLink") ? config.getString("attributionLink") : null;
      String captionText = config.hasKey("captionText") ? config.getString("captionText") : null;
      String type = config.hasKey("type") ? config.getString("type") : FILE;
      String media = config.hasKey("media") ? config.getString("media") : PHOTO;

      File backgroundFile = null;
      File stickerFile = null;

      if (!type.equals(BASE64) && !type.equals(FILE)){
        throw new Error(ERROR_TYPE_NOT_SUPPORTED);
      }

      if (!media.equals(PHOTO) && !media.equals(VIDEO)) {
        throw new Error(ERROR_TYPE_NOT_SUPPORTED);
      }

      if (backgroundAsset == null && stickerAsset == null){
        throw new Error("backgroundAsset and stickerAsset are not allowed to both be null.");
      }

      if (backgroundAsset != null){
        if (type.equals(BASE64)){
          backgroundFile = getFileFromBase64String(backgroundAsset);
        } else {
          backgroundFile = new File(backgroundAsset);
        }

        if(backgroundFile == null){
          throw new Error("Could not create file from Base64 in RNStoryShare");
        }
      }

      if (stickerAsset != null){
        stickerFile = getFileFromBase64String(stickerAsset);

        if(stickerFile == null){
          throw new Error("Could not create file from Base64 in RNStoryShare");
        }
      }

      _shareToSnapchat(backgroundFile, stickerFile, stickerOptions, attributionLink, captionText, media, promise);
    } catch (Error e){
      promise.reject(e.getMessage(), e);
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
