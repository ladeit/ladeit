package com.ladeit.biz.controller;

import com.ladeit.biz.services.MessageService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.pojo.ao.MessageAO;
import com.ladeit.pojo.ao.MessageStateAO;
import com.ladeit.pojo.ao.ServiceAO;
import com.ladeit.pojo.ao.ServiceGroupAO;
import io.ebean.SqlRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname MessageController
 * @Date 2020/3/14 8:01
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/${api.version}/message")
public class MessageController {

	@Autowired
	private MessageService messageService;

	/**
	 * 查询个人右上消息列表
	 *
	 * @param readFlag
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.ebean.SqlRow>>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	@GetMapping("/messages")
	public ExecuteResult<Pager<SqlRow>> getUserMessageInfos(@RequestParam("currentPage") int currentPage,
															@RequestParam("pageSize") int pageSize,
															@RequestParam(value = "readFlag", required = false) String readFlag, 
															@RequestParam(value = "serviceGroupId", required = false) String serviceGroupId, 
															@RequestParam(value = "type", required = false) String type) {
		return messageService.getUserMessageInfos(currentPage, pageSize, readFlag, serviceGroupId, type);
	}

	/**
	 * 查询某条消息
	 *
	 * @param messageId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.ebean.SqlRow>>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	@GetMapping("/{messageId}")
	public ExecuteResult<MessageAO> getUserMessageInfo(@PathVariable("messageId") String messageId) {
		return messageService.getUserMessageInfo(messageId);
	}

	/**
	 * 删除个人消息（物理删除message_state记录）
	 *
	 * @param messageStateAOS
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	@DeleteMapping("/state")
	public ExecuteResult<String> deleteMessageState(@RequestBody List<MessageStateAO> messageStateAOS) {
		return messageService.deleteMessageState(messageStateAOS);
	}

	/**
	 * 更新个人消息（标记已读）
	 *
	 * @param messageStateAOS
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	@PutMapping("/state")
	public ExecuteResult<String> updateMessageState(@RequestBody List<MessageStateAO> messageStateAOS) {
		return messageService.updateMessageState(messageStateAOS);
	}

	/**
	 * 删除全部个人消息（物理删除message_state记录）
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	@DeleteMapping("/states")
	public ExecuteResult<String> deleteAllMessageState() {
		return messageService.deleteAllMessageState();
	}

	/**
	 * 更新个人消息（全部标记已读）
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/14
	 * @ahthor MddandPyy
	 */
	@PutMapping("/states")
	public ExecuteResult<String> updateAllMessageState() {
		return messageService.updateAllMessageState();
	}

	/**
	 * 查询与当前登录人有关的service列表
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ServiceAO>>
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	@GetMapping("/user/service")
	public ExecuteResult<List<ServiceAO>> getMessageServiceList() {
		return messageService.getMessageServiceList();
	}

	/**
	 * 查询与当前登录人有关的group列表
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ServiceAO>>
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	@GetMapping("/user/group")
	public ExecuteResult<List<ServiceGroupAO>> getMessageServiceGroupList() {
		return messageService.getMessageServiceGroupList();
	}

}
