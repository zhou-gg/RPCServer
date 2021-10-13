package rpcserver.server.producor;

import lombok.Data;

@Data
public class RpcResponse {

    private String requestId;
    private int code;
    private String errorMsg;
    private Object data;

}
