package org.shirakawatyu.btmetadataserver.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.Statement;

@Configuration
public class SqliteConfig {
    public SqliteConfig(HikariDataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement())
        {
            String sql = "create table if not exists metadata (info_hash text primary key, info text, torrent blob)";
            statement.executeUpdate(sql);
            sql = "create table if not exists extra_data (info_hash text primary key, title text, publish text, category text)";
            statement.executeUpdate(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
