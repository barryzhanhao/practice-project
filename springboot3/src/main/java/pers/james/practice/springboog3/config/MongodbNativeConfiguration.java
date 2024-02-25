package pers.james.practice.springboog3.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.stream.Stream;

@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(MongodbNativeConfiguration.MongodbRuntimeHintsRegistrar.class)
public class MongodbNativeConfiguration {

    static class MongodbRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            Stream.of(AggregationOperation.class).forEach(x -> hints.reflection().registerType(x, MemberCategory.values()));
        }
    }

}
