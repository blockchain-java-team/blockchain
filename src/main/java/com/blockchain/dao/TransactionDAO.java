package com.blockchain.dao;

import com.blockchain.model.Transaction;
import java.util.List;

public interface TransactionDAO {
    void save(Transaction transaction);
    List<Transaction> findAll();
    Transaction findById(String id);
    void delete(String id);
}
