package ru.practicum.ewm.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.service.EventSimilarityServiceImpl;
import ru.practicum.ewm.service.UserActionServiceImpl;
import ru.practicum.ewm.stats.message.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.message.RecommendedEventProto;
import ru.practicum.ewm.stats.message.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.message.UserPredictionsRequestProto;
import ru.practicum.ewm.stats.service.RecommendationsControllerGrpc;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final UserActionServiceImpl userActionServiceImpl;
    private final EventSimilarityServiceImpl eventSimilarityServiceImpl;

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> observer) {
        try {
            log.info("Received interactions weight request for eventIds: {}", request.getEventIdList());
            List<RecommendedEventProto> events = userActionServiceImpl.getTotalInteractionWeight(request.getEventIdList());
            events.forEach(event -> {
                observer.onNext(event);
                log.info("Response to interactions weight request: eventId: {}, rating: {}",
                        event.getEventId(), event.getScore());
            });
            observer.onCompleted();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            observer.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription(e.getLocalizedMessage()).withCause(e)));
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> observer) {
        try {
            log.info("Received similar events request: userId: {}, eventId: {}, maxResults = {}",
                    request.getUserId(), request.getEventId(), request.getMaxResults());
            List<RecommendedEventProto> events = eventSimilarityServiceImpl.getSimilarEvents(
                    request.getUserId(),
                    request.getEventId(),
                    request.getMaxResults());
            events.forEach(event -> {
                observer.onNext(event);
                log.info("Response to similar events request: eventId: {}, similarity: {}",
                        event.getEventId(), event.getScore());
            });
            observer.onCompleted();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            observer.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription(e.getLocalizedMessage()).withCause(e)));
        }
    }

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> observer) {
        try {
            log.info("Received recommendations for user request: userId: {}, maxResults: {}",
                    request.getUserId(), request.getMaxResults());
            List<RecommendedEventProto> events = eventSimilarityServiceImpl.getRecommendationsForUser(request.getUserId(),
                    request.getMaxResults());
            events.forEach(event -> {
                observer.onNext(event);
                log.info("Response to recommendations for user request: eventId: {}, similarity: {}",
                        event.getEventId(), event.getScore());
            });
            observer.onCompleted();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            observer.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription(e.getLocalizedMessage()).withCause(e)));
        }
    }
}