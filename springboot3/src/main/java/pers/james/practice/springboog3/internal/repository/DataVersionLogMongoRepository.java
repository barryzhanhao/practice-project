package pers.james.practice.springboog3.internal.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import pers.james.practice.springboog3.internal.entity.DataVersionLogMongo;

@Repository
public class DataVersionLogMongoRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public DataVersionLogMongo get() {
        // 查询数据最新版本
        Criteria criteria = Criteria.where(DataVersionLogMongo.Fields.tableName)
                .is("a")
                .and(DataVersionLogMongo.Fields.dataId)
                .is("b");
        Sort sort = Sort.by(Sort.Order.desc(DataVersionLogMongo.Fields.version));
        Query query = new Query().addCriteria(criteria).with(sort).limit(1);
        DataVersionLogMongo one = mongoTemplate.findOne(query, DataVersionLogMongo.class);
        return one;
    }
}
