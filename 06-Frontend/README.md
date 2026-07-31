# Frontend

> **Provenance:** Editorial addition.

## Overview

Frontend engineering builds the part of a system that users see and interact with. It runs primarily in web browsers or native web views.

The work combines semantic user interfaces (UIs), state and data flow, networking, performance, accessibility, security, testing, and deployment. A frontend must continue to behave predictably across devices and unreliable network conditions.

## Beginner mental models

### Browser

- **Everyday mental model:** A browser is a reader, translator, and safety guard. It requests a page, translates the page's files into pixels and interactive controls, and limits what the page may do on a device.
- **Deeper mechanism:** The browser sends Hypertext Transfer Protocol (HTTP) requests, parses Hypertext Markup Language (HTML) into a document tree, applies Cascading Style Sheets (CSS), runs JavaScript, and coordinates networking, storage, input, and painting. Different tasks may run in separate processes for stability and security.
- **Example:** Opening a shop page makes the browser request HTML, styling, scripts, fonts, images, and product data, then combine them into one interactive page.
- **Edge cases:** A slow connection, blocked script, old browser, disabled JavaScript, small screen, keyboard-only navigation, or unavailable server can reveal assumptions that were invisible on a developer's machine.
- **Production trade-offs:** More browser-side work can create rich interaction and reduce server work after startup, but it increases download size, device processing, battery use, security exposure, and variation across browsers.

### Rendering

- **Everyday mental model:** Rendering is turning a recipe into the page a person can see and use.
- **Deeper mechanism:** The browser combines the document structure and styles, calculates each element's size and position, paints pixels, and composites visual layers. JavaScript can change the document and trigger some of this work again.
- **Example:** A news article can arrive already represented in HTML from the server, be generated ahead of time, or be assembled by JavaScript after the browser downloads data.
- **Edge cases:** Server and browser output can disagree during hydration, fonts can move text after loading, and repeated layout calculations can make scrolling or typing feel slow. Hydration is the step that attaches browser behavior to server-produced HTML.
- **Production trade-offs:** Server rendering improves the first view and search discovery but costs server capacity. Static generation is fast and cacheable but can become stale. Client rendering supports interaction but delays useful content on slow devices.

### State

- **Everyday mental model:** State is the application's memory of what is true right now.
- **Deeper mechanism:** Local state belongs to one interface session, while server state is a remote system's current record. Updates cause affected views to render again; remote state also needs request, freshness, error, and conflict rules.
- **Example:** Whether a cart drawer is open is local state. The cart's saved items and prices are server state.
- **Edge cases:** Two tabs may disagree, a late response may overwrite a newer one, an optimistic update may fail, and offline edits may conflict after reconnection. An optimistic update shows the expected result before the server confirms it.
- **Production trade-offs:** Centralizing state can simplify sharing but creates coupling and broad updates. Keeping state close to its owner reduces complexity but may require deliberate synchronization.

### Cache

- **Everyday mental model:** A cache is a nearby copy kept to avoid fetching or calculating the same thing again.
- **Deeper mechanism:** Browsers, applications, and network intermediaries store responses under keys for a defined lifetime. Validation checks whether a stored copy is still current; invalidation removes or replaces a copy when its source changes.
- **Example:** A versioned logo can be cached for a year, while an account balance may be reused briefly and then checked with the server.
- **Edge cases:** A cache can serve stale or private data, different users can receive the wrong variant, and old HTML can point to assets removed by a new release.
- **Production trade-offs:** Longer caching improves speed, cost, and resilience but increases staleness risk. Shorter caching improves freshness but adds network traffic and dependency on origin servers.

### Content delivery network

- **Everyday mental model:** A content delivery network (CDN) is a group of nearby pickup points that keep copies of content so every customer does not travel to one distant warehouse.
- **Deeper mechanism:** Geographically distributed edge servers answer requests from cached copies and contact the origin server when content is missing or expired. Cache keys, request routing, and invalidation rules decide which copy is served.
- **Example:** A user in India can download a product image from a nearby edge location instead of the application's main server in another region.
- **Edge cases:** Incorrect cache keys can mix languages or user-specific responses, invalidation can take time, and an unavailable origin still matters when the edge lacks a requested object.
- **Production trade-offs:** A CDN lowers latency, origin traffic, and some denial-of-service risk, but adds cost, configuration complexity, another failure boundary, and careful privacy requirements.

## Why do we need it?

Users experience product quality through responsiveness, correctness, accessibility, and resilience. A sound frontend architecture lets teams evolve features without shipping excessive JavaScript or duplicating server state. It also helps teams avoid exposing sensitive data or coupling every screen to the structure of backend services.

## How does it work?

Choose and operate a frontend step by step:

