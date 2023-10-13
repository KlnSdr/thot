package thot.common.response;

import java.io.Serializable;

public class Response implements Serializable {
    private String errorMessage;
    private Serializable value;
    private ResponseType responseType;

    public Response() {
    }

    public String getError() {
        return errorMessage;
    }

    public void setError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Serializable getValue() {
        return value;
    }

    public void setValue(Serializable value) {
        this.value = value;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }
}
