package org.example.api_server;

import com.google.gson.JsonObject;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.example.api_server.exception.RequestParamException;
import org.example.api_server.exception.ServiceException;


import java.util.Map;

public abstract class ApiRequestTemplate implements ApiRequest {
    protected Logger logger;
    protected Map<String, String> reqData;
    protected JsonObject apiResult;

    protected ApiRequestTemplate(Map<String, String> reqData) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.apiResult = new JsonObject();
        this.reqData = reqData;

        logger.debug("request data : " + this.reqData);
    }

    public void executeService() {
        try {
            this.requestParamValidation();

            this.service();
        } catch (RequestParamException e) {
            logger.error(e.getMessage());
            this.apiResult.addProperty("resultCode", "405");
        } catch (ServiceException e) {
            logger.error(e.getMessage());
            this.apiResult.addProperty("resultCode", "501");
        }
    }

    public JsonObject getApiResult() {
        return this.apiResult;
    }

}
