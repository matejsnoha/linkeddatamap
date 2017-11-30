package info.snoha.matej.linkeddatamap;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Locale;
import java.util.UUID;

public class UIDUtils {
	
	// 8-24 and divisible by 4
	public static final int ID_LENGTH = 12; 

	// UUID-based
	
	public static boolean isUUID(String uuid){
       
		if (uuid == null) 
        	return false;
		
        try {
            return uuid.equalsIgnoreCase(
            		UUID.fromString(uuid).toString());
            
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
	
	// T-XXXX-XXXX...

	private static boolean isTypedID(Character type, String id){
	    
		if (id == null || id.isEmpty())
			return false;
		
		id = id.toUpperCase(Locale.US);
		
		return id.matches("^" + type + "(\\-\\p{Alnum}{4}){2,}$");
    }
	
	public static String getErrorID() {
		return getTypedID('E');
	}
	
	private static String getTypedID(Character type) {
		String base;

		// we cut id from the last part of UUID (xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx)
		if (ID_LENGTH <= 12) {

			base = UUID.randomUUID().toString().substring(24, 24 + ID_LENGTH).toUpperCase(Locale.US);

		} else if (ID_LENGTH <= 24) {

			base = UUID.randomUUID().toString().substring(24, 36).toUpperCase(Locale.US)
					+ UUID.randomUUID().toString().substring(24, ID_LENGTH - 12).toUpperCase(Locale.US);

		} else {

			// not as secure / random
			base = RandomStringUtils.random(ID_LENGTH, "01234567879abcdef").toUpperCase(Locale.US);
		}

		if (base.length() % 4 != 0)
			return base;
			
		String id = String.valueOf(type);
		for (int i = 0; i + 4 <= base.length(); i += 4) {
			id += "-" + base.substring(i, i + 4);
		}

		return id;
	}
}
