package org.rpc.simple.example.impl;


import org.rpc.simple.example.api.AppApi;

/**
 * Hello world!
 */
public class AppImpl implements AppApi {
    @Override
    public String doAction(String str) {
        return "Hello " + str;
    }
}
