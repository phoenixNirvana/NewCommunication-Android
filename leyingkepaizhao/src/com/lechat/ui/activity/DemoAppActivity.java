/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lechat.ui.activity;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.lechat.R;
import com.lechat.client.MyChatManager;
import com.lechat.client.ServiceManager;
import com.lechat.client.XmppManager.IChatMessageListener;

/**
 * This is an androidpn client demo application.
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class DemoAppActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("DemoAppActivity", "onCreate()...");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Settings
        Button okButton = (Button) findViewById(R.id.btn_settings);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ServiceManager.viewNotificationSettings(DemoAppActivity.this);
            }
        });
        
        Button sendBtn = (Button) findViewById(R.id.btn_send);
        
        final EditText etInput = (EditText) findViewById(R.id.et_inputTxt);
        
        final MyChatManager chatManager = new MyChatManager(this);
        
        final String target = "zoushuai1@127.0.0.1/Spark 2.6.3";
        
        chatManager.addChatMessageListener(target, new IChatMessageListener() {
			
			@Override
			public void processMessage(Message msg) { 
				Log.i("DemoAppActivity", "msg body : "+msg.getBody() + " from : "+msg.getFrom());
			}
		});
        
        sendBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				try {
					Chat chat = chatManager.createChat(target);
					
					chat.sendMessage(etInput.getText().toString());
					
					chatManager.output(target);
					
					chatManager.getFile();
					
				} catch (XMPPException e) {
					e.printStackTrace();
				}
				
				
			}
		});

        // Start the service
        ServiceManager serviceManager = new ServiceManager(this);
        serviceManager.setNotificationIcon(R.drawable.back_nor);
        serviceManager.startService();
        
    }

}