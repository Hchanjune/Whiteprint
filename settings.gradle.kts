rootProject.name = "CQRS"

include(
    "core-kernel",
    "core-jpa",
    "user:user-command",
    "user:user-query"
)
include("core-jwt")
include("core-jwt")