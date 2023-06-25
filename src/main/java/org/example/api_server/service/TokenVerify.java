package org.example.api_server.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mysql.cj.core.util.StringUtils;
import org.example.api_server.ApiRequestTemplate;
import org.example.api_server.core.JedisHelper;
import org.example.api_server.exception.RequestParamException;
import org.example.api_server.exception.ServiceException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Map;

@Service("tokenVerify")
@Scope("prototype")
public class TokenVerify extends ApiRequestTemplate {
    private static final JedisHelper helper = JedisHelper.getInstance();
    public TokenVerify(Map<String, String> reqData) {
        super(reqData);
    }

    @Override
    public void requestParamValidation() throws RequestParamException {
        if (StringUtils.isEmptyOrWhitespaceOnly(this.reqData.get("token"))) {
            throw new RequestParamException("token이 없습니다.");
        }
    }

    @Override
    public void service() throws ServiceException {
        Jedis jedis = null;

        try {
            jedis = helper.getConnection();
            String tokenString = jedis.get(this.reqData.get("token"));

            if (tokenString == null) {
                this.apiResult.addProperty("resultCode", "404");
                this.apiResult.addProperty("message", "Fail");
            } else {
                Gson gson = new Gson();
                JsonObject token = gson.fromJson(tokenString, JsonObject.class);

                this.apiResult.addProperty("resultCode", "200");
                this.apiResult.addProperty("message", "Success");
                this.apiResult.addProperty("issueDate", String.valueOf(token.get("issueDate")));
                this.apiResult.addProperty("email", String.valueOf(token.get("email")));
                this.apiResult.addProperty("userNo", String.valueOf(token.get("userNo")));
            }

        } catch (Exception e){
            e.printStackTrace();
            helper.returnResource(jedis);
        }

    }
}
