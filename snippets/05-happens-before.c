/*
 * 05-happens-before.c — Lesson 5.
 *
 * Demonstrates a release-store / acquire-load establishing a
 * happens-before edge from the producer's write of `data` to the
 * consumer's read of `data`, with no mutex.
 *
 * Compile: make build/05-happens-before
 * Run:     make run-05-happens-before
 *
 * Correctness argument:
 *   - producer: `data = 42` is sequenced-before `atomic_store_explicit(&ready, 1, release)`.
 *   - consumer: `atomic_load_explicit(&ready, acquire)` observing 1 synchronizes-with
 *     the release store above.
 *   - therefore `data = 42` happens-before `assert(data == 42)` in the consumer.
 *   - the assertion can never fire under a conforming C11 implementation.
 */
#include <assert.h>
#include <stdatomic.h>
#include <pthread.h>
#include <stdio.h>

static int data = 0;
static atomic_int ready = 0;

static void *producer(void *arg) {
    (void)arg;
    data = 42;                                              /* plain write */
    atomic_store_explicit(&ready, 1, memory_order_release); /* release */
    return NULL;
}

static void *consumer(void *arg) {
    (void)arg;
    while (atomic_load_explicit(&ready, memory_order_acquire) == 0) {
        /* spin */
    }
    assert(data == 42);
    printf("consumer observed data = %d\n", data);
    return NULL;
}

int main(void) {
    pthread_t p, c;
    pthread_create(&p, NULL, producer, NULL);
    pthread_create(&c, NULL, consumer, NULL);
    pthread_join(p, NULL);
    pthread_join(c, NULL);
    return 0;
}
