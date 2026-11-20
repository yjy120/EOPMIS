package com.eopmis.config;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;

/**
 * 强制连接初始化字符集为 utf8mb4。
 *
 * 背景：用户机器 MySQL 服务端默认字符集是 latin1
 * （my.ini 第 101 行 # character-set-server= 被注释），
 * 仅靠 JDBC URL 的 characterEncoding=UTF-8 + Druid 的
 * connection-init-sqls 配置都未生效（行为不可靠）。
 *
 * 因此在 Spring 启动完成后，显式调用
 * DruidDataSource.setConnectionInitSqls(...)，
 * 并主动拿一个连接跑一遍 SET NAMES utf8mb4
 * 让本机连接池立刻按 utf8mb4 工作。
 */
@Slf4j
@Configuration
public class DruidCharsetConfig {

    @Autowired
    private javax.sql.DataSource dataSource;

    @PostConstruct
    public void forceUtf8mb4() {
        try {
            if (dataSource instanceof DruidDataSource) {
                DruidDataSource dds = (DruidDataSource) dataSource;
                dds.setConnectionInitSqls(Arrays.asList("SET NAMES utf8mb4"));
                log.info("[DruidCharsetConfig] 已强制设置 Druid connectionInitSqls = SET NAMES utf8mb4");
            }
            // 主动跑一次 SET NAMES，触发 init sqls，并验证编码
            try (Connection c = dataSource.getConnection();
                 Statement s = c.createStatement()) {
                s.execute("SET NAMES utf8mb4");
                log.info("[DruidCharsetConfig] 已执行 SET NAMES utf8mb4 验证成功");
            }
        } catch (Exception e) {
            log.error("[DruidCharsetConfig] 设置 utf8mb4 失败", e);
        }
    }
}