package dvhh.surrogateshot;

import android.hardware.Camera;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.hardware.Camera.CameraInfo;
import android.view.SurfaceView;

import java.util.List;

public class CameraView extends ActionBarActivity implements SurfaceHolder.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    private double getSizeDelta(final Camera.Size size,final int targetWidth, final int targetHeight) {

    }
    private void setPreviewSize() {
        if(mCamera!=null) {
            Camera.Parameters parameters = mCamera.getParameters();
            final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
            List<Camera.Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
            Camera.Size bestSize=localSizes.get(0);
            double delta=getSizeDelta(bestSize, surfaceView.getWidth(), surfaceView.getHeight());
            for (Camera.Size size:localSizes) {

            }

            //mSupportedPreviewSizes = localSizes;

            parameters.setPreviewSize(surfaceView.getWidth(), surfaceView.getHeight());
            surfaceView.requestLayout();
            mCamera.setParameters(parameters);
        }

    }
    private void Preview() {

    }
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        startCameraPreview(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
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
        }
    }
}
