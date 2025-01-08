package authservice.org.gatewayservice.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@Component
public class FilterUtils {
    public static final String CORRELATION_ID = "correlation-id";

    public String getCorrelationId(HttpHeaders requestHeaders) {
        List<String> correlationIds = requestHeaders.get(CORRELATION_ID);
        return correlationIds != null && !correlationIds.isEmpty() ? correlationIds.get(0) : null;
    }
    public ServerWebExchange setRequestHeader(ServerWebExchange exchange, String name, String value) {
        return exchange.mutate().request(
                exchange.getRequest().mutate()
                        .header(name, value)
                        .build())
                .build();
    }

    public ServerWebExchange setCorrelationId(ServerWebExchange exchange, String correlationId) {
        return this.setRequestHeader(exchange, CORRELATION_ID, correlationId);
    }
}
