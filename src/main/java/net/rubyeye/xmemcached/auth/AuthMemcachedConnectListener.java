package net.rubyeye.xmemcached.auth;

import java.net.InetSocketAddress;
import java.util.Map;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClient;
import net.rubyeye.xmemcached.impl.MemcachedTCPSession;
import net.rubyeye.xmemcached.networking.MemcachedSession;
import net.rubyeye.xmemcached.networking.MemcachedSessionConnectListener;
import net.rubyeye.xmemcached.utils.AddrUtil;

/**
 * Client state listener for auth
 *
 * @author dennis
 *
 */
public class AuthMemcachedConnectListener implements MemcachedSessionConnectListener {

  public void onConnect(MemcachedSession session, MemcachedClient client) {
    MemcachedTCPSession tcpSession = (MemcachedTCPSession) session;
    Map<String, AuthInfo> authInfoMap = client.getAuthInfoStringMap();
    if (authInfoMap != null) {
      AuthInfo authInfo =
          authInfoMap.get(AddrUtil.getServerString(tcpSession.getRemoteSocketAddress()));
      if (authInfo != null) {
        XMemcachedClient xMemcachedClient = (XMemcachedClient) client;
        AuthTask task = new AuthTask(authInfo, xMemcachedClient.getCommandFactory(), tcpSession);
        task.start();
        // First time,try to wait
        if (authInfo.isFirstTime()) {
          try {
            task.join(1000);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      }
    }
  }

}
