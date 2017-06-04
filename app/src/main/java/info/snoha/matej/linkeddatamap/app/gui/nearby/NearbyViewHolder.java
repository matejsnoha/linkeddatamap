package info.snoha.matej.linkeddatamap.app.gui.nearby;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class NearbyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

	private OnCLickListener listener;

	public interface OnCLickListener {
		void onClick(View view, int itemIndex);
	}

	public NearbyViewHolder(View v, OnCLickListener listener) {
		super(v);
		v.setOnClickListener(this);
		this.listener = listener;
	}

	@Override
	public void onClick(View v) {
		if (listener != null) {
			listener.onClick(v, getAdapterPosition());
		}
	}

	public TextView getPrimaryTextView() {
		return (TextView) itemView.findViewById(android.R.id.text1);
	}

	public TextView getSecondaryTextView() {
		return (TextView) itemView.findViewById(android.R.id.text2);
	}

}
