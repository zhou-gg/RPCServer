package rpcclient.client.test;

import lombok.Data;

import java.util.List;

@Data
public class RpcResponse {

    private String requestId;
    private int code;
    private String errorMsg;
    private String[] result;

}
