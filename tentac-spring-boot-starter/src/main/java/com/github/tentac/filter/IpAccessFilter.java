package com.github.tentac.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter that controls access to Swagger2md endpoints based on
 * IP whitelist/blacklist with CIDR notation support.
 */
public class IpAccessFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IpAccessFilter.class);

    private final List<String> pathPatterns;
    private final List<CidrMatcher> whitelist = new ArrayList<>();
    private final List<CidrMatcher> blacklist = new ArrayList<>();
    private final boolean whitelistEnabled;
    private final boolean blacklistEnabled;

    public IpAccessFilter(List<String> pathPatterns,
                          List<String> whitelistCidrs,
                          List<String> blacklistCidrs) {
        this.pathPatterns = pathPatterns != null ? pathPatterns : new ArrayList<>();
        this.whitelistEnabled = whitelistCidrs != null && !whitelistCidrs.isEmpty();
        this.blacklistEnabled = blacklistCidrs != null && !blacklistCidrs.isEmpty();

        if (whitelistCidrs != null) {
            for (String cidr : whitelistCidrs) {
                try {
                    this.whitelist.add(new CidrMatcher(cidr.trim()));
                } catch (Exception e) {
                    log.warn("Invalid whitelist CIDR: {}", cidr, e);
                }
            }
        }

        if (blacklistCidrs != null) {
            for (String cidr : blacklistCidrs) {
                try {
                    this.blacklist.add(new CidrMatcher(cidr.trim()));
                } catch (Exception e) {
                    log.warn("Invalid blacklist CIDR: {}", cidr, e);
                }
            }
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Only filter matching paths
        String requestPath = request.getRequestURI();
        if (!matchesAnyPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);

        // Blacklist check first (deny if matches)
        if (blacklistEnabled && matchesBlacklist(clientIp)) {
            log.warn("Access denied by blacklist for IP: {} to path: {}", clientIp, requestPath);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("{\"error\":\"Access denied\",\"message\":\"IP is blacklisted\"}");
            response.setContentType("application/json");
            return;
        }

        // Whitelist check (deny if NOT matches)
        if (whitelistEnabled && !matchesWhitelist(clientIp)) {
            log.warn("Access denied - IP not in whitelist: {} to path: {}", clientIp, requestPath);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("{\"error\":\"Access denied\",\"message\":\"IP not in whitelist\"}");
            response.setContentType("application/json");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean matchesAnyPath(String path) {
        if (pathPatterns.isEmpty()) {
            return true;
        }
        for (String pattern : pathPatterns) {
            if (path.equals(pattern) || path.startsWith(pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesWhitelist(String ip) {
        for (CidrMatcher matcher : whitelist) {
            if (matcher.matches(ip)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesBlacklist(String ip) {
        for (CidrMatcher matcher : blacklist) {
            if (matcher.matches(ip)) {
                return true;
            }
        }
        return false;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // Get the first IP in the chain
            int idx = ip.indexOf(',');
            if (idx > 0) {
                ip = ip.substring(0, idx);
            }
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.trim() : "unknown";
    }

    /**
     * Simple CIDR matcher supporting both IPv4 and IPv6.
     */
    private static class CidrMatcher {

        private final InetAddress network;
        private final int prefixLength;
        private final byte[] networkBytes;

        CidrMatcher(String cidr) throws UnknownHostException {
            // Support simple IP without CIDR (treat as /32 or /128)
            String[] parts = cidr.split("/");
            String ip = parts[0];
            this.prefixLength = parts.length > 1 ? Integer.parseInt(parts[1]) : 128;
            this.network = InetAddress.getByName(ip);
            this.networkBytes = this.network.getAddress();
        }

        boolean matches(String ip) {
            try {
                InetAddress addr = InetAddress.getByName(ip);
                byte[] addrBytes = addr.getAddress();

                if (addrBytes.length != networkBytes.length) {
                    return false; // IPv4 vs IPv6 mismatch
                }

                // Compare full bytes
                int fullBytes = prefixLength / 8;
                for (int i = 0; i < fullBytes; i++) {
                    if (addrBytes[i] != networkBytes[i]) {
                        return false;
                    }
                }

                // Compare remaining bits
                int remainingBits = prefixLength % 8;
                if (remainingBits > 0) {
                    int mask = (0xFF << (8 - remainingBits)) & 0xFF;
                    if ((addrBytes[fullBytes] & mask) != (networkBytes[fullBytes] & mask)) {
                        return false;
                    }
                }

                return true;
            } catch (UnknownHostException e) {
                return false;
            }
        }
    }
}
