package org.example.api_server;

import com.google.gson.JsonObject;
import org.example.api_server.exception.RequestParamException;
import org.example.api_server.exception.ServiceException;

public interface ApiRequest {
    public void requestParamValidation() throws RequestParamException;
    public void service() throws ServiceException;
    public void executeService();
    public JsonObject getApiResult();
}
