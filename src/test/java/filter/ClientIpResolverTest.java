package filter;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientIpResolverTest {

    @Test
    void directRequestsIgnoreEveryForwardedHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("198.51.100.10");
        when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.99");
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.98, 198.51.100.10");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("203.0.113.97");

        ClientIpResolver.ClientAddress address = new ClientIpResolver("192.0.2.10").resolve(request);
        assertEquals("198.51.100.10", address.clientIp());
        assertNull(address.proxyIp());
    }

    @Test
    void trustedNginxPeerUsesOnlyItsSingleRealIp() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.7");
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.66, 203.0.113.7");

        ClientIpResolver.ClientAddress address = new ClientIpResolver("127.0.0.1").resolve(request);
        assertEquals("203.0.113.7", address.clientIp());
        assertEquals("127.0.0.1", address.proxyIp());
    }

    @Test
    void invalidRealIpFallsBackToTheTrustedProxyPeer() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.7, 198.51.100.4");

        ClientIpResolver.ClientAddress address = new ClientIpResolver("127.0.0.1").resolve(request);
        assertEquals("127.0.0.1", address.clientIp());
        assertEquals("127.0.0.1", address.proxyIp());
    }

    @Test
    void trustedProxyCanReportAnIpv6Literal() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn("2001:db8::7");

        ClientIpResolver.ClientAddress address = new ClientIpResolver("127.0.0.1").resolve(request);
        assertTrue(InetAddress.getByName(address.clientIp())
                .equals(InetAddress.getByName("2001:db8::7")));
        assertEquals("127.0.0.1", address.proxyIp());
    }

    @Test
    void trustedProxyCanBeConfiguredByDnsName() throws Exception {
        String peerIp = InetAddress.getAllByName("localhost")[0].getHostAddress();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(peerIp);
        when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.8");

        ClientIpResolver.ClientAddress address = new ClientIpResolver("localhost").resolve(request);
        assertEquals("203.0.113.8", address.clientIp());
        assertEquals(peerIp, address.proxyIp());
    }
}
