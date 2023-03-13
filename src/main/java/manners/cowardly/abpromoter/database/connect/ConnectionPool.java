package manners.cowardly.abpromoter.database.connect;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.configuration.ConfigurationSection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionPool {

    private HikariDataSource ds;
    private HikariConfig hc;

    public ConnectionPool(String host, String port, String db, String user, String pass, int poolSize) {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + db;
        hc = new HikariConfig();
        hc.setJdbcUrl(url);
        hc.setDriverClassName("com.mysql.jdbc.Driver");
        hc.setUsername(user);
        hc.setPassword(pass);
        hc.setMinimumIdle(poolSize);
        hc.setMaximumPoolSize(poolSize);
        ds = new HikariDataSource(hc);
    }

    /**
     * host: '127.0.0.1' port: '3306' database: 'ABPromoter' user: 'user' pass:
     * 'pass'
     * 
     * @param databaseSection
     */
    public ConnectionPool(ConfigurationSection databaseSection) {
        this(databaseSection.getString("host"), databaseSection.getString("port"), databaseSection.getString("database"),
                databaseSection.getString("user"), databaseSection.getString("pass"),
                databaseSection.getInt("poolSize", 6));
    }

    public void reload(String host, String port, String db, String user, String pass, int poolSize) {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + db;
        hc = new HikariConfig();
        hc.setJdbcUrl(url);
        hc.setDriverClassName("com.mysql.jdbc.Driver");
        hc.setUsername(user);
        hc.setPassword(pass);
        hc.setMinimumIdle(poolSize);
        hc.setMaximumPoolSize(poolSize);
        ds.close();
        ds = new HikariDataSource(hc);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void close() {
        ds.close();
    }

}