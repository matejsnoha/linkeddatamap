package info.snoha.matej.linkeddatamap.datasets.doubleshot;

import info.snoha.matej.linkeddatamap.Log;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DoubleShotModel {

    public DSResponse response;

    public static class DSResponse {
        public DSList list;
    }

    public static class DSList {
        public DSListItems listItems;
    }

    public static class DSListItems {
        public List<DSItem> items;
    }

    public static class DSItem {
        public DSVenue venue;
    }

    public static class DSVenue {
    	public String id;
        public String name;
        public DSLocation location;
        public Float rating;

		@Override
		public boolean equals(Object o) {
			return o instanceof DSVenue && id.equals(((DSVenue) o).id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}

    public static class DSLocation {
        public Float lat;
        public Float lng;
        public String[] formattedAddress;
    }

    public Set<DSVenue> getVenues() {
        try {
            return new HashSet<>(CollectionUtils.collect(
            		response.list.listItems.items, (item) -> item.venue));
        } catch (Exception e) {
            Log.error("Could not get venues from model", e);
            return Collections.emptySet();
        }
    }
}
