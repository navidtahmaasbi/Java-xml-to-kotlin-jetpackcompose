package com.azarpark.watchman.detection;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.support.image.TensorImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AOCR_Analog_TF {
    private String TAG = "Ocr model";
    private double accepted_prob;
    private final CompatibilityList compatList = new CompatibilityList();
    private String decode;
    private int decode_length;
    private Dictionary_char dictionary_char;
    boolean dyanamic;
    private int height;
    private int max_heigth;
    private float[][] out1 = null;
    private long[][] out2 = null;
    private float[][] out2_per = null;
    private Map<Integer, Object> outputs = null;
    private ArrayList<String> plate_list;
    private float prob;
    IntBuffer[] results;
    protected Interpreter tflite;
    private MappedByteBuffer tfliteModel;
    private final Interpreter.Options tfliteOptions;
    private TensorImage timage;

    public AOCR_Analog_TF(Activity activity, int i, int i2, int i3, int i4, String str, boolean z, Context context) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        this.tfliteOptions = options;
        options.setNumThreads(2);
        try {

//            Util util = new Util(context);
//            File result = util.read("model1.tflite");
            this.tfliteModel = loadModelFile(activity, i4);
            this.tflite = new Interpreter(this.tfliteModel, options);
//            result.delete();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(this.TAG, "Ocr model is not Load");
            Toast.makeText(activity, "Ocr model is not load", Toast.LENGTH_LONG).show();
        }
//        catch (GeneralSecurityException e) {
//            e.printStackTrace();
//        }
        this.height = i;
        this.timage = new TensorImage(DataType.FLOAT32);
        this.out1 = (float[][]) Array.newInstance(float.class, new int[]{1, 1});
        int[] iArr = new int[2];
        iArr[1] = i3;
        iArr[0] = 1;
        this.out2 = (long[][]) Array.newInstance(long.class, iArr);
        this.outputs = new HashMap();
        this.dictionary_char = new Dictionary_char();
        this.outputs.put(0, this.out1);
        this.results = new IntBuffer[3];
        this.plate_list = new ArrayList<>();
        this.decode_length = i3;
        this.decode = str;
        this.max_heigth = i2;
        this.dyanamic = z;
//        this.accepted_prob = Double.parseDouble(CplusConfig.getInstance().Get_Config("accepted_prob"));
        this.accepted_prob = 0.5;
    }

    public ArrayList<String> recongnized(List<Mat> list) {
        int i;
        String str;
        ArrayList<String> arrayList = new ArrayList<>();
        if (this.tflite == null) {
            Log.e(this.TAG, "Image classifier has not been initialized; Skipped.");
        } else {
            Log.e(this.TAG, "numerator size " + Float.toString((float) list.size()));
            for (int i2 = 0; i2 < list.size(); i2++) {
                Mat mat = list.get(i2);
                mat.cols();
                mat.rows();
                Log.e(this.TAG, "img size " + Float.toString((float) mat.rows()) + "---" + Float.toString((float) mat.cols()));
                float rows = (float) ((((double) this.height) * 1.0d) / ((double) mat.rows()));
                if (this.dyanamic) {
                    i = (int) Math.min((float) this.max_heigth, rows * ((float) mat.cols()));
                } else {
                    i = this.max_heigth;
                }
                Mat mat2 = new Mat();
                Imgproc.resize(mat, mat2, new Size((double) i, (double) this.height));
                mat2.convertTo(mat2, CvType.CV_32FC3, 0.00392156862745098d);
                int[] iArr = {1, this.height, i, 3};
                if (this.dyanamic) {
                    this.tflite.resizeInput(0, iArr);
                }
                float[] fArr = new float[(this.height * i * 3)];
                mat2.get(0, 0, fArr);
                ByteBuffer allocateDirect = ByteBuffer.allocateDirect(this.decode_length * 4);
                allocateDirect.order(ByteOrder.nativeOrder());
                IntBuffer asIntBuffer = allocateDirect.asIntBuffer();
                asIntBuffer.rewind();
                this.outputs.put(1, asIntBuffer);
                this.timage.load(fArr, iArr);
                try {
                    this.tflite.runForMultipleInputsOutputs(new Object[]{this.timage.getBuffer()}, this.outputs);
                } catch (Exception e) {
                    Log.e(this.TAG, e.toString());
                    arrayList.add("");
                }
                this.prob = this.out1[0][0];
                Log.e(this.TAG, "prob " + Float.toString(this.out1[0][0]));
                if (((double) Math.abs(this.out1[0][0])) < this.accepted_prob) {
                    str = "";
                    for (int i3 = 0; i3 < this.decode_length; i3++) {
                        if (!(asIntBuffer.get(i3) == -1 || asIntBuffer.get(i3) == 0)) {
                            int i4 = asIntBuffer.get(i3);
                            if (i3 == 2 && i4 > 12) {
                                str = str + this.decode.charAt(asIntBuffer.get(i3));
                            }
                            if (i3 != 2 && i4 <= 10) {
                                str = str + this.decode.charAt(asIntBuffer.get(i3));
                            }
                        }
                    }
                    if (str.length() == 8) {
                        arrayList.add(str);
                        this.plate_list.add(str);
                    }
                    if (this.plate_list.size() > 10) {
                        int size = this.plate_list.size();
                        for (int i5 = 0; i5 < size - 10; i5++) {
                            this.plate_list.remove(0);
                        }
                    }
                } else {
                    Log.e(this.TAG, "error reading " + Float.toString(this.out1[0][0]));
                    str = "";
                }
                Log.e(this.TAG, "prob " + str + "--" + Float.toString(this.out1[0][0]));
            }
        }
        return arrayList;
    }

    private MappedByteBuffer loadModelFile(Activity activity, int i) throws IOException {
//        AssetFileDescriptor openFd = activity.getAssets().openFd(CplusConfig.getInstance().Get_Config("model_file_ocr"));
        AssetFileDescriptor openFd = activity.getAssets().openFd("model1.tflite");
        return new FileInputStream(openFd.getFileDescriptor()).getChannel().map(FileChannel.MapMode.READ_ONLY, openFd.getStartOffset(), openFd.getDeclaredLength());
    }

    public void close() {
        this.tflite.close();
        this.tflite = null;
        this.tfliteModel = null;
    }

    private void recreateInterpreter() {
        Interpreter interpreter = this.tflite;
        if (interpreter != null) {
            interpreter.close();
            this.tflite = new Interpreter((ByteBuffer) this.tfliteModel, this.tfliteOptions);
        }
    }

    public void setUseNNAPI(Boolean bool) {
        this.tfliteOptions.setUseNNAPI(bool.booleanValue());
        recreateInterpreter();
    }

    public void setNumThreads(int i) {
        this.tfliteOptions.setNumThreads(i);
        recreateInterpreter();
    }

    public float get_prob() {
        return 1.0f / (this.prob + 1.0f);
    }
}
