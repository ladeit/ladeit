package com.ladeit.biz.controller;

import com.ladeit.biz.services.ImageService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.ao.ImageAO;
import com.ladeit.pojo.doo.Image;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: ladeit-parent
 * @description: ImageController
 * @author: falcomlife
 * @create: 2020/06/04
 * @version: 1.0.0
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/${api.version}/image")
public class ImageController {

	@Autowired
	private ImageService imageService;

	@PostMapping("")
	public ExecuteResult<String> addImageManual(@RequestBody ImageAO imageAO){
		Image image = new Image();
		BeanUtils.copyProperties(imageAO,image);
		return this.imageService.addImageManual(image);
	}

}
