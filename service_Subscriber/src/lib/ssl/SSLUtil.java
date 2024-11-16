package lib.ssl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.CertStore;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.util.Collection;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

public class SSLUtil{
	public static final String NONE = "NONE";
	private String protocol = "TLS";
	private String keystoreProvider = TransportConstants.DEFAULT_KEYSTORE_PROVIDER;
	private String keystoreType = TransportConstants.DEFAULT_KEYSTORE_TYPE;
	private String keystorePath = TransportConstants.DEFAULT_KEYSTORE_PATH;
	private String keystorePassword = TransportConstants.DEFAULT_KEYSTORE_PASSWORD;
	private String truststoreProvider = TransportConstants.DEFAULT_TRUSTSTORE_PROVIDER;
	private String truststoreType = TransportConstants.DEFAULT_TRUSTSTORE_TYPE;
	private String truststorePath = TransportConstants.DEFAULT_TRUSTSTORE_PATH;
	private String truststorePassword = TransportConstants.DEFAULT_TRUSTSTORE_PASSWORD;
	private String crlPath = TransportConstants.DEFAULT_CRL_PATH;
	private String sslProvider = TransportConstants.DEFAULT_SSL_PROVIDER;
	private boolean trustAll = TransportConstants.DEFAULT_TRUST_ALL;
	private String trustManagerFactoryPlugin = TransportConstants.DEFAULT_TRUST_MANAGER_FACTORY_PLUGIN;
	private String keystoreAlias = TransportConstants.DEFAULT_KEYSTORE_ALIAS;
	
	public SSLUtil(){
		
	}
	
	public SSLUtil(String protocol){
		this.protocol = protocol;
	}
	
	public SSLUtil(final SSLContextConfig config){
		this.keystoreProvider = config.getKeystoreProvider();
	    this.keystorePath = config.getKeystorePath();
	    this.keystoreType = config.getKeystoreType();
	    this.keystorePassword = config.getKeystorePassword();
	    this.truststoreProvider = config.getTruststoreProvider();
	    this.truststorePath = config.getTruststorePath();
	    this.truststoreType = config.getTruststoreType();
	    this.truststorePassword = config.getTruststorePassword();
	    this.crlPath = config.getCrlPath();
	    this.trustAll = config.isTrustAll();
	    this.trustManagerFactoryPlugin = config.getTrustManagerFactoryPlugin();
	    this.keystoreAlias = config.getKeystoreAlias();
	}
	
	public SSLUtil(final SSLContextConfig config,String protocol){
		this.protocol = protocol;
		this.keystoreProvider = config.getKeystoreProvider();
	    this.keystorePath = config.getKeystorePath();
	    this.keystoreType = config.getKeystoreType();
	    this.keystorePassword = config.getKeystorePassword();
	    this.truststoreProvider = config.getTruststoreProvider();
	    this.truststorePath = config.getTruststorePath();
	    this.truststoreType = config.getTruststoreType();
	    this.truststorePassword = config.getTruststorePassword();
	    this.crlPath = config.getCrlPath();
	    this.trustAll = config.isTrustAll();
	    this.trustManagerFactoryPlugin = config.getTrustManagerFactoryPlugin();
	    this.keystoreAlias = config.getKeystoreAlias();
	}
	
	public SSLContext createContext()throws Exception{
		SSLContext context = SSLContext.getInstance(this.protocol);
		KeyManager[] keyManagers = loadKeyManagers();
		TrustManager[] trustManagers = loadTrustManagers();
		context.init(keyManagers, trustManagers, new SecureRandom());
		return context;
	}
	
	private KeyManager[] loadKeyManagers() throws Exception{
		KeyManagerFactory factory = loadKeyManagerFactory();
		if(factory == null)
			return null;
		KeyManager[] keyManagers = factory.getKeyManagers();
		if(keystoreAlias != null){
			for(int i=0;i<keyManagers.length;i++){
				if(keyManagers[i] instanceof X509KeyManager){
					keyManagers[i] = new AliasedKeyManager((X509KeyManager) keyManagers[i], keystoreAlias);
				}
			}
		}
		return keyManagers;
	}
	
