package com.blockchain.state;

import com.blockchain.model.Block;
import com.blockchain.model.Transaction;
import com.blockchain.model.Wallet;

import java.util.ArrayList;
import java.util.List;


public class BlockchainState {
    public static List<Block> blocks = new ArrayList<>();
    public static List<Wallet> wallets = new ArrayList<>();
    public static List<Transaction> transactions = new ArrayList<>();
}
