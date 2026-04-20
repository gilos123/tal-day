/*
 * 06-atomic-counter.c — Lesson 6.
 *
 * The minimal fix to 01-race.c: upgrade the plain int to atomic_long.
 * We still use the default seq_cst ordering on `fetch_add`; lesson 7
 * explores when relaxed would be sufficient.
 *
 * Compile: make build/06-atomic-counter
 * Run:     make run-06-atomic-counter
 */
#include <stdatomic.h>
#include <stdio.h>
#include <pthread.h>

#define ITERS 1000000

static atomic_long x = 0;

static void *worker(void *arg) {
    (void)arg;
    for (long i = 0; i < ITERS; i++) {
        atomic_fetch_add(&x, 1);
    }
    return NULL;
}

int main(void) {
    pthread_t a, b;
    pthread_create(&a, NULL, worker, NULL);
    pthread_create(&b, NULL, worker, NULL);
    pthread_join(a, NULL);
    pthread_join(b, NULL);
    long final = atomic_load(&x);
    printf("expected = %ld\n", 2L * ITERS);
    printf("final    = %ld\n", final);
    return (final == 2L * ITERS) ? 0 : 1;
}
