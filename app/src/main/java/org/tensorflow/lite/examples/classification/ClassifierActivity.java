/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.classification;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Toast;
import android.os.Environment;
import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

//import com.chaquo.python.Kwarg;
//import com.chaquo.python.PyObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.examples.classification.env.BorderedText;
import org.tensorflow.lite.examples.classification.env.Logger;
import org.tensorflow.lite.examples.classification.tflite.Classifier;
import org.tensorflow.lite.examples.classification.tflite.Classifier.Device;
import org.tensorflow.lite.examples.classification.tflite.Classifier.Model;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class ClassifierActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();
  private static final Size DESIRED_PREVIEW_SIZE = new Size(3840, 2160);
  private static final float TEXT_SIZE_DIP = 10;
  private Bitmap rgbFrameBitmap = null;
  private long lastProcessingTimeMs;
  private Integer sensorOrientation;
  //private Classifier classifier;
  private BorderedText borderedText;
  private org.tensorflow.lite.examples.classification.ml.Model myModel;
  /** Input image size of the model along x axis. */
  //private int imageSizeX;
  /** Input image size of the model along y axis. */
  //private int imageSizeY;
  private String result;
  //private byte[] result_bytes;
  private int count = 0;

  public native String BCHDecode(byte[] data,byte[] ecc);

  static {
    System.loadLibrary("bch");
  }

  @Override
  protected int getLayoutId() {
    return R.layout.tfe_ic_camera_connection_fragment;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    recreateClassifier();
//    if (classifier == null) {
//      LOGGER.e("No classifier on preview!");
//      return;
//    }
    if (myModel == null) {
      LOGGER.e("No myModel on preview!");
      return;
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
  }

  @Override
  protected void processImage() {
    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
    final int cropSize = Math.min(previewWidth, previewHeight);

    runInBackground(
            new Runnable() {
              @Override
              public void run() {
                if (myModel != null) {
                  //final long startTime = SystemClock.uptimeMillis();
                  //final List<Classifier.Recognition> results =
                          //classifier.recognizeImage(rgbFrameBitmap, sensorOrientation);
                  //lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                  //LOGGER.v("Detect: %s", results);

                  Matrix matrix=new Matrix();
                  matrix.postRotate(90);
                  Bitmap rgbFrameBitmapRotated=Bitmap.createBitmap(rgbFrameBitmap,0,0,rgbFrameBitmap.getWidth(), rgbFrameBitmap.getHeight(),matrix,true);

                  Log.v("getWidth: ", Integer.toString(rgbFrameBitmapRotated.getWidth()));
                  Log.v("getHeight: ", Integer.toString(rgbFrameBitmapRotated.getHeight()));

                  int viewWidth = rgbFrameBitmapRotated.getWidth();
                  int viewHeight = rgbFrameBitmapRotated.getHeight();
                  int boxWidth = viewWidth / 4;
                  int boxHeight = boxWidth*3/2;

                  Log.v("boxWidth: ", Integer.toString(boxWidth));
                  Log.v("boxHeight: ", Integer.toString(boxHeight));

                  Bitmap resizedBmp = Bitmap.createBitmap(rgbFrameBitmapRotated,
                          viewWidth/2 - boxWidth, viewHeight/2 - boxHeight, boxWidth*2, boxHeight*2);
                  Bitmap bitmap = Bitmap.createScaledBitmap(resizedBmp, 320, 480, true);

                  //saveBitmap("rgbFrameBitmapRotated.png",rgbFrameBitmapRotated,this);
//                  try {
//                    FileOutputStream fileOutputStream = context.openFileOutput("test.png", Context.MODE_PRIVATE);
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
//                    fileOutputStream.close();
//                  } catch (Exception e) {
//                    e.printStackTrace();
//                  }

                  //bitmapToFile(context,rgbFrameBitmapRotated,"rgbFrameBitmapRotated"+Integer.toString(count)+".png");
                  //bitmapToFile(context,bitmap,"bitmap"+Integer.toString(count)+".png");
                  //bitmapToFile(context,resizedBmp,"resizedBmp"+Integer.toString(count)+".png");
                  //count++;


                  ByteBuffer input = ByteBuffer.allocateDirect(320 * 480 * 3 * 4).order(ByteOrder.nativeOrder());
                  for (int y = 0; y < 480; y++) {
                    for (int x = 0; x < 320; x++) {
                      int px = bitmap.getPixel(x, y);

                      // Get channel values from the pixel value.
                      int r = Color.red(px);
                      int g = Color.green(px);
                      int b = Color.blue(px);

                      // Normalize channel values to [0.0, 1.0].
                      float rf = (r) / 255.0f;
                      float gf = (g) / 255.0f;
                      float bf = (b) / 255.0f;

                      input.putFloat(rf);
                      input.putFloat(gf);
                      input.putFloat(bf);
                    }
                  }

                  //Log.v("input: ", input.toString());
                  //TensorImage tensorImage = TensorImage.fromBitmap(rgbFrameBitmap);
                  TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 480, 320, 3}, DataType.FLOAT32);
                  //ByteBuffer input = tensorImage.getBuffer();
                  //Log.v("input: ", Float.toString(input.getFloat()));

                  inputFeature0.loadBuffer(input);

                  // Runs model inference and gets result.
                  final long startTime = SystemClock.uptimeMillis();
                  org.tensorflow.lite.examples.classification.ml.Model.Outputs outputs = myModel.process(inputFeature0);
                  lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                  TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                  float[] data=outputFeature0.getFloatArray();
                  float[] testGT={0,1,0,0,1,0,0,0,0,1,1,0,0,1,0,1,0,1,1,0,1,1,0,0,0,1,1,0,1,1,0,0,0,1,1,0,1,1,1,1,0,0,1,0,0,0,0,
                          0,0,0,1,0,0,0,0,0,1,1,0,0,0,1,0,1,0,0,0,1,0,0,0,0,1,1,1,1,1,0,1,1,1,1,1,0,0,0,1,0,1,1,1,0,0,0,0,0};
                  //int[] data_int = new int[data.length];
                  byte[] data_byte= new byte[7];
                  byte[] ecc_byte= new byte[5];
                  int data_length = 56;
                  int ecc_length = 40;
                  for (int i = 0 ; i < data_length; i+=8)
                  {
                    String tmp="";
                    for(int j =i;j<i+8;j++)
                    {
                      tmp+= Integer.toString((int)data[j]);
                    }
                    //Log.i("Decode", tmp);
                    int tmpInt = Integer.parseInt(tmp,2);
                    data_byte[i/8]=(byte) tmpInt;
                  }
                  for (int i = 56 ; i < data_length+ecc_length-1; i+=8)
                  {
                    String tmp="";
                    for(int j =i;j<i+8;j++)
                    {
                      tmp+= Integer.toString((int)data[j]);
                    }
                    //Log.i("Decode", tmp);
                    int tmpInt = Integer.parseInt(tmp,2);
                    //ecc_byte[i/8]=Byte.parseByte(tmp, 2);
                    ecc_byte[(i-56)/8]=(byte) tmpInt;
                    //String s1 = String.format("%8s", Integer.toBinaryString((byte) tmpInt & 0xFF)).replace(' ', '0');
                    //Log.i("Decode", s1);
                  }
                  //Log.i("Decode", Float.toString(testGT[0]));
                    //Log.i("Decode", Arrays.toString(data_byte));
                    //Log.i("Decode", Arrays.toString(ecc_byte));
                    //Log.i("Decode", Arrays.toString(data));


                  result = BCHDecode(data_byte,ecc_byte);
                  //Log.i("Decode", Arrays.toString(data_byte));
                  //Log.i("Decode", Arrays.toString(ecc_byte));
                  //Log.v("Decode", Integer.toString(data.length));

                  if(result.length()!=0)
                  {
                      for (int s_length = 0 ; s_length < result.length(); s_length+=1)
                      {
                          int ascii = result.charAt(s_length);
                        //Log.i("JNI1", String.valueOf(ascii));
                        if(((ascii>=65&&ascii<=90)||(ascii>=97&&ascii<=122)||(ascii==32)))
                        {
                        }
                        else
                        {
                            result="Failed to decode";
                            break;
                        }
                      }
                    Log.i("JNI1", result);
                    //Log.i("JNI", String.valueOf(result.length()));
                  }
                  else
                  {
                    result="Failed to decode";
                    Log.i("JNI2", "Failed to decode");
                  }
                  //LOGGER.v("Decode: %f", data[0]);

                  // Call Python Bch Decode
                  //PyObject obj1 = py.getModule("BchDecode").callAttr("BchDecode",new Kwarg("secretList", data));
                  //String result = obj1.toJava(String.class);
                  //Log.i("result", result);


                  runOnUiThread(
                          new Runnable() {
                            @Override
                            public void run() {
                              showResultsInBottomSheet(result);
                              //showFrameInfo(previewWidth + "x" + previewHeight);
                              //showCropInfo(imageSizeX + "x" + imageSizeY);
                              //showCameraResolution(cropSize + "x" + cropSize);
                              //showRotationInfo(String.valueOf(sensorOrientation));
                              showInference(String.format("%.2f", (1.0f / (lastProcessingTimeMs/1000.0f))) );
                            }
                          });
                }
                readyForNextImage();
              }
            });
  }

  @Override
  protected void onInferenceConfigurationChanged() {
    if (rgbFrameBitmap == null) {
      // Defer creation until we're getting camera frames.
      return;
    }
    //final Device device = getDevice();
    //final Model model = getModel();
    final int numThreads = getNumThreads();
    runInBackground(() -> recreateClassifier());
  }

  private void recreateClassifier() {
//    if (classifier != null) {
//      LOGGER.d("Closing classifier.");
//      classifier.close();
//      classifier = null;
//    }
    if (myModel != null) {
      LOGGER.d("Closing myModel.");
      myModel.close();
      myModel = null;
    }

//    if (device == Device.GPU
//            && (model == Model.QUANTIZED_MOBILENET || model == Model.QUANTIZED_EFFICIENTNET)) {
//      LOGGER.d("Not creating classifier: GPU doesn't support quantized models.");
//      runOnUiThread(
//              () -> {
//                Toast.makeText(this, R.string.tfe_ic_gpu_quant_error, Toast.LENGTH_LONG).show();
//              });
//      return;
//    }
//    try {
//      LOGGER.d(
//              "Creating classifier (model=%s, device=%s, numThreads=%d)", model, device, numThreads);
//      classifier = Classifier.create(this, model, device, numThreads);
//    } catch (IOException | IllegalArgumentException e) {
//      LOGGER.e(e, "Failed to create classifier.");
//      runOnUiThread(
//              () -> {
//                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
//              });
//      return;
//    }

    try {
      LOGGER.d(
              "Creating myModel ");
      myModel = org.tensorflow.lite.examples.classification.ml.Model.newInstance(this);
    } catch (IOException | IllegalArgumentException e) {
      LOGGER.e(e, "Failed to create myModel.");
      runOnUiThread(
              () -> {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
              });
      return;
    }

    // Updates the input image size.
//    imageSizeX = classifier.getImageSizeX();
//    imageSizeY = classifier.getImageSizeY();
  }

  public static File bitmapToFile(Context context,Bitmap bitmap, String fileNameToSave) { // File name like "image.png"
    //create a file to write bitmap data
    File file = null;
    try {
      file = new File(Environment.getExternalStorageDirectory() + File.separator + fileNameToSave);
      Log.v("file",Environment.getExternalStorageDirectory().getPath());
      file.createNewFile();

//Convert bitmap to byte array
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.PNG, 0 , bos); // YOU can also save it in JPEG
      byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(bitmapdata);
      fos.flush();
      fos.close();
      return file;
    }catch (Exception e){
      e.printStackTrace();
      return file; // it will return null
    }
  }

}
