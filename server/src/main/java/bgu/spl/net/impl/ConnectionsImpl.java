package bgu.spl.net.impl;

import bgu.spl.net.api.Connections;
import bgu.spl.net.api.ConnectionHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ConnectionsImpl<T> implements Connections<T> {
    private final Map<Integer, ConnectionHandler<T>> clients;
    private final Map<String, ConcurrentHashMap<Integer, Boolean>> channelSubscribers;

    public ConnectionsImpl() {
        clients = new ConcurrentHashMap<>();
        channelSubscribers = new ConcurrentHashMap<>();
    }

    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> handler = clients.get(connectionId);
        if (handler != null) {
            handler.send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void send(String channel, T msg) {
        if (channelSubscribers.containsKey(channel)) {
            for (Integer connectionId : channelSubscribers.get(channel).keySet()) {
                send(connectionId, msg);
            }
        }
    }

    @Override
    public void disconnect(int connectionId) {
        clients.remove(connectionId);
    }

    public void addClient(int connectionId, ConnectionHandler<T> handler) {
        clients.put(connectionId, handler);
    }

    public void subscribe(int connectionId, String channel) {
        channelSubscribers.putIfAbsent(channel, new ConcurrentHashMap<>());
        channelSubscribers.get(channel).put(connectionId, true);
    }

    public void unsubscribe(int connectionId, String channel) {
        if (channelSubscribers.containsKey(channel)) {
            channelSubscribers.get(channel).remove(connectionId);
            if (channelSubscribers.get(channel).isEmpty()) {
                channelSubscribers.remove(channel);
            }
        }
    }
}
