package org.example;

import org.example.api_server.exception.RequestParamException;
import org.example.api_server.exception.ServiceException;
import org.json.JSONObject;

public interface ApiRequest {
    public void requestParamValidation() throws RequestParamException;
    public void service() throws ServiceException;
    public void executeService();
    public JSONObject getApiResult();
}
