package org.example.api_server.service;

import io.netty.util.internal.StringUtil;
import org.apache.ibatis.session.SqlSession;
import org.example.api_server.ApiRequestTemplate;
import org.example.api_server.exception.RequestParamException;
import org.example.api_server.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service("users")
@Scope("prototype") // 호출될때마다 객체 생성
public class UserInfoService extends ApiRequestTemplate {

    @Autowired
    private SqlSession sqlSession;

    public UserInfoService(Map<String, String> reqData) {
        super(reqData);
    }

    @Override
    public void requestParamValidation() throws RequestParamException {
        if(StringUtils.isEmpty(this.reqData.get("email"))) {
            throw new RequestParamException("email이 없습니다.");
        }
    }

    @Override
    public void service() throws ServiceException {
        Map<String, Object> result = sqlSession.selectOne("users.userInfoByEmail", this.reqData);

        if (result != null) {
            String userNo = String.valueOf(result.get("USERNO"));

            this.apiResult.addProperty("resultCode", "200");
            this.apiResult.addProperty("message", "Success");
            this.apiResult.addProperty("userNo", userNo);
        } else {
            this.apiResult.addProperty("resultCode", "404");
            this.apiResult.addProperty("message", "Fail");
        }
    }
}
