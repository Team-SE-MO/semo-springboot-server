package sandbox.semo.domain.common.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import sandbox.semo.domain.common.crypto.AES256;
import sandbox.semo.domain.device.entity.DatabaseType;
import sandbox.semo.domain.device.entity.Device;

public class HikariDataSourceUtil {

    public static HikariDataSource createDataSource(Device device, AES256 aes256) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(buildJdbcUrl(device));
        config.setUsername(device.getUsername());
        config.setPassword(aes256.decrypt(device.getPassword()));
        config.setDriverClassName(getDriverClassName(device.getType()));
        config.setInitializationFailTimeout(3000);
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }

    private static String buildJdbcUrl(Device device) {
        String template = switch (device.getType()) {
            case ORACLE -> "jdbc:oracle:thin:@%s:%d:%s";
            case MYSQL -> "jdbc:mysql://%s:%d/%s";
            case POSTGRESQL -> "jdbc:postgresql://%s:%d/%s";
            case MARIADB -> "jdbc:mariadb://%s:%d/%s";
            case SQLSERVER -> "jdbc:sqlserver://%s:%d;databaseName=%s";
        };
        return String.format(template, device.getIp(), device.getPort(), device.getSid());
    }

    private static String getDriverClassName(DatabaseType dbType) {
        return switch (dbType) {
            case ORACLE -> "oracle.jdbc.OracleDriver";
            case MYSQL -> "com.mysql.cj.jdbc.Driver";
            case POSTGRESQL -> "org.postgresql.Driver";
            case MARIADB -> "org.mariadb.jdbc.Driver";
            case SQLSERVER -> "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        };
    }
}
