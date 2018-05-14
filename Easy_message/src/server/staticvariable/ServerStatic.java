package server.staticvariable;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerStatic {
    public static Map<String,Socket> socketMap=new HashMap<String, Socket>();
}
