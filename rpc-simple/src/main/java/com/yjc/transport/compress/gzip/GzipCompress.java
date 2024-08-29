package com.yjc.transport.compress.gzip;

import com.yjc.transport.compress.Compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipCompress implements Compress {

    private static final int BUFFER_SIZE = 1024 * 4;

    @Override
    public byte[] compress(byte[] bytes) {
        if(bytes == null){
            throw new NullPointerException("压缩消息为空");
        }
        try(ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out)){
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return out.toByteArray();
        }catch (IOException e){
            throw new RuntimeException("压缩失败");
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if(bytes == null){
            throw new NullPointerException("解压缩消息为空");
        }
        try(ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes))){
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while((n = gzip.read(buffer)) >= 0){
                out.write(buffer,0,n);
            }
            return out.toByteArray();
        }catch (IOException e){
            throw new RuntimeException("解压缩失败");
        }
    }
}
