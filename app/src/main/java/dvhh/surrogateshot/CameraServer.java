package dvhh.surrogateshot;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by huy on 15/09/28.
 */
public class CameraServer extends NanoHTTPD {

    private CameraView mView=null;

    @Override
    public Response serve( String uri, String method, Properties header, Properties parms, Properties files ) {
        try {
            if (method == "GET") {
                if (uri == "/stream") {
                    return serveStream();
                }
            }
        }
        catch (Exception ex) {
            Log.e(mView.getString(R.string.app_name),"error with request");
            ex.printStackTrace();
        }
        return new NanoHTTPD.Response(HTTP_BADREQUEST,"text/plain","bad request");
    }

    protected Response serveStream() throws IOException {

        Response response=new Response(HTTP_OK,"video/mp4",mView.getVideoStream());
        response.isStreaming=true;
        return response;
    }


    public CameraServer(int port,File wwwRoot,CameraView view) throws IOException{
        super(port,wwwRoot);
        mView=view;
    }


}
