package scc.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

/**
 * Represents a User, as returned to the clients
 */
public class User {
	
	private String id;
	private String name;
	private String pwd;
	private String photoId;
	private String[] channelIds;
	
	public User(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("pwd") String pwd, @JsonProperty("photoId") String photoId, @JsonProperty("channelIds") String[] channelIds) {
		super();
		this.id = id;
		this.name = name;
		this.pwd = pwd;
		this.photoId = photoId;
		this.channelIds = channelIds;
	}
	
	public User(UserDAO u) {
		this(u.getId(), u.getName(), u.getPwd(), u.getPhotoId(), u.getChannelIds());
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPwd() {
		return pwd;
	}
	
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
	public String getPhotoId() {
		return photoId;
	}
	
	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}
	
	public String[] getChannelIds() {
		return channelIds == null ? new String[0] : channelIds;
	}
	
	public void setChannelIds(String[] channelIds) {
		this.channelIds = channelIds;
	}
	
	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", pwd=" + pwd + ", photoId=" + photoId + ", channelIds="
				+ Arrays.toString(channelIds) + "]";
	}
	
}
