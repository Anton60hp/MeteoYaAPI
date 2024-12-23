import com.google.gson.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;


public class App {
    public static void main(String[] args) throws ParseException {
        Scanner scanner = new Scanner(System.in);
        JsonParser jsonParser = new JsonParser();

        String responseBody = null;

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.weather.yandex.ru/v2/forecast"))
                .header("X-Yandex-Weather-Key", "4124124129412841284128482")
                .header("lat", "55.649885")
                .header("lon", "37.664355")
                .GET()
                .build();

        System.out.println("Получить среднюю температуру за Х следующие часов: ");
        while (!scanner.hasNextInt()) {
            System.out.println("Целочисленое число, пожалуйста");
            System.out.println("Получить среднюю температуру за Х следующие часов: ");
            scanner.next();
        }

        int limit = scanner.nextInt();
//        System.out.println(limit);
        System.out.println();
        System.out.println();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("json тело целиком: " + response.body());

//            System.out.println(
//                    response.body().substring(
//                            response.body().indexOf("\"temp\""),
//                            response.body().indexOf(",", response.body().indexOf("\"temp\""))
//                    )
//            );  // Сделал через сабстринг веселья ради

            responseBody = response.body();
        } catch (Exception e) {
            System.err.println("Error making HTTP request: " + e.getMessage());
        }
        JsonObject json = jsonParser.parse(responseBody).getAsJsonObject();
        System.out.println("Текущая температура: " + json.get("fact").getAsJsonObject().get("temp").getAsInt());

        getAVGTemp(json, limit);
    }


    public static void getAVGTemp(JsonObject jsonObj, int num) throws ParseException {

        // Создаём календарь с текущим временем
        Calendar startTime = new GregorianCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        startTime.setTime(sdf.parse((jsonObj.get("now_dt").getAsString())));

        // Достаём следующий час
        int curHour = startTime.get(Calendar.HOUR_OF_DAY) + 1;


        int d = 0;
        int sum = 0;
        int i = 0;
        // Если запуск в 23 часа, то переходим на следуюший день 00 часов
        if (curHour == 23) {
            curHour = 0;
            d++;
        }

        try {
            for (i=0; i < num; i++) {
                sum += jsonObj
                        .get("forecasts").getAsJsonArray()
                        .get(d).getAsJsonObject()
                        .get("hours").getAsJsonArray()
                        .get(curHour).getAsJsonObject()
                        .get("temp").getAsInt();
                curHour++;

                if (curHour > 23) {
                    curHour = 0;
                    d++;
                }
                if (d == 7) break;
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.printf("Недостаточно данных для расчёта на %d часов. \n Уменьшено до %d", num, i);
            System.out.println();
            System.out.println();
        }

        System.out.printf("Средняя температура за следующие %d часов: %.2f", i,(double) sum /i);

    }
}
