package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.ReleaseDao;
import com.ladeit.pojo.doo.Release;
import io.ebean.EbeanServer;
import io.ebean.ExpressionList;
import io.ebean.UpdateQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @program: ladeit
 * @description: ReleaseDaoImpl
 * @author: falcomlife
 * @create: 2019/11/06
 * @version: 1.0.0
 */
@Repository
public class ReleaseDaoImpl implements ReleaseDao {
    @Autowired
    private EbeanServer server;

    /**
     * 新建release
     *
     * @param release
     * @return void
     * @author falcomlife
     * @date 19-11-6
     * @version 1.0.0
     */
    @Override
    public void insert(Release release) {
        this.server.insert(release);
    }

    /**
     * 根据serviceid得到服役中的release
     *
     * @param serviceId
     * @return com.ladeit.pojo.doo.Release
     * @author falcomlife
     * @date 19-11-11
     * @version 1.0.0
     */
    @Override
    public Release getInUseReleaseByServiceId(String serviceId) {
        return this.server.createQuery(Release.class).where().eq("service_id", serviceId).eq("status", 1).findOne();
    }

    @Override
    public Release getInUseReleaseByReleaseId(String releaseId) {
        return this.server.createQuery(Release.class).where().eq("id", releaseId).findOne();
    }

    @Override
    public List<Release> getReleaseList(String serviceId) {
        return this.server.createQuery(Release.class).where().eq("service_id", serviceId).orderBy("deploy_start_at " +
                "desc").findList();
    }

    @Override
    public List<Release> getReleaseListPager(String serviceId, int currentPage, int pageSize) {
        return this.server.createQuery(Release.class).where().eq("service_id", serviceId).setFirstRow((currentPage - 1) * pageSize).setMaxRows(pageSize).orderBy("deploy_start_at " +
                "desc").findList();
    }

    @Override
    public int getReleaseListCount(String serviceId) {
        return this.server.createQuery(Release.class).where().eq("service_id", serviceId).orderBy("deploy_start_at " +
                "desc").findCount();
    }



    @Override
    public List<Release> getReleaseListByIdAndName(String releaseId, String releaseName) {
        ExpressionList expressionList = this.server.createQuery(Release.class).where();
        if (!(releaseId == null || releaseId.trim().length() == 0)) {
            expressionList.eq("id", releaseId);
        }

        if (!(releaseName == null || releaseName.trim().length() == 0)) {
            expressionList.eq("name", releaseName);
        }
        return expressionList.findList();
    }

    @Override
    public List<Release> getReleasesByServiceIdAndImageId(String serviceId, String imageId) {
        return this.server.createQuery(Release.class).where().eq("serviceId", serviceId).eq("imageId", imageId).findList();
    }

    /**
     * 修改release状态
     *
     * @param releaseInUse
     * @return void
     * @author falcomlife
     * @date 19-12-13
     * @version 1.0.0
     */
    @Override
    public void updateReleaseStatusToRetire(Release releaseInUse) {
        this.server.update(Release.class).set("status", releaseInUse.getStatus()).set("service_finish_at", releaseInUse.getServiceFinishAt()).where().idEq(releaseInUse.getId()).update();
    }

    /**
     * 发布完成，修改状态，同时修改时间
     *
     * @param r1
     */
    @Override
    public void updateStatus(Release r1) {
        UpdateQuery<Release> updateQuery = this.server.update(Release.class);
        if (r1.getStatus() != null) {
            updateQuery.set("status", r1.getStatus());
        }
        if (r1.getDeployFinishAt() != null) {
            updateQuery.set("deploy_finish_at", r1.getDeployFinishAt());
        }
        if (r1.getServiceStartAt() != null) {
            updateQuery.set("service_start_at", r1.getServiceStartAt());
        }
        if (r1.getServiceFinishAt() != null) {
            updateQuery.set("service_finish_at", r1.getServiceFinishAt());
        }
        if (r1.getDuration() != null) {
            updateQuery.set("duration", r1.getDuration());
        }
        updateQuery.where().eq("id", r1.getId()).update();
    }

    /**
     * 查询发布中的release
     * @param serviceId
     * @return
     */
    @Override
    public Release getInUpdateRelease(String serviceId) {
        return this.server.createQuery(Release.class).where().eq("service_id",serviceId).eq("status",0).findOne();
    }

    @Override
    public Boolean isReleaseNew(String serviceId, Date imageDate) {
        List<Release> releases = server.createQuery(Release.class).where().eq("serviceId",serviceId).ge("deployStartAt",imageDate).findList();
        if(releases.size()==0){
            return false;
        }else{
            return true;
        }
    }
}
