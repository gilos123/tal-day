/*
 * 02-tls.c — Lesson 2.
 *
 * Thread-local storage via C11's _Thread_local (same as gcc __thread).
 * Each thread gets its own copy of `counter`. The print at the end of
 * each thread is deterministic per thread, with no races.
 *
 * Compile: make build/02-tls
 * Run:     make run-02-tls
 */
#include <pthread.h>
#include <stdio.h>

#define N 3
#define ITERS 100000

static _Thread_local long counter = 0;

static void *worker(void *arg) {
    long id = (long)arg;
    for (int i = 0; i < ITERS; i++) counter++;
    printf("[worker %ld] counter = %ld\n", id, counter);
    return NULL;
}

int main(void) {
    pthread_t ts[N];
    for (long i = 0; i < N; i++) pthread_create(&ts[i], NULL, worker, (void *)i);
    for (int i = 0; i < N; i++) pthread_join(ts[i], NULL);
    printf("main thread: counter = %ld   (always 0 — main's TLS is untouched)\n", counter);
    return 0;
}
