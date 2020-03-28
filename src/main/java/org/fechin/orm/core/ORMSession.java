package org.fechin.orm.core;

import java.sql.Connection;

/**
 * @Author:朱国庆
 * @Date：2020/1/8 20:54
 * @Desription: fechin_orm
 * @Version: 1.0
 */
public class ORMSession {

    private Connection connection;

    public ORMSession(Connection connection){
        this.connection = connection;
    }
}
