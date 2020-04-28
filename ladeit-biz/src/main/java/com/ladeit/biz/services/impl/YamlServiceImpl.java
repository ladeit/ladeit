package com.ladeit.biz.services.impl;

import com.ladeit.biz.dao.YamlDao;
import com.ladeit.biz.services.YamlService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.pojo.ao.YamlAO;
import com.ladeit.pojo.doo.Yaml;
import com.ladeit.util.ListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname YamlServiceImpl
 * @Date 2019/12/31 14:45
 */
@Service
public class YamlServiceImpl implements YamlService {

    @Autowired
    private YamlDao yamlDao;

    /**
     * 新增yaml记录
     * @param serviceGroupId, serviceId, yamlContent
     * @return void
     * @date 2019/12/31
     * @ahthor MddandPyy
     */
    @Override
    public void insertYaml(String serviceGroupId, String serviceId, String yamlContent,String yamlName,String type) {
        Yaml yaml = new Yaml();
        yaml.setId(UUID.randomUUID().toString());
        yaml.setServiceGroupId(serviceGroupId);
        yaml.setServiceId(serviceId);
        yaml.setContent(yamlContent);
        yaml.setName(yamlName);
        yaml.setType(type);
        yaml.setCreateAt(new Date());
        yamlDao.insert(yaml);
    }


    /**
     * 查询服务组下某服务的yaml
     * @param serviceGroupId, serviceId
     * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.YamlAO>>
     * @date 2019/12/31
     * @ahthor MddandPyy
     */
    @Override
    public ExecuteResult<Pager<YamlAO>> queryYaml(String serviceGroupId, String serviceId, int currentPage, int pageSize) {
        ExecuteResult<Pager<YamlAO>> result = new ExecuteResult<>();
        Pager<YamlAO> pager = new Pager<>();
        pager.setPageNum(currentPage);
        pager.setPageSize(pageSize);
        List<Yaml> yamls = yamlDao.queryYamls( serviceGroupId, serviceId,currentPage, pageSize);
        int yamlCount = yamlDao.queryYamlCount(serviceGroupId, serviceId);
        List<YamlAO> resultList = new ListUtil<Yaml, YamlAO>().copyList(yamls,
                YamlAO.class);
        pager.setRecords(resultList);
        pager.setTotalRecord(yamlCount);
        result.setResult(pager);
        return result;
    }

    /**
     * 下载yaml文件
     * @param yamlId, response
     * @return void
     * @date 2019/12/31
     * @ahthor MddandPyy
     */
    @Override
    public void downloadYaml(String yamlId, HttpServletResponse response) throws IOException {
        Yaml yaml = yamlDao.queryYaml(yamlId);
        response.setContentType("text/plain");
        response.setHeader("Content-disposition", "attachment; filename=yaml");
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        ZipOutputStream zip = null;
        //将字符串变成输入流
        bis = new BufferedInputStream(new ByteArrayInputStream(yaml.getContent().getBytes("utf-8")));
        //获取输出流
        bos = new BufferedOutputStream(response.getOutputStream());
        byte[] buff = new byte[yaml.getContent().getBytes().length];
        //zip压缩文件输出流
        zip = new ZipOutputStream(bos);
        //获取zip压缩文件中的实体（test.txt为压缩文件中的文本文件名称）
        ZipEntry entry = new ZipEntry("service.yaml");
        entry.setSize(yaml.getContent().getBytes().length);
        //将实体放入压缩文件流中
        zip.putNextEntry(entry);

        //写入
        int len = 0;
        while ((len = bis.read(buff)) != -1) {
            zip.write(buff, 0, len);
        }
        bis.close();
        zip.flush();
        zip.close();
    }

    /**
     * 下载yaml文件
     * @param serviceId, response
     * @return void
     * @date 2019/12/31
     * @ahthor MddandPyy
     */
    @Override
    public void downloadAllYaml(String serviceId, HttpServletResponse response) throws IOException {
        List<Yaml> yamls = yamlDao.queryYamlsByServiceId(serviceId);
        response.setContentType("text/plain");
        response.setHeader("Content-disposition", "attachment; filename=yaml");
        BufferedInputStream bis = null;
        BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());;
        ZipOutputStream zip = new ZipOutputStream(bos);;
        for(Yaml yaml:yamls){
            //将字符串变成输入流
            bis = new BufferedInputStream(new ByteArrayInputStream(yaml.getContent().getBytes("utf-8")));
            //获取输出流
            byte[] buff = new byte[yaml.getContent().getBytes().length];
            //zip压缩文件输出流
            //获取zip压缩文件中的实体（test.txt为压缩文件中的文本文件名称）
            ZipEntry entry = new ZipEntry(yaml.getName()+".yaml");
            entry.setSize(yaml.getContent().getBytes().length);
            //将实体放入压缩文件流中
            zip.putNextEntry(entry);
            //写入
            int len = 0;
            while ((len = bis.read(buff)) != -1) {
                zip.write(buff, 0, len);
            }
        }
        bis.close();
        zip.flush();
        zip.close();
    }
}
