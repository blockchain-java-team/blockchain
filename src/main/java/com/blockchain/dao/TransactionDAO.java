package com.blockchain.dao;

import com.blockchain.model.Transaction;

import java.util.List;

public interface TransactionDAO {
    void save(Transaction transaction) throws Exception;

    List<Transaction> findAll() throws Exception;

    List<Transaction> findByLedgerId(int ledgerId) throws Exception;

    void deleteByLedgerId(int ledgerId) throws Exception;
}
