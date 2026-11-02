---
name: add-prototype
description: Use when adding a new prototype (one kind of OGE task) to spc-task-service. A strict workflow - study the reference tasks of the whole task number, define and get approved the task's parameters, design and get approved the generation logic, then implement and validate 10 generated examples through the API. Not for changing an existing prototype's behavior and not for DSL or framework changes.
compatibility: opencode
---

# Add a prototype

A prototype is a family of similar tasks: each `seed` yields a different instance of the same kind of task.
It has one generator, one template, one `InputForm` and one checker.

The work has four phases and **two approval gates**. At a gate, present your result in the reply and stop. Do not write
code and do not start the next phase until the developer approves. Questions to the developer go in one batch, not drip
by drip.

Before anything else, confirm the identity `(exam, subject, number, prototypeIdx)` and that this id is unused.
Never reuse or renumber an existing pair(`prototypeIdx` + `number`) .

## Phase 1: study the references

1. Read `docs/agentic/dsl.md`. It lists what the DSL can do and the formatting rules for generators. Follow it over this
   file.
2. Read the reference tasks for this prototype in `docs/agentic/references/NN-PP/`:
    - `statements/0.md`, `1.md`, ...: example statements
    - `attachments/<i>/...`: files belonging to `statements/<i>.md` (e.g. Excel files)
3. Read the references of **every prototype of the same task number** (`references/NN-*`), not only the one you are
   adding.
   Most prototypes of one number share models and algorithms: find them, and reuse existing utility and model classes
   instead of writing parallel ones.

If the references for the target prototype are missing, stop and ask. Do not guess the task from memory.

## Phase 2: define the parameters (then GATE 1)

This phase fixes the **shape** of the final task, not how to generate it. You do not need to know yet how a thing is
built.
Example: "a sparse graph with edge density between 0.5 and 0.7" is a valid statement at this stage.

1. Find everything that varies between the example statements, and state the **invariants**: what must hold in every
   instance.
2. Classify every parameter:
    - **decorative**: words that do not change the task logic (names, sets of rivers, cities, objects). Varying this is
      expected and desirable: Ob, Lena are rivers; Omsk, Tomsk are cities. Propose a set rather than a single value,
      e.g. `personName`, `"Обь, Лена -- реки" <==> "Москва, Томск -- города"` (3 different connected with each other
      parameters)
    - **leaf**: independent of other parameters, e.g. random int or city name from resources
    - **complex**: depends on other parameters.
3. Find the **raw parameters**: values that go to the frontend unrendered, which only the frontend can render. Usually it is about tables, graphs, attachment.
4. Define the user's answer: the `InputSchema` and the `InputForm` (see `docs/agentic/forms/` and
   `docs/agentic/schemas/`).
5. Ask the developer for the **range of every non-decorative parameter**. Be concrete: an integer or floating? Can it be 0?
   Negative? How large? Which combinations are forbidden?
6. Statements are in Russian and Morphy is not used in the MVP. A decorative set must fit the sentence grammar for every
   member (same gender, number, case), or the needed forms must be stored explicitly. Check that.

Present a table, then stop for approval:

| parameter | kind | depends on | domain / range | notes (invariants, raw, decorative set) |
|-----------|------|------------|----------------|-----------------------------------------|

Follow with the answer schema, the form, and the open questions.

## Phase 3: design the generation (then GATE 2)

For each **non-trivial** parameter (complex ones and anything not a plain random pick), produce several candidate
approaches
and compare them: how it is built, strengths, weaknesses, risks (can it dead-end, is it uniform enough, is it too easy
or
too hard, can the answer be ambiguous?). Recommend one and say why. Examples of how to think about generation are in
`docs/agentic/generation.md` (`TODO(owner)`: confirm this name).

Also define: how the reference answer is computed, and how the checker scores (criteria).
State which logic goes into utility or model classes (shared with other prototypes where possible).

Present it and stop for approval.

## Phase 4: implement and validate

Implement exactly what was approved. If you find a problem with the plan, stop and say so. Do not quietly deviate.

- Class `Generator_<number>_<prototype>`, `@Register(exam, subject, number, prototype)`, extends `PrototypeGenerator`.
  No shared base class per task number. Follow `dsl.md` for the declaration style.
- The generator is a declarative shell: parameter nodes and wiring. Algorithms, predicates, solvers and models go in
  utility classes. English identifiers and comments, Lombok where useful, `@NonNull` for nullability.
- It also defines the `InputForm` factory and the checker factory. The checker produces the reference answer.
- One template per prototype: `statement.mustache` (existing `spc-res/` files are leftovers in an old layout; the target
  is
  `NN-PP/statement.mustache`; ask if unsure).
- Determinism: all randomness comes from the seeded generation `Random`. No clock, IO, `Math.random`, own `Random`.
  If iteration order affects a result, use an ordered collection. The generator is a long-lived shared bean: no
  per-generation state in its fields.
- A state with no valid solution is a bug, guarded by `InvariantFailedException` (the class does not exist yet: ask
  before
  creating it). Never catch it. Avoid `RetryNodeDef`; it is a legacy workaround.
- Add unit tests for non-trivial utility or model logic. Do not write many-seed tests and do not touch the fingerprint
  golden file (the fingerprint module owns that; a new prototype needs a new entry, so tell the developer).

### Validate through the API

1. Run `./gradlew test`, then start the app (`docs/agentic/running.md`).
2. Through the API, generate **10 random instances** of the new prototype.
3. For each one, validate it **by hand**: read the statement, solve it independently (by reasoning, or a throwaway
   script
   outside the repo), and compare with the reference answer. Check that the invariants hold, the decorative words read
   correctly, the form matches the answer, and the checker accepts the correct answer.
4. Report a table of the 10 cases (seed, key parameter values, your answer, reference answer, verdict). If anything is
   wrong,
   fix it and validate again with fresh examples.

## Report

State the new `PrototypeId`, files created, assumptions, and anything you stopped on. Do not commit unless asked.