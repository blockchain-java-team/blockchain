package com.blockchain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
@author khabir
**/
class EnsaChainTest {

    @Test
    void hash() {
        assertEquals(123, new EnsaChain().hash());
    }
}