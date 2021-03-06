package com.jukusoft.mmo.network.impl;

import com.jukusoft.mmo.network.Callback;
import com.jukusoft.mmo.network.NetworkManager;
import com.jukusoft.mmo.network.NetworkResult;
import com.jukusoft.mmo.Protocol;
import com.jukusoft.mmo.network.backend.TCPConnection;
import com.jukusoft.mmo.network.backend.UDPConnection;
import com.jukusoft.mmo.network.backend.impl.VertxTCPConnection;
import com.jukusoft.mmo.network.backend.impl.VertxUDPConnection;
import com.jukusoft.mmo.network.config.NetConfig;
import com.jukusoft.mmo.network.message.MessageReceiver;
import com.jukusoft.mmo.network.traffic.TrafficCounter;
import com.jukusoft.mmo.utils.ReportUtils;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultNetworkManager implements NetworkManager<Buffer> {

    //vertx options
    protected VertxOptions options = null;

    //vertx instance
    protected Vertx vertx = null;

    //network config
    protected NetConfig config = null;

    //traffic counter
    protected TrafficCounter counter = null;

    //singleton instance
    protected static DefaultNetworkManager instance = null;

    //tcp connection
    protected TCPConnection<Buffer> tcpConnection = null;

    //udp connection
    protected UDPConnection<Buffer> udpConnection = null;

    //message receiver
    protected MessageReceiver<Buffer> messageReceiver = null;

    public DefaultNetworkManager () {
        this.options = new VertxOptions();

        //create new vertx.io instance
        this.vertx = Vertx.vertx(this.options);

        //create new traffic counter
        this.counter = new TrafficCounter();

        //create connections and set message listeners
        this.tcpConnection = new VertxTCPConnection(this.vertx);
        this.tcpConnection.setMessageReceiver((Buffer msg) -> {
            //call message listener
            this.receive(msg);

            //count traffic
            this.counter.addReceiveBytes(msg.length(), Protocol.TCP);
        });

        this.udpConnection = new VertxUDPConnection(this.vertx);
        this.udpConnection.setMessageReceiver((Buffer msg) -> {
            //call message listener
            this.receive(msg);

            //count traffic
            this.counter.addReceiveBytes(msg.length(), Protocol.UDP);
        });

        //set message listener
    }

    protected void receive (Buffer msg) {
        if (this.config.getReceiveDelay() > 0) {
            this.executeDelayed(this.config.getReceiveDelay(), () -> {
                //call message listener
                this.messageReceiver.onReceive(msg);
            });
        } else {
            //dont use delay

            //call message listener
            this.messageReceiver.onReceive(msg);
        }
    }

    public void load (String configFile) throws IOException {
        this.config = new NetConfig(configFile);
    }

    @Override
    public void connectTCP(String host, int port, Callback<NetworkResult<Boolean>> callback) {
        //enable SSL encryption
        this.tcpConnection.enableSSL();

        //connect to tcp server
        this.tcpConnection.connect(host, port, callback);
    }

    @Override
    public void send(Buffer msg, Protocol protocol) {
        //check for delay
        if (this.config.getSendDelay() > 0) {
            //send message with delay
            this.executeDelayed(this.config.getSendDelay(), () -> {
                this.executeSending(msg, protocol);
            });
        } else {
            //dont use delay
            this.executeSending(msg, protocol);
        }
    }

    @Override
    public boolean canSend() {
        //because we are using vertx, vertx is handling this for us
        return true;
    }

    protected void executeSending (Buffer msg, Protocol protocol) {
        //send message to specific network backend
        if (protocol == Protocol.TCP) {
            this.tcpConnection.send(msg);
        } else {
            this.udpConnection.send(msg);
        }

        //count traffic
        this.counter.addSendBytes(msg.length(), protocol);
    }

    @Override
    public void setMessageReceiver(MessageReceiver<Buffer> receiver) {
        if (receiver == null) {
            throw new NullPointerException("message receiver cannot be null.");
        }

        this.messageReceiver = receiver;
    }

    @Override
    public void shutdown() {
        this.vertx.close();
    }

    @Override
    public boolean isConnected() {
        return this.tcpConnection.isConnected() || this.udpConnection.isConnected();
    }

    @Override
    public void executeBlocking(Runnable runnable) {
        this.vertx.executeBlocking(future -> {
            //execute blocking code
            runnable.run();

            //task was executed
            future.complete();
        }, res -> {
            //
        });
    }

    @Override
    public long startPeriodicTimer(long delay, Runnable runnable) {
        return this.vertx.setPeriodic(delay, event -> {
            runnable.run();
        });
    }

    @Override
    public void stopPeriodicTimer(long timerID) {
        this.vertx.cancelTimer(timerID);
    }

    @Override
    public long executeDelayed(long delay, Runnable runnable) {
        return this.vertx.setTimer(delay, id -> {
            runnable.run();
        });
    }

    public static DefaultNetworkManager getManagerInstance () {
        if (instance == null) {
            instance = new DefaultNetworkManager();

            //load configuration
            try {
                if (isJUnitTest()) {
                    //its an junit test
                    instance.load("../data/config/junit-network.cfg");
                } else {
                    instance.load("./data/config/junit-network.cfg");
                }
            } catch (IOException e) {
                ReportUtils.sendExceptionToServer(e);

                Logger.getAnonymousLogger().log(Level.SEVERE, "Exception occurred while loading network configuration file: ", e);
            }
        }

        return instance;
    }

    protected static boolean isJUnitTest () {
        return !new File("./data/config/network.cfg").exists();
    }

}
