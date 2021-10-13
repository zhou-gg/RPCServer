package rpcserver.server.producor;

import lombok.Data;

@Data
public class RpcRequest {

    private String id;
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

}
