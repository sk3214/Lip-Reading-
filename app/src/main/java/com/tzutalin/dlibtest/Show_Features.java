package com.tzutalin.dlibtest;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceView;

import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Show_Features extends Activity implements CvCameraViewListener2 {
    private static final String  TAG = "Processing";
    private CameraBridgeViewBase mOpenCvCameraView;

    private List<Classifier> mClassifiers = new ArrayList<>();
    private List<Point> list_of_points = new ArrayList<>();

    private Mat mRgba;
    private Mat mGray;

    private FaceDet mFaceDet;
    private Bitmap bmp = null;

    int point_count,flag;
    double X_sum,Y_sum;

    static {
        try {
            System.loadLibrary("dlib-android");
            Log.d(TAG, "jniNativeClassInit success");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "library not found");
        }
    }

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();

                    final String targetPath = Constants.getFaceShapeModelPath();
                    if (!new File(targetPath).exists())
                        FileUtils.copyFileFromRawToOthers(getApplicationContext(), R.raw.shape_predictor_68_face_landmarks, targetPath);



                    if (mFaceDet == null) {
                        mFaceDet = new FaceDet(Constants.getFaceShapeModelPath());
                    }

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public Show_Features() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.layout_opencv);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.view_opencv);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {


    }

    public void onCameraViewStopped() {

    }



    // This is the Main loop for processing individual frames - RM
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {


            mRgba = inputFrame.rgba();    // Rgb frame
            mGray = inputFrame.gray();    // Gray frame

            bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mRgba, bmp);
            List<VisionDetRet> faceList = mFaceDet.detect(bmp);
            for (VisionDetRet ret : faceList) {
                ArrayList<Point> landmarks = ret.getFaceLandmarks();
                list_of_points.clear();
                point_count = 0;
                X_sum = 0;
                Y_sum = 0;
                flag = 0;

                for (Point point : landmarks) {
                    if (point_count >= 48 && point_count < 68) {
                        org.opencv.core.Point opencv_point = new org.opencv.core.Point((double) point.x, (double) point.y);
                        Imgproc.circle(mRgba, opencv_point, 1, new Scalar(0, 0, 255), -1);
                    }
                }
            }

        return mRgba;
    }
}


