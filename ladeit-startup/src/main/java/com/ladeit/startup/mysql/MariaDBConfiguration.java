package com.ladeit.startup.mysql;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.ladeit.util.auth.TokenUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.*;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Properties;
import java.util.Set;

/**
 * 嵌入式mysql服务启动类
 * @param
 * @return
 * @date 2020/4/3
 * @author MddandPyy
 */
public class MariaDBConfiguration {

    //private static String properties = "./ladeit/config/.config.properties";
//    private static String properties = "./ladeit/config/.config.conf";
//    private static String basedir = "./ladeit/mysqlembed/baseDir";
//    private static String datadir = "./ladeit/mysqlembed/dataDir";

    private static String properties = System.getProperty("user.home")+"/.ladeit/config/.mysql.conf";
    private static String basedir = System.getProperty("user.home")+"/.ladeit/mysqlembed/baseDir";
    private static String datadir = System.getProperty("user.home")+"/.ladeit/mysqlembed/dataDir";
    private static String filepath = System.getProperty("user.home")+"/.ladeit/config";



    /**
     * 启动mysql 服务器
     */
    public Boolean startService() {
        try {
            String user = null;
            String ps = null;
            String port = null;
            //Map<String, String> options = new HashMap<String, String>();
            // 读取配置
            Properties props = new Properties();
            File file = new File(properties);
            //是否需要初始化标识，生成.conf文件
            boolean initflag = true;
            //mysql类型 0-内嵌，1-外联
            String redisType = null;
            if(file.exists()){
                props.load(new FileInputStream(file));
                // 处理启动参数
                final Set<Object> keys = props.keySet();
                for (Object key : keys) {
                    String val = props.getProperty(key.toString());
                    if ("".equals(val)) {
                    } else {
                        if("datasource.mysql.type".equals(key.toString())){
                            initflag = false;
                            redisType = val;
                        }
                        if("datasource.default.username".equals(key.toString())){
                            user = val;
                        }
                        if("datasource.default.password".equals(key.toString())){
                            ps = val;
                        }
                        if("port".equals(key.toString())){
                            port = val;
                        }
                    }
                }
            }
            if(initflag){
                props.setProperty("datasource.mysql.type","0");
                props.setProperty("port","3306");
                props.setProperty("datasource.default.username","root");
                props.setProperty("datasource.default.url","jdbc:mysql://localhost:3306/ladeit?useUnicode=true&characterEncoding=utf-8&useSSL=false");
                props.setProperty("datasource.default.driver","com.mysql.jdbc.Driver");
                //ps = TokenUtil.createToken("ps"+System.currentTimeMillis());
                ps = "mariaDB4j";
                props.setProperty("datasource.default.password",ps);
                putProperties(props);
                user = "root";
                port = "3306";
                startMysql(user,ps,port,initflag);
            }else{
                if(redisType!=null){
                    if("0".equals(redisType)){
                        startMysql(user,ps,port,initflag);
                    }
                }

            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
//        catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            return false;
//        }
        catch (ManagedProcessException e) {
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

    }

    public void startMysql(String user,String ps,String port,boolean initflag) throws ManagedProcessException, SQLException, IOException, ClassNotFoundException {
        // 启动 MySQL 资源实例
        System.out.println("启动内嵌mysql服务");
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setPort(Integer.parseInt(port)); // OR, default: setPort(0); => autom. detect free port
        configBuilder.setDataDir(datadir); // just an example
        configBuilder.setBaseDir(basedir);
        configBuilder.addArg("--user="+user);
        DB db = null;
        db = DB.newEmbeddedDB(configBuilder.build());
        db.start();
        initDataBase(user,ps,port,initflag);
    }


    public void putProperties(Properties props) throws IOException {
        FileOutputStream oFile = null;
        //File file = new File("./ladeit/config/.config.properties");
        File file = new File(properties);
        if(!file.exists()){
            File dir = new File(filepath);
            if(!dir.exists()){
                dir.mkdirs();
            }
            file.createNewFile();
        }
        oFile = new FileOutputStream(file,false);
        props.store(oFile, "ladeit-mysql-config");
        oFile.close();
    }

    public void initDataBase(String user,String ps,String port,boolean initflag) throws ClassNotFoundException, SQLException, IOException {
        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:"+port+"/?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC";
        String USER = user;
        String PASS = null;
        if(!initflag){
            PASS = ps;
        }
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
            String usedatabase = "use mysql";
            st.execute(usedatabase);
            st.execute("update user set password=password('"+ps+"') where user='root'");
            st.execute("flush privileges");
        }else {
           // st.execute("update user set password=password('"+ps+"') where user='root'");
            st.execute("use mysql");
            st.execute("update user set password=password('"+ps+"') where user='root'");
            st.execute("flush privileges");
            //创建数据库
            st.execute("create database ladeit");
            System.out.println("创建数据库成功");
            String usedatabase = "use ladeit";
            st.execute(usedatabase);
            ScriptRunner runner = new ScriptRunner(conn);
            Resources.setCharset(Charset.forName("UTF-8")); //设置字符集,不然中文乱码插入错误
            runner.setLogWriter(null);//设置是否输出日志
            // 绝对路径读取
            //Reader read = new FileReader(new File("f:\\test.sql"));
            //String sqlPath = this.getClass().getClassLoader().getResource("ladeitdata.sql").getPath();
            //Reader read = new FileReader(new File(sqlPath));


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