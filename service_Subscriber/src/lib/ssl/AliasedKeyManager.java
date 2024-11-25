package lib.ssl;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;

public final class AliasedKeyManager extends X509ExtendedKeyManager{
	
	private X509KeyManager wrapped;
	private String keystoreAlias;
	
	public AliasedKeyManager(X509KeyManager wrapped, String keystoreAlias){
		super();
		this.wrapped = wrapped;
		this.keystoreAlias = keystoreAlias;
	}
	
	@Override
	public String chooseServerAlias(String keyType,Principal[] issuers,Socket socket){
		if(keystoreAlias != null){
			return keystoreAlias;
		}
		return wrapped.chooseServerAlias(keyType, issuers, socket);
	}
	
	@Override
	public String chooseEngineServerAlias(String keyType,Principal[] issuers,SSLEngine engine){
		if(keystoreAlias != null){
			return keystoreAlias;
		}
		return super.chooseEngineServerAlias(keyType, issuers, engine);
	}
	
	@Override
	public String chooseClientAlias(String[] keyType,Principal[] issuers,Socket socket){
		if(keystoreAlias != null){
			return keystoreAlias;
		}
		return wrapped.chooseClientAlias(keyType, issuers, socket);
	}
	
	@Override
	public X509Certificate[] getCertificateChain(String alias){
		return wrapped.getCertificateChain(alias);
	}
	
	@Override
	public String[] getClientAliases(String keyType,Principal[] issuers){
		return wrapped.getClientAliases(keyType, issuers);
	}
	
	@Override
	public String[] getServerAliases(String keyType,Principal[] issuers){
		return wrapped.getClientAliases(keyType, issuers);
	}
	
	@Override
	public PrivateKey getPrivateKey(String alias){
		return wrapped.getPrivateKey(alias);
	}
	 
	@Override
	public String chooseEngineClientAlias(String[] keyType,Principal[] issuers,SSLEngine engine){
		return chooseClientAlias(keyType,issuers,null);
	}

}