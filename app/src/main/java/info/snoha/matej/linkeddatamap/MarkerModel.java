package info.snoha.matej.linkeddatamap;

public class MarkerModel {

    private Position position;
    private String name;
    private String text;

    public MarkerModel(Position position, String name, String text) {
        this.position = position;
        this.name = name;
        this.text = text;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}