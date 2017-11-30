package info.snoha.matej.linkeddatamap.cloud;

/**
 * Represents the behaviour - business model.<br>
 * (Strategy pattern)
 */
public interface Action {
	
	/**
	 * Executes the business logic of the action.
	 * 
	 * @param context
	 * @return identifier of the view (filename)
	 * @throws Exception
	 */
	String execute(Context context) throws Exception;
}
