package org.tkit.onecx.workspace.rs.user.services;

import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ClaimService {

    @SuppressWarnings("java:S5998")
    private static final Pattern CLAIM_PATH_PATTERN = Pattern.compile("\\/(?=(?:(?:[^\"]*\"){2})*[^\"]*$)");

    private static String[] claimPath;

    @Inject
    TokenConfig config;

    @PostConstruct
    @SuppressWarnings("java:S2696")
    public void init() {
        claimPath = splitClaimPath(config.token().claimPath());
    }

    public String[] getClaimPath() {
        return claimPath;
    }

    @SuppressWarnings("java:S2692")
    static String[] splitClaimPath(String claimPath) {
        return claimPath.indexOf('/') > 0 ? CLAIM_PATH_PATTERN.split(claimPath) : new String[] { claimPath };
    }
}
