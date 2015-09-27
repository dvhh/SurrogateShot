package dvhh.surrogateshot;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.hardware.Camera.CameraInfo;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CameraView extends AppCompatActivity implements SurfaceHolder.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        //requestWindowFeature(Window.KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(getActionBar()!=null) {
            getActionBar().hide();
        }
        setContentView(R.layout.activity_camera_view);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final SurfaceView surfaceView= (SurfaceView) findViewById(R.id.surface_camera);
        final SurfaceHolder surfaceHolder=surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


    }
    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private Camera mCamera=null;
    private Surface mPreview;


    private boolean cameraOpen(int cameraID) {
        boolean result=false;
        try{
            releaseCamera();
            mCamera = Camera.open(cameraID);
            final SurfaceView surfaceView= (SurfaceView) findViewById(R.id.surface_camera);
            mCamera.setPreviewDisplay(surfaceView.getHolder());
            result = mCamera != null;
        }
        catch (Exception ex) {
            Log.e(getString(R.string.app_name),"Failed to open camera");
            ex.printStackTrace();
        }
        return result;
    }

    private void releaseCamera() {
        if(mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera=null;
        }
    }

    private int getSizeDelta(final Camera.Size size,final int targetWidth, final int targetHeight) {
        int dh = size.height - targetHeight;
        int dw = size.width - targetWidth;

        return( dh * dh + dw * dw );
    }
    private void setPreviewSize() {
        if(mCamera!=null) {
            Camera.Parameters parameters = mCamera.getParameters();
            final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
            List<Camera.Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
            Camera.Size bestSize=localSizes.get(0);
            final int targetWidth = surfaceView.getWidth();
            final int targetHeight = surfaceView.getHeight();
            Log.d(getString(R.string.app_name),Integer.toString(targetWidth) + "," +Integer.toString(targetHeight));
            int delta=Math.min(getSizeDelta(bestSize, targetWidth, targetHeight), getSizeDelta(bestSize,  targetHeight,targetWidth));
            for (Camera.Size size:localSizes) {
                final int test=Math.min ( getSizeDelta(size,targetWidth,targetHeight) , getSizeDelta(size,targetHeight,targetWidth));
                Log.d(getString(R.string.app_name),Integer.toString(size.width) + "," +Integer.toString(size.height));
                if( test < delta ) {
                    bestSize = size;
                }
            }
            Log.d(getString(R.string.app_name),Integer.toString(bestSize.width) + "," +Integer.toString(bestSize.height));
            //mSupportedPreviewSizes = localSizes;

            parameters.setPreviewSize(bestSize.width, bestSize.height);
            surfaceView.requestLayout();
            mCamera.setParameters(parameters);
        }

    }
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        startCameraPreview(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        releaseCamera();
        startCameraPreview(holder);
        if(mCamera!=null) {

            setPreviewSize();
            mCamera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    private synchronized void startCameraPreview(SurfaceHolder surfaceHolder) {
        final int cameraID=CameraInfo.CAMERA_FACING_BACK;
        //Camera camera = Camera.open(0);
        //setupCameraOrientation(camera,0);
        cameraOpen(cameraID);
        setupCameraOrientation(mCamera, cameraID);
    }

    private int getDeviceRotation() {
        final int rotation=this.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        // Unknown rotation value
        return 0;
    }

    private void setupCameraOrientation(Camera camera,final int cameraID) {
        CameraInfo cameraInfo=new CameraInfo();
        Camera.getCameraInfo(cameraID,cameraInfo);
        final int rotation=getDeviceRotation();
        switch (cameraID) {
            case CameraInfo.CAMERA_FACING_BACK:
                //camera
                camera.setDisplayOrientation( (cameraInfo.orientation - rotation +360) % 360 );
                break;
            case CameraInfo.CAMERA_FACING_FRONT:
                camera.setDisplayOrientation( (360 - (cameraInfo.orientation + rotation)) % 360 );
                break;

        }
    }

    public InputStream getVideoStream() throws IOException {
        if(mCamera!=null) {
            MediaRecorder recorder = new MediaRecorder();
            recorder.setCamera(mCamera);
            //recorder.setAudioSource(MediaRecorder.AudioSource.);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            ParcelFileDescriptor pipe[]=ParcelFileDescriptor.createPipe();
            recorder.setOutputFile(pipe[1].getFileDescriptor());
            InputStream result=new FileInputStream(pipe[0].getFileDescriptor());
            recorder.prepare();
            recorder.start();
            return result;
        }else{
            return null;
        }
    }
}
