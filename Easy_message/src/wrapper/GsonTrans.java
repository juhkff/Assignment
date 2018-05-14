package wrapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.property.User;

import java.lang.reflect.Type;

public class GsonTrans {
    private static Gson gson=new Gson();
    private static Gson gson1=new GsonBuilder().enableComplexMapKeySerialization().create();
    private static Type type1=new TypeToken<User>(){}.getType();


    /**
     *
     * @param userString
     * @return User user
     */
    /*用Gson将用户字符串转换为User类对象*/
    public static User stringToUser(String userString){
        return gson1.fromJson(userString,type1);
    }

    /*用Gson将byte[]对象转换为String类对象*/
    public static String bytestoString(byte[] bytes){
        return gson.toJson(bytes);
    }

}
