/*
 * 07-relaxed-vs-acquire.c — Lesson 7.
 *
 * Reads the RELAXED env var. If set, uses memory_order_relaxed for the
 * flag, breaking the publication pattern; under a weak-memory CPU this
 * can trip the assert. Under seq_cst (default) or release/acquire it
 * never can.
 *
 * Compile: make build/07-relaxed-vs-acquire
 * Run:     make run-07-relaxed-vs-acquire
 *          RELAXED=1 ./build/07-relaxed-vs-acquire
 */
#include <assert.h>
#include <stdatomic.h>
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

static int data = 0;
static atomic_int ready = 0;

static memory_order STORE_ORD;
static memory_order LOAD_ORD;

static void *producer(void *arg) {
    (void)arg;
    data = 42;
    atomic_store_explicit(&ready, 1, STORE_ORD);
    return NULL;
}

static void *consumer(void *arg) {
    (void)arg;
    while (atomic_load_explicit(&ready, LOAD_ORD) == 0) { /* spin */ }
    /* With relaxed orderings, the compiler and CPU may let `data`'s
     * write appear after the flag set, or let the consumer's read of
     * `data` speculate past the flag check. Either breaks the invariant. */
    assert(data == 42);
    return NULL;
}

int main(void) {
    int relaxed = getenv("RELAXED") != NULL;
    STORE_ORD = relaxed ? memory_order_relaxed : memory_order_release;
    LOAD_ORD = relaxed ? memory_order_relaxed : memory_order_acquire;
    printf("ordering: %s\n", relaxed ? "relaxed (UNSAFE)" : "release/acquire");

    pthread_t p, c;
    pthread_create(&p, NULL, producer, NULL);
    pthread_create(&c, NULL, consumer, NULL);
    pthread_join(p, NULL);
    pthread_join(c, NULL);
    puts("ok");
    return 0;
}
