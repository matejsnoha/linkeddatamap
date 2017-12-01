package info.snoha.matej.linkeddatamap.app.gui.nearby;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.utils.UI;
import info.snoha.matej.linkeddatamap.app.internal.layers.LocalLayerManager;
import info.snoha.matej.linkeddatamap.app.internal.model.MarkerModel;
import info.snoha.matej.linkeddatamap.app.internal.model.Position;

import java.util.List;
import java.util.Locale;

public class NearbyAdapter extends RecyclerView.Adapter<NearbyViewHolder> {

	private Context context;
	private List<MarkerModel> nearbyMarkers;
	private Position myPosition;

	public NearbyAdapter(Context context, List<MarkerModel> nearbyMarkers) {
		this(context, nearbyMarkers, null);
	}

	public NearbyAdapter(Context context, List<MarkerModel> nearbyMarkers, Position myPosition) {
		this.context = context;
		this.nearbyMarkers = nearbyMarkers;
		this.myPosition = myPosition;
	}

	@Override
	public NearbyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(
				android.R.layout.simple_list_item_2,
				parent,
				false);

		NearbyViewHolder vh = new NearbyViewHolder(v, (view, itemIndex) -> {
			MarkerModel marker = nearbyMarkers.get(itemIndex);
			UI.message(context, "Launching navigation to " + marker.getName());
			String uri = String.format(Locale.US,
					"http://maps.google.com/maps?daddr=%f,%f",
					marker.getPosition().getLatitude(),
					marker.getPosition().getLongitude());
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			context.startActivity(intent);
		});

		return vh;
	}

	@Override
	public void onBindViewHolder(NearbyViewHolder vh, int position) {

		MarkerModel marker = nearbyMarkers.get(position);

		String primaryText = "[" + LocalLayerManager.getLayerName(marker.getLayer()) + "] "
				+ marker.getName();
		vh.getPrimaryTextView().setText(primaryText);

		String secondaryText = (myPosition != null ? String.format(Locale.US, "(%.2f km) ",
				myPosition.distanceTo(marker.getPosition()) / 1000) : "")
				+ (marker.getAddress() != null ? marker.getAddress() : marker.getPosition().toShortString());
		vh.getSecondaryTextView().setText(secondaryText);
		vh.getSecondaryTextView().setCompoundDrawablesWithIntrinsicBounds(
				R.drawable.ic_near_me_white_24dp, 0, 0, 0);

	}

	@Override
	public int getItemCount() {
		return nearbyMarkers.size();
	}

	public List<MarkerModel> getItems() {
		return nearbyMarkers;
	}
}
