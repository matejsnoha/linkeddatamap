package info.snoha.matej.linkeddatamap.app.internal.layers;

import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import info.snoha.matej.linkeddatamap.Log;

public class Layer {

	private MapLayer mapLayer;
	private DataLayer dataLayer;

	private boolean enabled;
	private String color;

	// meta
	private String uri;
	private String title;
	private String description;
	private String publisherName;
	private String publisherEmail;
	private String license;

	public Layer() {
	}

	public Layer(MapLayer mapLayer, DataLayer dataLayer) {
		this.mapLayer = mapLayer;
		this.dataLayer = dataLayer;
	}

	public MapLayer getMapLayer() {
		return mapLayer;
	}

	public Layer mapLayer(MapLayer mapLayer) {
		this.mapLayer = mapLayer;
		return this;
	}

	public DataLayer getDataLayer() {
		return dataLayer;
	}

	public Layer dataLayer(DataLayer dataLayer) {
		this.dataLayer = dataLayer;
		return this;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Layer enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public String getColor() {
		return color;
	}

	public int getColorAndroid() {
		try {
			return Color.parseColor(color);
		} catch (Exception e) {
			Log.warn("Unknown color " + color);
			return Color.DKGRAY;
		}
	}

	public float getColorHue() {
		try {
			float[] hsl = new float[3];
			ColorUtils.colorToHSL(Color.parseColor(color), hsl);
			return hsl[0];
		} catch (Exception e) {
			Log.warn("Unknown color " + color);
			return BitmapDescriptorFactory.HUE_VIOLET;
		}
	}

	public Layer color(String color) {
		this.color = color;
		return this;
	}

	@Deprecated
	public String getSparqlEndpoint() {
		return mapLayer != null ? mapLayer.getSparqlEndpoint() : null; // TODO
	}

	public String getUri() {
		return uri;
	}

	public Layer uri(String uri) {
		this.uri = uri;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public Layer title(String title) {
		this.title = title;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public Layer description(String description) {
		this.description = description;
		return this;
	}

	public String getPublisherName() {
		return publisherName;
	}

	public Layer publisherName(String publisherName) {
		this.publisherName = publisherName;
		return this;
	}

	public String getPublisherEmail() {
		return publisherEmail;
	}

	public Layer publisherEmail(String publisherEmail) {
		this.publisherEmail = publisherEmail;
		return this;
	}

	public String getLicense() {
		return license;
	}

	public Layer license(String license) {
		this.license = license;
		return this;
	}

	@Override
	public String toString() {
		return "Layer{" +
				"uri='" + uri + '\'' +
				", title='" + title + '\'' +
				'}';
	}
}
