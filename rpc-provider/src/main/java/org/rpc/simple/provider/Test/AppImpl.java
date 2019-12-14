package org.rpc.simple.provider.Test;

import org.rpc.simple.api.AppApi;

/**
 * Hello world!
 */
public class AppImpl implements AppApi {
    @Override
    public String doAction(String str) {
        return "Hello " + str;
    }
}
