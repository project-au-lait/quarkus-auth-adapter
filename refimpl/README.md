# QAA Reference Implementation

This directory contains the reference implementation of QAA.
Each subdirectory contains the following resources:

- container
  Containers for Keycloak and the SMTP server ([Mailpit](https://mailpit.axllent.org/))
- keycloak-refimpl
  Backend using Keycloak as the IAM Provider
- svelte-refimpl
  Frontend implemented with SvelteKit
- e2etest
  Playwright tests for end-to-end testing of the above components

In addition to the software listed in the QAA usage instructions, the following are required to run the reference implementation:

- Git
- Docker
- Node.js
- pnpm
- VSCode

First, obtain and build the entire QAA project resources with the following commands:

```sh
git clone http://sitoolkit-dev.monocrea.co.jp/gitbucket/git/project-au-lait/quarkus-auth-adapter.git
cd quarkus-auth-adapter

./mvnw install -T 1C -P setup

# TODO: Provide a way to run everything with commands only
```

Next, open the project as a VSCode workspace from the same directory with the following command:

```sh
code quarkus-auth-adapter.code-workspace
```

Run the VSCode Tasks: `start-backend` and `start-frontend` to start the Backend and Frontend.
