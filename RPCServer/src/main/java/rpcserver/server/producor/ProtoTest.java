package rpcserver.server.producor;

public class ProtoTest {

    public static void main(String[] args) {
        RpcRequestProto.RpcRequest.Builder builder = RpcRequestProto.RpcRequest.newBuilder();
        builder.setClassName(2,"222");

    }

}
