package com.qqriceball.aspect;

import com.qqriceball.enumeration.AutoFillEnum;
import com.qqriceball.enumeration.OperationType;
import com.qqriceball.model.entity.Emp;
import com.qqriceball.model.vo.EmpVO;
import com.qqriceball.annotation.AutoFill;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Component
@Aspect
public class AutoFillAspect {


    @Pointcut("execution(* com.qqriceball.mapper.*.*(..)) && @annotation(com.qqriceball.annotation.AutoFill)")
    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("[AutoFillAspect] 開始執行");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0){
            log.debug("[AutoFillAspect] joinPoint 沒有參數");
            return;
        }

        Object entity = args[0];
        log.debug("[AutoFillAspect] Entity 類別: {}", entity == null ? "null" : entity.getClass().getName());


        LocalDateTime now = LocalDateTime.now();

        Integer operatorId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            log.debug("[AutoFillAspect] principal 類型: {}", principal == null ? "null" : principal.getClass().getName());

            if (principal instanceof EmpVO) {
                operatorId = ((EmpVO) principal).getId();
            } else if (principal instanceof Emp) {
                operatorId = ((Emp) principal).getId();
            } else {
                log.debug("[AutoFillAspect] 無法從 principal 取得 id，principal: {}", String.valueOf(principal));
            }
        }

        if (operatorId == null) {
            log.warn("[AutoFillAspect] operatorId 為 null，跳過自動填充");
            return;
        }

        BeanWrapperImpl wrapper = new BeanWrapperImpl(entity);
        if(operationType == OperationType.INSERT){
            try {
                Method setCreateId = entity.getClass().getDeclaredMethod(AutoFillEnum.SET_CREATE_ID.getValue(), Integer.class);
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillEnum.SET_CREATE_TIME.getValue(), LocalDateTime.class);
                Method setUpdateId = entity.getClass().getDeclaredMethod(AutoFillEnum.SET_UPDATE_ID.getValue(), Integer.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillEnum.SET_UPDATE_TIME.getValue(), LocalDateTime.class);

                setCreateId.invoke(entity, operatorId);
                setCreateTime.invoke(entity, now);
                setUpdateId.invoke(entity, operatorId);
                setUpdateTime.invoke(entity, now);

            }catch (Exception e) {
                log.error("[AutoFillAspect] INSERT 自動填充失敗", e);
            }

        }else if(operationType == OperationType.UPDATE){
            try {
                Method setUpdateId = entity.getClass().getDeclaredMethod(AutoFillEnum.SET_UPDATE_ID.getValue(), Integer.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillEnum.SET_UPDATE_TIME.getValue(), LocalDateTime.class);

                setUpdateId.invoke(entity, operatorId);
                setUpdateTime.invoke(entity, now);

            }catch (Exception e){
                log.error("[AutoFillAspect] UPDATE 自動填充失敗", e);
            }
        }
    }
}
