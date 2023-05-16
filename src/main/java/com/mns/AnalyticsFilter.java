package com.mns;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Filter("/orders/?*")
public class AnalyticsFilter implements HttpServerFilter {

    private final AnalyticsClient analyticsClient;

    public AnalyticsFilter(AnalyticsClient analyticsClient) {
        this.analyticsClient = analyticsClient;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request,
                                                      ServerFilterChain chain) {
        return Flux
                .from(chain.proceed(request))
                .flatMap(response -> {
                    Order order = response.getBody(Order.class).orElse(null);
                    if (order == null) {
                        return Flux.just(response);
                    }
                    return Flux.from(analyticsClient.updateAnalytics(order.id(),order,"abcd")).map(b -> response);
                });
    }
}