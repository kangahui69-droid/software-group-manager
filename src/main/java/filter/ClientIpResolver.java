package filter;

import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/** Resolves a client IP only through peers explicitly configured as trusted proxies. */
final class ClientIpResolver {
    static final String TRUSTED_PROXY_HOSTS_ENV = "AUDIT_TRUSTED_PROXY_HOSTS";

    private final List<String> trustedProxyHosts;

    ClientIpResolver() {
        this(System.getenv(TRUSTED_PROXY_HOSTS_ENV));
    }

    ClientIpResolver(String hosts) {
        trustedProxyHosts = new ArrayList<>();
        if (hosts != null) {
            for (String host : hosts.split(",")) {
                if (!host.trim().isEmpty()) trustedProxyHosts.add(host.trim());
            }
        }
    }

    ClientAddress resolve(HttpServletRequest request) {
        String peerIp = normalizeIpLiteral(request.getRemoteAddr());
        if (peerIp == null) return new ClientAddress(null, null);

        if (!isTrustedProxy(peerIp)) return new ClientAddress(peerIp, null);

        // Nginx overwrites X-Real-IP with its direct client's address. Do not use
        // X-Forwarded-For: an incoming chain may contain values supplied by clients.
        String forwardedIp = normalizeIpLiteral(request.getHeader("X-Real-IP"));
        return new ClientAddress(forwardedIp == null ? peerIp : forwardedIp, peerIp);
    }

    private boolean isTrustedProxy(String peerIp) {
        if (trustedProxyHosts.isEmpty()) return false;
        try {
            InetAddress peer = InetAddress.getByName(peerIp);
            for (String host : trustedProxyHosts) {
                try {
                    for (InetAddress address : InetAddress.getAllByName(host)) {
                        if (peer.equals(address)) return true;
                    }
                } catch (UnknownHostException ignored) {
                    // DNS failure must not make request headers trustworthy.
                }
            }
        } catch (UnknownHostException ignored) {
            return false;
        }
        return false;
    }

    private String normalizeIpLiteral(String value) {
        if (value == null || value.isEmpty() || value.length() > 45 || !value.equals(value.trim())) {
            return null;
        }
        if (value.indexOf(':') >= 0) {
            if (!value.matches("[0-9A-Fa-f:.]+")) return null;
            try {
                return InetAddress.getByName(value).getHostAddress();
            } catch (UnknownHostException ignored) {
                return null;
            }
        }
        String[] octets = value.split("\\.", -1);
        if (octets.length != 4) return null;
        for (String octet : octets) {
            if (!octet.matches("0|[1-9][0-9]{0,2}")) return null;
            if (Integer.parseInt(octet) > 255) return null;
        }
        return value;
    }

    static final class ClientAddress {
        private final String clientIp;
        private final String proxyIp;

        ClientAddress(String clientIp, String proxyIp) {
            this.clientIp = clientIp;
            this.proxyIp = proxyIp;
        }

        String clientIp() { return clientIp; }
        String proxyIp() { return proxyIp; }
    }
}
