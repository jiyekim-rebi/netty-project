package org.example.api_server.service;

import com.mysql.cj.core.util.StringUtils;
import jdk.nashorn.internal.parser.Token;
import org.example.api_server.ApiRequestTemplate;
import org.example.api_server.core.JedisHelper;
import org.example.api_server.exception.RequestParamException;
import org.example.api_server.exception.ServiceException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Map;

@Service("tokenExpire")
@Scope("prototype")
public class TokenExpire extends ApiRequestTemplate {
    private static final JedisHelper helper = JedisHelper.getInstance();

    public TokenExpire(Map<String, String> reqData) {
        super(reqData);
    }
    @Override
    public void requestParamValidation() throws RequestParamException {
        if (StringUtils.isEmptyOrWhitespaceOnly(this.reqData.get("token"))) {
            throw new RequestParamException("token이 없습니다");
        }

    }

    @Override
    public void service() throws ServiceException {
        Jedis jedis = null;

        try {
            jedis = helper.getConnection();
            long result = jedis.del(this.reqData.get("token"));

            this.apiResult.addProperty("resultCode", "200");
            this.apiResult.addProperty("message", "Success");
            this.apiResult.addProperty("token", this.reqData.get("token"));

        } catch (Exception e){
            e.printStackTrace();
            helper.returnResource(jedis);
        }

    }
}
