rootProject.name = "CQRS"

include("core-kernel")
include("core-exception")
include("core-cache")
include("core-messaging")

include("infra-jpa")
include("infra-redis")
include("infra-kafka")
include("infra-security:provider")
include("infra-security:verifier")

include("infra-web:servlet")
include("infra-web:reactive")

include("service-auth")

include("service-user:command")
include("service-user:query")
