package com.blockchain.dao;

import com.blockchain.model.Block;
import java.util.List;

public interface BlockDAO {
    void save(Block block);
    List<Block> findAll();
    Block findById(String id);
    void delete(String id);
}
