/*
 * 03-mutex-counter.c — Lesson 3.
 *
 * Same counter as 01-race.c, but each increment is now inside the
 * critical section guarded by a mutex. Final value is always
 * 2,000,000.
 *
 * Compile: make build/03-mutex-counter
 * Run:     make run-03-mutex-counter
 */
#include <pthread.h>
#include <stdio.h>

#define ITERS 1000000

static long x = 0;
static pthread_mutex_t mu = PTHREAD_MUTEX_INITIALIZER;

static void *worker(void *arg) {
    (void)arg;
    for (long i = 0; i < ITERS; i++) {
        pthread_mutex_lock(&mu);
        x = x + 1;
        pthread_mutex_unlock(&mu);
    }
    return NULL;
}

int main(void) {
    pthread_t a, b;
    pthread_create(&a, NULL, worker, NULL);
    pthread_create(&b, NULL, worker, NULL);
    pthread_join(a, NULL);
    pthread_join(b, NULL);
    printf("expected = %ld\nfinal    = %ld\n", 2L * ITERS, x);
    return 0;
}
