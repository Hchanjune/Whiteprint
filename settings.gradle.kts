rootProject.name = "CQRS"

include(
    "core-domain",
    "core-jpa",
    "user:user-command-api",
    "user:user-query-api"
)