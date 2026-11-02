# TD-01 (p3): openApi drift protection — DONE

**Was:** `ModelResolver` bean deriving `discriminator.mapping` from `@JsonSubTypes`
(fragile converter ordering, silent failure mode).
**Now:** `PolymorphicCustomizer` derives `discriminator.mapping` + `oneOf` from
`@JsonSubTypes` via the `JacksonPolymorphism` util; bases are pure unions,
subtypes are standalone with `type` enum. Covered by
`common.openapi.JacksonPolymorphismTest` + `PolymorphicCustomizerTest`.

# TD-01 (p1): rewrite old tech debt

**Scope:** only this file.

**Problem:** old tech debt was written by llm, now author can't read it easily.

**Proposal:** rewrite tech debt by author this code himself