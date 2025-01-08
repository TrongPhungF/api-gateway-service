package authservice.org.gatewayservice.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

    public static final List<String> openApiEndpoints = List.of(
        "auth"
        ,"email"
        ,"public"
        ,"nearest"
        ,"GPS"
        ,"s3"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(request.getURI().getPath()::contains);
}
