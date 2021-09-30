package org.example.framework.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.framework.attribute.ContextAttributes;
import org.example.framework.attribute.RequestAttributes;
import org.example.framework.security.*;
import org.postgresql.util.Base64;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BasicAuthenticationFilter extends HttpFilter implements AuthenticationFilter {
    private AuthenticationProvider provider;

    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);
        provider = ((AuthenticationProvider) getServletContext().getAttribute(ContextAttributes.AUTH_PROVIDER_ATTR));
    }

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (!authenticationIsRequired(req)) {
            super.doFilter(req, res, chain);
            return;
        }

        final var header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Basic ")) {
            super.doFilter(req, res, chain);
            return;
        }

        try {
            final var usernamePassword = new String(Base64.decode(header.substring("Basic ".length())),
                    StandardCharsets.UTF_8).split(":", 2);
            if (usernamePassword.length != 2) throw new AuthenticationException();

            final var username = usernamePassword[0].trim().toLowerCase();
            final var password = usernamePassword[1].trim();

            final var authentication = provider
                    .authenticateBasic(new BasicAuthentication(username, password));

            req.setAttribute(RequestAttributes.AUTH_ATTR, authentication);
        } catch (AuthenticationException e) {
            res.sendError(401);
            return;
        }

        super.doFilter(req, res, chain);
    }
}
