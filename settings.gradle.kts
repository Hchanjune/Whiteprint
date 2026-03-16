rootProject.name = "CQRS"

include("core-kernel")
include("core-domain")
include("core-cache")
include("core-messaging")

include("infra-jpa:core")
include("infra-jpa:messaging")

include("infra-redis:core")

include("infra-kafka:core")

include("infra-security:provider")
include("infra-security:verifier")

include("infra-web:servlet")
include("infra-web:reactive")

include("service-auth")

include("service-user:command")
include("service-user:query")