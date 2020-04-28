package com.ladeit.biz.services.impl;

import com.alibaba.fastjson.JSON;
import com.ladeit.biz.dao.*;
import com.ladeit.biz.services.MessageService;
import com.ladeit.biz.utils.HttpHelper;
import com.ladeit.biz.utils.MessageUtils;
import com.ladeit.biz.utils.PropertiesUtil;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.ao.MessageAO;
import com.ladeit.pojo.ao.MessageStateAO;
import com.ladeit.pojo.ao.ServiceAO;
import com.ladeit.pojo.ao.ServiceGroupAO;
import com.ladeit.pojo.doo.*;
import io.ebean.SqlRow;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname MessageServiceImpl
 * @Date 2020/3/14 8:15
 */
@Service
public class MessageServiceImpl implements MessageService {

	@Autowired
	private MessageDao messageDao;
	@Autowired
	private MessageStateDao messageStateDao;
	@Autowired
	private UserServiceGroupRelationDao userServiceGroupRelationDao;
	@Autowired
	private UserServiceRelationDao userServiceRelationDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private ServiceDao serviceDao;
	@Autowired
	private ServiceGroupDao serviceGroupDao;
	//@Value("${ladeit-bot-notif-mngr.host}")
	//private String host;
	@Autowired
	private MessageUtils messageUtils;

	@Autowired
	private PropertiesUtil propertiesUtil;

