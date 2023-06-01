# MerkleTree Project

This project provides a Java implementation of a Merkle Tree. A Merkle Tree is a tree in which every leaf node is labelled with the cryptographic hash of a data block, and every non-leaf node is labelled with the hash of the labels of its child nodes. It is widely used in distributed systems for efficient data verification.

## Requirements

- Java JDK 8 or later
- Gradle 8.1.1 or later

## Getting Started

1. Clone the repository to your local machine:

    ```sh
    git clone https://github.com/jlarrieux/MerkleTree.git
    ```

2. Navigate to the project directory:

    ```sh
    cd MerkleTree
    ```

3. Build the project using Gradle:

    ```sh
    gradle build
    ```

## Running Tests

This project contains a comprehensive suite of unit tests. To run these tests, use the following Gradle command:

```sh
gradle test
```

## Usage
```
import org.example.MerkleTree;

List<String> transactions = Arrays.asList("transaction1", "transaction2", "transaction3", "transaction4");
MerkleTree merkleTree = new MerkleTree(transactions);

// Get the root of the Merkle Tree
String root = merkleTree.getRoot();

// Verify a transaction
List<String> proof = merkleTree.getProof(0);
boolean isValid = merkleTree.verifyProof(transactions.get(0), proof);

```