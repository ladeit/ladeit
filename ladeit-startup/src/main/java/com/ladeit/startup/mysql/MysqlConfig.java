package com.ladeit.startup.mysql;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.*;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname MysqlConfiguration
 * @Date 2020/4/27 14:07
 */
public class MysqlConfig {
    private static String basedir = System.getProperty("user.home")+"/.ladeit/mysqlembed/baseDir";
    private static String datadir = System.getProperty("user.home")+"/.ladeit/mysqlembed/dataDir";

    public boolean startMysql(){
        try {
            // 启动 MySQL 资源实例
            System.out.println("启动内嵌mysql服务");
            DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
            configBuilder.setPort(3306); // OR, default: setPort(0); => autom. detect free port
            configBuilder.setDataDir(datadir); // just an example
            configBuilder.setBaseDir(basedir);
            configBuilder.addArg("--user="+"root");
            DB db = null;
            db = DB.newEmbeddedDB(configBuilder.build());
            db.start();
            initDataBase();
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (ManagedProcessException e) {
            e.printStackTrace();
            return false;
        }
    }



    public void initDataBase() throws ClassNotFoundException, SQLException, IOException {
        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC";
        String USER = "root";
        String PASS = "";
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;

        Class.forName(JDBC_DRIVER);
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        st = conn.createStatement();
        //校验数据库是否存在
        String getDB = "SELECT information_schema.SCHEMATA.SCHEMA_NAME FROM information_schema.SCHEMATA where SCHEMA_NAME='ladeit'";
        rs =st.executeQuery(getDB);

        if(rs.next()) {
            System.out.println("数据库已经存在");
        }else {
            //创建数据库
            st.execute("create database ladeit");
            System.out.println("创建数据库成功");
            String usedatabase = "use ladeit";
            st.execute(usedatabase);
            ScriptRunner runner = new ScriptRunner(conn);
            Resources.setCharset(Charset.forName("UTF-8")); //设置字符集,不然中文乱码插入错误
            runner.setLogWriter(null);//设置是否输出日志
            // 从class目录下直接读取
            Reader read = Resources.getResourceAsReader("ladeitdata.sql");
            runner.runScript(read);
            runner.closeConnection();
            System.out.println("sql脚本执行完毕");
            conn.close();
        }
        st.close();
        conn.close();
    }
}
