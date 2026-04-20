/*
 * 12-aba.c — Lesson 12. Demonstrate ABA on a Treiber stack.
 *
 * The scenario this snippet orchestrates (via deliberate delays):
 *   T1: pop starts: snapshots head = A, reads A->next = B; sleeps before CAS.
 *   T2: pops A, pops B, pushes A back — head == A again, but the stack is now { A }.
 *   T1: resumes, CAS(head, A, B) succeeds, but B has been freed and may point anywhere.
 *
 * We don't actually crash (freeing and re-allocating can be benign by luck);
 * instead we detect the bug by noticing that head became a node whose `next`
 * differs from the B we snapshotted, and print a failure.
 *
 * Compile: make build/12-aba
 * Run:     make run-12-aba
 */
#include <stdatomic.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <time.h>

typedef struct node {
    int value;
    struct node *next;
} node_t;

static _Atomic(node_t *) head;
static atomic_int detected = 0;

/* Tiny portable replacement for pthread_barrier_wait(n=2).
 * macOS's libc doesn't ship pthread_barrier_*. */
static atomic_int gate = 0;
static void rendezvous(void) {
    atomic_fetch_add(&gate, 1);
    while (atomic_load_explicit(&gate, memory_order_acquire) < 2) { /* spin */ }
}

static void push(node_t *n) {
    node_t *old = atomic_load_explicit(&head, memory_order_relaxed);
    do {
        n->next = old;
    } while (!atomic_compare_exchange_weak_explicit(
        &head, &old, n, memory_order_release, memory_order_relaxed));
}

static node_t *pop_raw(void) {
    node_t *old = atomic_load_explicit(&head, memory_order_acquire);
    while (old != NULL) {
        if (atomic_compare_exchange_weak_explicit(
                &head, &old, old->next, memory_order_acquire, memory_order_acquire)) {
            return old;
        }
    }
    return NULL;
}

static void sleep_ms(int ms) {
    struct timespec ts = { 0, ms * 1000000L };
    nanosleep(&ts, NULL);
}

/* Thread 1: the victim. Holds an old snapshot across a sleep to let T2 race. */
static void *victim(void *arg) {
    (void)arg;
    rendezvous();

    node_t *snap_head = atomic_load_explicit(&head, memory_order_acquire);
    node_t *snap_next = snap_head->next;
    int snap_head_val = snap_head->value;
    int snap_next_val = snap_next->value;

    sleep_ms(80);  /* give T2 time to pop A+B and push A' */

    /* This CAS succeeds if head == snap_head (pointer compare).
     * But after ABA, snap_next may be dangling. */
    if (atomic_compare_exchange_strong_explicit(
            &head, &snap_head, snap_next, memory_order_acq_rel, memory_order_acquire)) {
        /* Were we tricked? The "new head" now is snap_next, which T2 freed. */
        node_t *now = atomic_load_explicit(&head, memory_order_acquire);
        /* Heuristic: if the now-head's value differs from what we expected,
         * or if the memory got reused for a fresh value. */
        if (now && now->value != snap_next_val) {
            atomic_store_explicit(&detected, 1, memory_order_relaxed);
            printf("ABA: CAS saw head == A again, but snap_next (%d) is garbage; "
                   "after CAS head is now node whose value = %d\n",
                   snap_next_val, now->value);
        } else if (now == NULL) {
            atomic_store_explicit(&detected, 1, memory_order_relaxed);
            printf("ABA: CAS succeeded but installed a freed pointer; "
                   "stack corrupted (head now NULL even though original A is still live)\n");
        } else {
            printf("[victim] CAS succeeded; snap_head_val=%d, now->value=%d (no detectable ABA this run)\n",
                   snap_head_val, now->value);
        }
    } else {
        printf("[victim] CAS failed (ABA not observed this run)\n");
    }
    return NULL;
}

/* Thread 2: pops A and B, frees B, then re-pushes A so head == A again. */
static void *attacker(void *arg) {
    (void)arg;
    rendezvous();
    sleep_ms(20);

    node_t *a = pop_raw();
    node_t *b = pop_raw();
    if (!a || !b) { fprintf(stderr, "attacker: unexpected empty stack\n"); exit(2); }

    free(b);  /* the dangling pointer victim still holds */

    /* Reuse a's allocation address with a new value — push it back. */
    push(a);
    return NULL;
}

int main(void) {
    /* Seed stack with [A, B, C]. */
    node_t *c = malloc(sizeof *c); c->value = 3; c->next = NULL;
    node_t *b = malloc(sizeof *b); b->value = 2; b->next = c;
    node_t *a = malloc(sizeof *a); a->value = 1; a->next = b;
    atomic_store(&head, a);

    pthread_t t1, t2;
    pthread_create(&t1, NULL, victim, NULL);
    pthread_create(&t2, NULL, attacker, NULL);
    pthread_join(t1, NULL);
    pthread_join(t2, NULL);
    return atomic_load(&detected) ? 0 : 0; /* always 0; detection is best-effort */
}
