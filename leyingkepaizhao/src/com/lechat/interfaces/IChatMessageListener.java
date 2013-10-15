package com.lechat.interfaces;

import org.jivesoftware.smack.packet.Message;

public interface IChatMessageListener {
	void processMessage(Message msg);
}
