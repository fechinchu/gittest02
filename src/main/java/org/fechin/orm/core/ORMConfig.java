package org.fechin.orm.core;

import org.dom4j.Document;
import org.fechin.orm.utils.AnnotationUtil;
import org.fechin.orm.utils.Dom4jUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author:朱国庆
 * @Date：2020/1/8 19:49
 * @Desription: fechin_orm
 * @Version: 1.0
 * 该类用来解析和封装核心配置文件
 */
public class ORMConfig {

    /**
     * classpath路径
     */
    private static String classpath;

    /**
     * 核心配置文件
     */
    private static File coreConfigFile;

    /**
     * 标签中的数据
     */
    private static Map<String, String> propConfig;

    /**
     * 映射配置文件路径
     */
    private static Set<String> mappingSet;

    /**
     * 实体类路径
     */
    private static Set<String> entitySet;

    /**
     * 映射信息
     */
    private static List<Mapper> mapperList;

    static {
        classpath = Thread.currentThread().getContextClassLoader().getResource("/").getPath();
        //针对中文路径进行转码
        try {
            classpath = URLDecoder.decode(classpath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //得到核心配置文件
        coreConfigFile = new File(classpath + "fechinORM.xml");

        if (coreConfigFile.exists()) {
            Document document = Dom4jUtil.getXMLByFilePath(coreConfigFile.getPath());
            propConfig = Dom4jUtil.Elements2Map(document, "property", "name");
            mappingSet = Dom4jUtil.Elements2Set(document, "mapping", "resource");
            entitySet = Dom4jUtil.Elements2Set(document, "entity", "package");

        } else {
            throw new RuntimeException("未能找到配置文件,请在classpath下添加'fechinORM.xml'的配置文件");
        }
    }

    /**
     * 获取连接
     *
     * @return
     */
    private Connection getConnection() {
        String url = propConfig.get("connection.url");
        String driverClass = propConfig.get("connection.driverClass");
        String username = propConfig.get("connection.username");
        String password = propConfig.get("connection.password");
        try {
            Class.forName(driverClass);
            Connection connection = DriverManager.getConnection(url, username, password);
            connection.setAutoCommit(false);
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void getMapping() {
        mapperList = new ArrayList<>();

        //解析mapper映射文件拿到数据
        for (String mappingPath : mappingSet) {
            Document document = Dom4jUtil.getXMLByFilePath(classpath + mappingPath);
            String className = Dom4jUtil.getPropValue(document, "class", "name");
            String tableName = Dom4jUtil.getPropValue(document, "class", "table");
            Map<String, String> id2Map = Dom4jUtil.ElementsID2Map(document);
            Map<String, String> elements2Map = Dom4jUtil.Elements2Map(document);
            Mapper mapper = new Mapper();
            mapper.setClassName(className);
            mapper.setTableName(tableName);
            mapper.setIdMapper(id2Map);
            mapper.setPropMapper(elements2Map);
            mapperList.add(mapper);
        }

        //解析实体类中的注解拿到映射数据
        for (String packagePath : entitySet) {
            Set<String> nameSet = AnnotationUtil.getClassNameByPackage(packagePath);
            for (String name : nameSet) {
                Class clz = null;
                try {
                    clz = Class.forName(name);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                String className = AnnotationUtil.getClassName(clz);
                String tableName = AnnotationUtil.getTableName(clz);
                Map<String, String> id_id = AnnotationUtil.getIdMapper(clz);
                Map<String, String> mapping = AnnotationUtil.getPropMapping(clz);
                Mapper mapper = new Mapper();
                mapper.setTableName(tableName);
                mapper.setClassName(className);
                mapper.setIdMapper(id_id);
                mapper.setPropMapper(mapping);
                mapperList.add(mapper);
            }
        }
    }

    public ORMSession buildORMSession(){
        //1.连接数据库
        Connection connection = getConnection();
        //2.得到映射数据
        getMapping();
        //3.创建ORMSession对象
        return new ORMSession(connection);
    }

}
