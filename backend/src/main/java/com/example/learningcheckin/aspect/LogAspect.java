package com.example.learningcheckin.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.learningcheckin.annotation.Log;
import com.example.learningcheckin.entity.SysLog;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.SysLogMapper;
import com.example.learningcheckin.mapper.UserMapper;
import com.example.learningcheckin.utils.JwtUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private SysLogMapper sysLogMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Around("@annotation(com.example.learningcheckin.annotation.Log)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result = null;
        String errorMsg = null;
        int status = 0; // 0: Success

        try {
            result = point.proceed();
        } catch (Throwable e) {
            status = 1; // Fail
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long time = System.currentTimeMillis() - beginTime;
            saveLog(point, time, status, errorMsg);
        }
        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long time, int status, String errorMsg) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            SysLog log = new SysLog();
            log.setLogType("OPERATION");
            Log logAnnotation = method.getAnnotation(Log.class);
            if (logAnnotation != null) {
                log.setModule(logAnnotation.module());
                log.setAction(logAnnotation.action());
            }

            String className = joinPoint.getTarget().getClass().getName();
            String methodName = signature.getName();
            // log.setMethod(...) is no longer in SysLog, we can put it in extraInfo or action
            log.setAction(log.getAction() + " [" + className + "." + methodName + "]");

            // Request Args (Simplified)
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] != null) {
                try {
                    String argsStr = "";
                    if (args[0] instanceof java.util.Map) {
                        argsStr = args[0].toString();
                    } else {
                        argsStr = String.valueOf(args[0]);
                    }
                    
                    if (argsStr.length() > 200) argsStr = argsStr.substring(0, 200) + "...";
                    log.setContent(argsStr);
                } catch (Exception e) {
                }
            }
            
            if (errorMsg != null) {
                log.setContent(errorMsg);
            }

            // Request Info
            try {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                if (request != null) {
                    log.setIp(request.getRemoteAddr());
                    String token = request.getHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        String username = jwtUtils.getUsernameFromToken(token.substring(7));
                        if (username != null) {
                            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
                            if (user != null) {
                                log.setUserId(user.getId());
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }

            log.setStatus(status);
            log.setExecutionTime(time);
            log.setCreateTime(LocalDateTime.now());
            sysLogMapper.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
