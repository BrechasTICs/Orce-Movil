package com.fiis.orcemovil;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by daniel on 03/02/14.
 */
public abstract class JSONTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... arg) {
        String linha = "";
        String retorno = "";
        String url = arg[0];

        //mDialog = ProgressDialog.show(mContext, "Espere", "Cargando...", true);

        // Create client connection
        HttpClient client = new DefaultHttpClient();
        HttpPost get = new HttpPost(url);

        try {
            // Do Http request
            HttpResponse response = client.execute(get);

            // Pega o status da solicitação
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            if (statusCode == 200) { // Ok
                // Pega o retorno
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                // Lê o buffer e coloca na variável
                while ((linha = rd.readLine()) != null) {
                    retorno += linha;
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return retorno; // This value will be returned to your onPostExecute(result) method
    }

    @Override
    protected void onPostExecute(String result) {
        // Create here your JSONObject...
        try {
            JSONObject json = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //mDialog.dismiss();
    }

}