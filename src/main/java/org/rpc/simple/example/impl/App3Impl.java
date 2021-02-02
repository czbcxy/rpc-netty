package org.rpc.simple.example.impl;

import org.rpc.simple.example.api.AppApi3;

/**
 * Hello world!
 */
public class App3Impl implements AppApi3 {
    @Override
    public String doAction(String str) {
        return "Hello App3Impl " + str;
    }
}
