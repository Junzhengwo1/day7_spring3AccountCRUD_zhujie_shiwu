package com.kou.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * @author JIAJUN KOU
 *
 * 事务管理的工具类
 *
 * 包含开启事务
 * 提交事务
 * 回滚事务
 * 释放连接
 */
@Component("txManager")
@Aspect
public class TransactionManager {

    @Autowired
    private ConnectionUtils  connectionUtils;

    @Pointcut("execution(* com.kou.service.impl.*.*(..))")
    private void pt1(){}

    /**
     * 开启事务
     */

    public void beginTransaction(){
        try {
            connectionUtils.getThreadConnection().setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 提交事务
     */

    public void commit(){
        try {
            connectionUtils.getThreadConnection().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 回滚事务
     */

    public void rollback(){
        try {
            connectionUtils.getThreadConnection().rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放连接
     */

    public void close(){
        try {
            connectionUtils.getThreadConnection().close();//不是真的关闭，而是还回了线程池中
            connectionUtils.removeConnection();//解绑
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 环绕通知的使用  注解时使用
     * @param pjp
     * @return
     */
    @Around("pt1()")
    public Object aroundAdvice(ProceedingJoinPoint pjp){
        Object rtValue=null;
        try {
            Object[] args=pjp.getArgs();
            this.beginTransaction();
            rtValue=pjp.proceed(args);
            this.commit();
            return rtValue;
        } catch (Throwable e) {
            this.rollback();
            throw new RuntimeException(e);

        } finally {
            this.close();
        }

    }

}
