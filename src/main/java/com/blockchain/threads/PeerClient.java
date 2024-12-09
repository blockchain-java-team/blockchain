package com.blockchain.threads;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class PeerClient {
    
    private Queue<Integer> ports = new ConcurrentLinkedDeque<>();

    public PeerClient(){
        this.ports.add(6001);
        this.ports.add(6002);
    }
}
