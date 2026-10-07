#!/usr/bin/env python3
"""Curate morphy/allowed-forms.json using build/morphy-pending/*.json.

Usage:
  python3 scripts/curate-morphy.py [--pending-dir build/morphy-pending]
                                   [--allowed src/main/resources/morphy/allowed-forms.json]
                                   [--required src/main/resources/morphy/required-words.txt]

Pending files are written by MorphyService itself whenever it fails to
resolve a word (missing/ambiguous/nosuchform): each file holds the word,
the reason, the requested tags and all meanings with forms and tags.
After a word is curated (non-empty allowed entry), its pending file is
removed.

Commands per word: allow-meaning <id> | allow <id> <form> | remove <id> <form> | remove-meaning <id> | skip | quit
Stdlib only (no dependencies). Curses UI when a TTY is available,
plain prompt loop otherwise.
"""

import argparse
import copy
import json
import sys
from pathlib import Path


def load_json(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def load_required(path):
    words = []
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith("#"):
                words.append(line)
    return words


def describe_meaning(meaning):
    lines = []
    lines.append(
        "  meaning id=%s pos=%s (%d forms)"
        % (meaning.get("id"), meaning.get("partOfSpeech"), len(meaning.get("transformations", [])))
    )
    for tr in meaning.get("transformations", [])[:12]:
        lines.append("    [%s] -- %s" % (", ".join(tr.get("tags", [])), tr.get("form")))
    rest = len(meaning.get("transformations", [])) - 12
    if rest > 0:
        lines.append("    ... +%d more" % rest)
    return "\n".join(lines)


def meanings_of(entry):
    return entry.get("meanings") or {}


def summarize(entry):
    return {mid: len(forms) for mid, forms in meanings_of(entry).items()}


def apply_cmd(entry, meanings, cmd):
    """Apply a curation command to entry. Returns True if understood."""
    parts = cmd.split(None, 2)
    if not parts:
        return False
    table = meanings_of(entry)
    if parts[0] == "allow-meaning" and len(parts) == 2:
        try:
            mid = int(parts[1])
        except ValueError:
            print("id must be an integer")
            return True
        found = [m for m in meanings if m.get("id") == mid]
        if not found:
            print("no meaning with id %s in dump" % parts[1])
            return True
        table[str(mid)] = sorted({tr["form"] for tr in found[0].get("transformations", [])})
        entry["meanings"] = table
        return True
    if parts[0] == "remove-meaning" and len(parts) == 2:
        table.pop(parts[1], None)
        entry["meanings"] = table
        return True
    if parts[0] in ("allow", "remove") and len(parts) == 3:
        try:
            int(parts[1])
        except ValueError:
            print("id must be an integer")
            return True
        forms = list(table.get(parts[1], []))
        if parts[0] == "allow":
            if parts[2] not in forms:
                forms.append(parts[2])
        else:
            if parts[2] in forms:
                forms.remove(parts[2])
        table[parts[1]] = forms
        entry["meanings"] = table
        return True
    return False


def prompt_loop(words, dump_by_word, allowed):
    for word in words:
        entry = allowed.get(word, {"meanings": {}, "note": ""})
        meanings = dump_by_word.get(word)
        if meanings is None:
            print('normal form: "%s". No dump available, run MorphyDump first. [skip]' % word)
            continue
        print('normal form: "%s".' % word)
        print("meanings:")
        for i, m in enumerate(meanings, 1):
            print("%d) %s" % (i, describe_meaning(m).strip()))
        print("currently allowed: %s" % summarize(entry))
        while True:
            try:
                cmd = input("[%s] allow-meaning <id> | allow <id> <form> | remove <id> <form> | remove-meaning <id> | skip | quit > " % word).strip()
            except EOFError:
                return False
            if cmd == "quit":
                return False
            if cmd == "skip" or cmd == "":
                break
            if apply_cmd(entry, meanings, cmd):
                allowed[word] = entry
                print("currently allowed: %s" % summarize(entry))
            else:
                print("unknown command")
    return True


def curses_ui(stdscr, words, dump_by_word, allowed):
    import curses

    idx = 0
    while 0 <= idx < len(words):
        word = words[idx]
        entry = allowed.get(word, {"meanings": {}, "note": ""})
        meanings = dump_by_word.get(word, [])
        stdscr.clear()
        stdscr.addstr(0, 0, 'normal form: "%s" (%d/%d)' % (word, idx + 1, len(words)))
        stdscr.addstr(1, 0, "meanings:")
        row = 2
        for m in meanings:
            stdscr.addstr(row, 0, "  id=%s pos=%s" % (m.get("id"), m.get("partOfSpeech")))
            row += 1
            for tr in m.get("transformations", [])[:8]:
                stdscr.addstr(row, 2, "[%s] -- %s" % (", ".join(tr.get("tags", [])), tr.get("form"))[:100])
                row += 1
        row += 1
        stdscr.addstr(row, 0, "allowed: %s" % summarize(entry))
        row += 1
        stdscr.addstr(row, 0, "[M]eaning id  [a] <id> <form>  [r] <id> <form>  [x] del meaning  [n]ext  [p]rev  [q]uit")
        stdscr.refresh()
        curses.echo()
        try:
            cmd = stdscr.getstr(row + 1, 0, 80).decode().strip()
        finally:
            curses.noecho()
        if cmd == "q":
            break
        if cmd == "n" or cmd == "":
            idx += 1
        elif cmd == "p":
            idx -= 1
        else:
            parts = cmd.split(None, 2)
            single = cmd.split(None, 1)
            if len(single) == 2 and single[0] == "M":
                if apply_cmd(entry, meanings, "allow-meaning " + single[1]):
                    allowed[word] = entry
            elif len(parts) == 3 and parts[0] in ("a", "r"):
                long_cmd = ("allow " if parts[0] == "a" else "remove ") + parts[1] + " " + parts[2]
                if apply_cmd(entry, meanings, long_cmd):
                    allowed[word] = entry
            elif len(single) == 2 and single[0] == "x":
                if apply_cmd(entry, meanings, "remove-meaning " + single[1]):
                    allowed[word] = entry


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--pending-dir", default="build/morphy-pending")
    parser.add_argument("--allowed", default="src/main/resources/morphy/allowed-forms.json")
    parser.add_argument("--required", default="src/main/resources/morphy/required-words.txt")
    parser.add_argument("--keep-pending", action="store_true",
                        help="do not delete pending files after curation")
    args = parser.parse_args()

    allowed_path = Path(args.allowed)
    allowed = load_json(allowed_path)
    required = load_required(args.required)
    pending_dir = Path(args.pending_dir)
    dump_by_word = {}
    pending_files = {}
    if pending_dir.is_dir():
        for child in sorted(pending_dir.glob("*.json")):
            try:
                item = load_json(child)
            except (json.JSONDecodeError, UnicodeDecodeError) as e:
                print("Skipping unreadable pending file %s: %s" % (child, e))
                continue
            word = item.get("normalForm")
            if not word:
                continue
            dump_by_word[word] = item.get("meanings", [])
            pending_files[word] = child
    if not dump_by_word:
        print("No pending words in %s: nothing failed to resolve yet." % pending_dir)
        return 1

    def is_pending(word):
        table = (allowed.get(word, {}).get("meanings") or {})
        return not any(forms for forms in table.values())

    pending = [w for w in required if w in dump_by_word and is_pending(w)]
    pending += [w for w in dump_by_word if w not in required and is_pending(w)]
    if not pending:
        print("Nothing to curate: every failed word already has allowed meanings.")
        return 0
    print("Pending words: %s" % pending)

    use_curses = sys.stdin.isatty()
    if use_curses:
        try:
            import curses

            curses.wrapper(curses_ui, pending, dump_by_word, allowed)
        except Exception as e:  # noqa: BLE001 - fall back to prompts
            print("curses unavailable (%s), using prompt loop" % e)
            prompt_loop(pending, dump_by_word, allowed)
    else:
        prompt_loop(pending, dump_by_word, allowed)

    backup = allowed_path.with_suffix(".json.bak")
    backup.write_text(json.dumps(allowed, ensure_ascii=False, indent=2), encoding="utf-8")
    allowed_path.write_text(json.dumps(allowed, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("written: %s (backup: %s)" % (allowed_path, backup))
    if not args.keep_pending:
        for word in pending:
            table = (allowed.get(word, {}).get("meanings") or {})
            if any(forms for forms in table.values()) and word in pending_files:
                try:
                    pending_files[word].unlink()
                    print("pending resolved, removed: %s" % pending_files[word])
                except OSError as e:
                    print("cannot remove %s: %s" % (pending_files[word], e))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
