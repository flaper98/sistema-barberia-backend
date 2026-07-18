package com.barberia.modules.publicbooking.sync;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Spring Boot solo auto-configura un {@link DataSource}. Al declarar el segundo
 * (Postgres, para el sync de citas públicas), hay que redeclarar también el
 * principal explícitamente -- por eso este {@code dataSource()} reemplaza al que
 * antes creaba la auto-configuración, reusando las mismas properties
 * {@code spring.datasource.*} (incluida la afinación de Hikari) para que el
 * comportamiento no cambie.
 *
 * El datasource de Vercel se construye siempre (no con
 * {@code @ConditionalOnProperty}, que trataría el default vacío del YAML como
 * "presente" e intentaría conectar con una URL en blanco). Si no está
 * configurado, el pool se crea apuntando a una URL placeholder con
 * {@code initializationFailTimeout(-1)} para no romper el arranque -- recién
 * falla si algo intenta usarlo de verdad, y {@link VercelCitasSyncService} ya
 * chequea esto antes de intentar nada.
 */
@Slf4j
@Configuration
public class VercelCitasDataSourceConfig {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "vercelCitasDataSource")
    public DataSource vercelCitasDataSource(
            @Value("${app.sync.vercel-database-url:}") String rawUrl) {

        if (!StringUtils.hasText(rawUrl)) {
            log.info("VERCEL_CITAS_DATABASE_URL no configurada -- el sync de citas públicas queda inactivo");
            HikariDataSource unconfigured = new HikariDataSource();
            unconfigured.setJdbcUrl("jdbc:postgresql://unconfigured.invalid:5432/unconfigured");
            unconfigured.setInitializationFailTimeout(-1);
            return unconfigured;
        }

        URI uri = URI.create(rawUrl);
        String[] userInfo = uri.getUserInfo().split(":", 2);
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath() + "?sslmode=require";

        log.info("Configurando datasource Postgres (Vercel) para sync de citas: host={}", uri.getHost());

        HikariDataSource ds = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(jdbcUrl)
                .username(userInfo[0])
                .password(userInfo.length > 1 ? userInfo[1] : "")
                .build();
        ds.setInitializationFailTimeout(-1);
        return ds;
    }

    @Bean
    public JdbcTemplate vercelCitasJdbcTemplate(
            @Qualifier("vercelCitasDataSource") DataSource vercelCitasDataSource) {
        return new JdbcTemplate(vercelCitasDataSource);
    }
}
