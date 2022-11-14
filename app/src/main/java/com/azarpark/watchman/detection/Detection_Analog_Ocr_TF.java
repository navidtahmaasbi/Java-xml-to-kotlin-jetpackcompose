package com.azarpark.watchman.detection;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.support.image.TensorImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Detection_Analog_Ocr_TF {
    private String TAG = "Detection Analog model";
    private boolean detection_is_successful = false;
    private int heigth;
    int model;
    ArrayList<Mat> numerator_img = null;
    private Map<Integer, Object> outputs = null;
    private float[][][] prob = null;
    private Point scale;
    private int scale_h;
    private int scale_w;
    protected Interpreter tflite;
    private MappedByteBuffer tfliteModel;
    private final Interpreter.Options tfliteOptions;
    private TensorImage timage;
    private int width;
    private float[][][][] zcoord = null;
    private Context context;

    public Detection_Analog_Ocr_TF(Activity activity, int i, int i2, int i3, Context context) throws IOException {
        this.context = context;
        Class<Float> cls = float.class;
        Interpreter.Options options = new Interpreter.Options();
        this.tfliteOptions = options;
        CompatibilityList compatibilityList = new CompatibilityList();
        this.model = i3;
        //if (compatibilityList.isDelegateSupportedOnThisDevice()) {
            //options.addDelegate(new GpuDelegate(compatibilityList.getBestOptionsForThisDevice()));
        //} else {
            int size = Thread.getAllStackTraces().keySet().size();
            Context applicationContext = activity.getApplicationContext();
//            Toast.makeText(applicationContext, "Number of thread is" + String.valueOf(size), Toast.LENGTH_LONG).show();
            if (size > 4) {
                options.setNumThreads(4);
            } else {
                options.setNumThreads(2);
            }
        //}
        try {

//            Util util = new Util(context);
//            File result = util.read("model2.tflite");
            this.tfliteModel = loadModelFile(activity);
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
        this.tflite.resizeInput(0, new int[]{1, i2, i, 3});
//        this.scale_w = Integer.parseInt(CplusConfig.getInstance().Get_Config("scale_w"));
        this.scale_w = 450;
//        this.scale_h = Integer.parseInt(CplusConfig.getInstance().Get_Config("scale_h"));
        this.scale_h = 220;
        this.heigth = i2;
        this.width = i;
        this.timage = new TensorImage(DataType.FLOAT32);
        int i4 = i2 / 4;
        int i5 = i / 4;
        int[] iArr = new int[3];
        iArr[2] = i5;
        iArr[1] = i4;
        iArr[0] = 1;
        this.prob = (float[][][]) Array.newInstance(cls, iArr);
        int[] iArr2 = new int[4];
        iArr2[3] = 8;
        iArr2[2] = i5;
        iArr2[1] = i4;
        iArr2[0] = 1;
        this.zcoord = (float[][][][]) Array.newInstance(cls, iArr2);
        HashMap hashMap = new HashMap();
        this.outputs = hashMap;
        hashMap.put(0, this.prob);
        this.outputs.put(1, this.zcoord);
        this.numerator_img = new ArrayList<>();
        this.scale = new Point();
    }

    public void recognize(Mat mat) {
        this.detection_is_successful = false;
        if (this.tflite != null) {
            Mat mat2 = new Mat();
            Imgproc.resize(mat, mat2, new Size((double) this.width, (double) this.heigth));
            mat2.convertTo(mat2, CvType.CV_32FC3, 0.00392156862745098d);
            this.scale.x = (double) (((float) (((double) mat.width()) * 1.0d)) / ((float) this.width));
            this.scale.y = (double) (((float) (((double) mat.height()) * 1.0d)) / ((float) this.heigth));
            float[] fArr = new float[(this.heigth * this.width * 3)];
            mat2.get(0, 0, fArr);
            this.timage.load(fArr, new int[]{1, this.heigth, this.width, 3});
            try {
                this.tflite.runForMultipleInputsOutputs(new Object[]{this.timage.getBuffer()}, this.outputs);
            } catch (Exception e) {
                Log.e(this.TAG, e.toString());
            }
            get_rect_box(mat, mat.width(), mat.height());
        }
    }

    public boolean is_run_successful() {
        return this.detection_is_successful;
    }

    private void get_rect_box(Mat mat, int i, int i2) {
        double d;
        int i3;
        new Mat();
        mat.clone();
        this.numerator_img.clear();
        ArrayList arrayList = new ArrayList();
        new MatOfPoint2f();
//        double parseDouble = Double.parseDouble(CplusConfig.getInstance().Get_Config("prob_detection"));
        double parseDouble = .55;
        new RotatedRect();
        char c = 0;
        int i4 = 0;
        while (true) {
            int i5 = 4;
            if (i4 >= this.heigth / 4) {
                break;
            }
            int i6 = 0;
            while (i6 < this.width / i5) {
                if (((double) this.prob[c][i4][i6]) < parseDouble) {
                    int i7 = i;
                    int i8 = i2;
                    d = parseDouble;
                    i3 = i4;
                } else {
                    Point point = new Point((double) (i6 * 4), (double) (i4 * 4));
                    ArrayList arrayList2 = new ArrayList();
                    int i9 = 0;
                    while (true) {
                        if (i9 >= i5) {
                            int i10 = i;
                            int i11 = i2;
                            d = parseDouble;
                            i3 = i4;
                            break;
                        }
                        int i12 = i9 * 2;
                        double d2 = this.scale.x * (((double) (((float) this.scale_w) * this.zcoord[c][i4][i6][i12])) + point.x);
                        i3 = i4;
                        double d3 = this.scale.y * (((double) (((float) this.scale_h) * this.zcoord[c][i4][i6][i12 + 1])) + point.y);
                        if (d2 >= 0.0d) {
                            if (d2 >= ((double) i) || d3 < 0.0d) {
                                break;
                            }
                            d = parseDouble;
                            if (d3 >= ((double) i2)) {
                                break;
                            }
                            arrayList2.add(new Point(d2, d3));
                            i9++;
                            i4 = i3;
                            parseDouble = d;
                            c = 0;
                            i5 = 4;
                        } else {
                            int i13 = i;
                            break;
                        }
                    }
                    int i14 = i2;
                    d = parseDouble;
                    if (arrayList2.size() >= 4) {
                        arrayList.add(new plate_prob((RotatedRect) null, this.prob[0][i3][i6], arrayList2));
                        MatOfPoint matOfPoint = new MatOfPoint();
                        matOfPoint.fromList(arrayList2);
                        new ArrayList().add(matOfPoint);
                        i6++;
                        i4 = i3;
                        parseDouble = d;
                        c = 0;
                        i5 = 4;
                    }
                }
                i6++;
                i4 = i3;
                parseDouble = d;
                c = 0;
                i5 = 4;
            }
            int i15 = i;
            int i16 = i2;
            double d4 = parseDouble;
            i4++;
            c = 0;
        }
        if (arrayList.size() > 0) {
            ArrayList<RotatedRect> compute = new compute_plates(arrayList).compute();
            for (int i17 = 0; i17 < compute.size(); i17++) {
                Log.e("rotated rect", String.valueOf(compute.get(i17).size.width) + "---" + String.valueOf(compute.get(i17).size.height));
                this.numerator_img.add(get_crop(mat, compute.get(i17)));
            }
            this.detection_is_successful = true;
            return;
        }
        Log.e("numerator dectector", "numerator is not found");
    }

    private RotatedRect get_rot_rect(List<Point> list) {
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        matOfPoint2f.fromList(list);
        RotatedRect minAreaRect = Imgproc.minAreaRect(matOfPoint2f);
        Size size = minAreaRect.size;
        if (minAreaRect.angle < 0.0d && size.height > size.width) {
            minAreaRect.angle += 90.0d;
            minAreaRect.size.height = size.width;
            minAreaRect.size.width = size.height;
        } else if (size.height > size.width) {
            minAreaRect.angle = 90.0d - minAreaRect.angle;
            minAreaRect.size.height = size.width;
            minAreaRect.size.width = size.height;
        }
        return minAreaRect;
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor openFd = activity.getAssets().openFd("model2.tflite");
        return new FileInputStream(openFd.getFileDescriptor()).getChannel().map(FileChannel.MapMode.READ_ONLY, openFd.getStartOffset(), openFd.getDeclaredLength());
    }

    private File createFileFromInputStream(InputStream inputStream) {
        try{
            File f = new File(context.getFilesDir(), "temp1.tflite");
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        }
        catch (IOException e) { e.printStackTrace(); }
        return null;
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

    private Mat get_crop(Mat mat, RotatedRect rotatedRect) {
        Mat mat2 = new Mat();
        Mat mat3 = new Mat();
        double d = rotatedRect.angle;
        Size size = rotatedRect.size;
        if (rotatedRect.angle < -45.0d) {
            rotatedRect.angle += 90.0d;
            double d2 = size.height;
            size.height = size.width;
            size.width = d2;
        } else if (rotatedRect.angle > 45.0d) {
            rotatedRect.angle = 90.0d - Math.abs(rotatedRect.angle);
            double d3 = size.height;
            size.height = size.width;
            size.width = d3;
        }
        Mat rotationMatrix2D = Imgproc.getRotationMatrix2D(rotatedRect.center, rotatedRect.angle, 1.0d);
        Imgproc.warpAffine(mat, mat2, rotationMatrix2D, mat.size(), 4);
        Imgproc.getRectSubPix(mat2, size, rotatedRect.center, mat3);
        rotationMatrix2D.release();
        mat2.release();
        return mat3;
    }

    public List<Mat> get_numerator() {
        return this.numerator_img;
    }

    private void save_to_phone(Mat mat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Scaned_Documnet");
        if (!file.exists()) {
            file.mkdir();
        }
        File file2 = new File(file, simpleDateFormat.format(new Date()) + ".jpg");
        if (!mat.empty()) {
            Imgcodecs.imwrite(file2.toString(), mat);
        } else {
            Log.e(this.TAG, "Image of Type is None");
        }
    }
}
