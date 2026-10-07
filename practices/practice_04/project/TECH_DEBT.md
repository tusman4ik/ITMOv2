# Technical debt

Conventions: each item has an ID, priority, and done criteria.
Priority: P0 — wiring correctness blocker, P1 — structural rule, P2 — cleanup, P3 — low / non-v1.

## TD-1 (P0): Exactly one `@*NodeDef` per node field

Problem: a `Node` field may end up with zero or two+ definition annotations
(`@NodeDef`, `@PickNodeDef`, `@PickBoolNodeDef`, `@PickIntNodeDef`, `@RetryNodeDef`),
failing late in different handlers.

Scope:
- A "node" is a field of type `Node` carrying a definition annotation.
  Fields without annotations are not nodes. `static` fields are not nodes.
- `@RetryNodeDef` is an ordinary node annotation, not an exception:
  `@NodeDef + @RetryNodeDef` on one field is a violation.

Proposal: new invariant handler, first in `WireConfig.chain()`,
scanning `Fields.allOf(generator)` including the hierarchy:
fail fast with `prototypeId + field + annotations` on count != 1.

Done when: handler runs before all others; positive/negative tests
(0 / 1 / 2 annotations, including the `@NodeDef + @RetryNodeDef` case);
error message contains `prototypeId`, field name, and annotation names.

## TD-2 (P1): Forbid field shadowing in generator hierarchy

Problem: a descendant field with the same name as a parent field ("hiding")
has undefined semantics for node collection (`Fields.allOf` walks the hierarchy).

Rule: same field name in more than one class of the hierarchy is an error.
Different names with overlapping meaning are out of scope and not checked.

Proposal: check in the first invariant handler (together with TD-1), fail fast
with `prototypeId + field name + both classes`.

Done when: `parent.left` + `child.left` fails at startup with a clear message;
covered by a test.

## TD-3 (P1): `@Register` classes must be final

Problem: generator inheritance is not supported. Non-final `@Register` classes
invite subclassing and shadowing (see TD-2) and complicate proxy handling.
Without `@Register`, parent generators are not scanned by `GeneratorRegistry`
anyway; finality makes this explicit.

Rule: a `@Register` class must be `final`. No new marker annotation.
No AOP annotations (`@Transactional`, `@Cacheable`, `@Async`, etc.) on generators,
so no Spring proxies are expected; `AopUtils.getTargetClass()` must resolve
to the class itself.

Proposal: fail-fast check at startup (invariant handler or `GeneratorRegistry.init`):
`!Modifier.isFinal(class)` -> throw with the class name.

Done when: non-final `@Register` class fails at startup with
`prototype class must be final: X`; covered by a test.

## TD-4 (P2): Remove `RawType`, use plain `Node`

Problem: `RawType<T> extends Node<T>` is a method-less marker duplicating
the `Template.raw(...) / rawNodes()` mechanism, forcing answer nodes to
implement an empty interface.

Context: `task/ui/RawType.java`, `task/ui/GeneratedTask`, `task/templates/Template`.

Proposal: single source of truth in `Template`:
- `Template.raw(...)` accepts ordinary `Node<?>`;
- `GeneratedTask` holds `List<Node<?>>` (answer nodes = those listed in `Template.raw()`);
- deprecate then delete `RawType.java`, remove `instanceof RawType` usages.

Done when: no `RawType.java`, no `instanceof RawType`;
`Generator_4Test`, `GraphCheckTest`, `MutatedTest` are green on `Node<?>`.

## TD-5 (P3): Cache Mustache compilation

Problem: `MustacheRenderEngine` compiles `statement.mustache` on every `render`.
Functionally correct (the `Loader` cache saves I/O), but wasteful.

Proposal: cache `template -> compiled` in the render engine. No behavior change.

Done when: statement text compilation happens once per template;
render output unchanged. Not required for v1.
