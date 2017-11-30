package info.snoha.matej.linkeddatamap.api;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class ApiResponse implements Serializable {

    protected static final long serialVersionUID = 1L; // api version 1

    @Expose
    protected final boolean success;

    @Expose
    protected final String message;

    public ApiResponse(String message) {
        this.success = true;
        this.message = message;
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
