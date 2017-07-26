package info.snoha.matej.linkeddatamap.app.internal.model;

public class BoundingBox {

	private float minLat;
	private float maxLat;
	private float minLong;
	private float maxLong;

	// this is very approximate and not good around poles, TODO use a proper projection library
	private static final double M_IN_DEGREES_APPROX = 0.0000089982311916;

	public static BoundingBox from(Position position, float distance) {
		return new BoundingBox()
				.setMinLat((float) (position.getLatitude() - distance * M_IN_DEGREES_APPROX))
				.setMaxLat((float) (position.getLatitude() + distance * M_IN_DEGREES_APPROX))
				.setMinLong((float) (position.getLongitude() - distance * M_IN_DEGREES_APPROX))
				.setMaxLong((float) (position.getLongitude() + distance * M_IN_DEGREES_APPROX));
	}

	public float getMinLat() {
		return minLat;
	}

	public BoundingBox setMinLat(float minLat) {
		this.minLat = minLat;
		return this;
	}

	public float getMaxLat() {
		return maxLat;
	}

	public BoundingBox setMaxLat(float maxLat) {
		this.maxLat = maxLat;
		return this;
	}

	public float getMinLong() {
		return minLong;
	}

	public BoundingBox setMinLong(float minLong) {
		this.minLong = minLong;
		return this;
	}

	public float getMaxLong() {
		return maxLong;
	}

	public BoundingBox setMaxLong(float maxLong) {
		this.maxLong = maxLong;
		return this;
	}
}
