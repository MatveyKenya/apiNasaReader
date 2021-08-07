import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
    public static ObjectMapper mapper = new ObjectMapper();
    public static void main(String[] args) {

        CloseableHttpClient httpClient = createClient();
        HttpGet request = new HttpGet("https://api.nasa.gov/planetary/apod?api_key=TU5oE8BwcuNLXE8dEDbGyuYKJInJSmLmE9PViByn");
        CloseableHttpResponse response = null;
        try{
            response = httpClient.execute(request);
            if (response != null){
                Post post = mapper.readValue(response.getEntity().getContent(), new TypeReference<>() {});
                String url = post.getUrl();

                response = httpClient.execute(new HttpGet(url));
                if (response != null){
                    byte[] result = response.getEntity().getContent().readAllBytes();
                    if (saveToFile(result, getFileName(url))){
                        System.out.printf("файл %s записан", getFileName(url));
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    static private CloseableHttpClient createClient(){
        return HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                        .build())
                .build();
    }

    static private boolean saveToFile(byte[] content, String fileName){
        try (FileOutputStream fos = new FileOutputStream(fileName)){
            fos.write(content);
            fos.flush();
            return true;
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    static private String getFileName(String url){
        String[] subs = url.split("/");
        return subs[subs.length-1];
    }
}
