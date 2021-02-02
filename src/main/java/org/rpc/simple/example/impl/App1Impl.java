package org.rpc.simple.example.impl;


import org.rpc.simple.example.api.AppApi2;

/**
 * Hello world!
 */
public class App1Impl implements AppApi2 {
    @Override
    public String doAction(String str) {
        return "Hello App1Impl " + str;
    }
}
