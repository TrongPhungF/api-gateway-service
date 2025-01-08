package authservice.org.gatewayservice.filter;

import io.jsonwebtoken.Claims;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Log4j2
public class AuthenticationFilter implements GlobalFilter {

  private final RouterValidator routerValidator;

  private final JwtUtil jwtUtil;

  private final FilterUtils filterUtils;

  @Override
  // Code that executes everytime when a request passes through filter
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    HttpHeaders requestHeader = exchange.getRequest().getHeaders(); // HTTP header from request
    ServerHttpRequest request = exchange.getRequest();
    if (isCorrelationIdPresent(requestHeader)) {
      log.info("API Gateway | TrackingFilter | filter | correlation_id found in trackingfilter: {}",
          filterUtils.getCorrelationId(requestHeader));
    } else {
      String correlationId = generateCorrelationId(); // if correlation_id not found in header, generate one
      exchange = filterUtils.setCorrelationId(exchange, correlationId);
      log.info(
          "API Gateway | TrackingFilter | filter | correlation_id generated in tracking filter: {}",
          correlationId);
    }
    if (routerValidator.isSecured.test(request)) {
        if (this.isAuthMissing(request)) {
            return this.onError(exchange, "Authorization header is missing in request",
                HttpStatus.UNAUTHORIZED);
        }
      final String token = this.getAuthHeader(request);
        if (jwtUtil.isInvalid(token)) {
            return this.onError(exchange, "Authorization header is invalid",
                HttpStatus.UNAUTHORIZED);
        }
      this.populateRequestWithHeaders(exchange, token);
    }
    return chain.filter(exchange);
  }


  private boolean isCorrelationIdPresent(HttpHeaders header) {
    return filterUtils.getCorrelationId(header) != null;
  }

  private String generateCorrelationId() {
    return UUID.randomUUID().toString();
  }

  private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(httpStatus);
    return response.setComplete();
  }

  private String getAuthHeader(ServerHttpRequest request) {
    return request.getHeaders().getOrEmpty("Authorization").get(0).split(" ")[1].trim();
  }

  private boolean isAuthMissing(ServerHttpRequest request) {
    return !request.getHeaders().containsKey("Authorization");
  }

  private void populateRequestWithHeaders(ServerWebExchange exchange, String token) {
    Claims claims = jwtUtil.getAllClaimsFromToken(token);
    exchange.getRequest().mutate()
        .header("id", String.valueOf(claims.get("id")))
        .header("role", String.valueOf(claims.get("role")))
        .build();
  }
}
