package org.example.api_server;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.example.SystemCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ServiceDispatcher {
    private static ApplicationContext springContext;

    @Autowired
    public void init(ApplicationContext springContext) {
        ServiceDispatcher.springContext = springContext;
    }

    private static final Logger logger = LoggerFactory.getLogger(ApiRequestParser.class);

    public static ApiRequest dispatch(Map<String, String> requestMap) {
        String serviceUri = requestMap.get("REQUEST_URI");
        String beanName = null;

        if (serviceUri == null) {
            beanName = SystemCode.NOT_FOUND;
        }

        if (serviceUri.startsWith("/tokens")) {
            String httpMethod = requestMap.get("REQUEST_METHOD");

            switch (httpMethod) {
                case "POST":
                    beanName = SystemCode.TOKEN_ISSUE;
                    break;
                case "DELETE":
                    beanName = SystemCode.TOKEN_EXPIER;
                    break;
                case "GET":
                    beanName = SystemCode.TOKEN_VERIFY;
                    break;
                default:
                    beanName = SystemCode.NOT_FOUND;
                    break;
            }
        } else if (serviceUri.startsWith("/mapper")) {
            beanName = SystemCode.USERS;
        } else {
            beanName = SystemCode.NOT_FOUND;
        }

        ApiRequest service = null;

        try {
            service = (ApiRequest) springContext.getBean(beanName, requestMap);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            service = (ApiRequest) springContext.getBean(SystemCode.NOT_FOUND, requestMap);
        }

        return service;
    }
}
