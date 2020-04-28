package com.ladeit.biz.aop;

import com.ladeit.biz.annotation.Authority;
import com.ladeit.biz.dao.UserClusterRelationDao;
import com.ladeit.biz.dao.UserEnvRelationDao;
import com.ladeit.biz.dao.UserServiceGroupRelationDao;
import com.ladeit.biz.dao.UserServiceRelationDao;
import com.ladeit.biz.shiro.SecurityUtils;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.doo.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname AuthorityAspect
 * @Date 2020/2/7 10:04
 */
@Aspect
@Component
public class AuthorityAspect {

    @Autowired
    private UserServiceGroupRelationDao userServiceGroupRelationDao;

    @Autowired
    private UserServiceRelationDao userServiceRelationDao;

    @Autowired
    private UserClusterRelationDao userClusterRelationDao;

    @Autowired
    private UserEnvRelationDao userEnvRelationDao;


    //Controller层切点
    @Pointcut("@annotation(com.ladeit.biz.annotation.Authority)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        String username = user.getUsername();
        Object object = null;
        if("admin".equals(username)){
            object = point.proceed();
        }else{
            ExecuteResult result = new ExecuteResult();
            String targetName = point.getTarget().getClass().getName();
            String methodName = point.getSignature().getName();
            Object[] arguments = point.getArgs();
            Class targetClass = null;
            try {
                targetClass = Class.forName(targetName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Method[] methods = targetClass.getMethods();
            String type = "";
            String level = "";
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    Class[] clazzs = method.getParameterTypes();
                    if (clazzs.length == arguments.length) {
                        type = method.getAnnotation(Authority.class).type();
                        level = method.getAnnotation(Authority.class).level();
                        break;
                    }
                }
            }
            Object[] args = point.getArgs();
            String targetId = args[0].toString();
            switch (type){
                case "group":{
                    UserServiceGroupRelation userServiceGroupRelation = userServiceGroupRelationDao.getGrouprelation(user.getId(),targetId);
                    if(userServiceGroupRelation!=null){
                        String userlevel = userServiceGroupRelation.getAccessLevel();
                        if(userlevel.contains(level)){
                            object = point.proceed();
                        }else{
                            result.setCode(Code.AUTH_ERROR);
                            return result;
                        }
                    }else{
                        result.setCode(Code.AUTH_ERROR);
                        return result;
                    }
                    break;
                }
                case "service":{
                    UserServiceRelation userServiceRelation = userServiceRelationDao.getServiceRelation(user.getId(),targetId);
                    if(userServiceRelation!=null){
                        String userlevel = userServiceRelation.getRoleNum();
                        if(userlevel.contains(level)){
                            object = point.proceed();
                        }else{
                            result.setCode(Code.AUTH_ERROR);
                            return result;
                        }
                    }else{
                        result.setCode(Code.AUTH_ERROR);
                        return result;
                    }
                    break;
                }
                case "cluster":{
                    UserClusterRelation userClusterRelation = userClusterRelationDao.queryByClusterIdAndUserId(targetId,user.getId());
                    if(userClusterRelation!=null){
                        String userlevel = userClusterRelation.getAccessLevel();
                        if(userlevel.contains(level)){
                            object = point.proceed();
                        }else{
                            result.setCode(Code.AUTH_ERROR);
                            return result;
                        }
                    }else{
                        result.setCode(Code.AUTH_ERROR);
                        return result;
                    }
                    break;
                }
                case "env":{
                    UserEnvRelation userEnvRelation = userEnvRelationDao.queryByEnvIdAndUserId(targetId,user.getId());
                    if(userEnvRelation!=null){
                        String userlevel = userEnvRelation.getAccessLevel();
                        if(userlevel.contains(level)){
                            object = point.proceed();
                        }else{
                            result.setCode(Code.AUTH_ERROR);
                            return result;
                        }
                    }else{
                        result.setCode(Code.AUTH_ERROR);
                        return result;
                    }
                    break;
                }
                case "adminquery":{
                    result.setCode(Code.AUTH_ERROR);
                    return result;
                }
                default:{
                    break;
                }
            }

        }
        return object;
    }

}
