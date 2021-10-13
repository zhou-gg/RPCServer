package rpcclient.client.test;

import java.util.HashMap;
import java.util.Map;

public class RpcResult {

    private static Map<String,RpcResponse> map = new HashMap<>();

    public static void add(RpcResponse rpcResponse){
        map.put(rpcResponse.getRequestId(),rpcResponse);
    }

    public static RpcResponse get(String requestId){
        RpcResponse rpcResponse = map.remove(requestId);
        while (rpcResponse==null){
            map.remove(requestId);
        }
        return rpcResponse;
    }

}
