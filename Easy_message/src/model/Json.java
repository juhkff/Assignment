package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
/**
     Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                 Type type = new TypeToken<Map<String, String>>() {
                  }.getType();
                  Map<String, String> contact = gson.fromJson(resultInfo, type);
  **/
public class Json {
    public static void main(String[] args){
        ChatMessage chatMessage1=new ChatMessage("123", (byte) 0,"20000212","Test1");
        ChatMessage chatMessage2=new ChatMessage("456", (byte) 0,"1231224","Test2");
        ArrayList<ChatMessage> chatMessages=new ArrayList<ChatMessage>();
        chatMessages.add(chatMessage1);
        chatMessages.add(chatMessage2);
        Json json=new Json();
        String toJson=json.ChatMessageToJsonArray(chatMessages);
        System.out.println(toJson);
    }

    private JsonArray jsonElements;
    private Gson gson=new Gson();
    private Gson gsonBuilder=new GsonBuilder().enableComplexMapKeySerialization().create();

    public String ChatMessageToJsonObject(ChatMessage chatMessage){
        return gson.toJson(chatMessage);
    }

    /*public JsonObject ChatMessageToJsonObject(ChatMessage chatMessage){

    }*/

    public String ChatMessageToJsonArray(ArrayList<ChatMessage> chatMessages){
        return gson.toJson(chatMessages);
    }
}
