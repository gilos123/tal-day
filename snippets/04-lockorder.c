/*
 * 04-lockorder.c — Lesson 4.
 *
 * Same workload as 04-deadlock.c, but with address-ordered locking.
 * Each thread sorts the two mutex pointers by address and always
 * acquires the lower-addressed one first. No AB/BA cycle can form,
 * and the program runs cleanly forever (we bound it to 1M rounds).
 *
 * Compile: make build/04-lockorder
 * Run:     make run-04-lockorder
 */
#include <pthread.h>
#include <stdio.h>
#include <unistd.h>

static pthread_mutex_t a = PTHREAD_MUTEX_INITIALIZER;
static pthread_mutex_t b = PTHREAD_MUTEX_INITIALIZER;

static void lock_both(pthread_mutex_t *x, pthread_mutex_t *y) {
    /* lower address first — a canonical global order */
    pthread_mutex_t *lo = x < y ? x : y;
    pthread_mutex_t *hi = x < y ? y : x;
    pthread_mutex_lock(lo);
    pthread_mutex_lock(hi);
}

static void unlock_both(pthread_mutex_t *x, pthread_mutex_t *y) {
    /* unlock order is not load-bearing for deadlock avoidance */
    pthread_mutex_unlock(x);
    pthread_mutex_unlock(y);
}

#define ROUNDS 1000000

static void *worker(void *arg) {
    int reverse = (long)arg != 0;
    for (long i = 0; i < ROUNDS; i++) {
        if (reverse) lock_both(&b, &a);
        else         lock_both(&a, &b);
        unlock_both(&a, &b);
    }
    return NULL;
}

int main(void) {
    pthread_t x, y;
    pthread_create(&x, NULL, worker, (void *)0);
    pthread_create(&y, NULL, worker, (void *)1);
    pthread_join(x, NULL);
    pthread_join(y, NULL);
    printf("ok — %d rounds without deadlock\n", ROUNDS);
    return 0;
}
