# Assets

This directory contains diagrams and media reused across multiple topics or case studies. Keep one-off assets beside the content that owns them.

Use descriptive kebab-case filenames, prefer editable text-based diagram sources, optimize binary files, and include attribution when required. Reference assets with relative paths and provide meaningful alt text. See the repository's [diagram](../CONTRIBUTING.md#diagram-standards) and [reference](../CONTRIBUTING.md#reference-standards) standards before adding files.

## Beginner guide

- **Everyday mental model:** An asset is a supporting file—such as a diagram or image—that helps explain the handbook. This directory is the shared media shelf; a file used by only one topic stays beside that topic.
- **Deeper mechanism:** A relative path points from a document to an asset without depending on one person's computer. Alternative text, usually shortened to alt text, describes meaningful visual content for people using screen readers or when an image cannot load. Kebab-case writes lowercase filename words separated by hyphens, such as `request-flow.svg`.
- **Example:** A diagram reused by frontend and backend chapters belongs here as a Scalable Vector Graphics (SVG) file and can be referenced as `../Assets/request-flow.svg`. A screenshot used by one case study belongs in that case study's directory.
- **Edge cases:** Renaming an asset can break every document that links to it, unclear alt text can exclude readers, an uncredited image can violate its license, and a large binary file can make the repository slow to download.
- **Production trade-offs:** Editable text-based sources are easier to review and update, while binary formats may render more consistently. Shared assets reduce duplication, but changes can affect many documents and therefore need link validation.

### Key-point interview answer

**How should documentation assets be managed?** Keep reusable media in one shared location, keep one-off media with its owner, use stable descriptive filenames and relative links, preserve editable sources and attribution, optimize file size, and provide meaningful alt text.
