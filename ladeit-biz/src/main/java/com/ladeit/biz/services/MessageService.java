package com.ladeit.biz.services;

import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.pojo.ao.MessageAO;
import com.ladeit.pojo.ao.MessageStateAO;
import com.ladeit.pojo.ao.ServiceAO;
import com.ladeit.pojo.ao.ServiceGroupAO;
import com.ladeit.pojo.doo.Message;
import io.ebean.SqlRow;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname MessageService
 * @Date 2020/3/14 8:03
 */
public interface MessageService {

	/**
	 * 消息新增公共方法
	 *
	 * @param message
	 * @param isUser
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> insertMessage(Message message,boolean isUser);

	/**
	 * slack消息新增公共方法
	 *
	 * @param message
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> insertSlackMessage(Message message);

	/**
	 * 查询个人右上消息列表
	 *
	 * @param readFlag
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.ebean.SqlRow>>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	ExecuteResult<Pager<SqlRow>> getUserMessageInfos(int currentPage, int pageSize, String readFlag, String serviceId,
  String type);

	/**
	 * 查询某条消息
	 *
	 * @param messageId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.ebean.SqlRow>>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	ExecuteResult<MessageAO> getUserMessageInfo(String messageId);

	/**
	 * 删除个人消息（物理删除message_state记录）
	 *
	 * @param messageStateAOS
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> deleteMessageState(List<MessageStateAO> messageStateAOS);

	/**
	 * 更新个人消息（标记已读）
	 *
	 * @param messageStateAOS
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> updateMessageState(List<MessageStateAO> messageStateAOS);

	/**
	 * 删除全部个人消息（物理删除message_state记录）
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> deleteAllMessageState();

	/**
	 * 更新个人消息（全部标记已读）
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> updateAllMessageState();

	/**
	 * 查询与当前登录人有关的service列表
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ServiceAO>>
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	ExecuteResult<List<ServiceAO>> getMessageServiceList();

	/**
	 * 查询与当前登录人有关的group列表
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ServiceAO>>
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	ExecuteResult<List<ServiceGroupAO>> getMessageServiceGroupList();
}
