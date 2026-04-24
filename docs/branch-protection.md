# Branch Protection Recommendation

Apply these rules to the default branch (for example `main`):

## Required status checks

Require these checks to pass before merge:

- `Spotless and tests`
- `Docker build`

## Recommended settings

- Require a pull request before merging
- Require at least 1 approval
- Dismiss stale approvals when new commits are pushed
- Require conversation resolution before merging
- Require branches to be up to date before merging
- Restrict force pushes
- Restrict branch deletion
- Include administrators

## Merge strategy

Recommended defaults:

- Allow squash merge
- Disable merge commits if you want a linear history
- Optionally allow rebase merge if your team prefers it

## Review policy

Recommended minimum review checklist:

- Architecture boundaries stay intact
- New code passes `spotless:check`
- New code passes tests
- No secrets are committed
- Public contracts are reviewed for compatibility

