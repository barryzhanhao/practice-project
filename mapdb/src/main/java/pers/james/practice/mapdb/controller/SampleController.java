package pers.james.practice.mapdb.controller;

import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.james.practice.mapdb.FurySerializer;
import pers.james.practice.mapdb.MyData;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class SampleController {

    @GetMapping("/hello")
    public String get() {
        // 1. 创建或打开一个磁盘数据库文件（自动创建）
        DB db = DBMaker
                .fileDB("data.db")      // 数据文件
                .fileMmapEnable()       // 启用 mmap，提高性能
                .fileMmapPreclearDisable()
                .fileMmapEnableIfSupported()
                .make();

        // 2. 获取或创建一个 Map
        HTreeMap<String, String> map = db
                .hashMap("myMap", Serializer.STRING, Serializer.STRING)
                .createOrOpen();

        // 3. 写入数据
        map.put("hello", "world");

        // 4. 读取数据
        System.out.println("value => " + map.get("hello"));

        // 5. 关闭数据库（释放文件句柄）
        db.close();

        return "2222";
    }

    @GetMapping("/hello1")
    public String get2() {
        // 1. 创建或打开一个磁盘数据库文件（自动创建）
        DB db = DBMaker
                .fileDB("data1.db")      // 数据文件
                .fileMmapEnable()       // 启用 mmap，提高性能
                .fileMmapPreclearDisable()
                .fileMmapEnableIfSupported()
                .make();

        // 2. 获取或创建一个 Map
        HTreeMap<String, MyData> map = db
                .hashMap("myMap1", Serializer.STRING, new FurySerializer<>(MyData.class))
                .createOrOpen();

        // 3. 写入数据
        map.put("hello", new MyData("11",1));

        MyData hello = map.get("hello");

        // 4. 读取数据
        System.out.println("value => " + hello);

        // 5. 关闭数据库（释放文件句柄）
        db.close();

        return "2222";
    }

}
