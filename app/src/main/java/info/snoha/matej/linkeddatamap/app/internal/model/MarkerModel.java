package info.snoha.matej.linkeddatamap.app.internal.model;

import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;

public class MarkerModel {

    private Position position;
    private String name;
    private String text;
    private String address;
    private Layer layer;

    public MarkerModel(Layer layer, Position position, String name, String text) {
        this(layer, position, name, text, null);
    }

    public MarkerModel(Layer layer, Position position, String name, String text, String address) {
        this.layer = layer;
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

    public String getAddress() {
        // TODO fix this by proper parsing logic in SparqlClient
        if (address == null && text.contains("ruian")) {
            return name;
        }
        if (address == null && text.contains("\n\n")) {
            return text.substring(text.lastIndexOf("\n\n")).replace("\n", "");
        }
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }
}