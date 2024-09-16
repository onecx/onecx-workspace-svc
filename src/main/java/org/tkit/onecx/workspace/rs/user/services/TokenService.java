package org.tkit.onecx.workspace.rs.user.services;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.tkit.quarkus.rs.context.token.TokenClaimUtility;
import org.tkit.quarkus.rs.context.token.TokenParserRequest;
import org.tkit.quarkus.rs.context.token.TokenParserService;

import io.quarkus.oidc.common.runtime.OidcConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class TokenService {

    @Inject
    TokenConfig config;

    @Inject
    ClaimService claimService;

    @Inject
    TokenParserService tokenParserService;

    private static final String BEARER_PREFIX = OidcConstants.BEARER_SCHEME + " ";

    public List<String> getTokenRoles(String tokenData) {

        try {
            var token = tokenData;
            if (token.startsWith(BEARER_PREFIX)) {
                token = token.substring(BEARER_PREFIX.length());
            }

            var request = new TokenParserRequest(token)
                    .verify(config.config().verified())
                    .issuerEnabled(config.config().publicKeyEnabled())
                    .issuerSuffix(config.config().publicKeyLocationSuffix());

            var permissionToken = tokenParserService.parseToken(request);
            var path = claimService.getClaimPath();
            return TokenClaimUtility.findClaimStringList(permissionToken, path, config.config().claimSeparator().orElse(" "));

        } catch (Exception ex) {
            throw new TokenException("Error parsing permission token", ex);
        }
    }

    public static class TokenException extends RuntimeException {

        public TokenException(String message, Throwable t) {
            super(message, t);
        }
    }
}
