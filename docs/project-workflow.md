# Project Workflow

## Source of Truth

Use the GitHub repository for code and documentation, GitHub Issues for work items, and a GitHub Project board for status.

Recommended board columns:

- Backlog
- Next
- In Progress
- Done

## Issue Guidelines

- One issue should describe one independently testable outcome.
- Keep most issues small enough to complete in one or two coding sessions.
- Use milestones to group work by release phase.
- Label issues as `feature`, `bug`, `chore`, or `documentation`.

Examples:

- Add `FoodEntry` database entity
- Validate food price input
- Show today's total water intake
- Add delete action for a water entry

## Branch and Commit Guidelines

Create a branch per issue when practical. Use concise commits that describe the user-facing or technical outcome.

Examples:

- `Add food entry database model (#1)`
- `Show daily water progress (#8)`
- `Fix price input validation (#14)`

## Planning Rhythm

Before a session, move one or two ready issues into `In Progress`. At the end, update the issue and move completed work to `Done`. Keep new ideas in `Backlog` instead of interrupting the current milestone.

