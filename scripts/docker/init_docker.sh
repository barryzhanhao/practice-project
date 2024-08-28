docker run --name mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:8 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

docker run --name mongo -p 27017:27017 -d mongo

docker exec -it mongo mongosh admin

db.createUser({ user:'admin',pwd:'123456',roles:[ { role:'userAdminAnyDatabase', db: 'admin'},"readWriteAnyDatabase"]});

db.auth('admin', '123456')

db.createUser({ user:'test',pwd:'123456',roles:[ { role:'readWrite', db: 'test'}]});

db.auth('test', '123456')

db.getCollection("tt_data_version_log").insert({"tableName":"a","dataId":"b","version":"2"})

db.products.insert( { item: "card", qty: 15 } )

docker run --name redis -p 6379:6379 -d redis

docker exec -it redis /bin/bash