package org.example;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class MerkleTree {
  private List<String> transactions;
  private String root;
  private final ReentrantLock lock = new ReentrantLock();
  private List<List<String>> tree;

  public MerkleTree(List<String> transactions){
      if(transactions == null || transactions.size() == 0){
          throw new IllegalArgumentException("Transaction list must not be null or empty");
      }
      Set<String> set = new HashSet<>(transactions);
      if(set.size() != transactions.size()){
          throw new IllegalArgumentException("Duplicates not allowed.");
      }
      this.transactions= transactions;
      buildTree();
  }

    /**
     *
     * @return the root of the merkle tree.
     */
  public String getRoot(){
      lock.lock();
      try{
          return root;
      }finally {
          lock.unlock();
      }
  }

    /**
     *
     * @param index the index of the transaction in the transaction list
     * @return a list of hashes needed for verification
     */
  public List<String> getProof(int index){
      lock.lock();
      try{
          if(index < 0 || index >= transactions.size()){
              throw new IllegalArgumentException("Index out of bounds");
          }

          List<String> proof = new ArrayList<>();
          for(int i= tree.size()-1; i>-1; i--){
              int siblingPosition = index ^ 1;
              if(siblingPosition < tree.get(i).size()){
                  String hash = tree.get(i).get(siblingPosition);
                  proof.add(hash);
                  index >>=1;
              }
          }

          return proof;

      }finally {
          lock.unlock();
      }
  }

    /**
     *
     * @param leaf the leaf data that we are trying to prove is part of the Merkle tree.
     * @param proof the list of hashes needed to go from the leaf data all the way to the root.
     * @return
     */
  public boolean verifyProof(String leaf, List<String> proof){
      lock.lock();
      try {
          String computedHash = sha256(leaf);
          int leafIndex = transactions.indexOf(leaf);
          for(int i=0; i< proof.size(); i++){
              String proofItem = proof.get(i);
              computedHash = leafIndex % 2 == 0? sha256(computedHash+proofItem): sha256(proofItem + computedHash);
              leafIndex >>=1;
          }
          return computedHash.equals(getRoot());
      } finally {
          lock.unlock();
      }
  }

    /**
     *
     * @param index the index of the data to be updated
     * @param newTransaction the replacement data
     */
  public void updateLeaf(int index, String newTransaction){
      if(newTransaction == null || newTransaction.length() == 0){
          throw new IllegalArgumentException("Transaction cannot be null or empty");
      }
      lock.lock();
      try{
          if(index < 0 || index >= transactions.size()){
              throw new IllegalArgumentException("Index out of bounds");
          }
          transactions.set(index, newTransaction);
          buildTree();
      }finally {
          lock.unlock();
      }
  }

    /**
     *
     * @return a copy of the transaction list
     */
  public List<String> getTransactions(){
      lock.lock();
      try{
          List<String> transactionCopy = new ArrayList<>();
          for(String transaction: transactions){
              transactionCopy.add(new String(transaction));
          }
          return transactionCopy;
      }finally {
          lock.unlock();
      }
  }


  private void buildTree(){
      lock.lock();
      try{
          tree = new ArrayList<>();

          List<String> hashedTransactions = new ArrayList<>();
          for(String transaction: transactions){
              hashedTransactions.add(sha256(transaction));
          }
          tree.add(hashedTransactions);
          while(tree.get(0).size() >1){
              List<String> currentLevel = new ArrayList<>();
              List<String> previousLevel = tree.get(0);
              int index = 0;
              while(index < previousLevel.size()){
                  if(index == previousLevel.size() -1){
                      currentLevel.add(sha256(previousLevel.get(index) + previousLevel.get(index)));
                      index++;
                  }else{
                      currentLevel.add(sha256(previousLevel.get(index) + previousLevel.get(index +1)));
                      index +=2;
                  }
              }
              tree.add(0, currentLevel);
          }
          root = tree.get(0).get(0);

      } finally {
          lock.unlock();
      }
  }

  private static String sha256(String input){
      try{
          MessageDigest digest = MessageDigest.getInstance("SHA-256");
          byte[] hash = digest.digest(input.getBytes());
          StringBuilder hexString = new StringBuilder();
          for(byte b: hash){
              String hex = Integer.toHexString(0xff & b);
              if(hex.length() ==1){
                  hexString.append('0');
              }
              hexString.append(hex);
          }
          return hexString.toString();

      }catch (NoSuchAlgorithmException e){
          throw new RuntimeException(e);
      }
  }

  public void printTree(){
      for(List<String> level: tree){
          StringBuilder b = new StringBuilder("[");
          for(String val: level){
              if(b.length()>2){
                  b.append(", ");
              }
              b.append(val);
          }
          b.append("]");
          System.out.println(b);
      }
  }
}
