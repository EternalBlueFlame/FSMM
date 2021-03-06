package net.fexcraft.mod.lib.fcl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


/**
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class HttpUtil {

    /** Requests a JsonObject from the given adress and parameters, using the POST HTML method. */
    public static JsonObject request(String adress, String parameters, String[] cookies){
        try{
            URL url = new URL(adress);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            if(cookies != null && cookies.length > 0){
                String str = ""; for(int i = 0; i < cookies.length; i++){
                    str += cookies[i]; if(i != cookies.length - 1){ str += "; "; }
                } connection.setRequestProperty("Cookie", str);
            }
            connection.setConnectTimeout(5000);
            connection.setDoOutput(true);
            //
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(parameters); wr.flush(); wr.close();
            //
            JsonObject cook = new JsonObject();
            for(int i = 0;; i++){
                String name = connection.getHeaderFieldKey(i), val = connection.getHeaderField(i);
                if(name == null && val == null){ break; }
                if("Set-Cookie".equalsIgnoreCase(name)){
                    String[] fields = val.split(";\\s*"), split;
                    for(String str : fields){
                        if((split = str.split("=")).length >= 2){
                            cook.addProperty(split[0], split[1]);
                        }
                    }
                }
            }
            JsonObject obj = JsonUtil.getObjectFromInputStream(connection.getInputStream());
            if(cook.entrySet().size() > 0) obj.add(obj.has("cookies") ? "%http:cookies%" : "cookies", cook);
            connection.disconnect(); return obj;
        }
        catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static JsonObject request(String adress, String parameters){
        return request(adress, parameters, null);
    }

    public static JsonElement request(String adress, String[] cookies){
        try{
            URL url = new URL(adress);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            if(cookies != null && cookies.length > 0){
                String str = ""; for(int i = 0; i < cookies.length; i++){
                    str += cookies[i]; if(i != cookies.length - 1){ str += "; "; }
                } connection.setRequestProperty("Cookie", str);
            }
            connection.setConnectTimeout(5000);
            //
            JsonObject cook = new JsonObject();
            for(int i = 0;; i++){
                String name = connection.getHeaderFieldKey(i), val = connection.getHeaderField(i);
                if(name == null && val == null){ break; }
                if("Set-Cookie".equalsIgnoreCase(name)){
                    String[] fields = val.split(";\\s*"), split;
                    for(String str : fields){
                        if((split = str.split("=")).length >= 2){
                            cook.addProperty(split[0], split[1]);
                        }
                    }
                }
            }
            //
            JsonElement obj = JsonUtil.getElementFromInputStream(connection.getInputStream());
            if(obj.isJsonObject() && cook.entrySet().size() > 0) obj.getAsJsonObject().add(obj.getAsJsonObject().has("cookies") ? "%http:cookies%" : "cookies", cook);
            connection.disconnect(); return obj;
        }
        catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static JsonElement request(String adress){
        return request(adress, (String[])null);
    }

}
