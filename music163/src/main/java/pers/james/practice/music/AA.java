package pers.james.practice.music;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

public class AA {

    public static void main(String[] args) throws Exception {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().followRedirects(false)
                .followSslRedirects(false).build();
        List<String> strings = FileUtils.readLines(new File("/home/zhanhao/Desktop/1111.txt"));
        for (String string : strings) {
            String url = "http://classic-product.test.bkjk.cn/api/v1.0/combinations/products/" + string;

            Request request = new Request.Builder().url(url).build();
            Response response = okHttpClient.newCall(request).execute();
            String string1 = response.body().string();

            if (string1.contains("errorCode")) {
                System.out.println(string);
            }
        }

        System.out.println();

    }
}