1. Choose a rendering method for each route. Server-side rendering (SSR) returns ready-to-display Hypertext Markup Language (HTML) for a fast initial view and search discoverability. Static generation creates cacheable HTML at build time. Client-side rendering creates the view in the browser after JavaScript loads. Hybrid applications combine these methods.
2. Separate local UI state, such as whether a menu is open, from server state, such as an account balance.
3. Fetch server data through a typed boundary. Cache it with explicit freshness and invalidation rules, and cancel requests that are no longer relevant.
4. Represent every important state: loading, empty, error, stale, and offline. A backend-for-frontend (BFF) can adapt multiple services for one client, but business rules should remain in the domain services.
5. Deliver versioned, immutable assets through a content delivery network (CDN). Apply Hypertext Transfer Protocol (HTTP) [caching](../03-System-Design/Caching/README.md), code splitting, correct image sizing, and selective preloading.
6. Measure real-user **Core Web Vitals**, a small set of browser metrics for
   loading speed, responsiveness, and visual stability. These include
   **Largest Contentful Paint (LCP)**, which measures when the largest visible
   element appears, and **Interaction to Next Paint (INP)**, which measures how
   quickly the page responds to user actions. Also measure errors and task
   duration instead of relying on bundle size alone.
7. Build with semantic HTML, keyboard access, visible focus, labels, sufficient contrast, and testing with assistive technology.

**Production failure modes and practices**

- A hydration mismatch occurs when browser-rendered output differs from server-rendered HTML. It can cause flicker or broken controls, so keep server and client rendering deterministic.
- Stale responses overwrite newer state; cancel or sequence requests.
- A large bundle blocks low-end devices; enforce route budgets and inspect third-party code.
- Cached HTML references removed assets; use immutable hashed assets and compatible rollout/rollback.
- Cross-site scripting (XSS) can steal sessions or data. Escape output, avoid unsafe Document Object Model (DOM) APIs, use a Content Security Policy (CSP), and keep secrets off the client.
- Common mistakes: duplicating server state globally, index keys in mutable lists, inaccessible custom controls, optimistic updates without rollback, and logging personal data.
- Test behavior at unit/component level, critical flows end-to-end, accessibility automatically and manually, and performance with real-user monitoring.

## Advantages

- Immediate interaction and rich user experiences.
- Client caching and optimistic UI can mask network latency.
- Component boundaries support reuse and parallel development.
- Progressive enhancement can keep core journeys robust.

## Limitations

- Browsers, devices, networks, and assistive technologies vary widely.
- Client code and data are observable and untrusted.
- State synchronization and partial failures are complex.
- More JavaScript increases startup, memory, and maintenance costs.

## Real-world examples

- An e-commerce product page uses server rendering, CDN-cached assets, client-side cart interaction, and resilient checkout errors.
- A dashboard streams updates but preserves the last known data and indicates staleness during disconnects.
- An offline-capable form stores a draft locally and requires explicit conflict handling on reconnect.

## Interview Questions

1. **SSR, static generation, and client rendering: how do you choose?**
   - Use SSR when the first response must contain current, discoverable content.
   - Use static generation when content can be built ahead of time and cached widely.
   - Use client rendering for highly interactive views after startup.
   - Choose per route, based on freshness, discoverability, interaction, infrastructure cost, and failure behavior.
2. **Local state versus server state?**
   - Local state belongs to the current interface, such as an open dialog or draft input.
   - Server state comes from a remote system, can become stale, and needs fetching, caching, invalidation, and error handling.
   - Keep the two separate so remote synchronization rules do not spread through UI code.
3. **How would you diagnose poor Largest Contentful Paint or Interaction to Next Paint?**
   - Largest Contentful Paint (LCP) measures when the largest visible content element finishes rendering. Inspect server response time, render-blocking resources, image delivery, fonts, and preload priority.
   - Interaction to Next Paint (INP) measures interaction responsiveness. Inspect long main-thread tasks, expensive event handlers, layout work, and excessive JavaScript.
   - Start with field data from real users, reproduce the affected route and device conditions, profile the bottleneck, make one targeted change, and verify the result.
4. **How do you prevent XSS and design accessible controls?**
   - Prevent XSS with context-aware output encoding, safe DOM APIs, sanitized trusted HTML, CSP, and no client-side secrets.
   - Prefer native semantic controls. Add accessible names, keyboard behavior, visible focus, correct states, and sufficient contrast, then test with assistive technology.
5. **Interview tip:** connect architecture to user constraints, failure states, measurable performance, accessibility, and security.

### Key-point interview answers

- **What does a browser do?** It requests resources, parses HTML and CSS, executes JavaScript, renders pixels, handles input and storage, and enforces security boundaries.
- **What is rendering?** Rendering converts application data, HTML, and CSS into a visible, interactive page; choose server, static, client, or hybrid rendering per route.
- **What is state?** State is current application memory; keep temporary interface state separate from remote server state and define synchronization failures.
- **What is caching?** Caching reuses a nearby copy to reduce delay and work; every cache needs explicit freshness, invalidation, privacy, and failure rules.
- **Why use a CDN?** A CDN serves cacheable content from geographically closer edge servers, improving speed and origin resilience at the cost of configuration and consistency complexity.

## References

- [Mozilla Developer Network (MDN) Web Docs](https://developer.mozilla.org/)
- [web.dev: Core Web Vitals](https://web.dev/articles/vitals)
- [World Wide Web Consortium (W3C) Web Content Accessibility Guidelines](https://www.w3.org/TR/WCAG22/)
- [Open Worldwide Application Security Project (OWASP) Cross Site Scripting Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)
- [Related: DevOps](../07-DevOps/README.md)
