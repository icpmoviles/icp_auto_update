package es.icp.commons.icpcommons;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class WebServiceDescargaAPK extends AsyncTask<Object, Integer, Boolean> {

    private StringBuffer sb;
    private InterfazListener listener;
    private ProgressDialog pd;
    private Context context;
    private String mensaje;
    private String carpeta;
    private String nombreAPK;

    /**
     * @param context  si no es necesario vendrá a null.
     * @param mensaje  Primero el webServiceMensaje de carga normal, segundo el webServiceMensaje del progress update.
     * @param carpeta  La ruta de la carpeta donde se guardara temporalmente la nueva version de la apk.
     * @param nombreAPK  El nombre de la APK.
     * @param listener
     */
    public WebServiceDescargaAPK(Context context, String mensaje, String carpeta, String nombreAPK, InterfazListener listener)
    {
        this.context = context;
        this.mensaje = mensaje;
        this.listener = listener;
        this.carpeta = carpeta;
        this.nombreAPK = nombreAPK;
        sb = new StringBuffer();
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        pd = new ProgressDialog(context);
        pd.setMessage(mensaje);
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress)
    {
        //        if (progress[0] == 1)
        //        {
        //            //pd.setMessage(context.getString(R.string.descargando_nueva_version_del_servidor));
        //            pd.setMessage(webServiceMensaje);
        //        }
        pd.setIndeterminate(false);
        pd.setMax(100);
        pd.setProgress(progress[0]);
    }

    /**
     * @param params Primer parámetro la URL donde está alojado el servicio
     *               Segundo parámetro la clase contenedora de los parámetros que han de ser pasados al web service
     * @return true si el resultado es satisfactorio y el web service a constestado correctamente
     * false si no se ha podido obtener uan respuesta
     */
    @Override
    protected Boolean doInBackground(Object... params)
    {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection urlConnection = null;
        try
        {
            URL url;
            url = new URL(String.valueOf(params[0]));
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setConnectTimeout(50000);
            urlConnection.setReadTimeout(50000);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.connect();
            int HttpResult = urlConnection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK)
            {
                int longitudApk = urlConnection.getContentLength();
//                File carpetaAplicacion = new File(Environment.getExternalStorageDirectory(), "GestorIcpPTL");
                File carpetaAplicacion = new File(Environment.getExternalStorageDirectory(), carpeta);
                if (!carpetaAplicacion.exists())
                {
                    carpetaAplicacion.mkdir();
                }
                File apk = new File(carpetaAplicacion, nombreAPK);
//                File apk = new File(carpetaAplicacion, "GestorIcpPTL.apk");
                if(apk.exists())
                {
                    apk.delete();
                    apk.createNewFile();
                }
                //Environment.getExternalStorageDirectory().toString() + File.separator + Constantes.carpetaAplicacion + File.separator + Constantes.nombreApk;
                input = urlConnection.getInputStream();
                output = new FileOutputStream(apk);
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1)
                {
                    if (isCancelled())
                    {
                        input.close();
                        return null;
                    }
                    total += count;
                    if (longitudApk > 0)
                    {
                        publishProgress((int) (total * 100 / longitudApk));
                    }
                    output.write(data, 0, count);
                }
                return true;
            }
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (output != null)
                {
                    output.close();
                }
                if (input != null)
                {
                    input.close();
                }
            }
            catch (IOException ignorado)
            {
                ignorado.printStackTrace();
            }
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(final Boolean success)
    {
        pd.dismiss();
        JSONObject jsonObject = null;
        if (listener != null)
        {
            if (success)
            {
                try
                {
                    jsonObject = new JSONObject();
                    jsonObject.put("RETCODE", 0);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                listener.resultadoAsincrono(jsonObject);
            }
            else
            {
                listener.resultadoAsincrono(null);
            }
        }
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
