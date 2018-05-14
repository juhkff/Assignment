package server.model;

public class ServerCommand {
    private String userID;
    private String command;

    public ServerCommand(String userID,String command) {
        this.userID=userID;
        this.command = command;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
