package system.core;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Reader {

    private static final String URL = "";
    private static final String CRLF = "\r\n";
    private static final int TIME_OUT = 6000;
    private ArrayList<String> lines = new ArrayList<>();

    public Reader(File file) throws FileNotFoundException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            while (reader.ready()) {
                lines.add(reader.readLine());
            }
        } catch (Exception ex) {
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
            }
        }
    }

    public void sendLines() {
        for (String line : lines) {

            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        lines = null;
    }

    public boolean sendFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        boolean returned = false;
        HttpURLConnection connection = null;
        try {
            connection = createConnection();
            // Just generate some unique random value
            String boundary = Long.toHexString(System.currentTimeMillis());
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                // Send binary file.
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"").append(CRLF);
                writer.append("Content-Type: text/plain");
                writer.append(CRLF);
                writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                writer.append(CRLF).flush();
                InputStream input = null;
                try {
                    input = new FileInputStream(file);
                    byte[] buffer = new byte[1024];
                    for (int length = 0; (length = input.read(buffer)) > 0;) {
                        connection.getOutputStream().write(buffer, 0, length);
                    }
                    // Important! Output cannot be closed. Close of writer will close output as well.
                    connection.getOutputStream().flush();
                } finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException ex) {
                        }
                    }
                }
                // CRLF is important! It indicates end of binary boundary.
                writer.append(CRLF).flush();
                // End of multipart/form-data.
                writer.append("--" + boundary + "--").append(CRLF);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
            returned = handleResponse(connection);
        } catch (Exception ex) {
            System.out.println("Erro durante o envio do arquivo de log ao PACS: " + file.getAbsolutePath() + ex);
            returned = false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return returned;
    }

    private HttpURLConnection createConnection() throws MalformedURLException, ProtocolException, IOException {
        System.out.println("Open Connection... " + URL);
        HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
        connection.setInstanceFollowRedirects(false); //será que isso precisa?
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setReadTimeout(TIME_OUT);
        connection.setConnectTimeout(TIME_OUT);
//        connection.setRequestProperty("Cookie", "JSESSIONID=" + workstation.getSessionID());
        // This sets request method to POST
        connection.setDoOutput(true);
        return connection;
    }

    private boolean handleResponse(HttpURLConnection connection) throws IOException {
        // Connection is lazily executed whenever you request any status.
        int responseCode = ((HttpURLConnection) connection).getResponseCode();
        System.out.println("ResponseCode=" + getReasonErrorCode(responseCode));
        int contentLength = ((HttpURLConnection) connection).getContentLength();
        if (contentLength > 2) {
            InputStream contentSt = ((HttpURLConnection) connection).getInputStream();
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = contentSt.read(buffer)) != -1) {
                bao.write(buffer, 0, length);
            }
            System.out.println("ContentLength=" + contentLength);
            System.out.println("Message=" + new String(bao.toByteArray(), Charset.forName("UTF-8")));
            bao.close();
        }
        if (responseCode != 200 || contentLength > 2) {
            System.out.println("Erro na conecção com o PACS!");
            return false;
        }
        return true;
    }

    private String getReasonErrorCode(int errorCode) {
        String msg = errorCode + " - ";
        switch (errorCode) {
            case 200:
                msg += "ok!";
                break;
            case 400:
                msg += "O pedido não pode ser entregue devido à sintaxe incorreta";
                break;
            case 404:
                msg += "O recurso requisitado não foi encontrado";
                break;
            case 414:
                msg += "O URI fornecido foi muito longo para ser processado pelo servidor";
                break;
            case 500:
                msg += "Erro interno do servidor";
                break;
            case 501:
                msg += "O servidor ainda não suporta a funcionalidade ativada";
                break;
            default:
                msg = "" + errorCode;
        }
        return msg;
    }
}