	/**
	 * 消息新增公共方法
	 *
	 * @param message
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<String> insertMessage(Message message,boolean isUser) {
		ExecuteResult<String> result = new ExecuteResult<>();
		String type = message.getType();
		if (type != null) {
			switch (type) {
				//新版本
				case "10": {
					result = insertServiceMessageAndState(message, isUser);
					break;
				}
				//新发布
				case "20": {
					Message messagenew = new Message();
					BeanUtils.copyProperties(message, messagenew);
					String content = message.getContent();
					Map<String, Object> param = message.getParams();
					// content+"   \n Image: "+param.get("imageVersioin")+"\nDeployment: "+param.get("releaseName")
					// +"\nFrom: "+param.get("operChannel")
					content =
							content + "   \nImage: " + param.get("imageVersioin") + "\nRelease: " + param.get("releaseName") + "\nTrigger at: " + param.get("operChannel");
					messagenew.setContent(content);
					result = insertServiceMessageAndState(messagenew, isUser);
					break;
				}
				//新服务
				case "30": {
					result = insertOneMessage(message);
					break;
				}
				//删除服务
				case "40": {
					result = insertServiceMessageAndState(message, Boolean.TRUE);
					break;
				}
				//新成员
				case "50": {
					result = insertServiceGroupMessageAndState(message);
					break;
				}
				//移出成员
				case "60": {
					result = insertServiceGroupMessageAndState(message);
					break;
				}
				//删除组
				case "70": {
					result = insertServiceGroupMessageAndState(message);
					break;
				}
				//解绑bot
				case "80": {
					result = insertServiceGroupMessageAndState(message);
					break;
				}
				//伸缩pod
				case "90": {
					result = insertServiceMessageAndState(message, isUser);
					break;
				}
				//调整拓扑
				case "100": {
					result = insertServiceMessageAndState(message, Boolean.TRUE);
					break;
				}
				//运行异常
				case "110": {
					result = insertServiceMessageAndState(message, Boolean.FALSE);
					break;
				}

				default: {
					result.setCode(Code.FAILED);
					String messagestr = messageUtils.matchMessage("M0010", new Object[]{}, isUser);
					result.addErrorMessage(messagestr);
					break;
				}
			}
		} else {
			result.setCode(Code.FAILED);
			String messagestr = messageUtils.matchMessage("M0011", new Object[]{}, isUser);
			result.addErrorMessage(messagestr);
		}
		return result;
	}

	/**
	 * slack消息新增公共方法
	 *
	 * @param message
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<String> insertSlackMessage(Message message) {
		ExecuteResult<String> result = new ExecuteResult<>();
		String type = message.getType();
		if (type != null) {
			switch (type) {
				//新版本
				case "10": {
					result = PushSlackMessageType10(message);
					break;
				}
				//新发布
				case "20": {
					Message messagenew = new Message();
					BeanUtils.copyProperties(message, messagenew);
					String content = message.getContent();
					Map<String, Object> param = message.getParams();
					// content+"   \nImage: "+param.get("imageVersioin")+"\nDeployment: "+param.get("releaseName")
					// +"\nFrom: "+param.get("operChannel");
					content =
							content + "   \nImage: " + param.get("imageVersioin") + "\nRelease: " + param.get("releaseName") + "\nOperated on: " + param.get("operChannel");
					messagenew.setContent(content);
					result = PushCommonMessage(messagenew);
					break;
				}
				//删除服务
				case "40": {
					result = PushCommonMessage(message);
					break;
				}
				//解绑bot
				case "80": {
					result = PushSlackMessageType80(message);
					break;
				}
				//伸缩pod
				case "90": {
					result = PushCommonMessage(message);
					break;
				}
				//调整拓扑
				case "100": {
					result = PushCommonMessage(message);
					break;
				}
				//运行异常
				case "110": {
					result = PushCommonMessage(message);
					break;
				}


				default: {
					result.setCode(Code.FAILED);
					String messagestr = messageUtils.matchMessage("M0010", new Object[]{}, Boolean.TRUE);
					result.addErrorMessage(messagestr);
					break;
				}
			}
		} else {
			result.setCode(Code.FAILED);
			String messagestr = messageUtils.matchMessage("M0011", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(messagestr);
		}
		return result;
	}

	/**
	 * 通用消息新增，根据传入的serviceId，生成服务相关成员的消息状态记录
	 *
	 * @param message
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	public ExecuteResult<String> insertServiceMessageAndState(Message message, boolean isUser) {
		ExecuteResult<String> result = new ExecuteResult<>();
		//维护服务相关人员右上消息通知
		String serviceId = message.getServiceId();
		List<UserServiceRelation> userServiceRelations =
				userServiceRelationDao.getServiceRelationByServiceId(serviceId);
		for (UserServiceRelation serviceRelation : userServiceRelations) {
			String roleNum = serviceRelation.getRoleNum();
			if (!(roleNum == null || roleNum.trim().length() == 0)) {
				MessageState messageState = new MessageState();
				messageState.setId(UUID.randomUUID().toString());
				messageState.setMessageId(message.getId());
				messageState.setUserId(serviceRelation.getUserId());
				messageState.setReadFlag(false);
				messageStateDao.insert(messageState);
			}
		}
		//新增消息主体
		messageDao.insert(message);
		String messagestr = messageUtils.matchMessage("M0100", new Object[]{}, isUser);
		result.setResult(messagestr);
		return result;
	}

	/**
	 * 通用消息新增，根据传入的serviceGroupId，生成组内成员的消息状态记录
	 *
	 * @param message
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	public ExecuteResult<String> insertServiceGroupMessageAndState(Message message) {
		ExecuteResult<String> result = new ExecuteResult<>();
		//维护组内人员右上消息通知
		String serviceGroupId = message.getServiceGroupId();
		List<UserServiceGroupRelation> groupRelations =
				userServiceGroupRelationDao.queryGrouprelationByGroupId(serviceGroupId);
		for (UserServiceGroupRelation groupRelation : groupRelations) {
			MessageState messageState = new MessageState();
			messageState.setId(UUID.randomUUID().toString());
			messageState.setMessageId(message.getId());
			messageState.setUserId(groupRelation.getUserId());
			messageState.setReadFlag(false);
			messageStateDao.insert(messageState);
		}
		//新增消息主体
		messageDao.insert(message);
		String messagestr = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(messagestr);
		return result;
	}

	/**
	 * 针对该用户生成一条消息,用户id存放在Message的target_user中
	 *
	 * @param message
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	public ExecuteResult<String> insertOneMessage(Message message) {
		ExecuteResult<String> result = new ExecuteResult<>();
		//维护组内人员右上消息通知
		MessageState messageState = new MessageState();
		messageState.setId(UUID.randomUUID().toString());
		messageState.setMessageId(message.getId());
		messageState.setUserId(message.getTarget_user());
		messageState.setReadFlag(false);
		messageStateDao.insert(messageState);
		//新增消息主体
		messageDao.insert(message);
		String messagestr = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(messagestr);
		return result;
	}

	/**
	 * 某人被拉入某个组，针对该该组的其他人生成一条消息,用户id存放在Message的target_user中
	 *
	 * @param message
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	public ExecuteResult<String> insertUserInGroupOther(Message message) {
		ExecuteResult<String> result = new ExecuteResult<>();
		//维护组内人员右上消息通知
		String serviceGroupId = message.getServiceGroupId();
		List<UserServiceGroupRelation> groupRelations =
				userServiceGroupRelationDao.queryGrouprelationByGroupId(serviceGroupId);
		for (UserServiceGroupRelation groupRelation : groupRelations) {
			String userId = groupRelation.getUserId();
			if (userId.equals(message.getTarget_user())) {
				continue;
			}
			MessageState messageState = new MessageState();
			messageState.setId(UUID.randomUUID().toString());
			messageState.setMessageId(message.getId());
			messageState.setUserId(groupRelation.getUserId());
			messageState.setReadFlag(false);
			messageStateDao.insert(messageState);
		}
		//新增消息主体
		messageDao.insert(message);
		String messagestr = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(messagestr);
		return result;
	}

	/**
	 * 此方法暂时不用
	 * 某人被拉入组或者移出组，针对该用户生成一条消息，组内其他用户生成一条消息,用户id存放在Message的target_id中
	 *
	 * @param message
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	public ExecuteResult<String> insertOneAndMoreMessage(Message message) {
		ExecuteResult<String> result = new ExecuteResult<>();
		//维护个人右上消息通知
		MessageState messageState = new MessageState();
		messageState.setId(UUID.randomUUID().toString());
		messageState.setMessageId(message.getId());
		messageState.setUserId(message.getTargetId());
		messageState.setReadFlag(false);
		messageStateDao.insert(messageState);
		//新增消息主体
		messageDao.insert(message);
		//维护组内其他人员右上消息通知
		String serviceGroupId = message.getServiceGroupId();
		List<UserServiceGroupRelation> groupRelations =
				userServiceGroupRelationDao.queryGrouprelationByGroupId(serviceGroupId);
		for (UserServiceGroupRelation groupRelation : groupRelations) {
			String userId = groupRelation.getUserId();
			if (userId.equals(message.getTargetId())) {
				continue;
			}
			MessageState messageStateMore = new MessageState();
			messageStateMore.setId(UUID.randomUUID().toString());
			messageStateMore.setMessageId(message.getId());
			messageStateMore.setUserId(groupRelation.getUserId());
			messageStateMore.setReadFlag(false);
			messageStateDao.insert(messageStateMore);
		}
		String messagestr = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(messagestr);
		return result;
	}

	/**
	 * 查询个人右上消息列表
	 *
	 * @param readFlag
	 * @return com.ladeit.common.ExecuteResult<java.util.List               <               io.ebean.SqlRow>>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<Pager<SqlRow>> getUserMessageInfos(int currentPage, int pageSize, String readFlag,
															String serviceGroupId, String type) {
		ExecuteResult<Pager<SqlRow>> result = new ExecuteResult<Pager<SqlRow>>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		String userId = user.getId();
		List<SqlRow> messages = messageDao.queryMessageSqlrowPagerList(userId, currentPage, pageSize, readFlag,
				serviceGroupId, type);
		int count = messageDao.queryMessageSqlrowCount(userId, readFlag, serviceGroupId, type);
		Pager<SqlRow> pager = new Pager<>();
		pager.setPageNum(currentPage);
		pager.setPageSize(pageSize);
		pager.setRecords(messages);
		pager.setTotalRecord(count);
		result.setResult(pager);
		return result;
	}

	/**
	 * 查询某条消息
	 *
	 * @param messageId
	 * @return com.ladeit.common.ExecuteResult<java.util.List               <               io.ebean.SqlRow>>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<MessageAO> getUserMessageInfo(String messageId) {
		ExecuteResult<MessageAO> result = new ExecuteResult<MessageAO>();
		Message message = messageDao.getMessage(messageId);
		if (message != null) {
			MessageAO messageAO = new MessageAO();
			BeanUtils.copyProperties(message, messageAO);
			User user = userDao.getUserById(message.getOperuserId());
			if (user != null) {
				messageAO.setOperuserName(user.getUsername());
			}
			String groupId = message.getServiceGroupId();
			if (groupId != null) {
				ServiceGroup serviceGroup = serviceGroupDao.getGroupById(groupId);
				if (serviceGroup != null) {
					messageAO.setServiceGroupName(serviceGroup.getName());
				}
			}
			String serviceId = message.getServiceId();
			if (serviceId != null) {
				com.ladeit.pojo.doo.Service service = serviceDao.queryServiceById(serviceId);
				if (service != null) {
					messageAO.setServiceName(service.getName());
				}
			}

			result.setResult(messageAO);
		}
		return result;
	}

	/**
	 * 删除个人消息（物理删除message_state记录）
	 *
	 * @param messageStateAOS
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<String> deleteMessageState(List<MessageStateAO> messageStateAOS) {
		ExecuteResult<String> result = new ExecuteResult<>();
		for (MessageStateAO messageStateAO : messageStateAOS) {
			MessageState messageState = messageStateDao.getState(messageStateAO.getId());
			messageStateDao.delete(messageState);
		}
		String messagestr = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(messagestr);
		return result;
	}

	/**
	 * 更新个人消息（标记已读）
	 *
	 * @param messageStateAOS
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<String> updateMessageState(List<MessageStateAO> messageStateAOS) {
		ExecuteResult<String> result = new ExecuteResult<>();
		for (MessageStateAO messageStateAO : messageStateAOS) {
			MessageState messageState = messageStateDao.getState(messageStateAO.getId());
			messageState.setReadFlag(true);
			messageStateDao.update(messageState);
		}
		String messagestr = messageUtils.matchMessage("M0012", new Object[]{}, Boolean.TRUE);
		result.setResult(messagestr);
		return result;
	}

	/**
	 * 删除全部个人消息（物理删除message_state记录）
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<String> deleteAllMessageState() {
		ExecuteResult<String> result = new ExecuteResult<>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		List<MessageState> messageStates = messageStateDao.getStates(user.getId());
		for (MessageState messageState : messageStates) {
			messageStateDao.delete(messageState);
		}
		String messagestr = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(messagestr);
		return result;
	}

	/**
	 * 更新个人消息（全部标记已读）
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<String> updateAllMessageState() {
		ExecuteResult<String> result = new ExecuteResult<>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		List<MessageState> messageStates = messageStateDao.getNoReadStates(user.getId());
		for (MessageState messageState : messageStates) {
			messageState.setReadFlag(true);
			messageStateDao.update(messageState);
		}
		String messagestr = messageUtils.matchMessage("M0012", new Object[]{}, Boolean.TRUE);
		result.setResult(messagestr);
		return result;
	}

	/**
	 * 查询与当前登录人有关的service列表
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.util.List               <               com.ladeit.pojo.ao.ServiceAO>>
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<List<ServiceAO>> getMessageServiceList() {
		ExecuteResult<List<ServiceAO>> result = new ExecuteResult<List<ServiceAO>>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		List<UserServiceRelation> userServiceRelations =
				userServiceRelationDao.getServiceRelationByUserId(user.getId());
		List<ServiceAO> serviceAOS = new ArrayList<>();
		for (UserServiceRelation userServiceRelation : userServiceRelations) {
			com.ladeit.pojo.doo.Service service = serviceDao.getById(userServiceRelation.getServiceId());
			if (service != null) {
				ServiceAO serviceAO = new ServiceAO();
				serviceAO.setId(service.getId());
				serviceAO.setName(service.getName());
				serviceAOS.add(serviceAO);
			}
		}
		result.setResult(serviceAOS);
		return result;
	}

	/**
	 * 查询group列表
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.util.List               <               com.ladeit.pojo.ao.ServiceAO>>
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<List<ServiceGroupAO>> getMessageServiceGroupList() {
		ExecuteResult<List<ServiceGroupAO>> result = new ExecuteResult<List<ServiceGroupAO>>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		List<UserServiceGroupRelation> groupRelations =
				userServiceGroupRelationDao.queryGrouprelationByUserId(user.getId());
		List<ServiceGroupAO> groupAOS = new ArrayList<>();
		for (UserServiceGroupRelation groupRelation : groupRelations) {
			ServiceGroup serviceGroup = serviceGroupDao.queryServiceById(groupRelation.getServiceGroupId());
			if (serviceGroup != null) {
				ServiceGroupAO serviceGroupAO = new ServiceGroupAO();
				BeanUtils.copyProperties(serviceGroup, serviceGroupAO);
				groupAOS.add(serviceGroupAO);
			}
		}
		result.setResult(groupAOS);
		return result;
	}

	/**
	 * 新版本slack消息新增
	 *
	 * @param message
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	public ExecuteResult<String> PushSlackMessageType10(Message message) {
		Map<String, Object> param = message.getParams();
		String host = propertiesUtil.getProperty("ladeit-bot-notif-mngr.host");
		String url = host + "/api/v1/message/ImageInfo";
		return pushSlack(url, param);
	}

	/**
	 * 解绑bot slack消息新增
	 *
	 * @param message
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	public ExecuteResult<String> PushSlackMessageType80(Message message) {
		Map<String, Object> param = message.getParams();
		String host = propertiesUtil.getProperty("ladeit-bot-notif-mngr.host");
		String url = host + "/api/v1/message/UnbindInfo";
		return pushSlack(url, param);
	}

	/**
	 * 公共消息
	 *
	 * @param message
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	public ExecuteResult<String> PushCommonMessage(Message message) {
		Map<String, Object> param = new HashMap<>();
		param.put("serviceGroupId", message.getServiceGroupId());
		param.put("content", message.getContent());
		Date date = message.getCreateAt();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		param.put("createAt", sdf.format(date));
		String host = propertiesUtil.getProperty("ladeit-bot-notif-mngr.host");
		String url = host + "/api/v1/message/LadeitMessageInfo";
		return pushSlack(url, param);
	}

	public ExecuteResult<String> pushSlack(String url, Map<String, Object> param) {
		ExecuteResult<String> result = new ExecuteResult<>();
		try {
			HttpHelper.doSendPost(url, JSON.toJSONString(param), null);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return result;
		}
	}

}
