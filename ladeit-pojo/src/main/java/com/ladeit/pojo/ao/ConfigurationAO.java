package com.ladeit.pojo.ao;

import com.ladeit.pojo.ao.configuration.Env;
import com.ladeit.pojo.ao.configuration.LivenessProbe;
import com.ladeit.pojo.ao.configuration.Port;
import com.ladeit.pojo.ao.configuration.Volume;
import lombok.Data;

import java.util.List;

/**
 * @program: ladeit
 * @description: ConfigurationAO
 * @author: falcomlife
 * @create: 2020/01/07
 * @version: 1.0.0
 */
@Data
public class ConfigurationAO {
	private String type;
	private int replicas;
	private List<Port> ports;
	private List<Env> envs;
	private String command;
	private List<String> args;
	private List<Volume> volumes;
	private List<String> host;
	private LivenessProbe livenessProbe;
	private Integer cpuLimit;
	private String cpuLimitUnit;
	private Integer memLimit;
	private String memLimitUnit;
	private Integer cpuRequest;
	private String cpuRequestUnit;
	private Integer memRequest;
	private String memRequestUnit;
	private Boolean resourceQuota;
}
