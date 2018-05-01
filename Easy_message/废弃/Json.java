package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
 * Type type = new TypeToken<Map<String, String>>() {
 * }.getType();
 * Map<String, String> contact = gson.fromJson(resultInfo, type);
 **/
public class Json {

    private JsonArray jsonElements;
    private Gson gson = new Gson();
    private Gson gsonBuilder = new GsonBuilder().enableComplexMapKeySerialization().create();

    public String ChatMessageToJsonObject(ChatMessage chatMessage) {
        return gson.toJson(chatMessage);
    }

    /*public JsonObject ChatMessageToJsonObject(ChatMessage chatMessage){

    }*/

    public String ChatMessageToJsonArray(ArrayList<ChatMessage> chatMessages) {
        return gson.toJson(chatMessages);
    }
}
