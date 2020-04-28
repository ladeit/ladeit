package com.ladeit.api.annotation;

import com.ladeit.biz.dao.ServiceGroupDao;
import com.ladeit.biz.dao.UserSlackRelationDao;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.UserInfo;
import com.ladeit.pojo.doo.Certificate;
import com.ladeit.pojo.doo.User;
import com.ladeit.pojo.doo.UserSlackRelation;
import com.ladeit.util.git.TokenUtil;
import com.ladeit.util.redis.RedisUtil;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname AuthorityAspect
 * @Date 2020/2/7 10:04
 */
@Aspect
@Component
public class SlackLoginAspect {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserSlackRelationDao userSlackRelationDao;

    @Autowired
    private ServiceGroupDao serviceGroupDao;


    //Controller层切点
    @Pointcut("@annotation(com.ladeit.api.annotation.SlackLogin)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object object = null;
        ExecuteResult result = new ExecuteResult();
        Object[] args = point.getArgs();
        String targetId = args[0].toString();
        UserSlackRelation userSlackRelation = userSlackRelationDao.queryUserSlackRelationBySlackUserId(targetId);
        boolean userflag = false;
        if(userSlackRelation==null){
            result.setCode(Code.FAILED);
            // You have not bind a ladeit account, use /ladeit setup to bind.
            //result.addErrorMessage("你还未关联ladeit账户，可以使用 `/ladeit setup` 进行关联");
            result.addErrorMessage("You have not bind a ladeit account, use `/ladeit setup` to bind");
            return result;
        }else{
            try {
                //slackLogin(userSlackRelation.getUserName(),userSlackRelation.getUserId());
                //object = point.proceed();
                userflag = true;
            }catch (UnknownAccountException e) {
                result.setCode(Code.FAILED);
                // You have not bind a ladeit account, use /ladeit setup to bind.
                //result.addErrorMessage("你还未关联ladeit账户，可以使用 `/ladeit setup` 进行关联");
                result.addErrorMessage("You have not bind a ladeit account, use `/ladeit setup` to bind");
                return result;
            }
        }
        String token = args[1].toString();
        List<Certificate> list = serviceGroupDao.queryGroupBytoken(token);
        boolean tokenflag = false;
        if(list.size()==0){
            result.setCode(Code.FAILED);
            //slack传过来的token，未查找到服务组信息，服务组token无效
            result.addErrorMessage("Invalid token");
            return result;
        }else if(list.size()>1){
            result.setCode(Code.FAILED);
            //slack传过来的token，查找到多个服务组信息，服务组token重复
            result.addErrorMessage("Invalid token");
            return result;
        }else{
            tokenflag = true;
        }

        if(userflag&&tokenflag){
            object = point.proceed();
        }
        return object;
    }

}
