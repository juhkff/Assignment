package connection;

import java.sql.*;
import java.util.Map;

public class ConnectionModel {
    private Connection connection;
    private PreparedStatement preparedStatement;
    private Statement statement;
    private String sql;
    private String[] sqlbatch;
    private int result;
    private String[] parameters;

    public ConnectionModel(String[] parameters) {
        this.parameters = parameters;
    }


    public ResultSet prepareConnect(String sql) throws SQLException {
        this.sql = sql;
        this.connection = Conn.getConnection();
        this.preparedStatement = connection.prepareStatement(this.sql);
        for (int i = 0; i < parameters.length; i++) {
            this.preparedStatement.setString(i + 1, parameters[i]);
        }
        return this.preparedStatement.executeQuery();
    }
}
