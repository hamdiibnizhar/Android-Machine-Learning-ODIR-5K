package com.azhariharisalhamdi.ODIR5K.imageprocessing;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import org.tensorflow.lite.TensorFlowLite;

import java.util.ArrayList;
import java.util.Arrays;

public class ImageProcessing {

    private Mat mMatImage;
    private Bitmap mBitmapImage;
    private int mWidth;
    private int mHeight;
    private Bitmap.Config mBitmapConfig;
    private int mTypeMat;
    private MatOfDouble mean = null, std = null;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Cannot connect to OpenCV Manager");
        } else {
            Log.e("OpenCV", "Connected Successfully");
        }
    }


    public ImageProcessing(){}

    public ImageProcessing(Mat image, int type){
        this.mHeight = image.height();
        this.mWidth = image.width();
        this.mMatImage = image;
        this.mTypeMat = type;
        this.mBitmapConfig = Bitmap.Config.ARGB_8888;
        this.mBitmapImage = Bitmap.createBitmap(this.mWidth, this.mHeight, this.mBitmapConfig);
        Utils.matToBitmap(image, this.mBitmapImage, true);
        this.mMatImage = image;
    }

    public ImageProcessing(Mat image){
        this.mHeight = image.height();
        this.mWidth = image.width();
        this.mTypeMat = CvType.CV_8UC4;
        this.mMatImage = image;
        this.mBitmapConfig = Bitmap.Config.ARGB_8888;
        this.mBitmapImage = Bitmap.createBitmap(this.mWidth, this.mHeight, this.mBitmapConfig);
        Utils.matToBitmap(image, this.mBitmapImage, true);
    }

    public ImageProcessing(Bitmap image){
        this.mHeight = image.getHeight();
        this.mWidth = image.getWidth();
        this.mBitmapConfig = image.getConfig();
        this.mBitmapImage = Bitmap.createBitmap(image);
        this.mMatImage = new Mat(this.mWidth, this.mHeight, CvType.CV_8UC4);
        Utils.bitmapToMat(image, this.mMatImage, true);
    }

    public void close(){
        this.mBitmapConfig = null;
        this.mBitmapImage = null;
        this.mMatImage = null;
    }

    public void setWidthImage(int width){
        this.mWidth = width;
    }
    public int getwidthImage(){
        return this.mWidth;
    }

    public void setHeightImage(int height){
        this.mHeight = height;
    }
    public int getHeightImage(){
        return this.mHeight;
    }

    public void setTypeMatImage(int type){
        this.mTypeMat = type;
    }
    public int getTypeMatImage(){
        return this.mTypeMat;
    }

    public void setBitmapImage(Bitmap image){
        this.mBitmapImage = image;
        this.mHeight = image.getHeight();
        this.mWidth = image.getWidth();
        this.mBitmapConfig = image.getConfig();;
        this.mBitmapImage = Bitmap.createBitmap(image);
        this.mMatImage = new Mat(this.mWidth, this.mHeight, CvType.CV_8UC4);
        Utils.bitmapToMat(image, this.mMatImage, true);
    }
    public Bitmap getBitmapImage(){
        if(this.mMatImage == null)
            return null;
        org.opencv.android.Utils.matToBitmap(this.mMatImage, this.mBitmapImage);
        return this.mBitmapImage;
    }

    public void setMatImage(Mat image){
        this.mHeight = image.height();
        this.mWidth = image.width();
        this.mTypeMat = CvType.CV_8UC4;
        this.mMatImage = image;
        this.mBitmapConfig = Bitmap.Config.ARGB_8888;
        this.mBitmapImage = Bitmap.createBitmap(this.mWidth, this.mHeight, this.mBitmapConfig);
        Utils.matToBitmap(image, this.mBitmapImage, true);
    }
    public Mat getMatImage(){
        if(this.mBitmapImage == null)
            return null;
        org.opencv.android.Utils.bitmapToMat(this.mBitmapImage, this.mMatImage);
        return this.mMatImage;
    }

    public Mat getMatCrop(int start_x, int start_y, int height, int width){
        Mat cropedImage = this.mMatImage;
        Rect roi = new Rect(start_x, start_y, width, height);
        Mat tempMat = new Mat(this.mMatImage, roi);
        return this.mMatImage;
    }

    public Mat getMatCropCenter(){
        Mat cropedImage = this.mMatImage;
        int start_x,
            start_y,
            width,
            height;
        if(this.mWidth > this.mHeight){
            start_x = mWidth/2 - mHeight/2;
            start_y = 0;
            this.mWidth = this.mHeight;
        }else if (this.mWidth < this.mHeight){
            start_x = 0;
            start_y = mHeight/2 - mWidth/2;
            this.mHeight = this.mWidth;
        }else{
            return null;
        }
        Rect roi = new Rect(start_x, start_y, this.mWidth, this.mHeight);
        this.mMatImage = new Mat(this.mMatImage, roi);
        this.mBitmapImage = Bitmap.createBitmap(this.mWidth, this.mHeight, this.mBitmapConfig);
        return this.mMatImage;
    }

    public double getImageMean(){
        Core.meanStdDev(mMatImage, mean, std);
        double mean = this.mean.get(0,0)[0];
        return mean;
    }

    public double getImageStd(){
        Core.meanStdDev(mMatImage, mean, std);
        double std = this.std.get(0,0)[0];
        return std;
    }

    public Mat resizeImage(int start_x, int start_y, int width, int height, int cv_interpolation){
        Mat tempImage = this.mMatImage;
        Imgproc.resize(this.mMatImage, tempImage, new Size(height, width), start_x, start_y, cv_interpolation);
        this.mMatImage = tempImage;
        return this.mMatImage;
    }

    public Mat resizeImage(int start_x, int start_y, int width, int height){
        Mat tempImage = this.mMatImage;
        Imgproc.resize(this.mMatImage, tempImage, new Size(height, width), start_x, start_y);
        this.mMatImage = tempImage;
        return this.mMatImage;
    }

    public Mat resizeImage(int width, int height){
        Mat tempImage = this.mMatImage;
        Imgproc.resize(this.mMatImage, tempImage, new Size(height, width));
        this.mMatImage = tempImage;
        return this.mMatImage;
    }

    public Mat resizeImage(int start_x, int start_y, double percent_width, double percent_height){
        Mat tempImage = this.mMatImage;
        Imgproc.resize(this.mMatImage, tempImage, new Size(mHeight * percent_height, mWidth*percent_width), start_x, start_y);
        this.mMatImage = tempImage;
        return this.mMatImage;
    }

    public Mat resizeImage(double percent_width, double percent_height){
        Mat tempImage = this.mMatImage;
        Imgproc.resize(this.mMatImage, tempImage, new Size(mHeight * percent_height, mWidth*percent_width));
        this.mMatImage = tempImage;
        return this.mMatImage;
    }

    //output mush be RGB format
    public Mat processCLAHE(int clipLimit, int width, int height){
        Mat labImage = new Mat(this.mHeight, this.mWidth, this.mTypeMat);
        Mat tempL = new Mat();
        Imgproc.cvtColor(this.mMatImage, labImage, Imgproc.COLOR_RGB2Lab);

        java.util.List<Mat> Lab = new ArrayList<Mat>();
        Core.split(labImage,Lab);
        Mat L = Lab.get(0); // L,a,b are references, not copies
        Mat a = Lab.get(1);
        Mat b = Lab.get(2);
        CLAHE ce = Imgproc.createCLAHE(clipLimit, new Size(width, height));
        ce.apply(L,tempL);

        Core.merge(new ArrayList<>(Arrays.asList(tempL, a, b)),labImage);
        Imgproc.cvtColor(labImage,this.mMatImage,Imgproc.COLOR_Lab2RGB);
        return this.mMatImage;
    }

    private byte saturate(double val) {
        int iVal = (int) Math.round(val);
        iVal = iVal > 255 ? 255 : (iVal < 0 ? 0 : iVal);
        return (byte) iVal;
    }

    //output mush be RGB format
    public Mat processGammaCorrection(double gamma){
        Mat lookUpTable = new Mat(1, 256, CvType.CV_8U);
        byte[] lookUpTableData = new byte[(int) (lookUpTable.total()*lookUpTable.channels())];
        for (int i = 0; i < lookUpTable.cols(); i++) {
            lookUpTableData[i] = saturate(Math.pow(i / 255.0, gamma) * 255.0);
        }
        lookUpTable.put(0, 0, lookUpTableData);
        Mat temp_img = new Mat();
        Core.LUT(this.mMatImage, lookUpTable, temp_img);
        this.mMatImage = temp_img;
        return this.mMatImage;
    }

    //output mush be RGB format
    public Mat processGammaHE(double gamma1, double gamma2, int clipLimit, int width, int height){
        this.mMatImage = processGammaCorrection(gamma1);
        Mat labImage = new Mat(this.mHeight, this.mWidth, this.mTypeMat);
        Mat tempL = new Mat();
        Imgproc.cvtColor(this.mMatImage, labImage, Imgproc.COLOR_RGB2Lab);

        java.util.List<Mat> Lab = new ArrayList<Mat>();
        Core.split(labImage,Lab);
        Mat L = Lab.get(0); // L,a,b are references, not copies
        Mat a = Lab.get(1);
        Mat b = Lab.get(2);
        CLAHE ce = Imgproc.createCLAHE(clipLimit, new Size(width, height));
        ce.apply(L,tempL);

        Imgproc.equalizeHist(tempL,L);
        Core.merge(new ArrayList<>(Arrays.asList(L, a, b)),labImage);
        Imgproc.cvtColor(labImage,this.mMatImage,Imgproc.COLOR_Lab2RGB);

        this.mMatImage = processGammaCorrection(gamma2);
        return this.mMatImage;
    }
}
