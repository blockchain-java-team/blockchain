package com.blockchain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
@author khabir
**/
class EnsaChainTest {

    @Test
    void hash1() {
        assertEquals(123, new EnsaChain().hash());
    }

    @Test
    void hash_test_getting_hash() {
        EnsaChain e = new EnsaChain();
        assertEquals(0, e.getNum());
    }
}