package org.shirakawatyu.btmetadataserver.common;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;


@Data(staticConstructor = "of")
@Accessors(chain = true)
public class Result implements Serializable {
    @Serial
    private static final long serialVersionUID = 1891139821031907141L;
    private boolean success;
    private int code;
    private String msg;
    private Object data;


    public static Result ok() {
        return Result.of().setSuccess(true);
    }

    public static Result fail() {
        return Result.of().setSuccess(false);
    }

    public Result data(Object data) {
        return setData(data);
    }

    public Result code(int code) {
        return setCode(code);
    }

    public Result msg(String msg) {
        return setMsg(msg);
    }
}