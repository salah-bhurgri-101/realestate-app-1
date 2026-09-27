package com.example.auditstarter.config;

import com.example.auditstarter.AuditClient;
import com.example.auditstarter.impl.WebClientAuditClient;
import io.netty.channel.ChannelOption;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Auto-wires the HTTP/WebClient transport — the default. Active unless
 * audit.client.transport=kafka is set. The audit.client.enabled flag is
 * enforced at runtime inside WebClientAuditClient (methods no-op when
 * disabled) rather than at bean-creation time, so toggling it doesn't
 * require an application restart context refresh either way.
 *
 * Registered via META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 */
@Configuration
@EnableConfigurationProperties(AuditClientProperties.class)
@ConditionalOnProperty(prefix = "audit.client", name = "transport", havingValue = "webclient", matchIfMissing = true)
public class AuditWebClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "auditWebClient")
    public WebClient auditWebClient(AuditClientProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.getTimeout().toMillis())
                .responseTimeout(properties.getTimeout());

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient));

        if (properties.getServiceToken() != null && !properties.getServiceToken().isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getServiceToken());
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(AuditClient.class)
    public AuditClient auditClient(WebClient auditWebClient, AuditClientProperties properties) {
        return new WebClientAuditClient(auditWebClient, properties);
    }
}
