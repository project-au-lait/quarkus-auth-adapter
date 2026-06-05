# Quarkus Auth Adapter

Quarkus Auth Adapter (QAA) is a library for implementing Bearer token and DPoP (Demonstration of Proof-of-Possession) authentication APIs in Quarkus.

The login flow using QAA is as follows:

### Bearer Token Flow

```mermaid
sequenceDiagram
  participant Frontend
  participant Backend as <<Quarkus>><br>Backend<br>+ QAA
  participant Provider as IAM Provider<br>(ex. Keycloak)

  Frontend->>Backend: login(userId, password)
  Backend->>Provider: login(userId, password)
  Provider-->>Backend: token

  Backend-->>Frontend: token
  Frontend->>Backend: call API with token
```

### DPoP Flow

```mermaid
sequenceDiagram
  participant Frontend
  participant Backend as <<Quarkus>><br>Backend<br>+ QAA
  participant Provider as IAM Provider<br>(ex. Keycloak)

  Note over Frontend: Generate key pair
  Frontend->>Backend: login(userId, password, DPoP proof)
  Backend->>Provider: login(userId, password, DPoP proof)
  Provider-->>Backend: DPoP-bound token
  Backend-->>Frontend: DPoP-bound token
  Frontend->>Backend: call API (Authorization: DPoP)
  Note right of Backend: Verify proof is bound to token
```

## Usage

This section explains how to use QAA with Keycloak as the IAM Provider.

Required software:

- Java 21+
- Maven (or another build tool)
- Keycloak

Assume that the OIDC provider (Keycloak) has a Realm and Client already set up.

First, add the QAA dependency to your backend project's `pom.xml`:

```xml
    <dependency>
      <groupId>dev.aulait.qaa</groupId>
      <artifactId>quarkus-auth-adapter-keycloak</artifactId>
      <version>0.8-SNAPSHOT</version>
    </dependency>

    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-hibernate-validator</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-keycloak-admin-rest-client</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-keycloak-authorization</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-rest</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-rest-jackson</artifactId>
    </dependency>
```

Next, add the following property to Quarkus's `application.properties`:

```properties
auth.refreshToken.cookie.timeout=Refresh token expiration time (seconds)
auth.token-endpoint=http://localhost:<keycloak-port>/realms/<realm>/protocol/openid-connect/token
```

Finally, add the properties to integrate Quarkus and Keycloak in `application.properties`:

- [Settings to enable Keycloak authentication/authorization in Quarkus](https://ja.quarkus.io/guides/security-keycloak-authorization#configuring-the-application)
- [Settings to use Keycloak Admin from Quarkus](https://ja.quarkus.io/guides/security-keycloak-admin-client)

### DPoP Configuration (Optional)

To enable DPoP support, add the following properties to `application.properties`:

```properties
# CORS headers for DPoP
quarkus.http.cors.headers=content-type,authorization,DPoP
quarkus.http.cors.exposed-headers=DPoP-Nonce,WWW-Authenticate

# DPoP OIDC tenant
quarkus.oidc.dpop.auth-server-url=http://localhost:<keycloak-port>/realms/<realm>
quarkus.oidc.dpop.client-id=<client-id>
quarkus.oidc.dpop.credentials.secret=<client-secret>
quarkus.oidc.dpop.token.authorization-scheme=dpop

# DPoP nonce TTL
qaa.dpop.nonce-ttl-seconds=DPoP nonce expiration time (seconds)
```

You also need to implement `io.quarkus.oidc.TenantResolver` to route DPoP requests to the `dpop` tenant, and a `DPoPNonceProvider` for server nonce management.

After these settings, start Quarkus and Keycloak, and open [Swagger UI](http://localhost:8080/q/swagger-ui/). The following APIs will be available.

TODO: Export Swagger UI as static HTML, store it under `docs`, and set the URL for access via pages.

For a working implementation example, see the [reference implementation](./refimpl/README.md).
