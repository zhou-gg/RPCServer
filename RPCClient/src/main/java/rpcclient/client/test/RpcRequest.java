package rpcclient.client.test;

import lombok.Data;

@Data
public class RpcRequest {

    private String id;
    private String className;
    private String methodName;
    private Object[] params;
}
