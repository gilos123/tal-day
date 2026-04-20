# tal-day

Monorepo with two independent projects:

| Path | Project | Stack |
|---|---|---|
| `/` (root) | **Concurrency in C** — code-first technical reference static site | Next.js 15 · React 19 · TypeScript · MDX · Tailwind |
| `/android/` | **Study tracker** — daily schedule, per-topic background timers, hierarchical notes, stats dashboard | Kotlin · Jetpack Compose · Room · Hilt · Foreground Service · Vico charts |

The two projects share no build configuration — they're independent and just happen to live in the same Git repo for convenience.

---

## Concurrency in C (root)

Code-first technical reference for concurrency in C — machine model through lock-free data structures. Lesson content authored in MDX, built as a static site.

```sh
npm install
npm run search-index   # builds public/search.json
npm run dev            # http://localhost:3000

# production
npm run build
npm start
```

Runnable C snippets live in `snippets/`:

```sh
cd snippets
make all               # compiles every .c with -std=c11 -Wall -Wextra -pthread -fsanitize=thread
make run-01-race       # data-race demo
make run-11-treiber    # Treiber stack stress test
```

Daily checkbox reset (Israel timezone): see `lib/daily-checks.ts`. The `MarkComplete` button on each lesson page visually resets at midnight **Asia/Jerusalem** while lifetime completion (`lib/progress.ts`) and the Progress Dashboard are untouched.

## Study Tracker (android/)

See [`android/README.md`](android/README.md). Build with Android Studio or:

```sh
cd android
./gradlew assembleDebug
./gradlew installDebug
```
