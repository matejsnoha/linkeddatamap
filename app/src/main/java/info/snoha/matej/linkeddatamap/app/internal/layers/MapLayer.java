package info.snoha.matej.linkeddatamap.app.internal.layers;

public class MapLayer {

	public enum Type {
		RDF_DUMP, SPARQL_PATHS, SPARQL_QUERY
	}

	private Type type;

	// meta
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

	// map structure
	private String addressPointType;
	private String addressPath;
	private String latitudePath;
	private String longitudePath;

	public Type getType() {
		return type;
	}

	public MapLayer type(Type type) {
		this.type = type;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public MapLayer title(String title) {
		this.title = title;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public MapLayer description(String description) {
		this.description = description;
		return this;
	}

	public String getPublisherName() {
		return publisherName;
	}

	public MapLayer publisherName(String publisherName) {
		this.publisherName = publisherName;
		return this;
	}

	public String getPublisherEmail() {
		return publisherEmail;
	}

	public MapLayer publisherEmail(String publisherEmail) {
		this.publisherEmail = publisherEmail;
		return this;
	}

	public String getLicense() {
		return license;
	}

	public MapLayer license(String license) {
		this.license = license;
		return this;
	}

	public String getDumpUrl() {
		return dumpUrl;
	}

	public MapLayer dumpUrl(String dumpUrl) {
		this.dumpUrl = dumpUrl;
		return this;
	}

	public String getSparqlEndpoint() {
		return sparqlEndpoint;
	}

	public MapLayer sparqlEndpoint(String sparqlEndpoint) {
		this.sparqlEndpoint = sparqlEndpoint;
		return this;
	}

	public String getSparqlNamedGraph() {
		return sparqlNamedGraph;
	}

	public MapLayer sparqlNamedGraph(String sparqlNamedGraph) {
		this.sparqlNamedGraph = sparqlNamedGraph;
		return this;
	}

	public String getAddressPointType() {
		return addressPointType;
	}

	public MapLayer addressPointType(String addressPointType) {
		this.addressPointType = addressPointType;
		return this;
	}

	public String getAddressPath() {
		return addressPath;
	}

	public MapLayer addressPath(String addressPath) {
		this.addressPath = addressPath;
		return this;
	}

	public String getLatitudePath() {
		return latitudePath;
	}

	public MapLayer latitudePath(String latitudePath) {
		this.latitudePath = latitudePath;
		return this;
	}

	public String getLongitudePath() {
		return longitudePath;
	}

	public MapLayer longitudePath(String longitudePath) {
		this.longitudePath = longitudePath;
		return this;
	}
}
