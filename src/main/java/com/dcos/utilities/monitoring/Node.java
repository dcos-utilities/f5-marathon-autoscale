package com.dcos.utilities.monitoring;

public class Node {
    public final String name;

    public Node(String name) {
        this.name = name != null ? name : "UNKNOWN";
    }
}
