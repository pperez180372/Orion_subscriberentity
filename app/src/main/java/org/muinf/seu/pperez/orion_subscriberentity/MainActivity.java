package org.muinf.seu.pperez.orion_subscriberentity;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity {


    public View rootView;

    public QueryContextTask ay;
    public unsubscribeContextTask ayy;
    public subscribeContextTask ayyy;

    int puerto_de_escucha=54545;
    ServerSocket sk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

    // crear un hilo que escuchará los mensajes de la subscripción: // servidor serie // una petición detras de otra
    // abre una conexión sin saber si se va a utilizar ... no se debe hacer así.

        new Thread(new Runnable() {
            public void run() {
                    // cliente echo
                try {
                    while (true)
                    {
                    sk = new ServerSocket(puerto_de_escucha);

                    while (!sk.isClosed()) {
                        Socket cliente = sk.accept();
                        BufferedReader entrada = new BufferedReader(
                                new InputStreamReader(cliente.getInputStream()));
                        PrintWriter salida = new PrintWriter(
                                new OutputStreamWriter(cliente.getOutputStream()), true);
                        String datos = entrada.readLine();
                        salida.println(datos);
                        cliente.close();
                    }
                        Thread.sleep(1000);

                    }
                } catch (IOException e) {
                    System.out.println(e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }).start();



    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {

        Button Botonsuscribir;
        Button Botonactualizar;
        Button Botondesconectar;


        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
            Botonactualizar = (Button) rootView.findViewById(R.id.buttonActualizar);
            Botonactualizar.setOnClickListener(               new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ay=new QueryContextTask();
                    ay.execute("");

                };
            });

            Botondesconectar = (Button) rootView.findViewById(R.id.buttonDesconectar);
            Botondesconectar.setOnClickListener(               new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ayy=new unsubscribeContextTask();
                    ayy.execute("");

                };
            });

            Botonsuscribir = (Button) rootView.findViewById(R.id.buttonSubscribir);
            Botonsuscribir.setOnClickListener(               new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ayyy=new subscribeContextTask();
                    ayyy.execute("");

                };
            });


            return rootView;
        }
    }




    class QueryContextTask extends AsyncTask<String, Void, String> {

        String res;

        protected String doInBackground(String... urls) {

            Map<String, List<String>> rr;
            String res = "";
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;
            // View rootView = findViewById(R.layout.fragment_main);


            EditText tEntidad;
            TextView tHumedad;
            TextView tTemperatura;

            tEntidad = (EditText) rootView.findViewById(R.id.editEntidad);
            tHumedad=  (EditText)  rootView.findViewById(R.id.editHumedad);
            tTemperatura=  (EditText)  rootView.findViewById(R.id.editTemperatura);

            //String username = tuser.getText().toString(); //"Android_SEU_3n5_1";//
            //String passwd = tpassword.getText().toString(); // "sensor";//
            //String domain = tdomain.getText().toString(); // "Asignatura SEU";

            String HeaderAccept = "application/json";
            String HeaderContent = "application/json";
            String payload = "{\"entities\": [{\"type\": \"Transducer\",\"isPattern\": \"false\",\"id\": \"Room1\"}]}";
            // String encodedData = URLEncoder.encode(payload, "UTF-8");
            // String encodedData = payload;
            String leng = null;
            String resp=        "none";

            try {
                leng = Integer.toString(payload.getBytes("UTF-8").length);

                OutputStreamWriter wr = null;
                BufferedReader rd = null;
                StringBuilder sb = null;


                URL url = null;

                url = new URL("http://pperez-seu-or.disca.upv.es:1026/v1/queryContext");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000); // miliseconds
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");

                //conn.setRequestProperty("Accept", HeaderAccept);
                conn.setRequestProperty("Content-type", HeaderContent);
                //conn.setRequestProperty("Fiware-Service", HeaderService);
                conn.setRequestProperty("Content-Length", leng);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(payload.getBytes("UTF-8"));
                os.flush();
                os.close();


                int rc = conn.getResponseCode();
                resp = conn.getContentEncoding();
                is = conn.getInputStream();

                if (rc == 200) {

                    resp = "OK";
                    //read the result from the server
                    rd = new BufferedReader(new InputStreamReader(is));
                    res = rd.readLine();
                    // cabeceras de recepcion
                    rr = conn.getHeaderFields();
                    System.out.println("headers: " + rr.toString());

                } else {
                    resp = "ERROR de conexión";
                    System.out.println("http response code error: " + rc + "\n");

                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return resp;
            }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            res=result;
            TextView ttoken =   (TextView)  rootView.findViewById(R.id.editResultado);
            ttoken.setText(result);
        }
    }
    class unsubscribeContextTask extends AsyncTask<String, Void, String> {

        String res;

        protected String doInBackground(String... urls) {

            Map<String, List<String>> rr;
            String res = "";
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;
            // View rootView = findViewById(R.layout.fragment_main);


            EditText tEntidad;
            TextView tHumedad;
            TextView tidsub;

            tidsub= (EditText) rootView.findViewById(R.id.editIdSubscripcion);

            //String username = tuser.getText().toString(); //"Android_SEU_3n5_1";//
            //String passwd = tpassword.getText().toString(); // "sensor";//
            //String domain = tdomain.getText().toString(); // "Asignatura SEU";

            // String HeaderAccept = "application/xml";
            String HeaderContent = "application/json";
            String payload =  "{\"subscribeResponse\" : {\"subscriptionId\" : \""+tidsub.getText().toString()+"\"}}";
            // String encodedData = URLEncoder.encode(payload, "UTF-8");
            // String encodedData = payload;
            String leng = null;
            try {
                leng = Integer.toString(payload.getBytes("UTF-8").length);

                OutputStreamWriter wr = null;
                BufferedReader rd = null;
                StringBuilder sb = null;


                URL url = null;

                url = new URL("http://pperez-seu-or.disca.upv.es:1026/v1/unsubscribeContext");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 ); // miliseconds
                conn.setConnectTimeout(15000 );
                conn.setRequestMethod("POST");

                //conn.setRequestProperty("Accept", HeaderAccept);
                conn.setRequestProperty("Content-type", HeaderContent);
                //conn.setRequestProperty("Fiware-Service", HeaderService);
                conn.setRequestProperty("Content-Length", leng);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(payload.getBytes("UTF-8"));
                os.flush();
                os.close();


                int rc = conn.getResponseCode();
                String resp = conn.getContentEncoding();
                is = conn.getInputStream();

                if (rc == 200) {

                    resp="OK";
                    //read the result from the server
                    rd = new BufferedReader(new InputStreamReader(is));
                    //res=rd.readLine();
                    // cabeceras de recepcion
                    rr = conn.getHeaderFields();
                    System.out.println("headers: " + rr.toString());

                } else {
                    resp="ERROR de conexión";
                    System.out.println("http response code error: " + rc + "\n");

                }



                return resp;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return "error";
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            res=result;
            TextView ttoken =   (TextView)  rootView.findViewById(R.id.editResultado);
            ttoken.setText(result);
        }
    }
    class subscribeContextTask extends AsyncTask<String, Void, String> {

        String res;

        protected String doInBackground(String... urls) {

            Map<String, List<String>> rr;
            String res = "";
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;
            // View rootView = findViewById(R.layout.fragment_main);


            EditText tEntidad;
            TextView tHumedad;
            TextView tTemperatura;

            tEntidad = (EditText) rootView.findViewById(R.id.editEntidad);
            tHumedad=  (EditText)  rootView.findViewById(R.id.editHumedad);
            tTemperatura=  (EditText)  rootView.findViewById(R.id.editTemperatura);

            //String username = tuser.getText().toString(); //"Android_SEU_3n5_1";//
            //String passwd = tpassword.getText().toString(); // "sensor";//
            //String domain = tdomain.getText().toString(); // "Asignatura SEU";

            // String HeaderAccept = "application/xml";
            String HeaderContent = "application/json";

            if (sk==null)
                return "error socket no abierto";

            String url="http://"+sk.getInetAddress().toString()+":"+sk.getLocalPort();

            String payload = "{\"entities\" : [{\"type\": \"Room\",\"isPattern\": \"false\",\"id\": \"Room1\"}],\"attributes\": [\"temperature\"], \"reference\": \""+url+"\", \"duration\": \"P1M\",\"notifyConditions\": [{    \"type\": \"ONCHANGE\", \"condValues\": [\"pressure\" ] } ], \"throttling\": \"PT5S\"}";

            // String encodedData = URLEncoder.encode(payload, "UTF-8");
            // String encodedData = payload;
            String leng = null;
            try {
                leng = Integer.toString(payload.getBytes("UTF-8").length);

                OutputStreamWriter wr = null;
                BufferedReader rd = null;
                StringBuilder sb = null;


                URL url = null;

                url = new URL("http://pperez-seu-or.disca.upv.es:1026/v1/subscribeContext");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 ); // miliseconds
                conn.setConnectTimeout(15000 );
                conn.setRequestMethod("POST");

                //conn.setRequestProperty("Accept", HeaderAccept);
                conn.setRequestProperty("Content-type", HeaderContent);
                //conn.setRequestProperty("Fiware-Service", HeaderService);
                conn.setRequestProperty("Content-Length", leng);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(payload.getBytes("UTF-8"));
                os.flush();
                os.close();


                int rc = conn.getResponseCode();
                String resp = conn.getContentEncoding();
                is = conn.getInputStream();

                if (rc == 200) {

                    resp="OK";
                    //read the result from the server
                    rd = new BufferedReader(new InputStreamReader(is));
                    //res=rd.readLine();
                    // cabeceras de recepcion
                    rr = conn.getHeaderFields();
                    System.out.println("headers: " + rr.toString());

                } else {
                    resp="ERROR de conexión";
                    System.out.println("http response code error: " + rc + "\n");

                }



                return resp;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return "error";
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            res=result;
            TextView tv =   (TextView)  rootView.findViewById(R.id.editIdSubscripcion);
            tv.setText(result);
        }
    }
}
