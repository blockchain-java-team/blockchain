package com.blockchain.dao;

import com.blockchain.model.Block;

import java.util.List;

public interface BlockDAO {
    void save(Block block) throws Exception;
    List<Block> findAll() throws Exception;
    Block findById(int id) throws Exception;
    void deleteById(int id) throws Exception;
}
