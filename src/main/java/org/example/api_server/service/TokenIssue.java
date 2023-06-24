package org.example.api_server.service;

import com.google.gson.JsonObject;
import com.mysql.cj.core.util.StringUtils;
import org.apache.ibatis.ognl.TokenMgrError;
import org.apache.ibatis.session.SqlSession;
import org.example.api_server.ApiRequestTemplate;
import org.example.api_server.core.JedisHelper;
import org.example.api_server.core.KeyMaker;
import org.example.api_server.dao.TokenKey;
import org.example.api_server.exception.RequestParamException;
import org.example.api_server.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Map;

@Service("tokenIssue")
@Scope("prototype")
public class TokenIssue extends ApiRequestTemplate {
    private static final JedisHelper helper = JedisHelper.getInstance();

    @Autowired
    private SqlSession sqlSession;

    public TokenIssue(Map<String, String> reqData) {
        super(reqData);
    }
    @Override
    public void requestParamValidation() throws RequestParamException {
        if(StringUtils.isEmptyOrWhitespaceOnly(this.reqData.get("userNo"))) {
            throw new RequestParamException("userNo이 없습니다.");
        }

        if(StringUtils.isEmptyOrWhitespaceOnly(this.reqData.get("password"))) {
            throw new RequestParamException("password가 없습니다.");
        }
    }

    @Override
    public void service() throws ServiceException {
        Jedis jedis = null;

        try {
            Map<String, Object> result = sqlSession.selectOne("users.userInfoByPassword", this.reqData);

            if (result != null) {
                final long threeHour = 60 * 60 * 3;
                long issueDate = System.currentTimeMillis() / 1000;
                String email = String.valueOf(result.get("USERID"));

                JsonObject token = new JsonObject();
                token.addProperty("issueDate", issueDate);
                token.addProperty("expireDate", issueDate + threeHour);
                token.addProperty("email", email);
                token.addProperty("userNo", reqData.get("userNo"));

                KeyMaker tokenKey = new TokenKey(email, issueDate);
                jedis = helper.getConnection();
                jedis.setex(tokenKey.getKey(), (int)threeHour, token.toString()); // setex : 지정시간이 지나면 자동으로 삭제함
            } else {
                this.apiResult.addProperty("resultCode", "404");
            }

            helper.returnResource(jedis);

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
