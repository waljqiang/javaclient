package lib.ssl;

import javax.net.ssl.TrustManagerFactory;

public interface TrustManagerFactoryPlugin {
	/**
	 * @return the TrustManagerFactory used when invoking javax.net.ssl.TrustManagerFactory#getTrustManagers() to initialize the SSLContext
	 */
	TrustManagerFactory getTrustManagerFactory();
}