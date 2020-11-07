package com.huilan.socket.service;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SocketIoTest {

    // 用来存已连接的客户端
    private static Map<String, SocketIOClient> clientMap = new ConcurrentHashMap<>();

    @Autowired
    private SocketIOServer socketIOServer;

    /**
     * Spring IoC容器创建之后，在加载SocketIOServiceImpl Bean之后启动
     * @throws Exception
     */
    @PostConstruct
    private void autoStartup() throws Exception {
        start();
    }

    /**
     * Spring IoC容器在销毁SocketIOServiceImpl Bean之前关闭,避免重启项目服务端口占用问题
     * @throws Exception
     */
    @PreDestroy
    private void autoStop() throws Exception  {
        stop();
    }

    public void start() {
        // 监听客户端连接
        socketIOServer.addConnectListener(client -> {
            String uid = getParamsByClient(client);
            if (uid != null) {
                clientMap.put(uid, client);
                log.info("有新客户端连接UID:{}",uid);
            }
            // 给客户端发送一条信息 发送ClientReceive事件 需要客户端绑定此事件即可接收到消息
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name","goat");
            jsonObject.put("message","hello client");
            client.sendEvent("ClientReceive",jsonObject);
        });

        // 监听客户端断开连接
        socketIOServer.addDisconnectListener(client -> {
            String uid = getParamsByClient(client);
            if (uid != null) {
                clientMap.remove(uid);
                client.disconnect();
            }
            log.info("一条客户端连接中断");
        });

        // 处理自定义的事件，与连接监听类似
        // 此示例中测试的json收发 所以接收参数为JSONObject 如果是字符类型可以用String.class或者Object.class
        socketIOServer.addEventListener("ServerReceive",JSONObject.class, (client, data, ackSender) -> {
            // TODO do something
            JSONObject jsonObject = data;
            String uid = getParamsByClient(client);
            if (uid != null) {
                log.info("接收到SID:{}发来的消息:{}",uid,jsonObject.toJSONString());
            }
        });
        socketIOServer.start();
        log.info("socket.io初始化服务完成");
    }

    public void stop() {
        if (socketIOServer != null) {
            socketIOServer.stop();
            socketIOServer = null;
        }
        log.info("socket.io服务已关闭");
    }

    /**
     * 此方法为获取client连接中的参数，可根据需求更改
     * @param client
     * @return
     */
    private String getParamsByClient(SocketIOClient client) {
        // 从请求的连接中拿出参数（这里的sid必须是唯一标识）
        Map<String, List<String>> params = client.getHandshakeData().getUrlParams();
        List<String> list = params.get("UID");
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}
