package info.snoha.matej.linkeddatamap.cloud.status;

import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.api.ApiResponse;
import info.snoha.matej.linkeddatamap.api.Status;
import info.snoha.matej.linkeddatamap.cloud.Action;
import info.snoha.matej.linkeddatamap.cloud.Context;
import info.snoha.matej.linkeddatamap.cloud.CloudUtils;

import java.util.Date;

/**
 * Status report
 */
public class StatusAction implements Action {
	   
	@Override
    public String execute(Context context) {

        Log.info("Sending status report");

        ApiResponse response = new Status.StatusResponse("OK", CloudUtils.formatDate(new Date()));

        context.setContentType(Context.ContentType.JSON);
        context.writeJsonData(response);
        return null;
    }
}
