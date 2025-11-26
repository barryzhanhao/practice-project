package pers.james.practice.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

@Slf4j
public class MongoExample {

    public static void main(String[] args) {

        for (int i = 0; i < 1; i++) {
            // 连接字符串
            String uri = "mongodb://localhost:27017";

            // 构建客户端配置
            ConnectionString connectionString = new ConnectionString(uri);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();

            // 创建客户端
            try (MongoClient mongoClient = MongoClients.create(settings)) {

                // 获取数据库（不存在会自动创建）
                MongoDatabase database = mongoClient.getDatabase("testdb");

                // 获取集合（类似表）
                MongoCollection<Document> collection = database.getCollection("users");

                // 插入文档
                Document doc = new Document("name", "Alice")
                        .append("age", 25)
                        .append("city", "Shanghai");
                collection.insertOne(doc);
                System.out.println("✅ 插入成功: " + doc.toJson());

                // 查询
                FindIterable<Document> users = collection.find();
                for (Document user : users) {
                    System.out.println("📄 查询结果: " + user.toJson());
                }

                // 更新
                collection.updateOne(new Document("name", "Alice"), new Document("$set", new Document("age", 26)));
                System.out.println("🔄 已更新 Alice 的年龄");

                // 删除
                collection.deleteOne(new Document("name", "Alice"));
                System.out.println("🗑️ 已删除 Alice");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

    }
}
