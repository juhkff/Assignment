package server.jsonupdate;

import com.google.gson.Gson;
import connection.Conn;
import model.contact.Sort;
import server.model.ServerCommand;
import wrapper.StaticVariable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class UpdateGroupSortJsonThread implements Runnable{
    private String userID;
    private ArrayList<Sort> sorts=new ArrayList<Sort>();
    private BlockingQueue<ServerCommand> groupSortJsonCommand;

    public UpdateGroupSortJsonThread(String userID, BlockingQueue<ServerCommand> groupSortJsonCommand) {
        this.userID = userID;
        this.groupSortJsonCommand = groupSortJsonCommand;
    }

    @Override
    public void run() {
        Connection connection=Conn.getConnection();
        String sql="SELECT sort FROM user_"+userID+"_contactlist WHERE types=1 GROUP BY sort";
        try {
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            ResultSet resultSet=preparedStatement.executeQuery();
            while (resultSet.next()){
                String sort=resultSet.getString("sort");
                Sort groupSort=new Sort(sort);
                sorts.add(groupSort);
            }

            //得到分组列表后
            Gson gson=new Gson();
            String fileContent=gson.toJson(sorts);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream=new FileOutputStream(StaticVariable.getUserinfoSortGroups(userID));
                fileOutputStream.write(fileContent.getBytes());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(fileOutputStream!=null){
                    try {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            ServerCommand serverCommand=new ServerCommand(userID,"GroupSort");
            try {
                groupSortJsonCommand.put(serverCommand);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
