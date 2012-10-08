package ca.apachegui.update;

public class UpdateInfo {
	private String version;
	private String size;
	private String details;
	private String url;
	private String compatibility;

	public UpdateInfo(String version, String size, String details, String url, String compatibility) {
		this.version=version;
		this.size=size;
		this.details=details;
		this.url=url;
		this.compatibility=compatibility;
	}
	
	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCompatibility() {
		return compatibility;
	}

	public void setCompatibility(String compatibility) {
		this.compatibility = compatibility;
	}
}
