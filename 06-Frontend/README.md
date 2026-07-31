# Frontend

## Overview

Frontend engineering builds the user-facing system that runs primarily in browsers or native web views. It combines semantic UI, state and data flow, networking, performance, accessibility, security, testing, and deployment under unreliable device and network conditions.

## Why do we need it?

Users experience product quality through responsiveness, correctness, accessibility, and resilience. A sound frontend architecture lets teams evolve features without shipping excessive JavaScript, duplicating server state, exposing sensitive data, or coupling every screen to backend topology.

## How does it work?

Render choices include server-side rendering (fast initial HTML and discoverability), static generation (cacheable build-time output), client rendering (rich interaction after JavaScript loads), and hybrids. Select per route rather than by fashion.

Separate local UI state from server state. Fetch server data through a typed boundary, cache it with explicit freshness and invalidation, cancel obsolete requests, and represent loading, empty, error, stale, and offline states. A backend-for-frontend can adapt multiple services to one client, but business rules should remain in domain services.

Ship versioned immutable assets through a CDN; use HTTP [caching](../03-System-Design/Caching/README.md), code splitting, image sizing, and preloading selectively. Measure field Core Web Vitals, errors, and task duration—not bundle size alone. Build with semantic HTML, keyboard access, visible focus, labels, contrast, and assistive-technology testing.

**Production failure modes and practices**

- Hydration mismatch causes flicker or broken controls; keep server/client rendering deterministic.
- Stale responses overwrite newer state; cancel or sequence requests.
- A large bundle blocks low-end devices; enforce route budgets and inspect third-party code.
- Cached HTML references removed assets; use immutable hashed assets and compatible rollout/rollback.
- XSS steals sessions or data; escape output, avoid unsafe DOM APIs, use CSP, and keep secrets off the client.
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

1. SSR, static generation, and client rendering: how do you choose?
2. Local state versus server state?
3. How would you diagnose poor Largest Contentful Paint or Interaction to Next Paint?
4. How do you prevent XSS and design accessible controls?
5. **Interview tip:** connect architecture to user constraints, failure states, measurable performance, accessibility, and security.

## References

- [MDN Web Docs](https://developer.mozilla.org/)
- [web.dev: Core Web Vitals](https://web.dev/articles/vitals)
- [W3C Web Content Accessibility Guidelines](https://www.w3.org/TR/WCAG22/)
- [OWASP Cross Site Scripting Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)
- [Related: DevOps](../07-DevOps/README.md)
