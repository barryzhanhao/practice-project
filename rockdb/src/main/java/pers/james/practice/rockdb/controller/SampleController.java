package pers.james.practice.rockdb.controller;

import lombok.extern.slf4j.Slf4j;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SampleController {

    @GetMapping("/hello")
    public String get() {

        RocksDB.loadLibrary();

        String dbPath = "rocksdb-data";

        // 配置 RocksDB 选项
        try (Options options = new Options().setCreateIfMissing(true)) {
            // 打开数据库
            try (RocksDB db = RocksDB.open(options, dbPath)) {

                // 插入键值对
                db.put("key1".getBytes(), "value1".getBytes());
                System.out.println("插入 key1:value1");

                // 获取值
                byte[] value = db.get("key1".getBytes());
                System.out.println("读取 key1: " + new String(value));

                // 删除键
                db.delete("key1".getBytes());
                System.out.println("删除 key1");

                // 尝试再次获取
                byte[] deletedValue = db.get("key1".getBytes());
                if (deletedValue == null) {
                    System.out.println("key1 不存在了");
                }

            } catch (RocksDBException e) {
                System.err.println("RocksDB 操作失败: " + e.getMessage());
            }
        }

        return "2222";
    }

}
