package scc.data;

import org.bson.Document;

/**
 * Represents a User, as returned to the clients
 */
public class User {
	
	private String id;
	private String name;
	private String pwd;
	private String photoId;
	
	public User() {
	}
	
	public User(String id, String name, String pwd, String photoId) {
		this();
		this.id = id;
		this.name = name;
		this.pwd = pwd;
		this.photoId = photoId;
	}
	
	public User(UserDAO u) {
		this(u.getId(), u.getName(), u.getPwd(), u.getPhotoId());
	}
	
	public User(String id) {
		this(id, null, null, null);
	}
	
	public User(Document document) {
		this(
			document.get("id").toString(),
			document.get("name").toString(),
			document.get("pwd").toString(),
			document.get("photoId").toString()
		);
	}
	
	public Document toDocument() {
		return new Document()
			.append("id", id)
			.append("name", name)
			.append("pwd", pwd)
			.append("photoId", photoId);
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
	
	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", pwd=" + pwd + ", photoId=" + photoId;
	}
	
}
