package info.snoha.matej.linkeddatamap.app.internal.layers;

import java.util.List;

public class DataLayer {

	public enum Type {
		RDF_DUMP, SPARQL_PATHS, SPARQL_QUERY
	}

	private Type type;

	// meta
	private String uri;
	private String title;
	private String description;
	private String publisherName;
	private String publisherEmail;
	private String license;

	// dump access
	private String dumpUrl;

	// sparql access
	private String sparqlEndpoint;
	private String sparqlNamedGraph;

	// data structure
	private String dataPointType;
	private String dataPointName;
	private List<String> dataPointDescription;
	private String mapPointPath;

	// map layer
	private String mapLayer;

	public Type getType() {
		return type;
	}

	public DataLayer type(Type type) {
		this.type = type;
		return this;
	}

	public String getUri() {
		return uri;
	}

	public DataLayer uri(String uri) {
		this.uri = uri;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public DataLayer title(String title) {
		this.title = title;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public DataLayer description(String description) {
		this.description = description;
		return this;
	}

	public String getPublisherName() {
		return publisherName;
	}

	public DataLayer publisherName(String publisherName) {
		this.publisherName = publisherName;
		return this;
	}

	public String getPublisherEmail() {
		return publisherEmail;
	}

	public DataLayer publisherEmail(String publisherEmail) {
		this.publisherEmail = publisherEmail;
		return this;
	}

	public String getLicense() {
		return license;
	}

	public DataLayer license(String license) {
		this.license = license;
		return this;
	}

	public String getDumpUrl() {
		return dumpUrl;
	}

	public DataLayer dumpUrl(String dumpUrl) {
		this.dumpUrl = dumpUrl;
		return this;
	}

	public String getSparqlEndpoint() {
		return sparqlEndpoint;
	}

	public DataLayer sparqlEndpoint(String sparqlEndpoint) {
		this.sparqlEndpoint = sparqlEndpoint;
		return this;
	}

	public String getSparqlNamedGraph() {
		return sparqlNamedGraph;
	}

	public DataLayer sparqlNamedGraph(String sparqlNamedGraph) {
		this.sparqlNamedGraph = sparqlNamedGraph;
		return this;
	}

	public String getDataPointType() {
		return dataPointType;
	}

	public DataLayer dataPointType(String dataPointType) {
		this.dataPointType = dataPointType;
		return this;
	}

	public String getdataPointName() {
		return dataPointName;
	}

	public DataLayer dataPointName(String dataPointName) {
		this.dataPointName = dataPointName;
		return this;
	}

	public List<String> getdataPointDescription() {
		return dataPointDescription;
	}

	public DataLayer dataPointDescription(List<String> dataPointDescription) {
		this.dataPointDescription = dataPointDescription;
		return this;
	}

	public String getMapPointPath() {
		return mapPointPath;
	}

	public DataLayer mapPointPath(String mapPointPath) {
		this.mapPointPath = mapPointPath;
		return this;
	}

	public String getMapLayer() {
		return mapLayer;
	}

	public DataLayer mapLayer(String mapLayer) {
		this.mapLayer = mapLayer;
		return this;
	}
}
