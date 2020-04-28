package com.ladeit.util.git;

public class IpConvertUrl {
	public static String convertUrl(String projectUrl) {
		String gitlabHostUrl = null;
		// 截取服务器地址
		if (IpVerifyUtil.isIP(projectUrl)) {
			// 判断开头是否包含http://
			if (projectUrl.startsWith("http://")) {
				String[] ipAndPort = projectUrl.split("/")[2].split(":");
				// 如果不是使用的80端口
				if (ipAndPort.length == 2) {
					gitlabHostUrl = "http://" + ipAndPort[0] + ":" + ipAndPort[1];
				} else {
					gitlabHostUrl = "http://" + ipAndPort[0];
				}
			} else {// 非http://开头
				String[] ipAndPort = projectUrl.split("/")[0].split(":");
				// 如果不是使用的80端口
				if (ipAndPort.length == 2) {
					gitlabHostUrl = "http://" + ipAndPort[0] + ":" + ipAndPort[1];
				} else {
					gitlabHostUrl = "http://" + ipAndPort[0];
				}
			}
		} else {
			// 判断开头是否包含http://
			if (projectUrl.startsWith("http://")) {
				gitlabHostUrl = projectUrl.split("/")[2];
			} else {
				gitlabHostUrl = projectUrl.split("/")[0];
			}
		}
		return "http://"+gitlabHostUrl;
	}
}
