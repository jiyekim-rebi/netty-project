package org.example.api_server;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.example.ApiRequest;
import org.example.api_server.exception.RequestParamException;
import org.example.api_server.exception.ServiceException;
import org.json.JSONObject;

import java.util.Map;

public abstract class ApiRequestTemplate implements ApiRequest {
    protected Logger logger;
    protected Map<String, String> reqData;
    protected JSONObject apiResult;

    public ApiRequestTemplate(Map<String, String> reqData) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.apiResult = new JSONObject();
        this.reqData = reqData;

        logger.debug("request data : " + this.reqData);
    }

    public void executeService() {
        try {
            this.requestParamValidation();

            this.service();
        } catch (RequestParamException e) {
            logger.error(e.getMessage());
            this.apiResult.append("resultCode", "405");
        } catch (ServiceException e) {
            logger.error(e.getMessage());
            this.apiResult.append("resultCode", "501");
        }
    }

    public JSONObject getApiResult() {
        return this.apiResult;
    }

}
