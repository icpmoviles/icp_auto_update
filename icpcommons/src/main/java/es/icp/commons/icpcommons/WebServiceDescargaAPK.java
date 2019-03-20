package es.icp.commons.icpcommons;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

public class WebServiceDescargaAPK extends AsyncTask<String, String, String> {

    private StringBuffer sb;
    private InterfazListener listener;
    private ProgressDialog pd;
    private Context context;
    private String mensaje_progress;
    private String applicationId;
    private String nombreAPK;


    /**
     * @param context  si no es necesario vendrá a null.
     * @param mensaje_progress  Primero el webServiceMensaje de carga normal, segundo el webServiceMensaje del progress update.
     * @param applicationId  La ruta de la carpeta donde se guardara temporalmente la nueva version de la apk.
     * @param nombreAPK  El nombre de la APK.
     * @param listener
     */
    public WebServiceDescargaAPK(Context context, String mensaje_progress, String applicationId, String nombreAPK, InterfazListener listener)
    {
        this.context = context;
        this.mensaje_progress = mensaje_progress;
        this.listener = listener;
        this.applicationId = applicationId;
        this.nombreAPK = nombreAPK;
        sb = new StringBuffer();
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        pd = new ProgressDialog(context);
        pd.setMessage(mensaje_progress);
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.show();
    }
    @Override
    protected void onProgressUpdate(String... progress)
    {
        //        if (progress[0] == 1)
        //        {
        //            //pd.setMessage(context.getString(R.string.descargando_nueva_version_del_servidor));
        //            pd.setMessage(webServiceMensaje);
        //        }
//        pd.setIndeterminate(false);
//        pd.setMax(100);
//        pd.setProgress(progress[0]);
        pd.setProgress(Integer.parseInt(progress[0]));
    }

    /**
     * @param params Primer parámetro la URL donde está alojado el servicio
     *               Segundo parámetro la clase contenedora de los parámetros que han de ser pasados al web service
     * @return true si el resultado es satisfactorio y el web service a constestado correctamente
     * false si no se ha podido obtener uan respuesta
     */
    @Override
    protected String doInBackground(String... params)
    {
        int count;
        try {
            URL url = new URL(params[0]);
            URLConnection conection = url.openConnection();
            conection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            int lenghtOfFile = conection.getContentLength();

            // download the file


            String carpeta = nombreAPK.replace(".apk", "");
            File folder =  new File(Environment.getExternalStorageDirectory(), carpeta);
            if (!folder.exists())
            {
                folder.mkdir();
            }
            File apk = new File(folder, nombreAPK);
            if (apk.exists())
            {
                apk.delete();
                apk.createNewFile();
            }

            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            // Output stream
            OutputStream output = new FileOutputStream(apk);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(final String file_url)
    {
        pd.dismiss();
        String carpeta = nombreAPK.replace(".apk", "");
        File apk = new File(Environment.getExternalStorageDirectory().toString() + File.separator + carpeta + File.separator + nombreAPK);
        Uri uriFile = FileProvider.getUriForFile(context, applicationId, apk);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(uriFile, "application/vnd.android.package-archive");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(i);
    }

    @Override
    protected void onCancelled()
    {
        //        mAuthTask = null;
        //        mostrarProgressBar(false);
    }

    public interface InterfazListener
    {
        public void resultadoAsincrono(JSONObject resultado);
    }

}
