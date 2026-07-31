# Authentication

## Overview

Authentication verifies that a principal—human, service, or device—is who it claims to be. It establishes identity; [authorization](../Authorization/README.md) separately determines permitted actions.

## Why do we need it?

Systems need a trustworthy identity before protecting private data, attributing operations, applying policy, or producing audit records. Authentication also limits account takeover through credential protection, multi-factor verification, session controls, and anomaly detection.

## How does it work?

### Password authentication

The server stores a salted output from a password-hashing function, not the password or a reversible encryption. On login it hashes the supplied password using the stored parameters and compares in constant time. Argon2id is a preferred modern choice; bcrypt and scrypt remain established options when configured appropriately.

Rate limiting, breached-password screening, multi-factor authentication, and generic failure messages reduce guessing and enumeration risk.

### Sessions

With server-side sessions, the browser stores an opaque identifier in a cookie while session state remains server-side. Cookies should use `Secure`, `HttpOnly`, and an appropriate `SameSite` policy. Rotate the identifier after authentication or privilege changes and invalidate it on logout or compromise.

Stateful sessions simplify revocation but require shared or sticky session storage across instances. CSRF protection is required when browsers automatically attach credentials to cross-site requests.

### Tokens

Bearer access tokens authorize possession; anyone who obtains one can use it until expiry or revocation. JWT is a token format, not an authentication protocol. Validate the expected algorithm, signature, issuer, audience, time claims, and application-specific claims. Keep access tokens short-lived and never accept an algorithm chosen outside trusted configuration.

Refresh-token rotation can provide longer sessions while detecting reuse. Store browser tokens in secure cookies when possible; persistent JavaScript-accessible storage increases exposure to cross-site scripting.

### Federated identity

OAuth 2.0 is an authorization framework. OpenID Connect adds authentication and identity claims. For browser and native clients, Authorization Code with PKCE protects the code exchange. Validate `state`, `nonce` where applicable, redirect URIs, issuer, and token audience.

Service authentication commonly uses short-lived workload identities, signed tokens, or mutual TLS instead of shared long-lived secrets.

### Trade-offs

- Server sessions support immediate revocation but need centralized state.
- Self-contained tokens reduce per-request lookup but complicate revocation and claim freshness.
- Passwordless and federated login reduce local password risk but add provider and recovery dependencies.
- Stronger factors improve assurance but can increase user friction and recovery complexity.

## Advantages

- Establishes accountable identity and protected sessions.
- Enables user-specific data and policy.
- Supports federation and single sign-on.
- Multi-factor authentication reduces password-only compromise.
- Short-lived workload identity reduces standing credentials.

## Limitations

- No method eliminates phishing, endpoint compromise, or unsafe recovery.
- Bearer tokens are usable by an attacker who steals them.
- Session and key lifecycle management add operational complexity.
- Federation creates external availability and trust dependencies.
- Authentication alone does not enforce resource permissions.

## Best Practices

- Follow established protocols and maintained libraries; do not design custom cryptography.
- Hash passwords with Argon2id and unique salts; tune cost and support rehashing.
- Require MFA for sensitive access and secure account recovery to comparable strength.
- Use short-lived credentials, key rotation, explicit issuer and audience validation, and least claim disclosure.
- Rotate sessions after login and privilege change; terminate compromised sessions.
- Rate-limit attempts without enabling trivial denial of service.
- Never log passwords, session identifiers, authorization codes, or tokens.

## Common Mistakes

- Encrypting passwords instead of hashing them.
- Using JWTs because a system is distributed, without a revocation or freshness design.
- Treating OAuth access tokens as proof of user identity without OpenID Connect validation.
- Storing long-lived bearer tokens in insecure browser storage.
- Omitting CSRF defenses for cookie-authenticated state changes.
- Returning different login or recovery messages that enumerate accounts.
- Building a weaker recovery path than the primary login path.

## Real-world examples

- A web application uses an opaque, rotating session cookie and server-side session store.
- A mobile application uses OpenID Connect Authorization Code with PKCE.
- An administrator must provide a phishing-resistant second factor for sensitive operations.
- A service obtains a short-lived workload token from its platform identity provider.

## Interview Questions

1. **Authentication versus authorization?** Authentication establishes identity; authorization evaluates whether that identity may perform an action.
2. **Session versus JWT?** Sessions centralize state and revocation; JWTs carry signed claims but require expiry, key, revocation, and freshness design.
3. **Why salt password hashes?** A unique salt prevents identical hashes and defeats precomputed tables.
4. **OAuth 2.0 versus OpenID Connect?** OAuth delegates authorization; OpenID Connect adds an interoperable authentication layer.
5. **What does PKCE prevent?** It prevents a stolen authorization code from being redeemed without the client's one-time verifier.
6. **Why rotate refresh tokens?** Rotation limits replay and allows reuse detection.
7. **When is CSRF relevant?** When a browser automatically sends credentials, such as cookies, on a cross-site request.

## Interview Tips

Start with threat model and client type. Name the credential, where it is stored, how it is validated, its lifetime, rotation and revocation path, and recovery behavior. Avoid saying JWT is “more secure” or “stateless” without qualification.

## References

- [NIST SP 800-63B: Authentication and Lifecycle Management](https://pages.nist.gov/800-63-4/sp800-63b.html)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
- [RFC 9700: OAuth 2.0 Security Best Current Practice](https://www.rfc-editor.org/rfc/rfc9700)
- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)
- [RFC 7519: JSON Web Token](https://www.rfc-editor.org/rfc/rfc7519)
