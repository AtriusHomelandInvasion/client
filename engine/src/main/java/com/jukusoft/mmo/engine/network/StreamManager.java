package com.jukusoft.mmo.engine.network;

import com.jukusoft.mmo.network.Callback;
import com.jukusoft.mmo.network.NetworkManager;
import com.jukusoft.mmo.network.NetworkResult;
import com.jukusoft.mmo.network.message.MessageReceiver;
import io.vertx.core.buffer.Buffer;

/**
* network layer on top of NetworkManager, priorizes messages, check if messages can be sended and so on (with queue)
*/
public interface StreamManager<T extends Message> {

    /**
    * initialize stream manager
    */
    public void init (NetworkManager<Buffer> networkManager);

    /**
    * send message without return value / ack response
     *
     * @param msg instance of message
    */
    public void sendMessage (T msg);

    /**
    * send message with ack response
     *
     * @param msg instance of message
     * @param cls requested response class type
     * @param callback callback, which will be executed if message response was received or timeout has reached
    */
    public <V extends Message> void sendACKMessage (T msg, Class<V> cls, Callback<NetworkResult<V>> callback);

    public <V extends Message>  void addMessageReceiver (MessageReceiver<V> receiver, Class<V> messageType);

    /**
    * check, if messages are in queue and send them, if neccessary
    */
    public void update ();

}
