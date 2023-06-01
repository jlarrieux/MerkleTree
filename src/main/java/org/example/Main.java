package org.example;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> transactions = Arrays.asList("tx1", "tx2", "tx3");
        MerkleTree merkleTree = new MerkleTree(transactions);
        System.out.println(merkleTree.getRoot());
    }
}