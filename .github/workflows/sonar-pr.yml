name: Sonar Pull Request

on:
  workflow_run:
    workflows: ["Build Pull Request"]
    types:
      - completed

jobs:
  pr:
    uses: onecx/ci-quarkus/.github/workflows/quarkus-pr-sonar.yml@v2
    secrets: inherit