package com.ladeit.pojo.ao;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @program: ladeit
 * @description: ResourceAO
 * @author: falcomlife
 * @create: 2019/11/15
 * @version: 1.0.0
 */
@Data
public class ResourceAO {
	private List<Map<String,String>> replicationControllers;
	private List<Map<String,String>> deployments;
	private List<Map<String,String>> statefulSets;
	private List<Map<String,String>> jobs;
	private List<Map<String,String>> cronJobs;
	private List<Map<String,String>> daemonSets;
	private List<Map<String,String>> pods;
	private List<Map<String,String>> services;
	private List<Map<String,String>> ingresses;
	private List<Map<String,String>> configMaps;
	private List<Map<String,String>> secrets;
	private List<Map<String,String>> serviceAccounts;
	private List<Map<String,String>> persistentVolumes;
	private List<Map<String,String>> persistentVolumeClaims;
	private List<Map<String,String>> storageClasses;
	private List<Map<String,String>> manualYaml;
}
