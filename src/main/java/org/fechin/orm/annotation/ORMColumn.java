package org.fechin.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author:朱国庆
 * @Date：2020/1/8 19:13
 * @Desription: fechin_orm
 * @Version: 1.0
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ORMColumn {

    String name() default "";
}
