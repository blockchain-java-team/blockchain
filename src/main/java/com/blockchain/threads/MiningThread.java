package com.blockchain.threads;

import com.blockchain.service.BlockchainData;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class MiningThread extends Thread {
    /**
     * The object of our mining thread is for it to handle all the mining operations
     * of the application. It will need to run continuously as long as our
     * application is running and make sure new blocks are created at the
     * appropriate interval. Also, since we will be using mining points as a method
     * of achieving block consensus, this thread will also need to keep track of the
     * points. Our mining thread will first check if we have a blockchain that is
     * current and up-to-date, and then it will initiate the mining of a new block
     * at a precise interval. This thread will loop every two seconds continuously.
     */
    @Override
    public void run() {
        while (true) {
            long lastMinedBlock = LocalDateTime
                    .parse(BlockchainData.getInstance().getCurrentBlockChain().getLast().getTimeStamp())
                    .toEpochSecond(ZoneOffset.UTC);
            if ((lastMinedBlock + BlockchainData.getTIMEOUT_INTERVAL()) < LocalDateTime.now()
                    .toEpochSecond(ZoneOffset.UTC)) {
                System.out.println("BlockChain is too old for mining! Update it from peers");
            } else if (((lastMinedBlock + BlockchainData.getMINING_INTERVAL())
                    - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) > 0) {
                System.out.println("BlockChain is current, mining will start in "
                        + ((lastMinedBlock + BlockchainData.getMINING_INTERVAL())
                                - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                        + " seconds");
            } else {
                System.out.println("MINING NEW BLOCK"); // 60< <65
                try {
                    BlockchainData.getInstance().mineBlock();
                } catch (Exception e) {
                    System.out.println("Can't min the current block");
                }
            }
            System.out.println("Your balance is " + BlockchainData.getInstance().getWalletBalance());

            try {
                Thread.sleep(2000);
                BlockchainData.getInstance().setMiningPoints(BlockchainData.getInstance().getMiningPoints() + 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
