rootProject.name = "CQRS"

include(
    "core-kernel",
    "core-jpa",
    "user:user-command",
    "user:user-query"
)