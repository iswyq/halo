package run.halo.app.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import run.halo.app.model.entity.BaseComment;
import run.halo.app.security.context.SecurityContextHolder;


/**
 * @author giveup
 * @description SensitiveMaskAspect
 * @date 10:22 PM 25/5/2020
 */
@Aspect
@Component
public class SensitiveConcealAspect {

    /*
     * 在这里对注解的功能进行实现
     *
     * */
    @Pointcut("@annotation(run.halo.app.annotation.SensitiveConceal)")
    public void pointCut() {
    }

    private Object sensitiveMask(Object comment) {
        if (comment instanceof BaseComment) {
            ((BaseComment) comment).setEmail("");
            ((BaseComment) comment).setIpAddress("");
        }
        return comment;
    }


    @Around("pointCut()")
    public Object mask(ProceedingJoinPoint joinPoint) throws Throwable {

        Object result = joinPoint.proceed();

        if (SecurityContextHolder.getContext().isAuthenticated()) {

            return result;

        }

        if (result instanceof Iterable) {
            /*
            * 将this直接传递到sensitiveMask方法中
            *
            * */
            //TODO lamaboda表达式的运行过程不清楚
            ((Iterable<?>) result).forEach(this::sensitiveMask);

        }

        return sensitiveMask(result);

    }


}
