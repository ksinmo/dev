package tbwauth.model;

public class Client {
	String clientId;
	String webServerRedirectUri;
	String logoutUri;
	String baseUri;
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getWebServerRedirectUri() {
		return webServerRedirectUri;
	}
	public void setWebServerRedirectUri(String webServerRedirectUri) {
		this.webServerRedirectUri = webServerRedirectUri;
	}
	public String getLogoutUri() {
		return logoutUri;
	}
	public void setLogoutUri(String logoutUri) {
		this.logoutUri = logoutUri;
	}
	public String getBaseUri() {
		return baseUri;
	}
	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}
	
}
