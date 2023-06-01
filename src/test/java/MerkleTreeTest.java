import org.example.MerkleTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class MerkleTreeTest {
    private List<String> transactions;
    private MerkleTree merkleTree;
    private String EXCEPTION_MESSAGE = "Transaction list must not be null or empty";

    @BeforeEach
    public void setUp(){
        transactions = new ArrayList<>();
        transactions.add("transaction1");
        transactions.add("transaction2");
        transactions.add("transaction3");
        transactions.add("transaction4");
        merkleTree = new MerkleTree(transactions);
    }

    @Test
    public void testNullTransactions(){
        Exception exception = assertThrows(IllegalArgumentException.class, () ->{new MerkleTree(null);} );
        assertTrue(exception.getMessage().contains(EXCEPTION_MESSAGE));
    }

    @Test
    public void testEmptyTransactions(){
        Exception exception = assertThrows(IllegalArgumentException.class, () ->{new MerkleTree(new ArrayList<String>());} );
        assertTrue(exception.getMessage().contains(EXCEPTION_MESSAGE));
    }

    @Test
    public void testRoot(){
        String root = merkleTree.getRoot();
        assertNotNull(root);
        assertFalse(root.isEmpty());
    }

    @Test
    public void testTransactions(){
        assertEquals(transactions, merkleTree.getTransactions());
    }

    @Test
    public void testUpdateLeaf(){
        String oldRoot = merkleTree.getRoot();
        String txNew = "txNew";
        merkleTree.updateLeaf(0,txNew);
        String newRoot = merkleTree.getRoot();

        assertNotEquals(oldRoot, newRoot);
        List<String>  updatedTransactions = merkleTree.getTransactions();
        assertEquals(txNew, updatedTransactions.get(0));
        for(int i =1; i<transactions.size(); i++){
            assertEquals(transactions.get(i), updatedTransactions.get(i));
        }
    }

    @Test
    public void testVerifyProof(){
        for(int i =0 ; i< transactions.size(); i++){
            List<String> proof = merkleTree.getProof(i);
            System.out.print(proof);
            assertTrue(merkleTree.verifyProof(transactions.get(i), proof));
        }
    }

    @Test
    public void testInvalidProof(){
        List<String> proof = merkleTree.getProof(0);
        assertTrue(merkleTree.verifyProof(transactions.get(0), proof));
        proof.set(0, "bad");
        assertFalse(merkleTree.verifyProof(transactions.get(0), proof));
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        int numThreads = 15;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for(int i=0; i< numThreads; i++){
            executorService.submit(() ->{
                try{
                    merkleTree.updateLeaf(2, "f");
                    String newRoot = merkleTree.getRoot();
                    List<String> proof = merkleTree.getProof(2);
                    assertTrue(merkleTree.verifyProof("f", proof));

                    String updatedRoot = merkleTree.getRoot();
                    assertEquals(newRoot, updatedRoot);

                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
    }

    @Test
    public void testDuplicateTransactions(){
        List<String> duplicates = Arrays.asList("tx1", "tx1", "tx2");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {new MerkleTree(duplicates);});
        assertTrue(exception.getMessage().contains("Duplicates not allowed."));
    }

    @Test
    public void testGetProofOutOfBounds(){
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> merkleTree.getProof(-1));
        assertTrue(exception.getMessage().contains("Index out of bounds"));

        exception = Assertions.assertThrows(IllegalArgumentException.class, () -> merkleTree.getProof(transactions.size()));
        assertTrue(exception.getMessage().contains("Index out of bounds"));
    }

    @Test
    public void testUpdateLeafOutOfBounds() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> merkleTree.updateLeaf(-1, "newTx"));
        assertTrue(exception.getMessage().contains("Index out of bounds"));

        exception = Assertions.assertThrows(IllegalArgumentException.class, () -> merkleTree.updateLeaf(transactions.size(), "newTx"));
        assertTrue(exception.getMessage().contains("Index out of bounds"));
    }

    @Test
    public void testRootConsistency() {
        for (int i = 0; i < transactions.size(); i++) {
            String newTx = "newTx" + i;
            merkleTree.updateLeaf(i, newTx);

            String root = merkleTree.getRoot();
            for (int j = 0; j <= i; j++) {
                String tx = j == i ? newTx : transactions.get(j);
                List<String> proof = merkleTree.getProof(j);
                assertTrue(merkleTree.verifyProof(tx, proof));
            }

            String newRoot = merkleTree.getRoot();
            assertEquals(root, newRoot);
        }
    }

    @Test
    public void testUpdateLeafInvalidInput() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> merkleTree.updateLeaf(0, null));
        assertTrue(exception.getMessage().contains("Transaction cannot be null or empty"));

        exception = Assertions.assertThrows(IllegalArgumentException.class, () -> merkleTree.updateLeaf(0, ""));
        assertTrue(exception.getMessage().contains("Transaction cannot be null or empty"));
    }

}
