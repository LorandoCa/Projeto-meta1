package com.example.servingwebcontent;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import src.webInterface;

@Component
public class webInterfaceImp extends UnicastRemoteObject implements webInterface{
    private final SimpMessagingTemplate messagingTemplate;
    private Map<String, List<String>> statistics;

    @Autowired
    public webInterfaceImp(SimpMessagingTemplate messagingTemplate) throws RemoteException {
        super();
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void update(Map<String, List<String>> info) {
        statistics = info;
        System.out.println("Message received");
        messagingTemplate.convertAndSend("/topic/messages", new Message(info));
    }
    
}