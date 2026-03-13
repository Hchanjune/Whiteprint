rootProject.name = "CQRS"

include("core-kernel")
include("core-exception")

include("infra-jpa")
include("infra-security:provider")
include("infra-security:verifier")

include("infra-web:servlet")
include("infra-web:reactive")

include("service-auth")

include("service-user:command")
include("service-user:query")
include("infra-redis")
include("core-cache")