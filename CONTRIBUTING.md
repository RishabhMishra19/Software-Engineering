# Contributing

Contributions should make this repository easier to study, navigate, and verify. Keep each change focused, technically accurate, and self-contained.

## Canonical topic README template

Every topic README should use the following structure. Omit an optional section only when it genuinely does not apply; do not rename the remaining sections.

```markdown
# Topic Name

> One-sentence summary of the topic and why it matters.

## Overview

Define the topic, its scope, and the problem it solves.

## Core concepts

Explain the essential terminology, invariants, and building blocks.

## How it works

Describe the mechanism or flow step by step. Add a diagram when it improves understanding.

## Example

Provide a minimal, runnable or concrete example with assumptions and expected behavior.

## Trade-offs

Compare benefits, limitations, alternatives, and relevant complexity or operational costs.

## Common pitfalls

List frequent mistakes, misconceptions, and failure modes.

## When to use it

State appropriate and inappropriate use cases.

## Further reading

- [Descriptive source title](https://example.com)
```

Keep the introductory summary useful on its own. Prefer explanations and examples over collections of definitions.

## Case-study contract

A case study is an applied analysis, not a duplicate of topic documentation. Its README must:

1. Define the problem, scope, assumptions, functional requirements, and non-functional requirements.
2. Identify actors, core use cases, constraints, and explicit out-of-scope items.
3. Present the proposed architecture or object model, with responsibilities and boundaries.
4. Explain key flows, APIs or interfaces, data models, and persistence choices where relevant.
5. Analyze scale, consistency, availability, security, failure handling, and observability as applicable.
6. Record major decisions, alternatives considered, and trade-offs.
7. Include diagrams that clarify relationships or sequences, without using diagrams as a substitute for prose.
8. End with limitations, possible extensions, and references.

Any accompanying code must support the documented design, remain scoped to its case-study directory, and include clear instructions for running or validating it.

## Naming standards

- Use `README.md` for the entry page of every directory.
- Use descriptive kebab-case directory and asset names, except where an established product or technology name is clearer.
- Use Markdown filenames in kebab-case, such as `consistent-hashing.md`.
- Use language-standard naming inside source code.
- Avoid ambiguous names such as `notes.md`, `misc.md`, or `diagram-1.png`.

## Link standards

- Use relative links for files and directories in this repository.
- Link to the canonical local explanation instead of repeating it.
- Use descriptive link text; do not use “here” or expose raw URLs as labels.
- Check anchors after changing headings.
- Do not add links to missing local targets.
- Prefer stable, primary external sources over blogs that merely summarize them.

## Diagram standards

- Add a diagram only when it communicates structure, sequence, state, or data flow more clearly than prose.
- Prefer text-based, version-controlled formats such as Mermaid or PlantUML.
- Store shared binary or source assets under [`Assets/`](Assets/); keep case-specific assets inside the relevant case-study directory when they are not reused.
- Give every diagram a descriptive title or nearby explanation, and provide alt text for rendered images.
- Keep labels readable, use consistent notation, and explain uncommon symbols.
- Update diagrams in the same change as the behavior or architecture they describe.

## Reference standards

- Cite claims, specifications, algorithms, benchmarks, and borrowed diagrams that are not original.
- Prefer official documentation, standards, specifications, papers, and authoritative books.
- Include the source title and direct URL; add an access date for content expected to change.
- Clearly distinguish quotations and adapted material from original writing.
- Do not copy substantial text or imagery without permission.

## Provenance policy

Label migrated or restoration content with one of these terms:

- **Source-derived:** Content preserved or adapted from a source repository. Link to the source repository or file and identify substantial adaptation.
- **Editorial addition:** Content newly written for this repository rather than derived from a source. When relevant, state that the matching source stub was empty or that no source counterpart existed.
- **Professional correction:** A factual, security, compatibility, or quality correction to source-derived content. Retain the source attribution and briefly identify the material correction.

Use the narrowest accurate label, combine labels when a document contains materially different origins, and never present editorial additions or corrections as source-derived text.

## Writing standards

- Write in clear, concise English for a reader learning the subject.
- Define acronyms and specialized terms on first use.
- Use short sections, descriptive headings, and complete sentences.
- State assumptions and distinguish facts from recommendations.
- Explain why a design choice is made, not only what it is.
- Keep examples minimal and correct; never include credentials, personal data, or secrets.
- Avoid duplicate topic content. Link to the canonical topic page and focus on the context-specific application.

## Validation checklist

Before submitting a contribution, verify:

- [ ] The content follows the canonical topic template or case-study contract.
- [ ] Names and paths follow repository conventions.
- [ ] All local links and heading anchors resolve.
- [ ] External links are relevant, authoritative, and accessible.
- [ ] Diagrams render, remain legible, and include context or alt text.
- [ ] Code samples compile or run as documented.
- [ ] Generated files, build output, editor metadata, and Java `.class` files are absent.
- [ ] Technical claims, commands, and expected outputs have been checked.
- [ ] References and adapted material are attributed.
- [ ] The change does not duplicate an existing explanation.
- [ ] No secrets, private data, or confidential material are included.
- [ ] Spelling, grammar, and Markdown formatting have been reviewed.

## Copyright and licensing

Contributing does not mean that this repository grants an open-source license. **No open-source license is granted for this repository or its contents.** In the absence of a license, copyright law reserves the applicable rights to each copyright holder; viewing the repository does not imply permission to use, copy, modify, redistribute, or create derivative works.

Only submit material that you have the right to contribute. A contribution does not place third-party material under a new license or override its existing terms. Obtain explicit permission from the relevant rights holder before reusing repository content beyond rights provided by law.
