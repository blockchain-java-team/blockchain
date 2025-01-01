package com.blockchain.dao;

import com.blockchain.model.Wallet;

import java.util.List;

public interface WalletDAO {
    void save(Wallet wallet) throws Exception;

    List<Wallet> findAll() throws Exception;

    Wallet findById(String publicKey) throws Exception;

    void deleteById(String publicKey) throws Exception;
}
