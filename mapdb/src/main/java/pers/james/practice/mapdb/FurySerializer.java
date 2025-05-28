package pers.james.practice.mapdb;

import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.Language;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;

public class FurySerializer<T> implements Serializer<T> {

    private final ThreadSafeFury fury;

    public FurySerializer(Class<T> type) {
        ThreadSafeFury fury = Fury.builder().withLanguage(Language.JAVA)
                .requireClassRegistration(true)
                .buildThreadSafeFury();
        fury.register(type);
        this.fury = fury;
    }

    @Override
    public void serialize(@NotNull DataOutput2 out, @NotNull T value) throws IOException {
        byte[] bytes = fury.serialize(value);
        out.packInt(bytes.length);
        out.write(bytes);
    }

    @Override
    public T deserialize(@NotNull DataInput2 input, int available) throws IOException {
        int size = input.unpackInt();
        byte[] ret = new byte[size];
        input.readFully(ret);
        return (T) fury.deserialize(ret);
    }

}
