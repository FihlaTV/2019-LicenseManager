import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.*;
public class Licencheck {

    private String endpoint = "localhost:8080/licencheck";
    private HttpURLConnection con;



    public boolean checkAccount(String userName, String password) {
        URL url = null;
        try {
            url = new URL("http://localhost:8080/licencheck/checkAccount");
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        if(url!=null){
            return this.makeRequestBooleanResponse(url,userName,password);
        }else{
            return false;
        }

    }

    //TODO The password can be removed 
    //Return values --> NULL (if check == false ) , type license (L,M,D,Y) = (Life,Month,Day,Year)
    public String checkLicense(String licenseSerial, String productName, String userName, String password){
        try {
            URL url = new URL("http://localhost:8080/licencheck/checkLicense/" + productName + "/" + licenseSerial);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(this.makeUserJSON(userName, password).toString());
            wr.flush();

            int HttpResult = con.getResponseCode();


            StringBuilder sb = new StringBuilder();

            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                JsonObject jsonObject = new JsonParser().parse(sb.toString()).getAsJsonObject();
                JsonElement type = jsonObject.get("type");
                System.out.println("The type of the license is --> " + type.getAsString());
                br.close();
                con.disconnect();
                return type.getAsString();
            } else {
                con.disconnect();
                return null;  //No existe la licencia para ese usuario (con su respectiva contraseña) y producto
            }
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }

    }

    private JSONObject makeUserJSON(String userName, String password){
        JSONObject user = new JSONObject();
        user.put("userName",userName);
        user.put("password", password);
        return user;
    }

    private boolean makeRequestBooleanResponse(URL url,String userName, String password) {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(this.makeUserJSON(userName, password).toString());
            wr.flush();

            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                con.disconnect();
                return true;
            } else {
                con.disconnect();
                return false;
            }
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

}