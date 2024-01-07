/**
 * JUAN VICTOR MORALES ORIBIO
 * 7CV2
 * SISTEMAS DISTRIBUIDOS
 * PROYECTO FINAL PARTE 1
 */
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// import java.util.Arrays;
// import java.util.Enumeration;
// import java.util.HashMap;
// import java.util.Map;
import java.util.concurrent.Executors;
// import java.util.List;

public class WebServerTDIDF {
    private static final String PRIMO_ENDPOINT = "/tdidf";
    private final int port;
    private HttpServer server;
    public static int cont = 0;
    private static Map<Integer, String> librosMap = new HashMap<Integer, String>();

    public static void main(String[] args) {
        int serverPort = 8080;
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }

        try {

            Files.walk(Paths.get("./LIBROS_TXT")).forEach(ruta -> { ///// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><
                if (Files.isRegularFile(ruta)) {
                    System.out.println(cont + "\t" + ruta);
                    librosMap.put(cont, ruta.toString());
                    cont++;
                }
            });

            System.out.println(librosMap.get(1));

        } catch (IOException e) {
            e.printStackTrace();
        }

        WebServerTDIDF webServer = new WebServerTDIDF(serverPort);
        webServer.startServer();

        System.out.println("Servidor escuchando en el puerto " + serverPort);
    }

    public WebServerTDIDF(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            // Creamos una instancia de tipo HttpServer, tamaño de la lista de solicitudes
            // pendiente.
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Creamos un objeto HttpContex a partir de la ruta relativa
        HttpContext statusContext = server.createContext(PRIMO_ENDPOINT);

        statusContext.setHandler(this::handleTaskRequest);
        // Pool de 8 hilos para asignarles tarea
        server.setExecutor(Executors.newFixedThreadPool(8));
        // Iniciamos el servidor
        server.start();
    }

    private void imprimirHeaders(Headers headers) {
        System.out.println("Headers: ");
        System.out.println("--------------------");
        for (Map.Entry<String, List<String>> set : headers.entrySet()) {
            System.out.println(set.getKey() + ": " + set.getValue());
        }
        System.out.println("Total headers: " + headers.size());
        System.out.println("------------------------");
    }

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        // Si no es el metodo post cerramos el exchange
        /*
         * if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
         * exchange.close();
         * return;
         * }
         */
        // Recuperamos los headers
        Headers headers = exchange.getRequestHeaders();
        imprimirHeaders(headers);
        // int inicio = Integer.parseInt(headers.getFirst("inicio"));
        // int fin = Integer.parseInt(headers.getFirst("fin"));
        // guardamos en bytes lo obtenido en el body
        byte[] requestBytes = exchange.getRequestBody().readAllBytes();
        // Ejecutamos el metodo calculateRESPONSE
        byte[] responseBytes = calculateResponse(requestBytes);

        // Enviamos la respuesta en el metodo sendResponse
        sendResponse(responseBytes, exchange);
    }

    private byte[] calculateResponse(byte[] requestBytes) {
        // Pasamos los bytes a String
        String bodyString = new String(requestBytes); /// Se recibe la palabra a buscar, el doc de inicio y el doc de
                                                      /// fin.
        System.out.println("Parametros:" + bodyString);
        String parametros[] = bodyString.split("\t");
        String listaLibros = getLibros(parametros[0], Integer.parseInt(parametros[1]), Integer.parseInt(parametros[2]));
        // int numero = Integer.parseInt(bodyString);
        // String mensaje = "La suma de todos los numeros impares anteriores a " +
        // numero + " es: "+getSuma(numero);
        return listaLibros.getBytes();
    }

    public String getLibros(String buscar, int inicio, int fin) {
        String frecuencias = "";
        try {
            for (int i = inicio; i < fin + 1; i++) { // for (int documento : librosMap.keySet())
                HashMap<String, Integer> mapaDeFrecuencias = new HashMap<>();

                FileReader fr = new FileReader(librosMap.get(i));
                BufferedReader br = new BufferedReader(fr);
                String linea;
                while ((linea = br.readLine()) != null) {
                    // System.out.println(linea);
                    String palabras[] = linea.replaceAll("[\n—()?¿!¡,.»]", "").toLowerCase().split(" ");
                    for (String palabra : palabras) {
                        if (mapaDeFrecuencias.containsKey(palabra)) {
                            mapaDeFrecuencias.put(palabra, mapaDeFrecuencias.get(palabra) + 1);
                        } else {
                            mapaDeFrecuencias.put(palabra, 1);
                        }
                    }
                }
                if (mapaDeFrecuencias.containsKey(buscar)) {
                    System.out.println(buscar + ":" + mapaDeFrecuencias.get(buscar));
                    frecuencias += /* "TF doc_" +*/ i + "\t"
                            + ((float) mapaDeFrecuencias.get(buscar) / mapaDeFrecuencias.size()) + "\n";
                }
                else{
                    frecuencias += i + "\t0\n";
                }
            }

            System.out.println("Resultado de busqueda de "+ buscar + ":\n" + frecuencias);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return frecuencias;
    }

    public BigInteger getSuma(int num) {
        BigInteger suma = BigInteger.valueOf(0);
        System.out.println("NUM: " + num);
        for (int i = 1; i < num; i += 2) {
            suma = suma.add(BigInteger.valueOf(i));
            // System.out.println("x: "+suma);
        }
        System.out.println("Suma: " + suma);
        return suma;
    }

    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        // status code, longitud de la respuesta
        exchange.sendResponseHeaders(200, responseBytes.length);  ////// ___________________________________________  quitar
        OutputStream outputStream = exchange.getResponseBody();
        // escribimos en el body la respuesta
        outputStream.write(responseBytes);
        outputStream.flush();
        // cerramos la solicitud
        outputStream.close();
        exchange.close();
    }
}
