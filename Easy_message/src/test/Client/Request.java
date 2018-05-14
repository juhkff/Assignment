package test.Client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//向servlet请求的模版方法
public class Request {
    private String urlAddress;
    private HttpURLConnection uRLConnection;
    private URL url;
    private String resultInfo = null;
    private Map<String, String> parameters;                                 //存储请求参数
    private String response = "";
    private String requestProperty;

    public Request(String urlAddress, Map<String, String> parameters, String RequestProperty) {
        this.urlAddress = urlAddress;
        this.parameters = parameters;
        this.requestProperty = RequestProperty;
    }

    public Request() {

    }

    public void setAll(String urlAddress, Map<String, String> parameters, String RequestProperty) {
        this.urlAddress = urlAddress;
        this.parameters = parameters;
        this.requestProperty = RequestProperty;
    }

    public void setUrlAddress(String urlAddress) {
        this.urlAddress = urlAddress;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void setRequestProperty(String RequestProperty) {
        this.requestProperty = requestProperty;
    }

    public String doPost() {
        try {
            this.url = new URL(urlAddress);
            this.uRLConnection = (HttpURLConnection) url.openConnection();
            this.uRLConnection.setDoInput(true);
            this.uRLConnection.setDoOutput(true);
            this.uRLConnection.setRequestMethod("POST");
            this.uRLConnection.setUseCaches(false);
            this.uRLConnection.setInstanceFollowRedirects(true);
            this.uRLConnection.setRequestProperty("Content-Type", requestProperty);
            this.uRLConnection.connect();

            DataOutputStream dataOutputStream = new DataOutputStream(uRLConnection.getOutputStream());
            String content = "";
            Set<String> paraSet = parameters.keySet();
            Iterator iterator = paraSet.iterator();
            while (iterator.hasNext()) {
                String next = (String) iterator.next();
                if(parameters.get(next)==null){
                    content+=(next + "=" + null + "&");
                }else
                    content += (next + "=" + URLEncoder.encode(parameters.get(next), "UTF-8") + "&");
            }
            content = content.substring(0, content.length() - 1);                                                //截去最后的&，将参数值送入请求
            //for (String parameter:parameters)
            //content += parameter+"="+ URLEncoder.encode(parameter, "UTF-8");;
            dataOutputStream.writeBytes(content);
            dataOutputStream.flush();
            dataOutputStream.close();
            //System.out.println("Inputstream连接...");
            InputStream inputStream = uRLConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String readLine = null;
            response = "";
            while ((readLine = bufferedReader.readLine()) != null) {/**  readLine!=""中readLine是不是指被等号赋值后的readLine?  **/
                //response=bufferedReader.readLine();
                response = response + readLine;
            }
            inputStream.close();
            bufferedReader.close();
            uRLConnection.disconnect();
            resultInfo = response;                                                                            //response或者resultInfo即为从servlet获得的结果
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (resultInfo != null)
            return resultInfo;
        else
            return response;
    }                                                                                                          //返回获得的请求结果

    public Map<String, String> JsonToMap(String JsonFormat) {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> MapFormat = gson.fromJson(JsonFormat, type);
        return MapFormat;
    }
}
