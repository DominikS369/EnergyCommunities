package gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class EnergyApiClient {

    private static final String BASE_URL = "http://localhost:8080";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public CurrentPercentage fetchCurrent() throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + "/energy/current")).GET().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IllegalStateException("GET /energy/current returned " + res.statusCode());
        }
        return mapper.readValue(res.body(), CurrentPercentage.class);
    }

    public List<HistoricalUsage> fetchHistorical(LocalDateTime start, LocalDateTime end) throws Exception {
        String url = BASE_URL + "/energy/historical"
                + "?start=" + URLEncoder.encode(start.toString(), StandardCharsets.UTF_8)
                + "&end="   + URLEncoder.encode(end.toString(),   StandardCharsets.UTF_8);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IllegalStateException("GET /energy/historical returned " + res.statusCode());
        }
        return mapper.readValue(res.body(), new TypeReference<List<HistoricalUsage>>() {});
    }

    public record CurrentPercentage(LocalDateTime hour, double communityDepleted, double gridPortion) {}

    public record HistoricalUsage(LocalDateTime hour, double communityProduced, double communityUsed, double gridUsed) {}
}
