package pers.james.practice.music;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.util.List;

@Slf4j
public class Music163 {

    private static OkHttpClient okHttpClient = new OkHttpClient().newBuilder().followRedirects(false)
            .followSslRedirects(false).build();

    public static void main(String[] args) throws Exception {
        List<String> songIds = getSongIds();
        for (String songId : songIds) {
            try {
                String fileName = getFileName(songId);
                if (StringUtils.isEmpty(fileName)) {
                    continue;
                }
                String downUrl = getDownUrl(songId);
                down(fileName, downUrl);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    private static List<String> getSongIds() throws Exception {
        String url = "https://music.163.com/playlist?id=367079271";

        String response = execute(url);
        Document parse = Jsoup.parse(response);
        Elements ul = parse.getElementsByTag("ul");

        List<String> songIds = Lists.newArrayList();
        ul.forEach(element -> {
            if (element.className().equals("f-hide")) {
                element.children().forEach(children -> {
                    children.children().forEach(child -> {
                        String href = child.attr("href");
                        String[] split = href.split("=");
                        songIds.add(split[1]);
                    });
                });
            }
        });
        return songIds;
    }

    private static String getFileName(String songId) throws Exception {
        String url = "https://music.163.com/song?id=" + songId;
        String response = execute(url);
        Document parse = Jsoup.parse(response);
        Elements artists = parse.getElementsByClass("des s-fc4");
        String artist = "";
        for (Element element : artists) {
            for (Element child : element.children()) {
                if (child.tagName().equals("span")) {
                    artist = child.attr("title");
                }
            }
        }
        Elements songNames = parse.getElementsByClass("f-ff2");
        String songName = "";
        for (Element item : songNames) {
            if (item.tagName().equals("em")) {
                songName = item.text();
            }
        }
        String fileName = artist + "-" + songName + ".mp3";
        if (fileName.contains("/")) {
            fileName = fileName.replace("/", "-");
        }
        return fileName;
    }

    private static String execute(String url) throws Exception {
        Request request = new Request.Builder().url(url).build();
        try (Response response = okHttpClient.newCall(request).execute();) {
            return response.body().string();
        }
    }

    private static String getDownUrl(String songId) throws Exception {
        String url = "http://music.163.com/song/media/outer/url?id=" + songId + ".mp3";
        Request request = new Request.Builder().url(url).build();
        try (Response response = okHttpClient.newCall(request).execute();) {
            List<String> location = response.headers("Location");
            return location.get(0);
        }
    }

    private static void down(String fileName, String downUrl) throws Exception {
        String saveDir = "/home/zhanhao/Music/music";
        Request request = new Request.Builder().url(downUrl).build();
        Response response = okHttpClient.newCall(request).execute();
        InputStream is = response.body().byteStream();
        File file = new File(saveDir, fileName);
        FileUtils.touch(new File(saveDir, fileName));
        Files.write(is.readAllBytes(), file);
        response.close();

    }


}
