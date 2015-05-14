package org.muinf.seu.pperez.orion_subscriberentity;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.ProtocolException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity {


    public View rootView;

    public QueryContextTask ay;
    public unsubscribeContextTask ayy;
    public subscribeContextTask ayyy;

    int puerto_de_escucha=54545;
    ServerSocket sk;


    /**
            * + * Returns a valid InetAddress to use for RMI communication. + * If the
    * system property java.rmi.server.hostname is set it is used. + * Secondly
    * InetAddress.getLocalHost is used. + * If neither of these are
    * non-loopback all network interfaces + * are enumerated and the first
    * non-loopback ipv4 + * address found is returned. If that also fails null
            * is returned. +
            */
    private static InetAddress getLocalAddress() {
        InetAddress inetAddr = null;

        /**
         * 1) If the property java.rmi.server.hostname is set and valid, use it
         */
        try {
            System.out
                    .println("Attempting to resolve java.rmi.server.hostname");
            String hostname = System.getProperty("java.rmi.server.hostname");
            if (hostname != null) {
                inetAddr = InetAddress.getByName(hostname);
                if (!inetAddr.isLoopbackAddress()) {
                    return inetAddr;
                } else {
                    System.out
                            .println("java.rmi.server.hostname is a loopback interface.");
                }

            }
        } catch (SecurityException e) {
            System.out
                    .println("Caught SecurityException when trying to resolve java.rmi.server.hostname");
        } catch (UnknownHostException e) {
            System.out
                    .println("Caught UnknownHostException when trying to resolve java.rmi.server.hostname");
        }

        /** 2) Try to use InetAddress.getLocalHost */
        try {
            System.out
                    .println("Attempting to resolve InetADdress.getLocalHost");
            InetAddress localHost = null;
            localHost = InetAddress.getLocalHost();
            if (!localHost.isLoopbackAddress()) {
                return localHost;
            } else {
                System.out
                        .println("InetAddress.getLocalHost() is a loopback interface.");
            }

        } catch (UnknownHostException e1) {
            System.out
                    .println("Caught UnknownHostException for InetAddress.getLocalHost()");
        }

        /** 3) Enumerate all interfaces looking for a candidate */
        Enumeration ifs = null;
        try {
            System.out
                    .println("Attempting to enumerate all network interfaces");
            ifs = NetworkInterface.getNetworkInterfaces();

            // Iterate all interfaces
            while (ifs.hasMoreElements()) {
                NetworkInterface iface = (NetworkInterface) ifs.nextElement();

                // Fetch all IP addresses on this interface
                Enumeration ips = iface.getInetAddresses();

                // Iterate the IP addresses
                while (ips.hasMoreElements()) {
                    InetAddress ip = (InetAddress) ips.nextElement();
                    if ((ip instanceof Inet4Address) && !ip.isLoopbackAddress()) {
                        return (InetAddress) ip;
                    }
                }
            }
        } catch (SocketException se) {
            System.out.println("Could not enumerate network interfaces");
        }

        /** 4) Epic fail */
        System.out
                .println("Failed to resolve a non-loopback ip address for this host.");
        return null;
    }


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
                      /*  java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
                        String g=addr.getHostAddress();*/
                        InetAddress g1=getLocalAddress();
                        sk = new ServerSocket(puerto_de_escucha,0,g1);
                        StringBuilder     sb=null,sbj=null;

                        while (!sk.isClosed()) {
                            sbj=null;
                            sb=null;
                        Socket cliente = sk.accept();
                        BufferedReader entrada = new BufferedReader(
                                new InputStreamReader(cliente.getInputStream()));
                        PrintWriter salida = new PrintWriter(
                                new OutputStreamWriter(cliente.getOutputStream()), true);
                        String line = null;
                        sb= new StringBuilder();
                        try {
                            do {
                                line = entrada.readLine();
                                sb.append(line + "\n");
                                //hacking
                                 if ((sbj==null)&&(line.contains("{"))) // date
                                     sbj= new StringBuilder();
                                 if (sbj!=null)
                                 {
                                     sbj.append(line+"\n");
                                 }
                                System.out.println(""+sb);
                            }
                            while (entrada.ready());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
   //String datos = entrada.readLine();

                        System.out.println("Fin de lectura");
                        JSONObject jObject = null;
                        try {
                            jObject = new JSONObject(sbj.toString());

                            JSONArray jArray = jObject.getJSONArray("contextResponses");
                            JSONObject c = jArray.getJSONObject(0);
                            JSONObject jo = c.getJSONObject("contextElement");
                            JSONArray jArrayattr = jo.getJSONArray("attributes");

                            for(int i = 0; i <  jArrayattr.length(); i++) {
                                try {
                                    JSONObject ca = jArrayattr.getJSONObject(i);

                                    if (ca.getString("name").contains("temperature")) {

                                        final double va = ca.getDouble("value");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                EditText t= (EditText)  rootView.findViewById(R.id.editTemperatura);
                                                t.setText("" + va);

                                            }
                                        });


                                    } else if (ca.getString("name").contains("humidity")) {

                                        final double va = ca.getDouble("value");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((EditText)  rootView.findViewById(R.id.editHumedad)).setText("" + va);

                                            }
                                        });
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } // del for



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        salida.println("POST HTTP/1.1 200");
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
            String payload = "{\"entities\": [{\"type\": \"Room\",\"isPattern\": \"false\",\"id\": \"Room1\"}]}";
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

                conn.setRequestProperty("Accept", HeaderAccept);
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
                    sb = new StringBuilder();

                    String line = null;
                    while ((line = rd.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    String result = sb.toString();


                    JSONObject jObject = null;
                    try {
                        jObject = new JSONObject(result);

                        JSONArray jArray = jObject.getJSONArray("contextResponses");
                        JSONObject c = jArray.getJSONObject(0);
                        JSONObject jo = c.getJSONObject("contextElement");
                        JSONArray jArrayattr = jo.getJSONArray("attributes");

                        for(int i = 0; i <  jArrayattr.length(); i++) {
                            try {
                                JSONObject ca = jArrayattr.getJSONObject(i);

                                if (ca.getString("name").contains("temperature")) {

                                    final double va = ca.getDouble("value");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((EditText)  rootView.findViewById(R.id.editTemperatura)).setText("" + va);

                                        }
                                    });


                                } else if (ca.getString("name").contains("humidity")) {

                                    final double va = ca.getDouble("value");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((EditText)  rootView.findViewById(R.id.editHumedad)).setText("" + va);

                                        }
                                    });
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } // del for



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


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

             String HeaderAccept = "application/json";
            String HeaderContent = "application/json";
            String payload =  "{\"subscriptionId\" : \""+tidsub.getText().toString()+"\"}";
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

                conn.setRequestProperty("Accept", HeaderAccept);
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

                    resp = "OK";



                //read the result from the server
                rd = new BufferedReader(new InputStreamReader(is));
                sb = new StringBuilder();

                String line = null;
                while ((line = rd.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                String result = sb.toString();


                JSONObject jObject = null;
                try {
                    jObject = new JSONObject(result);

                    JSONObject jo = jObject.getJSONObject("statusCode");

                    final String err= jo.getString("reasonPhrase");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((EditText)  rootView.findViewById(R.id.editIdSubscripcion)).setText(err);


                        }
                    });



                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((EditText)  rootView.findViewById(R.id.editIdSubscripcion)).setText("Error parsing json");


                        }
                    });
                    e.printStackTrace();
                }






            } else {
                resp = "ERROR de conexión";
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

             String HeaderAccept = "application/json";
            String HeaderContent = "application/json";

            if (sk==null)
                return "error socket no abierto";

            String urlEs="http://"+sk.getInetAddress().getHostAddress()+":"+sk.getLocalPort();
            String payload = "{\"entities\" : [{\"type\": \"Room\",\"isPattern\": \"false\",\"id\": \"Room1\"}],\"attributes\": [\"temperature\"], \"reference\": \""+urlEs+"\", \"duration\": \"P1M\",\"notifyConditions\": [{    \"type\": \"ONCHANGE\", \"condValues\": [\"temperature\" ] } ], \"throttling\": \"PT5S\"}";

            // String encodedData = URLEncoder.encode(payload, "UTF-8");
            // String encodedData = payload;
            String leng = null;
            try {
                try {
                    leng = Integer.toString(payload.getBytes("UTF-8").length);
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }

                OutputStreamWriter wr = null;
                BufferedReader rd = null;
                StringBuilder sb = null;


                URL url = null;

                url = new URL("http://pperez-seu-or.disca.upv.es:1026/v1/subscribeContext");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 ); // miliseconds
                conn.setConnectTimeout(15000 );
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Accept", HeaderAccept);
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

                    resp = "OK";
                    //read the result from the server
                    rd = new BufferedReader(new InputStreamReader(is));
                    sb = new StringBuilder();

                    String line = null;
                    while ((line = rd.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    String result = sb.toString();


                    JSONObject jObject = null;
                    try {
                        jObject = new JSONObject(result);

                        JSONObject jo = jObject.getJSONObject("subscribeResponse");
                        final String subsID= jo.getString("subscriptionId");

                                runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((EditText)  rootView.findViewById(R.id.editIdSubscripcion)).setText(subsID);


                                        }
                                    });



                            } catch (JSONException e) {
                                e.printStackTrace();
                            }






                } else {
                    resp = "ERROR de conexión";
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
            TextView tv =   (TextView)  rootView.findViewById(R.id.editResultado);
            tv.setText(result);
        }
    }
}
