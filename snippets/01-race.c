/*
 * 01-race.c — Lesson 1: two threads racing on a plain int.
 *
 * Compile:  make build/01-race
 * Run:      ./build/01-race  (or: make run-01-race)
 *
 * Under -fsanitize=thread you will see a WARNING: ThreadSanitizer: data race.
 * Even without TSAN, the final counter value is almost always less than
 * 2 * ITERS because x++ decomposes to load/add/store and the two threads
 * interleave those three instructions.
 */
#include <stdio.h>
#include <pthread.h>

#define ITERS 1000000

static long x = 0;

static void *worker(void *arg) {
    (void)arg;
    for (long i = 0; i < ITERS; i++) {
        x = x + 1;  /* not atomic, not ordered, not safe */
    }
    return NULL;
}

int main(void) {
    pthread_t a, b;
    pthread_create(&a, NULL, worker, NULL);
    pthread_create(&b, NULL, worker, NULL);
    pthread_join(a, NULL);
    pthread_join(b, NULL);
    printf("expected = %ld\n", 2L * ITERS);
    printf("final    = %ld\n", x);
    return 0;
}
