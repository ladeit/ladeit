package com.ladeit.biz.websocket;


import com.ladeit.biz.config.SpringBean;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @program: ladeit
 * @description: TerminalOutputStream
 * @author: falcomlife
 * @create: 2019/10/16
 * @version: 1.0.0
 */
public class TerminalOutputStream extends OutputStream {

	private List<Byte> list;

	public TerminalOutputStream() {
		super();
	}

	public TerminalOutputStream(String id) {
		Map<String, List<Byte>> pool = (Map<String, List<Byte>>) SpringBean.getBean("terminalPool");
		list = pool.get(id);
	}

	@Override
	public void write(int b) throws IOException {
		byte intbyte = ((Integer) b).byteValue();
		list.add(intbyte);
	}
}
