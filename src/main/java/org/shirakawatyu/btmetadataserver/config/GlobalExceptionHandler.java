package org.shirakawatyu.btmetadataserver.config;

import org.shirakawatyu.btmetadataserver.common.Result;
import org.shirakawatyu.btmetadataserver.common.ResultCode;
import org.shirakawatyu.btmetadataserver.exception.MetadataNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MetadataNotFoundException.class)
    public Result handleMetadataNotFoundException(MetadataNotFoundException e) {
        return Result.fail().code(ResultCode.METADATA_NOT_FOUND).msg("Metadata not found, may due to the info_hash is wrong or no connectable peers");
    }
}
