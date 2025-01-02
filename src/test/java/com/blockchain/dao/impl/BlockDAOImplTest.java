package com.blockchain.dao.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blockchain.dao.BlockDAO;
import com.blockchain.model.Block;

public class BlockDAOImplTest {
	private BlockDAO blockDAO;

	@BeforeEach
	void setUp() {
		blockDAO = new BlockDAOImpl();
	}

	@Test
	void testSave() {
		Block block = new Block();
		Block importedBlock = null;
		try {
			blockDAO.save(block);
			importedBlock = blockDAO.findById(block.getLedgerId());
			assertEquals(importedBlock.getLedgerId(), block.getLedgerId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
