package org.shirakawatyu.btmetadataserver.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.shirakawatyu.btmetadataserver.common.Result;
import org.shirakawatyu.btmetadataserver.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
@RequestMapping("/api/metadata")
public class MetadataController {
    @Autowired
    MetadataService metadataService;

    @GetMapping("/query/{hash}")
    public Result query(@PathVariable("hash") String infoHash) {
        return Result.ok().data(metadataService.getMetadata(infoHash));
    }

    @GetMapping("/torrent/{hash}")
    public void torrent(@PathVariable("hash") String infoHash, HttpServletResponse response) {
        byte[] torrent = metadataService.getTorrent(infoHash);
        response.reset();
        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment;filename=" + infoHash + ".torrent");
        try {
            response.getOutputStream().write(torrent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
