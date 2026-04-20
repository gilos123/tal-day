/*
 * 11-treiber.c — Lesson 11. Non-blocking LIFO stack using CAS on head.
 *
 * WARNING: This version *leaks* popped nodes. It deliberately does not
 * free them — freeing a node that another thread might still be
 * dereferencing is the ABA/reclamation problem. See lesson 15.
 *
 * Compile: make build/11-treiber
 * Run:     make run-11-treiber
 */
#include <stdatomic.h>
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

#define THREADS 4
#define PUSHES_PER_THREAD 50000

typedef struct node {
    int value;
    struct node *next;
} node_t;

static _Atomic(node_t *) head = NULL;

static void push(int v) {
    node_t *n = malloc(sizeof *n);
    n->value = v;
    node_t *old = atomic_load_explicit(&head, memory_order_relaxed);
    do {
        n->next = old;
    } while (!atomic_compare_exchange_weak_explicit(
        &head, &old, n, memory_order_release, memory_order_relaxed));
}

/* Returns 1 on success and writes to *out. Leaks the popped node. */
static int pop_leak(int *out) {
    node_t *old = atomic_load_explicit(&head, memory_order_acquire);
    while (old != NULL) {
        if (atomic_compare_exchange_weak_explicit(
                &head, &old, old->next, memory_order_acquire, memory_order_acquire)) {
            *out = old->value;
            /* intentionally not free(old) — see lesson 15 */
            return 1;
        }
    }
    return 0;
}

static void *producer(void *arg) {
    long tid = (long)arg;
    for (int i = 0; i < PUSHES_PER_THREAD; i++) {
        push((int)(tid * 1000000 + i));
    }
    return NULL;
}

int main(void) {
    pthread_t ts[THREADS];
    for (long i = 0; i < THREADS; i++) pthread_create(&ts[i], NULL, producer, (void *)i);
    for (int i = 0; i < THREADS; i++) pthread_join(ts[i], NULL);

    long count = 0;
    int v;
    while (pop_leak(&v)) count++;
    long expected = (long)THREADS * PUSHES_PER_THREAD;
    printf("popped   = %ld\nexpected = %ld\n", count, expected);
    return count == expected ? 0 : 1;
}
