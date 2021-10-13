package rpcserver.server.producor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RpcService
public class UserServiceImpl implements UserService {

    @Override
    public String name(String name) {
        log.error("查询用户的姓名!");
        return name;
    }



}
