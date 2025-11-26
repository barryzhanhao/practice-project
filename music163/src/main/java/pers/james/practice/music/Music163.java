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
import org.json.JSONArray;
import org.json.JSONObject;

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
                if (StringUtils.isEmpty(downUrl)) {
                    log.warn("No download url for songId: {}", songId);
                    continue;
                }
                down(fileName, downUrl);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    private static List<String> getSongIds() throws Exception {
        String url = "https://music.163.com/api/playlist/detail?id=367079271";
        String response = execute(url);
        List<String> songIds = Lists.newArrayList();
        JSONObject json = new JSONObject(response);
        if (!json.has("result")) {
            System.err.println("API返回异常，未包含result字段，原始返回: " + response);
            return songIds;
        }
        JSONArray tracks = json.getJSONObject("result").getJSONArray("tracks");
        for (int i = 0; i < tracks.length(); i++) {
            JSONObject track = tracks.getJSONObject(i);
            songIds.add(String.valueOf(track.getLong("id")));
        }
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
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .addHeader("Referer", "https://music.163.com/");
        // TODO: 请将下面的 YOUR_COOKIE_HERE 替换为你自己的网易云音乐登录Cookie
        String cookie = "NMTID=00OTqD-5SC7sjOoLkHPovevd5MqtxAAAAGZ2BVU1Q; JSESSIONID-WYYY=C2%2FdPn%2F%2Baw3y%2FqazMXaxKVy%2BXF57R6Bbt2f%2BTwEzFcFFyyIjtDMqs0jUrqDmpw5rEw0P7Gb%2BOOACY%2Bbf135%2B6gMpGCt6e91WvpQs7vZZRsF8sSq0UfemSVk%2B9AMSwcTEe7IrHBOqh9tpS00JmrdO6HfphNekekwz9EC85THPPPH%2Bxot1%3A1760268769868; _iuqxldmzr_=32; _ntes_nnid=732d0772963286f8faa8cc4f2e7d16dd,1760266969914; _ntes_nuid=732d0772963286f8faa8cc4f2e7d16dd; WEVNSM=1.0.0; WNMCID=ocdios.1760266972541.01.0; ntes_utid=tid._.xVrrc0kE4ARFQgVEVVbGhVExNkjhEacr._.0; sDeviceId=YD-549rg54fAAZBV1AVERfGxFBkI120RbZq; WM_TID=f6L%2BCMjdedFEUUVABQPWwUR1dlz0UJhh; __snaker__id=WbzdVN4jaJpjotNo; gdxidpyhxdE=jGSQ8CeXsNq%2BuyBA%2BM1gxqSgDKDAvaO2f3UWt4tCO%5ChRZBBX8p8Yq%5CCvV3pfo5JPvdgrcQU7J0il6yxskiSR6BWrZhgtvmnveKgujw5aQsoaMxZks%2BW%2FnAWWxe74hq5zizlHx6y2ovfx9xz%5C%5CeqgsHC2axCUHO6M3zpBqhQfE%5CRdEDZ%2B%3A1760268069830; __csrf=8e866d3b650affb16ffe15e6a92fa28b; MUSIC_U=007A6D9C3DBBCCF5B56BC13F860868725F6CCDC9DA0AC02A85BA5AEE147B1694DADCD88EFED4E31F0C7D397D018371F860DAF464B040DE819E743A462205344C637B0D5E3FD8D76A17870BB201484AA62821C40914EC24DF808D938E3B36BE4F8E78EBDEBDCB3B91D17B694E9F381B7DF7C11E75D8225A8E412400F2740676FC734CC522A017A119A19F76633A19CF616A29C7A5D1307EB0A7902EFD4E3C97E7A3106A92C73470D1650F7519D7DE0BF230440C3F7466A2D2DFC0F94D045002C43ECE337B15284D9AD56E683BAD37EEA152B2E329AE04C1638AC2B5535BD43E1B5B490B264568CD517332E38C16B830D617FE5BCA3380DC35FBA9292FC641FE3905D905BD9F09AEAD71D7A9A7AA68832DD88826368A67083D6525DE4CD01AA219BC53F19B7297D01F262039CD0CCD9AC6719B3BA2455FD3D85C93C2E8F4E38C497FD2459EEF97A02CD8D74066DC7FE1090655E8DA0966B9646BBBF77DFCED414118A3FCD0C4704EAE634FD38A40610FF78617DB3F2F2618166BFD7EC185688502DF3A264B0FC12AB01AC0E84D5D8EC839F9; ntes_kaola_ad=1";
        if (!cookie.equals("YOUR_COOKIE_HERE")) {
            builder.addHeader("Cookie", cookie);
        }
        Request request = builder.build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private static String getDownUrl(String songId) throws Exception {
        String url = "http://music.163.com/song/media/outer/url?id=" + songId + ".mp3";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .addHeader("Referer", "https://music.163.com/")
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            List<String> location = response.headers("Location");
            if (location == null || location.isEmpty()) {
                log.error("No Location header found for songId: {}", songId);
                return null;
            }
            return location.get(0);
        }
    }

    private static void down(String fileName, String downUrl) throws Exception {
        if(!StringUtils.isEmpty(fileName)) {
            String saveDir = "/Users/zhanhao/Music/music"; // 修改为你自己的保存目录
            Request request = new Request.Builder()
                    .url(downUrl)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .addHeader("Referer", "https://music.163.com/")
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            InputStream is = response.body().byteStream();
            File file = new File(saveDir, fileName);
            FileUtils.touch(file);
            Files.write(is.readAllBytes(), file);
            response.close();
        }
    }
}
