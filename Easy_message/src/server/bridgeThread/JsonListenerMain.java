package server.bridgeThread;

import connection.Conn;
import server.jsonupdate.UpdateUserJsonThread;
import server.model.ServerCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class JsonListenerMain implements Runnable {
    private BlockingQueue<ServerCommand> friendJsonCommand;
    private BlockingQueue<ServerCommand> friendSortJsonCommand;
    private BlockingQueue<ServerCommand> groupJsonCommand;
    private BlockingQueue<ServerCommand> groupSortJsonCommand;

    public JsonListenerMain(BlockingQueue<ServerCommand> friendJsonCommand, BlockingQueue<ServerCommand> friendSortJsonCommand, BlockingQueue<ServerCommand> groupJsonCommand, BlockingQueue<ServerCommand> groupSortJsonCommand) {
        this.friendJsonCommand = friendJsonCommand;
        this.friendSortJsonCommand = friendSortJsonCommand;
        this.groupJsonCommand = groupJsonCommand;
        this.groupSortJsonCommand = groupSortJsonCommand;
    }

    @Override
    public void run() {
        Connection connection = Conn.getConnection();
        String sql = "SELECT userID FROM userinfo";
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            Set<String> userSet = new HashSet<String>();
            while (resultSet.next()) {
                String userID = resultSet.getString("userID");
                userSet.add(userID);
                System.out.println(userID);
                //对表中每个用户设置监听线程
                UpdateUserJsonThread updateUserJsonThread = new UpdateUserJsonThread(userID,friendJsonCommand,friendSortJsonCommand,groupJsonCommand,groupSortJsonCommand);
                Thread thread = new Thread(updateUserJsonThread);
                thread.start();
            }
            Conn.Close();

            String userString = String.valueOf(userSet);
            userString = "(" + userString.substring(1, userString.length() - 1) + ")";
            while (true) {
                Thread.sleep(5000);
                Connection connection1 = Conn.getConnection();
                String sql1 = "SELECT userID FROM userinfo WHERE userID NOT IN " + userString;
                PreparedStatement preparedStatement1 = connection1.prepareStatement(sql1);
                ResultSet resultSet1 = preparedStatement1.executeQuery();
                while (resultSet1.next()) {
                    String userID = resultSet1.getString("userID");
                    userSet.add(userID);
                    System.out.println(userID);
                    UpdateUserJsonThread updateUserJsonThread = new UpdateUserJsonThread(userID,friendJsonCommand,friendSortJsonCommand,groupJsonCommand,groupSortJsonCommand);
                    Thread thread = new Thread(updateUserJsonThread);
                    thread.start();
                }
                userString = String.valueOf(userSet);
                userString = "(" + userString.substring(1, userString.length() - 1) + ")";
                Conn.Close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
