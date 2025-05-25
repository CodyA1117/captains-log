package com.minderall.captainslogapp.Services;

import com.minderall.captainslogapp.Models.OuraData;
import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.dto.OuraDailyActivityResponse;
import com.minderall.captainslogapp.dto.OuraDailyReadinessResponse;
import com.minderall.captainslogapp.dto.OuraDailySleepResponse;
import com.minderall.captainslogapp.exception.ResourceNotFoundException;
import com.minderall.captainslogapp.Repositories.OuraDataRepository;
import com.minderall.captainslogapp.Repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OuraServiceImpl implements OuraService {

    private static final Logger logger = LoggerFactory.getLogger(OuraServiceImpl.class);

    private final WebClient webClient;
    private final UserRepository userRepository;
    private final OuraDataRepository ouraDataRepository;
    private final UserService userService; // To check for valid tokens

    @Value("${oura.api.base.url}")
    private String ouraApiBaseUrl;

    @Autowired
    public OuraServiceImpl(WebClient.Builder webClientBuilder, UserRepository userRepository,
                           OuraDataRepository ouraDataRepository, UserService userService,
                           @Value("${oura.api.base.url}") String ouraApiBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(ouraApiBaseUrl).build();
        this.userRepository = userRepository;
        this.ouraDataRepository = ouraDataRepository;
        this.userService = userService;
        this.ouraApiBaseUrl = ouraApiBaseUrl; // Ensure it's set if using constructor injection
    }

    private Mono<User> getValidUserForOuraApi(Long userId) {
        return Mono.fromCallable(() -> userService.findUserWithValidOuraToken(userId)
                        .orElseThrow(() -> {
                            logger.warn("User {} not found or Oura token invalid/expired.", userId);
                            return new OuraApiTokenException("User not found or Oura token invalid/expired for user ID: " + userId);
                        }))
                .onErrorResume(e -> {
                    logger.error("Error retrieving user or token: {}", e.getMessage());
                    return Mono.error(e); // Propagate the specific exception
                });
    }


    // Generic method to fetch data from Oura
    private <T> Mono<T> fetchDataFromOura(User user, String path, Class<T> responseType, LocalDate date) {
        String formattedDate = date.toString(); // yyyy-MM-dd
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("start_date", formattedDate)
                        .queryParam("end_date", formattedDate) // For single day, start and end are same
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.getOuraOAuthToken())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus.UNAUTHORIZED::equals, clientResponse -> {
                    logger.error("Oura API returned 401 Unauthorized for user ID: {}. Token might be invalid or expired.", user.getId());
                    // Potentially trigger refresh token logic here in a more advanced setup
                    return Mono.error(new OuraApiTokenException("Oura API Unauthorized. Token may need refresh."));
                })
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    logger.error("Oura API Client Error (4xx) for user ID: {}: {} {}", user.getId(), clientResponse.statusCode(), clientResponse.toString());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new OuraApiException("Oura API client error: " + clientResponse.statusCode() + " Body: " + body)));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.error("Oura API Server Error (5xx) for user ID: {}: {} {}", user.getId(), clientResponse.statusCode(), clientResponse.toString());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new OuraApiException("Oura API server error: " + clientResponse.statusCode() + " Body: " + body)));
                })
                .bodyToMono(responseType)
                .doOnError(e -> logger.error("Error fetching {} for user ID {}: {}", path, user.getId(), e.getMessage()));
    }

    @Override
    public Mono<OuraDailyActivityResponse.ActivityData> getDailyActivityForDate(User user, LocalDate date) {
        return fetchDataFromOura(user, "/usercollection/daily_activity", OuraDailyActivityResponse.class, date)
                .map(response -> response.getData().isEmpty() ? null : response.getData().get(0)); // Oura returns a list
    }

    @Override
    public Mono<OuraDailySleepResponse.SleepData> getDailySleepForDate(User user, LocalDate date) {
        return fetchDataFromOura(user, "/usercollection/daily_sleep", OuraDailySleepResponse.class, date)
                .map(response -> response.getData().isEmpty() ? null : response.getData().get(0));
    }

    @Override
    public Mono<OuraDailyReadinessResponse.ReadinessData> getDailyReadinessForDate(User user, LocalDate date) {
        return fetchDataFromOura(user, "/usercollection/daily_readiness", OuraDailyReadinessResponse.class, date)
                .map(response -> response.getData().isEmpty() ? null : response.getData().get(0));
    }

    @Override
    @Transactional // This method involves database writes
    public void fetchAndStoreTodayOuraData(Long userId) {
        LocalDate today = LocalDate.now();

        getValidUserForOuraApi(userId).flatMap(user -> {
            Mono<OuraDailyActivityResponse.ActivityData> activityMono = getDailyActivityForDate(user, today).onErrorResume(e -> Mono.empty());
            Mono<OuraDailySleepResponse.SleepData> sleepMono = getDailySleepForDate(user, today).onErrorResume(e -> Mono.empty());
            Mono<OuraDailyReadinessResponse.ReadinessData> readinessMono = getDailyReadinessForDate(user, today).onErrorResume(e -> Mono.empty());

            return Mono.zip(activityMono, sleepMono, readinessMono)
                    .flatMap(tuple -> {
                        OuraDailyActivityResponse.ActivityData activityData = tuple.getT1();
                        OuraDailySleepResponse.SleepData sleepData = tuple.getT2();
                        OuraDailyReadinessResponse.ReadinessData readinessData = tuple.getT3();

                        if (activityData == null && sleepData == null && readinessData == null) {
                            logger.info("No Oura data found for user {} on {}", user.getId(), today);
                            return Mono.empty(); // Or Mono.error if this is unexpected
                        }

                        // Find existing or create new OuraData entity for the user and date
                        OuraData ouraDataEntry = ouraDataRepository.findByUserAndDataDate(user, today)
                                .orElse(OuraData.builder().user(user).dataDate(today).build());

                        if (activityData != null) {
                            ouraDataEntry.setActivityScore(activityData.getScore());
                        }
                        if (sleepData != null) {
                            ouraDataEntry.setSleepScore(sleepData.getScore());
                        }
                        if (readinessData != null) {
                            ouraDataEntry.setReadinessScore(readinessData.getScore());
                        }

                        // Ensure createdAt is set if it's a new entity
                        if (ouraDataEntry.getId() == null) {
                            ouraDataEntry.setCreatedAt(LocalDateTime.now());
                        }


                        ouraDataRepository.save(ouraDataEntry);
                        logger.info("Successfully fetched and stored Oura data for user {} on {}", user.getId(), today);
                        return Mono.just(ouraDataEntry);
                    });
        }).subscribe( // We need to subscribe to trigger the reactive chain
                data -> logger.debug("Oura data processing for user {} complete.", userId),
                error -> logger.error("Error in fetchAndStoreTodayOuraData for user {}: {}", userId, error.getMessage(), error)
        );
    }
}