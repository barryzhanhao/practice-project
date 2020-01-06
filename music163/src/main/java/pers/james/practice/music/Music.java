package pers.james.practice.music;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Music {

    private static OkHttpClient okHttpClient = new OkHttpClient().newBuilder().followRedirects(false)
        .followSslRedirects(false).build();

    public static void main(String[] args) throws Exception {
        String url = "https://y.09l.me/api.php?callback=jQuery1113000419663132517778_1561876632705";
        OkHttpClient okHttpClient = new OkHttpClient();
        FormBody formBody = new FormBody.Builder().add("types", "search").add("count", "20").add("source", "netease")
            .add("pages", "1").add("name", "136737").build();
        Request request = new Request.Builder().post(formBody).url(url).build();
        Response response = okHttpClient.newCall(request).execute();
        String result = response.body().string();

        System.out.println();
    }
}