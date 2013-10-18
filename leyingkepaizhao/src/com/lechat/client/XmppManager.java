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
package com.lechat.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.androidpn.bean.UserInfo;
import com.lechat.interfaces.IChatDao;
import com.lechat.interfaces.IChatMessageListener;
import com.lechat.interfaces.IConnectListener;
import com.lechat.interfaces.ILoginListener;
import com.lechat.interfaces.IRegisterListener;

/**
 * This class is to manage the XMPP connection between client and server.
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class XmppManager implements IChatDao{

    private static final String LOGTAG = LogUtil.makeLogTag(XmppManager.class);

//    private static final String XMPP_RESOURCE_NAME = "Androidpn";
    private static final String XMPP_RESOURCE_NAME = "Spark 2.6.3";

    private Context context;

    private TaskSubmitter taskSubmitter;

    private TaskTracker taskTracker;

    private SharedPreferences sharedPrefs;

    private String xmppHost;

    private int xmppPort;

    private XMPPConnection connection;

    private String username;

    private String password;

    private ConnectionListener connectionListener;

    private PacketListener notificationPacketListener;

    private Handler handler;

    private List<Runnable> taskList;

    private boolean running = false;

    private Future<?> futureTask;

    private Thread reconnection;
    
    private ExecutorService executorService;
    
    private Map<String, IChatMessageListener> mChatlisteners;
    
    private IConnectListener mConnectListener;
    
    private ILoginListener mLoginListener;
    
    private IRegisterListener mRegisterListener;
    
    public static final int CONNECT_SUCCESS = 0x001;
    public static final int CONNECT_FAIL = 0x002;
    
    public static final int LOGIN_SUCCESS = 0x003;
    public static final int LOGIN_FAIL = 0x004;
    
    public static final int REGISTER_SUCCESS = 0x005;
    public static final int REGISTER_FAIL = 0x006;
    
    public void setRegisterListener(IRegisterListener mRegisterListener) {
		this.mRegisterListener = mRegisterListener;
	}

	public void setConnectListener(IConnectListener mConnectListener) {
		this.mConnectListener = mConnectListener;
	}

	public void setLoginListener(ILoginListener mLoginListener) {
		this.mLoginListener = mLoginListener;
	}

    static{   
        try{  
           Class.forName("org.jivesoftware.smack.ReconnectionManager");  
        }catch(Exception e){  
            e.printStackTrace();  
        }  
    }  

    public XmppManager(Context context) {
        this.context = context;
        
        taskSubmitter = new TaskSubmitter();
        taskTracker = new TaskTracker();
        executorService = Executors.newSingleThreadExecutor();
        sharedPrefs = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        
        xmppHost = sharedPrefs.getString(Constants.XMPP_HOST, "localhost");
        xmppPort = sharedPrefs.getInt(Constants.XMPP_PORT, 5222);
        username = sharedPrefs.getString(Constants.XMPP_USERNAME, "");
        password = sharedPrefs.getString(Constants.XMPP_PASSWORD, "");

        connectionListener = new PersistentConnectionListener(this);
        notificationPacketListener = new NotificationPacketListener(this);

        handler = new Handler();
        taskList = new ArrayList<Runnable>();
        reconnection = new ReconnectionThread(this);
    }
    
    public void addChatMessageListener(String jid, IChatMessageListener listener){
    	
    	if(mChatlisteners == null){
    		mChatlisteners = new HashMap<String, IChatMessageListener>();
    	}
    	
    	mChatlisteners.put(jid, listener);
    }
    
    public void removeChatMessageListener(String jid){
    	if(mChatlisteners != null){
    		if(mChatlisteners.containsValue(jid)){
    			mChatlisteners.remove(jid);
    		}
    	}
    }
    
    public void removeAllChatMessageListener(){
    	if(mChatlisteners != null){
    		mChatlisteners.clear();
    		mChatlisteners = null;
    	}
    }

    public Context getContext() {
        return context;
    }

    /**
     * 已注册用户直接登录
     * 
     * @param account
     * @param password
     */
    public void connect(String account, String password) {
        Log.d(LOGTAG, "connect()...");
        submitConnectTask();
        submitLoginTask(account, password);
    }

    public void terminatePersistentConnection() {
        Log.d(LOGTAG, "terminatePersistentConnection()...");
        Runnable runnable = new Runnable() {

            final XmppManager xmppManager = XmppManager.this;

            public void run() {
                if (xmppManager.isConnected()) {
                    Log.d(LOGTAG, "terminatePersistentConnection()... run()");
                    xmppManager.getConnection().removePacketListener(
                            xmppManager.getNotificationPacketListener());
                    xmppManager.getConnection().disconnect();
                }
            }

        };
        addTask(runnable);
    }

    public XMPPConnection getConnection() {
        return connection;
    }

    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    public PacketListener getNotificationPacketListener() {
        return notificationPacketListener;
    }

