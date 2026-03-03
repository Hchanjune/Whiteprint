rootProject.name = "CQRS"

include(
    "core",
    "core-jpa",
    "user:user-command-api",
    "user:user-query-api"
)