	private KeyManagerFactory loadKeyManagerFactory() throws Exception{
		if((keystorePath == null || keystorePath.isEmpty() || keystorePath.equalsIgnoreCase(NONE)) && (keystoreProvider == null || !keystoreProvider.toUpperCase().contains("PKCS11"))){
			return null;
		}else{
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			KeyStore ks = loadKeystore(keystoreProvider,keystoreType,keystorePath,keystorePassword);
			kmf.init(ks,keystorePassword == null ? null : keystorePassword.toCharArray());
			return kmf;
		}
	}
	
	private TrustManager[] loadTrustManagers() throws Exception {
	    TrustManagerFactory trustManagerFactory = loadTrustManagerFactory();
	    if (trustManagerFactory == null) {
	    	TrustManager[] trustManagers = new TrustManager[1];
	    	trustManagers[0] = (TrustManager) new TrustAllManager();
	    	return trustManagers;
	    }
	    return trustManagerFactory.getTrustManagers();
    }
	
	private TrustManagerFactory loadTrustManagerFactory() throws Exception{
		if(trustManagerFactoryPlugin != null){
			return AccessController.doPrivileged((PrivilegedAction<TrustManagerFactory>) () -> ((TrustManagerFactoryPlugin) ClassloadingUtil.newInstanceFromClassLoader(SSLUtil.class, trustManagerFactoryPlugin)).getTrustManagerFactory());
		}else if(trustAll){
			//This is useful for testing but not should be used outside of that purpose
	        //return InsecureTrustManagerFactory.INSTANCE;
			return null;
		}else if((truststorePath == null || truststorePath.isEmpty() || truststorePath.equalsIgnoreCase(NONE)) && (truststoreProvider == null || !truststoreProvider.toUpperCase().contains("PKCS11"))){
		    return null;
		}else{
			TrustManagerFactory trustMgrFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			KeyStore trustStore = loadKeystore(truststoreProvider, truststoreType, truststorePath, truststorePassword);
			boolean ocsp = Boolean.valueOf(Security.getProperty("ocsp.enable"));
			
			boolean initialized = false;
			if ((ocsp || crlPath != null) && TrustManagerFactory.getDefaultAlgorithm().equalsIgnoreCase("PKIX")) {
			    PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustStore, new X509CertSelector());
			    if (crlPath != null) {
			    	pkixParams.setRevocationEnabled(true);
			    	Collection<? extends CRL> crlList = loadCRL();
			    	if (crlList != null) {
			    		pkixParams.addCertStore(CertStore.getInstance("Collection", new CollectionCertStoreParameters(crlList)));
			    	}
			    }
			    trustMgrFactory.init(new CertPathTrustManagerParameters(pkixParams));
			    initialized = true;
			}
			
			if (!initialized) {
				trustMgrFactory.init(trustStore);
			}
			return trustMgrFactory;
		}
	}
	
	private Collection<? extends CRL> loadCRL() throws Exception {
	    if (crlPath == null) {
	        return null;
	    }
	    URL resource = validateStoreURL(crlPath);
	    try (InputStream is = resource.openStream()) {
	        return CertificateFactory.getInstance("X.509").generateCRLs(is);
	    }
    }
	
	private KeyStore loadKeystore(final String keystoreProvider,
			final String keystoreType,final String keystorePath,final String keystorePassword) throws Exception{
		KeyStore ks = keystoreProvider == null ? KeyStore.getInstance(keystoreType) : KeyStore.getInstance(keystoreType,keystoreProvider);
		InputStream in = null;
		try{
			if(keystorePath != null && !keystorePath.isEmpty() && !keystorePath.equalsIgnoreCase(NONE)){
				URL keystoreURL = validateStoreURL(keystorePath);
				in = keystoreURL.openStream();
			}
			ks.load(in,keystorePassword == null ? null : keystorePassword.toCharArray());
		}finally{
			if(in != null){
				try{
					in.close();
				}catch(IOException ignored){
					
				}
			}
		}
		return ks;
	}
	
	private URL validateStoreURL(final String storePath) throws Exception{
		assert storePath != null;
		try{
			return new URL(storePath);
		}catch(MalformedURLException e){
			File file = new File(storePath);
			if(file.exists() && file.isFile()){
				return file.toURI().toURL();
			}else{
				URL url = findResource(storePath);
				if(url != null){
					return url;
				}
			}
		}
		throw new Exception("Failed to find a store at " + storePath);
	}
	
	/**
    * This seems duplicate code all over the place, but for security reasons we can't let something like this to be open in a
    * utility class, as it would be a door to load anything you like in a safe VM.
    * For that reason any class trying to do a privileged block should do with the AccessController directly.
    */
	private URL findResource(final String resourceName){
		return AccessController.doPrivileged(new PrivilegedAction<URL>(){
			@Override
			public URL run(){
				return ClassloadingUtil.findResource(resourceName);
			}
		});
	}
	
	public void SetProtocol(String protocol){
		this.protocol = protocol;
	}
	
	public String getProtocol(){
		return this.protocol;
	}
	
	public String getKeystoreProvider() {
		return keystoreProvider;
    }

	public SSLUtil setKeystoreProvider(String keystoreProvider) {
		this.keystoreProvider = keystoreProvider;
	    return this;
	}

	public String getKeystoreType() {
		return keystoreType;
	}

	public SSLUtil setKeystoreType(String keystoreType) {
	    this.keystoreType = keystoreType;
	    return this;
	}

	public String getKeystorePath() {
	    return keystorePath;
	}

	public SSLUtil setKeystorePath(String keystorePath) {
	    this.keystorePath = keystorePath;
	    return this;
	}

	public String getKeystorePassword() {
	    return keystorePassword;
	}

	public SSLUtil setKeystorePassword(String keystorePassword) {
	    this.keystorePassword = keystorePassword;
	    return this;
	}

	public String getKeystoreAlias() {
	    return keystoreAlias;
	}

	public SSLUtil setKeystoreAlias(String keystoreAlias) {
	    this.keystoreAlias = keystoreAlias;
	    return this;
	}

	public String getTruststoreProvider() {
	    return truststoreProvider;
	}

	public SSLUtil setTruststoreProvider(String truststoreProvider) {
	    this.truststoreProvider = truststoreProvider;
	    return this;
	}

	public String getTruststoreType() {
	    return truststoreType;
	}

	public SSLUtil setTruststoreType(String truststoreType) {
	    this.truststoreType = truststoreType;
	    return this;
	}

	public String getTruststorePath() {
	    return truststorePath;
	}

	public SSLUtil setTruststorePath(String truststorePath) {
	    this.truststorePath = truststorePath;
	    return this;
	}

	public String getTruststorePassword() {
	    return truststorePassword;
	}

	public SSLUtil setTruststorePassword(String truststorePassword) {
	    this.truststorePassword = truststorePassword;
	    return this;
	}

	public String getCrlPath() {
	    return crlPath;
	}

    public SSLUtil setCrlPath(String crlPath) {
	    this.crlPath = crlPath;
	    return this;
	}

	public String getSslProvider() {
	    return sslProvider;
	}

	public SSLUtil setSslProvider(String sslProvider) {
	    this.sslProvider = sslProvider;
	    return this;
	}

	public boolean isTrustAll() {
	    return trustAll;
	}

	public SSLUtil setTrustAll(boolean trustAll) {
	    this.trustAll = trustAll;
	    return this;
	}

	public String getTrustManagerFactoryPlugin() {
	    return trustManagerFactoryPlugin;
	}

	public SSLUtil setTrustManagerFactoryPlugin(String trustManagerFactoryPlugin) {
	    this.trustManagerFactoryPlugin = trustManagerFactoryPlugin;
	    return this;
	}
	
	@Override
	public String toString(){
		return "{\"protocol\":\""+this.protocol+"\",\"keystoreProvider\":\""+this.keystoreProvider+"\",\"keystoreType\":\""+this.keystoreType+"\""
				+ ",\"keystorePath\":\""+this.keystorePath+"\",\"keystorePassword\":\""+this.keystorePassword+"\""
				+ ",\"truststoreProvider\":\""+this.truststoreProvider+"\",\"truststoreType\":\""+this.truststoreType+"\""
				+",\"truststorePath\":\""+this.truststorePath+"\",\"truststorePassword\":\""+this.truststorePassword+"\""
				+",\"crlPath\":\""+this.crlPath+"\",\"sslProvider\":\""+this.sslProvider+"\""
				+",\"trustAll\":\""+this.trustAll+"\",\"trustManagerFactoryPlugin\":\""+this.trustManagerFactoryPlugin+"\""
				+ ",\"keystoreAlias\":\""+this.keystoreAlias+"\"}";
	}
}