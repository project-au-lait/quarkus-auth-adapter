package dev.aulait.qaa.qkref;

import io.quarkus.oidc.TenantResolver;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DPoPTenantResolver implements TenantResolver {

    @Override
    public String resolve(RoutingContext context) {
        String auth = context.request().headers().get("Authorization");
        if (auth != null && auth.startsWith("DPoP ")) {
            return "dpop";
        }
        return null; // default tenant (Bearer)
    }
}
