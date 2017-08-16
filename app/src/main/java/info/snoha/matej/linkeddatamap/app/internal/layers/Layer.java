package info.snoha.matej.linkeddatamap.app.internal.layers;

public class Layer {

	private MapLayer mapLayer;
	private DataLayer dataLayer;

	public Layer(MapLayer mapLayer, DataLayer dataLayer) {
		this.mapLayer = mapLayer;
		this.dataLayer = dataLayer;
	}

	public MapLayer getMapLayer() {
		return mapLayer;
	}

	public DataLayer getDataLayer() {
		return dataLayer;
	}

	public String getSparqlEndpoint() {
		return mapLayer != null ? mapLayer.getSparqlEndpoint() : null; // TODO
	}
}