//    public void startReconnectionThread() {
//        synchronized (reconnection) {
//            if (!reconnection.isAlive()) {
//                reconnection.setName("Xmpp Reconnection Thread");
//                reconnection.start();
//            }
//        }
//    }

    public Handler getHandler() {
        return handler;
    }

    public void reregisterAccount(String account, String password) {
        removeAccount();
        submitLoginTask(account, password);
        runTask();
    }

    public List<Runnable> getTaskList() {
        return taskList;
    }

    public Future<?> getFutureTask() {
        return futureTask;
    }

    public void runTask() {
        Log.d(LOGTAG, "runTask()...");
        synchronized (taskList) {
            running = false;
            futureTask = null;
            if (!taskList.isEmpty()) {
                Runnable runnable = (Runnable) taskList.get(0);
                taskList.remove(0);
                running = true;
                futureTask = taskSubmitter.submit(runnable);
                if (futureTask == null) {
                    taskTracker.decrease();
                }
            }
        }
        taskTracker.decrease();
        Log.d(LOGTAG, "runTask()...done");
    }

    private String newRandomUUID() {
        String uuidRaw = UUID.randomUUID().toString();
        return uuidRaw.replaceAll("-", "");
    }

    private boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    private boolean isAuthenticated() {
        return connection != null && connection.isConnected()
                && connection.isAuthenticated();
    }

    private boolean isRegistered(String account) {
    	boolean flag = false;
    	if(sharedPrefs.contains(Constants.XMPP_USERNAME)
                && sharedPrefs.contains(Constants.XMPP_PASSWORD)){
    		String str = sharedPrefs.getString(Constants.XMPP_USERNAME, null);
    		if(str.equals(account)){
    			flag = true;
    		}
    	}
        return flag;
    }

    private void submitConnectTask() {
        Log.d(LOGTAG, "submitConnectTask()...");
        addTask(new ConnectTask());
        runTask();
    }

    private void submitRegisterTask(String account, String password) {
        Log.d(LOGTAG, "submitRegisterTask()...");
        addTask(new RegisterTask(account, password));
    }

    private void submitLoginTask(String account, String password) {
        Log.d(LOGTAG, "submitLoginTask()...");
        addTask(new LoginTask(account, password));
    }

    private void addTask(Runnable runnable) {
        Log.d(LOGTAG, "addTask(runnable)...");
        taskTracker.increase();
        synchronized (taskList) {
            if (taskList.isEmpty() && !running) {
                running = true;
                futureTask = taskSubmitter.submit(runnable);
                if (futureTask == null) {
                    taskTracker.decrease();
                }
            } else {
                taskList.add(runnable);
            }
        }
        Log.d(LOGTAG, "addTask(runnable)... done");
    }

    private void removeAccount() {
        Editor editor = sharedPrefs.edit();
        editor.remove(Constants.XMPP_USERNAME);
        editor.remove(Constants.XMPP_PASSWORD);
        editor.commit();
    }

    /**
     * A runnable task to connect the server. 
     */
    private class ConnectTask implements Runnable {

        final XmppManager xmppManager;

        private ConnectTask() {
            this.xmppManager = XmppManager.this;
        }

		public void run() {
            Log.i(LOGTAG, "ConnectTask.run()...");
            
            if (!xmppManager.isConnected()) {
                // Create the configuration for this new connection
                ConnectionConfiguration connConfig = new ConnectionConfiguration(
                        "10.58.108.201", 5222);
                
                // connConfig.setSecurityMode(SecurityMode.disabled);
                connConfig.setSecurityMode(SecurityMode.required);
                connConfig.setSASLAuthenticationEnabled(false);
                connConfig.setCompressionEnabled(false);
                connConfig.setDebuggerEnabled(true);
                connConfig.setReconnectionAllowed(true);
                connConfig.setSendPresence(true);
                
                XMPPConnection connection = new XMPPConnection(connConfig);
                xmppManager.setConnection(connection);
                
                try {
                    // Connect to the server
                    connection.connect();
                    Log.i(LOGTAG, "XMPP connected successfully");

//                    // packet provider
                    ProviderManager.getInstance().addIQProvider("notification",
                            "androidpn:iq:notification",
                            new NotificationIQProvider());

                    if(mConnectListener != null){
                    	mConnectListener.connectSuccess();
                    }
                    
                } catch (XMPPException e) {
                    Log.e(LOGTAG, "XMPP connection failed", e);
                    
                    if(mConnectListener != null){
                    	mConnectListener.connectFail();
                    }
                }

                xmppManager.runTask();

            } else {
                Log.i(LOGTAG, "XMPP connected already");
                xmppManager.runTask();
            }
        }
    }
    
    // "zoushuai@127.0.0.1/Spark 2.6.3"
    
    public Chat createChat(String jid) throws XMPPException{
    	
    	return this.getConnection().getChatManager().createChat(jid, null);
    	
    }
    
    private void initChatManager(){
    	
    	ChatManager chatManager = this.getConnection().getChatManager();
    	
    	chatManager.addChatListener(chatManagerListener);
    	
    }
    
    public List<Message> getOfflineMsg() throws XMPPException{
    	
    	List<Message> msgList = null;
    	
    	OfflineMessageManager omm = new OfflineMessageManager(this.getConnection());
    	Iterator<Message> messages = omm.getMessages();
    	while(messages.hasNext()){
    		
    		if(msgList == null){
    			msgList = new ArrayList<Message>();
    		}
    		
    		msgList.add(messages.next());
    	}
    	
    	return msgList;
    }
    
    ChatManagerListener chatManagerListener = new ChatManagerListener() {
		
		@Override
		public void chatCreated(Chat chat, boolean createdLocally) {
			
			Log.i(LOGTAG, "chatCreated");
			
			chat.addMessageListener(new MessageListener() {
				
				@Override
				public void processMessage(Chat chat, Message message) {
					
					String from = message.getFrom();
					
					for (Map.Entry<String, IChatMessageListener> entry : mChatlisteners.entrySet()) {
						
						if(from.equals(entry.getKey())){    //��ǰ�û���������Ự   �ظ���Ϣ
							
							Log.i(LOGTAG, "��ǰ�û�����ĻỰ �ظ���Ϣ");
							
							entry.getValue().processMessage(message);
						}else{                              //������ �ظ���Ϣ 
							
							Log.i(LOGTAG, "�����û�  �ظ���Ϣ");
							
						}
						
						
					}
					
				}
			});
		}
	};
    
    /**
     * A runnable task to register a new user onto the server. 
     */
    private class RegisterTask implements Runnable {

        final XmppManager xmppManager;

        private String account, password;
        
        private RegisterTask(String account, String password) {
            xmppManager = XmppManager.this;
            this.account = account;
            this.password = password;
        }

        public void run() {
            Log.i(LOGTAG, "RegisterTask.run()...");
            if (!xmppManager.isRegistered(account)) {
                Registration registration = new Registration();
                PacketFilter packetFilter = new AndFilter(new PacketIDFilter(registration.getPacketID()), new PacketTypeFilter(IQ.class));
                PacketListener packetListener = new PacketListener() {
                    public void processPacket(Packet packet) {
                        Log.d("RegisterTask.PacketListener", "processPacket().....");
                        Log.d("RegisterTask.PacketListener", "packet=" + packet.toXML());
                        if (packet instanceof IQ) {
                            IQ response = (IQ) packet;
                            if (response.getType() == IQ.Type.ERROR) {
                                if (!response.getError().toString().contains("409")) {
                                    Log.e(LOGTAG, "Unknown error while registering XMPP account! " + response.getError().getCondition());
                                }
                                
                                if(mRegisterListener != null){
                                	mRegisterListener.registerFail();
                                }
                            } else if (response.getType() == IQ.Type.RESULT) {
                                xmppManager.setUsername(account);
                                xmppManager.setPassword(password);
                                Log.d(LOGTAG, "username=" + account);
                                Log.d(LOGTAG, "password=" + password);
                                Editor editor = sharedPrefs.edit();
                                editor.putString(Constants.XMPP_USERNAME, account);
                                editor.putString(Constants.XMPP_PASSWORD, password);
                                editor.commit();
                                Log.i(LOGTAG, "Account registered successfully");
                                xmppManager.runTask();
                                
                                if(mRegisterListener != null){
                                	mRegisterListener.registerSuccess();
                                }
                            }
                        }
                    }
                };
                connection.addPacketListener(packetListener, packetFilter);
                registration.setType(IQ.Type.SET);
                registration.addAttribute("username", account);
                registration.addAttribute("password", password);
                connection.sendPacket(registration);
            } else {
                Log.i(LOGTAG, "Account registered already");
                xmppManager.runTask();
            }
        }
    }

    /**
     * A runnable task to log into the server. 
     */
    private class LoginTask implements Runnable {

        final XmppManager xmppManager;
        
        private String account, password;

        private LoginTask(String account, String password) {
            this.xmppManager = XmppManager.this;
            this.account = account;
            this.password = password;
        }

        public void run() {
            Log.i(LOGTAG, "LoginTask.run()...");

            if (!xmppManager.isAuthenticated()) {
                Log.d(LOGTAG, "username=" + account);
                Log.d(LOGTAG, "password=" + password);

                try {
                    xmppManager.getConnection().login(
                    		account,
                    		password, XMPP_RESOURCE_NAME);
                    Log.d(LOGTAG, "Loggedn in successfully");
                    
                    if(mLoginListener != null){
                    	mLoginListener.loginSuccess();
                    }
                    
                    initChatManager();
                    
                    manager = new FileTransferManager(getConnection());
                    
                    // connection listener
                    if (xmppManager.getConnectionListener() != null) {
                        xmppManager.getConnection().addConnectionListener(
                                xmppManager.getConnectionListener());
                    }

                    // packet filter
                    PacketFilter packetFilter = new PacketTypeFilter(
                            NotificationIQ.class);
                    // packet listener
                    PacketListener packetListener = xmppManager
                            .getNotificationPacketListener();
                    connection.addPacketListener(packetListener, packetFilter);

                    xmppManager.runTask();

                } catch (XMPPException e) {
                	
                	if(mLoginListener != null){
                    	mLoginListener.logintFail();
                    }
                	
                    Log.e(LOGTAG, "LoginTask.run()... xmpp error");
                    Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
                            + e.getMessage());
                    String INVALID_CREDENTIALS_ERROR_CODE = "401";
                    String errorMessage = e.getMessage();
                    if (errorMessage != null
                            && errorMessage
                                    .contains(INVALID_CREDENTIALS_ERROR_CODE)) {
//                        xmppManager.reregisterAccount(account, password);
                        return;
                    }
//                    xmppManager.startReconnectionThread();

                } catch (Exception e) {
                    Log.e(LOGTAG, "LoginTask.run()... other error");
                    Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
                            + e.getMessage());
//                    xmppManager.startReconnectionThread();
                }

            } else {
                Log.i(LOGTAG, "Logged in already");
                xmppManager.runTask();
            }

        }
    }
    
    private FileTransferManager manager;
    
    
    public void getFile(){
    	
    	manager.addFileTransferListener(new FileTransferListener() {
			
			@Override
			public void fileTransferRequest(FileTransferRequest request) {
				
				IncomingFileTransfer fileTransfer = request.accept();
				
				try {
					fileTransfer.recieveFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "�����ļ�"));
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
    }
    
    public void output(String jid) throws XMPPException{
    	
    	FileTransferManager manager = new FileTransferManager(connection);
    	
    	// Create the outgoing file transfer
        OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(jid); //����û�ҪΪ �����jid��user@servername/resource
        // Send the file
        
        InputStream inputStream;
		try {
			inputStream = context.getResources().getAssets().open("xmpp.txt");
			transfer.sendStream(inputStream, "xmpp.txt", inputStream.available(), "aa");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
    
    public List<UserInfo> getUsers(){
    	
    	Roster roster = getConnection().getRoster();
    	Collection<RosterGroup> groups = roster.getGroups();
    	Collection<RosterEntry> entries = roster.getEntries();
    	
    	List<UserInfo> users = null;
    	
    	if(entries != null && !entries.isEmpty()){
    		users = new ArrayList<UserInfo>();
    		for (RosterEntry rosterEntry : entries) {
    			
    			Presence presence = roster.getPresence(rosterEntry.getUser()); 
    			
    			UserInfo user = new UserInfo();
    			user.setName(rosterEntry.getName());
    			user.setUser(rosterEntry.getUser());
    			user.setStatus(presence.getStatus());
    			user.setFrom(presence.getFrom());
    			
    			users.add(user);
    		}
    	}
    	
    	
    	return users;
    	
    }
    
    /**
     * Class for summiting a new runnable task.
     */
    public class TaskSubmitter {

        @SuppressWarnings("unchecked")
        public Future submit(Runnable task) {
            Future result = null;
            if (!executorService.isTerminated()
                    && !executorService.isShutdown()
                    && task != null) {
                result = executorService.submit(task);
            }
            return result;
        }

    }

    /**
     * Class for monitoring the running task count.
     */
    public class TaskTracker {

        public int count = 0;

        public void increase() {
            synchronized (taskTracker) {
            	taskTracker.count++;
                Log.d(LOGTAG, "Incremented task count to " + count);
            }
        }

        public void decrease() {
            synchronized (taskTracker) {
            	taskTracker.count--;
                Log.d(LOGTAG, "Decremented task count to " + count);
            }
        }

    }

	@Override
	public void onConnect() {
		submitConnectTask();
	}

	@Override
	public void onLogin(String account, String password) {
		submitConnectTask();
		submitLoginTask(account, password);
	}

	@Override
	public void onRegister(String account, String password) {
		submitConnectTask();
		submitRegisterTask(account, password);
	}

	@Override
	public void disconnect() {
		Log.d(LOGTAG, "disconnect()...");
        terminatePersistentConnection();
        executorService.shutdown();
	}

    
}
