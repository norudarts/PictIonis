package etna.hyvernparede.pictionis.chat;

public class ChatMessage {

    private String id;
    private String text;
    private String username;
    private String profilePicUrl;

    public ChatMessage() {

    }

    public ChatMessage(String text, String author, String profilePicUrl) {
        this.text = text;
        this.username = author;
        this.profilePicUrl = profilePicUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }
}
