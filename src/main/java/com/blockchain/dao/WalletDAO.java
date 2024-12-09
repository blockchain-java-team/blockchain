package com.blockchain.dao;

import com.blockchain.model.Wallet;
import java.util.List;

public interface WalletDAO {
    void save(Wallet wallet);
    List<Wallet> findAll();
    Wallet findById(String id);
    void delete(String id);
}
