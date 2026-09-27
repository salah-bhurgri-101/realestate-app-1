package com.example.auditstarter;

import com.example.auditstarter.model.AuditEvent;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

/**
 * Client for reporting audit events to the centralized audit-service.
 *
 * Three flavors are provided so you can pick whichever fits the call site:
 *  - logBlocking: waits for the HTTP call to finish (use sparingly — adds latency)
 *  - log:         fire-and-forget, returns a CompletableFuture if you want to track it
 *  - logReactive: for callers already inside a reactive (WebFlux) chain
 */
public interface AuditClient {

    /** Fire-and-forget. Returns immediately; the HTTP call happens in the background. */
    CompletableFuture<Void> log(AuditEvent event);

    /** Blocks the calling thread until the audit-service responds (or times out/retries exhaust). */
    void logBlocking(AuditEvent event);

    /** For use inside an existing reactive pipeline — compose with .then(), .flatMap(), etc. */
    Mono<Void> logReactive(AuditEvent event);
}
