CC ?= gcc
CFLAGS = -std=c11 -Wall -Wextra -pthread
TSAN_FLAGS = $(CFLAGS) -fsanitize=thread -g -O1
BUILD = build

SRCS := $(wildcard *.c)
BINS := $(patsubst %.c,$(BUILD)/%,$(SRCS))

.PHONY: all clean run-% check

all: $(BINS)

$(BUILD)/%: %.c | $(BUILD)
	$(CC) $(TSAN_FLAGS) $< -o $@

$(BUILD):
	mkdir -p $@

run-%: $(BUILD)/%
	@echo "--- $< ---"
	@./$(BUILD)/$*

clean:
	rm -rf $(BUILD)

# Sanity: list the full compile command used.
check:
	@echo "$(CC) $(TSAN_FLAGS)"
